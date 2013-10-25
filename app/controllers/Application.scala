package controllers

import play.api._
import play.api.mvc._
import es.weso.wfLodPortal.ModelLoader
import java.io.ByteArrayOutputStream

object Application extends Controller {

  val PlainText = Accepting("text/plain")
  val Html = Accepting("text/html")
  val Turtle = Accepting("text/turtle")
  val XTurtle = Accepting("application/x-turtle")
  val RdfXML = Accepting("application/rdf+xml")
  val Json = Accepting("application/json")
  val Xml = Accepting("application/xml")

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
      render {
        case Html() => Ok(views.html.fallback(resultQuery))
        case Turtle() =>
          val out = new ByteArrayOutputStream()
          subjectModel.write(out, "TURTLE")
          predicateModel.write(out, "TURTLE")
          Ok("" + out.toString())
        case XTurtle() =>
          val out = new ByteArrayOutputStream()
          subjectModel.write(out, "TURTLE")
          predicateModel.write(out, "TURTLE")
          Ok("" + out.toString())
        case RdfXML() => Ok("")
        case Json() => Ok("")
        case Xml() =>

          Ok("")
        case _ => BadRequest
      }
  }

}