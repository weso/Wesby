package controllers

import javax.inject.Inject

import play.Play
import play.api.Logger
import play.api.i18n.{Messages, I18nSupport, MessagesApi}
import play.api.mvc._

class Application @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  // Custom request extractors
  val AcceptsHtml = Accepting("text/html")
  val AcceptsPlainText = Accepting("text/plain")
  val AcceptsN3 = Accepting("text/n3")
  val AcceptsRdfN3 = Accepting("text/rdf+n3")
  val AcceptsTurtle = Accepting("text/turtle")
  val AcceptsXTurtle = Accepting("application/x-turtle")
  val AcceptsXml = Accepting("application/xml")
  val AcceptsRdfXML = Accepting("application/rdf+xml")
  val AcceptsRdfJSON = Accepting("application/rdf+json")
  val AcceptsJSONLD = Accepting("application/ld+json")

  def index = Action { implicit request =>
    Redirect(Play.application().configuration().getString("wesby.index"))
  }

  def welcome = Action { implicit request =>
    Ok(Messages("welcome.test"))
  }

  def dereference(path: String) = Action { implicit request =>
    Logger.debug("Dereferencing: " + path)
    render {
      case AcceptsHtml() => Redirect(request.path + ".html")
      case AcceptsPlainText() => ???
      case AcceptsJSONLD() => ???
      case AcceptsN3() => Redirect(request.path + ".n3")
      case AcceptsRdfN3() => ???
      case AcceptsRdfXML() => ???
      case AcceptsTurtle() => ???
      case AcceptsXml() => Redirect(request.path + ".xml")
      case AcceptsXTurtle() => ???
    }
  }

  def download(path: String, format: String) = Action {
    Logger.debug("Downloading: " + format)
    Ok(format)
  }

}
