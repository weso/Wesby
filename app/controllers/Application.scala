package controllers

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.weso.wfLodPortal.TemplateEgine
import es.weso.wfLodPortal.sparql._
import play.api.mvc.Accepting
import play.api.mvc.Action
import play.api.mvc.Controller
import es.weso.wfLodPortal.sparql.custom._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json

object Application extends Controller with TemplateEgine {

  val Html = Accepting("text/html")

  val PlainText = Accepting("text/plain")

  val N3 = Accepting("text/n3")
  val RdfN3 = Accepting("text/rdf+n3")

  val Turtle = Accepting("text/turtle")
  val XTurtle = Accepting("application/x-turtle")

  val Xml = Accepting("application/xml")
  val RdfXML = Accepting("application/rdf+xml")

  val RdfJSON = Accepting("application/rdf+json")

  val charsetDecoder = Charset.forName("UTF-8").newDecoder();
  charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
  charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

  def index = Action {
    Redirect {
      routes.Application.fallback(ModelLoader.indexPath)
    }
  }

  def fallback(uri: String) = Action {
    implicit request =>
      val resultQuery = ModelLoader.loadUri(uri)
      val subjectModel = resultQuery.subject.jenaModel
      val predicateModel = resultQuery.predicate.jenaModel
      val models = List(subjectModel, predicateModel)

      val mode = if (uri contains "/odb/") "odb" else "webindex"

      request.getQueryString("format") match {
        case Some(format) => downloadAs(uri: String, format, models)
        case None =>
          render {
            case Html() => renderAsTemplate(resultQuery, ModelLoader.fullUri(uri), mode)
            case N3() =>
              renderModelsAs(models, ("N3", "utf-8", N3.mimeType))
            case RdfN3() =>
              renderModelsAs(models, ("N3", "utf-8", RdfN3.mimeType))
            case Turtle() =>
              renderModelsAs(models, ("TURTLE", "utf-8", Turtle.mimeType))
            case XTurtle() =>
              renderModelsAs(models, ("TURTLE", "utf-8", XTurtle.mimeType))
            case RdfXML() =>
              renderModelsAs(models, ("RDF/XML", "utf-8", RdfXML.mimeType))
            case Xml() =>
              renderModelsAs(models, ("RDF/XML", "utf-8", Xml.mimeType))
            case RdfJSON() =>
              renderModelsAs(models, ("RDF/JSON", "utf-8", RdfJSON.mimeType))
            case PlainText() =>
              renderModelsAs(models, ("N-Triples", "utf-8", PlainText.mimeType))
          }
      }
  }

  def preCompare(mode: String, selectedCountries: Option[String], selectedIndicators: Option[String]) = Action {
    import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries._
    import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery._
    import es.weso.wfLodPortal.sparql.custom.YearsCustomQuery._

    val c = Json.toJson[List[Region]](RegionCustomQueries.loadRegions(mode))
    val y = Json.toJson[List[Int]](YearsCustomQuery.loadYears(mode))
    val s = Json.toJson[List[Subindex]](SubindexCustomQuery.loadSubindexes(mode))
    Ok(views.html.compare(c, y, s, selectedCountries, selectedIndicators, mode))
  }

  def compare(mode: String, countries: String, years: String, indicators: String) = Action {
    import es.weso.wfLodPortal.sparql.custom.IndicatorCustomQuery._
    
    val c = countries.split(",")
    val y = years.split(",")
    val i = indicators.split(",")
    val observations = IndicatorCustomQuery.loadObservations(c, y, i)
    val json = Json.toJson[Map[String, Indicator]](observations)
    Ok(views.html.comparison(json, mode))
  }

  protected def downloadAs(uri: String, format: String, models: Seq[JenaModel]) = {
    format match {
      case "n3" =>
        renderModelsAs(models, ("N3", "utf-8", N3.mimeType))
      case "turtle" =>
        renderModelsAs(models, ("TURTLE", "utf-8", Turtle.mimeType))
      case "rdfxml" =>
        renderModelsAs(models, ("RDF/XML", "utf-8", RdfXML.mimeType))
      case "rdfjson" =>
        renderModelsAs(models, ("RDF/JSON", "utf-8", RdfJSON.mimeType))
      case "n-triples" =>
        renderModelsAs(models, ("N-Triples", "utf-8", PlainText.mimeType))
      case _ => BadRequest
    }
  }

  protected def renderModelsAs(models: Seq[JenaModel], contentType: (String, String, String)) = {
    val out = new ByteArrayOutputStream
    val mergedModel: JenaModel = ModelFactory.createDefaultModel

    for (model <- models) {
      mergedModel.add(model)
    }
    mergedModel.write(out, contentType._1)

    Ok(out.toString).as {
      (new StringBuilder(contentType._3)).append(" ; charset=")
        .append(contentType._2).toString
    }
  }
}