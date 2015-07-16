package controllers

import javax.inject.Inject

import models.QueryEngineWithJena
import models.http.CustomContentTypes
import play.Play
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import views.ResourceSerialiser

class Application @Inject() (val messagesApi: MessagesApi)
  extends Controller
  with I18nSupport
  with CustomContentTypes {

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

  /**
   * Redirects to the index page declared in `application.conf`
   */
  def index = Action { implicit request =>
    Redirect(Play.application().configuration().getString("wesby.index"))
  }

  /**
   * It shows the defautl welcome page that is set as the index after the installation.
   * You can change the index page in `application.conf`.
   */
  def welcome = Action { implicit request =>
    Ok(Messages("welcome.test"))
  }

  /**
   * Redirects the resource request to the appropriate document depending on the `Accept` header field.
   *
   * @param path the resource path
   * @return a 303 redirection
   */
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

  /**
   * Renders the requested resource in the appropriate format depending on the `Accept` header field.
   *
   * @param path the resource path
   * @param extension the document extension
   * @return the HTTP response
   */
  def download(path: String, extension: String) = Action { implicit request =>
    Logger.debug("Downloading: " + extension)
    val resource = Play.application().configuration().getString("wesby.host") + path
    val query = Play.application().configuration().getString("queries.s")
    val constructQuery = Play.application().configuration().getString("queries.construct.s")

    val solutions = QueryEngineWithJena.select(resource, query)
    val graph = QueryEngineWithJena.construct(resource, constructQuery)

    val content = "TEST"

    val result = extension match { // TODO charset, etag
      case "html" => Ok(content).as(HTML)
      case "txt" => Ok(ResourceSerialiser.asPlainText(graph, Messages("wesby.title"))).as(TEXT)
      case "ttl" => Ok(ResourceSerialiser.asTurtle(graph, resource)).as(TURTLE)
      case "nt" => Ok(content).as(NTRIPLES)
      case "jsonld" => Ok(content).as(JSONLD)
      case "n3" => Ok(content).as(N3)
      case "rdf" => Ok(content).as(RDFXML)
      case _ => NotFound
    }

    result.withHeaders(ALLOW -> "GET")
  }

}

