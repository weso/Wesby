package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  val PlainText = Accepting("text/plain")
  val Html = Accepting("text/html")
  val Turtle = Accepting("text/turtle")
  val XTurtle = Accepting("application/x-turtle")
  val RdfXML = Accepting("application/rdf+xml")
  val Json = Accepting("application/json")
  val Xml = Accepting("application/xml")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def fallback = Action {
    implicit request =>
      render {
        case Html() => Ok("")
        case Turtle() => Ok("")
        case XTurtle() => Ok("")
        case RdfXML() => Ok("")
        case Json() => Ok("")
        case Xml() => Ok("")
        case _ => BadRequest
      }
  }

}