package controllers

import javax.inject.Inject

import models.QueryEngineWithJena
import play.Play
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._

class Application @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  // Custom request extractors
  val AcceptsHtml = Accepting("text/html")
  val AcceptsTurtle = Accepting("text/turtle")
  val AcceptsNTriples = Accepting("application/n-triples")
  val AcceptsJSONLD = Accepting("application/ld+json")
  val AcceptsN3 = Accepting("text/n3")
  val AcceptsRdfXML = Accepting("application/rdf+xml")
  // TODO
  val AcceptsPlainText = Accepting("text/plain")
  val AcceptsRdfN3 = Accepting("text/rdf+n3")
  val AcceptsXTurtle = Accepting("application/x-turtle")
  val AcceptsXml = Accepting("application/xml")
  val AcceptsRdfJSON = Accepting("application/rdf+json")

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
      case AcceptsTurtle() => Redirect(request.path + ".ttl")
      case AcceptsNTriples() => Redirect(request.path + ".nt")
      case AcceptsJSONLD() => Redirect(request.path + ".jsonld")
      case AcceptsN3() => Redirect(request.path + ".n3")
      case AcceptsRdfXML() => Redirect(request.path + ".rdf")
    }
  }

  def download(path: String, format: String) = Action {
    Logger.debug("Downloading: " + format)
    val resource = Play.application().configuration().getString("wesby.host") + path
    Ok(QueryEngineWithJena.queryTestConstructTurtle(resource))
  }

}
