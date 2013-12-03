package controllers

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }
import com.hp.hpl.jena.rdf.model.ModelFactory

import es.weso.wfLodPortal.TemplateEgine
import es.weso.wfLodPortal.sparql.ModelLoader
import play.api.mvc.Accepting
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader

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
    implicit request => Ok(views.html.custom.home(currentVersion))
  }

  def snorql() = Action {
    implicit request => Ok(views.html.snorql())
  }

  def redirect(to: String) = Action {
    Redirect(to)
  }

  def fallback(uri: String) = Action {
    implicit request =>
      val resultQuery = ModelLoader.loadUri(uri)
      val subjectModel = resultQuery.subject.get.jenaModel
      val predicateModel = resultQuery.predicate.get.jenaModel
      val models = List(subjectModel, predicateModel)

      request.getQueryString("format") match {
        case Some(format) => downloadAs(uri, format, models)
        case None =>
          render {
            case Html() => renderAsTemplate(resultQuery, uri)
            case N3() =>
              Redirect(request.path + "?format=n3")
            case RdfN3() =>
              Redirect(request.path + "?format=n3")
            case Turtle() =>
              Redirect(request.path + "?format=turtle")
            case XTurtle() =>
              Redirect(request.path + "?format=turtle")
            case RdfXML() =>
              Redirect(request.path + "?format=rdfxml")
            case Xml() =>
              Redirect(request.path + "?format=rdfxml")
            case RdfJSON() =>
              Redirect(request.path + "?format=rdfjson")
            case PlainText() =>
              Redirect(request.path + "?format=n-triples")
          }
      }
  }

  protected def downloadAs(uri: String, format: String, models: Seq[JenaModel])(implicit request: RequestHeader) = {
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

  protected def renderModelsAs(models: Seq[JenaModel], contentType: (String, String, String))(implicit request: RequestHeader) = {
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