package controllers

import play.api._
import play.api.mvc._
import es.weso.wfLodPortal.ModelLoader
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }

object Application extends Controller {

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
        case Html() => Ok(views.html.fallback(resultQuery))
        case N3() =>
          renderModels(models, ("N3", N3.mimeType))
        case Turtle() =>
          renderModels(models, ("TURTLE", Turtle.mimeType))
        case XTurtle() =>
          renderModels(models, ("TURTLE", XTurtle.mimeType))
        case RdfXML() =>
          renderModels(models, ("RDF/XML", RdfXML.mimeType))
        case Xml() =>
          renderModels(models, ("RDF/XML", Xml.mimeType))
        case RdfJSON() =>
          renderModels(models, ("RDF/JSON", RdfJSON.mimeType))
        case PlainText() =>
          renderModels(models, ("N-Triples", PlainText.mimeType))
        case _ => BadRequest
      }
  }

  def renderModels(models: Seq[JenaModel], contentType: (String, String)) = {
    val out = new ByteArrayOutputStream()
    for (model <- models) {
      model.write(out, contentType._1)
    }
    Ok(out.toString()).as(contentType._2)
  }

}