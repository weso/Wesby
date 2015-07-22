package controllers

import javax.inject.Inject

import models.QueryEngineWithJena
import models.http.{CustomMimeTypes, CustomHeaderNames, CustomContentTypes}
import play.Play
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.Codecs
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import views.ResourceSerialiser

import scala.util.{Failure, Success}

class Application @Inject()(val messagesApi: MessagesApi)
  extends Controller
  with I18nSupport
  with CustomContentTypes
  with CustomHeaderNames {

  // Custom request extractors
  val AcceptsPlainText = Accepting(CustomMimeTypes.TEXT)
  val AcceptsTurtle = Accepting(CustomMimeTypes.TURTLE)
  val AcceptsNTriples = Accepting(CustomMimeTypes.NTRIPLES)
  val AcceptsJSONLD = Accepting(CustomMimeTypes.JSONLD)
  val AcceptsN3 = Accepting(CustomMimeTypes.N3)
  val AcceptsRdfXML = Accepting(CustomMimeTypes.RDFXML)
  // TODO
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
      case Accepts.Html() => Redirect(request.path + ".html")
      case AcceptsPlainText() => Redirect(request.path + ".txt")
      case AcceptsTurtle() => Redirect(request.path + ".ttl")
      case AcceptsNTriples() => Redirect(request.path + ".nt")
      case AcceptsJSONLD() => Redirect(request.path + ".jsonld")
      case AcceptsN3() => Redirect(request.path + ".n3")
      case AcceptsRdfXML() => Redirect(request.path + ".rdf")
      case _ => UnsupportedMediaType
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
    val constructQuery = Play.application().configuration().getString("queries.construct.s")
    val graph = QueryEngineWithJena.construct(resource, constructQuery)

    //    val query = Play.application().configuration().getString("queries.s")
    //    val solutions = QueryEngineWithJena.select(resource, query)

    val result = extension match {
      // TODO charset, etag
      case "html" => Ok("TODO").as(HTML)
      case "txt" => Ok(ResourceSerialiser.asPlainText(graph, Messages("wesby.title"))).as(TEXT)
      case "ttl" => Ok(ResourceSerialiser.asTurtle(graph, resource) getOrElse Messages("error.serialise")).as(TURTLE)
      case "nt" => Ok(ResourceSerialiser.asTurtle(graph, resource) getOrElse Messages("error.serialise")).as(NTRIPLES)
      case "jsonld" => Ok(ResourceSerialiser.asJsonLd(graph, resource) getOrElse Messages("error.serialise")).as(JSONLD)
      // TODO      case "n3" => Ok(ResourceSerialiser.asN3(graph, resource) getOrElse Messages("error.serialise")).as(N3)
      case "rdf" => Ok(ResourceSerialiser.asRdfXml(graph, resource) getOrElse Messages("error.serialise")).as(RDFXML)
      case _ => NotFound
    }

    // LDP support advertising
    result.withHeaders(
      linkHeaders(),
      allowHeaders(),
      etagHeader("TODO")
    )
  }

  private def linkHeaders() = {
    LINK -> """<http://www.w3.org/ns/ldp#Resource>; rel="type""""
  }

  private def allowHeaders() = {
    ALLOW -> "GET"
  }

  private def etagHeader(content: String) = {
    ETAG -> {
      "W/\"" + Codecs.sha1(content) + "\""
    }
  }
}

