package controllers

import javax.inject.Inject

import models.{PlainTextRenderer, QueryEngineWithJena}
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
      case AcceptsPlainText() => Redirect(request.path + ".txt")
      case AcceptsTurtle() => Redirect(request.path + ".ttl")
      case AcceptsNTriples() => Redirect(request.path + ".nt")
      case AcceptsJSONLD() => Redirect(request.path + ".jsonld")
      case AcceptsN3() => Redirect(request.path + ".n3")
      case AcceptsRdfXML() => Redirect(request.path + ".rdf")
      case _ => Redirect(request.path + ".rdf")
    }
  }

  def download(path: String, extension: String) = Action { implicit request =>
    Logger.debug("Downloading: " + extension)
    val resource = Play.application().configuration().getString("wesby.host") + path
    val query = Play.application().configuration().getString("queries.s")

    val solutions = QueryEngineWithJena.select(resource, query)

    val content = "TEST"

    val result = request match { // TODO charset? etag
      case AcceptsHtml() => Ok(content).as(HTML)
      case AcceptsPlainText() => Ok(PlainTextRenderer.render(solutions, Messages("wesby.title"))).as(TEXT)
      case AcceptsTurtle() => Ok(content).as(AcceptsTurtle.mimeType)
      case AcceptsNTriples() => Ok(content).as(AcceptsNTriples.mimeType)
      case AcceptsJSONLD() => Ok(content).as(AcceptsJSONLD.mimeType)
      case AcceptsN3() => Ok(content).as(AcceptsN3.mimeType)
      case AcceptsRdfXML() => Ok(content).as(AcceptsRdfXML.mimeType)
      case _ => Ok(content)
    }

    result.withHeaders(ALLOW -> "GET")
  }

}
