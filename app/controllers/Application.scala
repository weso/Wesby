package controllers

import play.api._
import play.api.mvc._
import es.weso.wfLodPortal.ModelLoader
import es.weso.wfLodPortal.UriFormatter
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }
import models.ResultQuery
import es.weso.wfLodPortal.TemplateEgine

object Application extends Controller with TemplateEgine {

  val Html = Accepting("text/html")

  val PlainText = Accepting("text/plain")

  val N3 = Accepting("text/n3")

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
      render {
        case Html() => renderAsTemplate(resultQuery)
        case N3() =>
          renderModelsAs(models, ("N3", "utf-8", N3.mimeType))
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

  def renderModelsAs(models: Seq[JenaModel], contentType: (String, String, String)) = {
    val out = new ByteArrayOutputStream()
    for (model <- models) {
      model.write(out, contentType._1, contentType._2)
    }

    Ok(out.toString).as {
      (new StringBuilder(contentType._3)).append(" ; charset=")
        .append(contentType._2).toString
    }
  }

}