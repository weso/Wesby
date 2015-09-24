package controllers

import javax.inject.Inject

import arq.iri
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.hp.hpl.jena.graph.Graph
import es.weso.monads
import es.weso.monads.Passed
import es.weso.rdf.RDFTriples
import es.weso.rdfgraph.nodes.{RDFNode, IRI}
import es.weso.shacl.{SchemaFormats}
import es.weso.shex.{Typing, Schema, ShExMatcher}
import models.{ShapeMatcher, QueryEngineWithJena}
import models.http.{CustomMimeTypes, CustomHeaderNames, CustomContentTypes}
import play.Play
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.Codecs
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import views.ResourceSerialiser

import scala.util.{Try, Failure, Success}

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

  /**
   * Redirects to the index page declared in `application.conf`
   */
  def index = Action { implicit request =>
    Redirect(Play.application().configuration().getString("wesby.index"))
  }

  /**
   * It shows the default welcome page that is set as the index after the installation.
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
      case AcceptsTurtle() => Redirect(request.path + ".ttl")
      case AcceptsJSONLD() => Redirect(request.path + ".jsonld")
      case AcceptsPlainText() => Redirect(request.path + ".txt")
      case AcceptsNTriples() => Redirect(request.path + ".nt")
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
  def getLDPR(path: String, extension: String) = Action { implicit request =>
    Logger.debug("Downloading: " + extension)
    val resource = Play.application().configuration().getString("wesby.host") + path
    val constructQuery = Play.application().configuration().getString("queries.construct.s")
    val graph = QueryEngineWithJena.construct(resource, constructQuery)

    graph match {
      case Failure(f) => InternalServerError
      case Success(g) => if (g.isEmpty) NotFound
        else extension match {
          case "html" => buildHTMLResult(resource, g)
          case "ttl" => buildResult(resource, g, TURTLE, ResourceSerialiser.asTurtle)
          case "txt" => Ok(ResourceSerialiser.asPlainText(g, Messages("wesby.title"))).as(TEXT)
          case "nt" => buildResult(resource, g, NTRIPLES, ResourceSerialiser.asNTriples)
          case "jsonld" => buildResult(resource, g, JSONLD, ResourceSerialiser.asJsonLd)
          // TODO      case "n3" =>
          case "rdf" => buildResult(resource, g, RDFXML, ResourceSerialiser.asRdfXml)
          case _ => NotFound
        }
    }
  }

  /**
   * Renders the requested LDPC resource.
   *
   * @param path the resource path
   * @return the HTTP response
   */
  def getLDPC(path: String) = Action { implicit request =>
    Logger.debug("Container: " + path)
    val resource = Play.application().configuration().getString("wesby.host") + path + "/"
    val constructQuery = Play.application().configuration().getString("queries.construct.s")
    val graph = QueryEngineWithJena.construct(resource, constructQuery)

    //    val query = Play.application().configuration().getString("queries.s")
    //    val solutions = QueryEngineWithJena.select(resource, query)

    graph match {
      case Failure(f) => InternalServerError
      case Success(g) => if (g.isEmpty) NotFound
      else buildResult(resource, g, TURTLE, ResourceSerialiser.asTurtle)
    }
  }

  /**
   * TODO temporary
   */
  def buildHTMLResult(resource: String, graph: Graph): Result = {
    val strRDF = ResourceSerialiser.asTurtle(graph, resource).get
    val rdf = RDFTriples.parse(strRDF).get

    val shape = ShapeMatcher.matchWithShex(rdf, resource).getOrElse("No shape found")

    Ok(views.html.resource(resource, strRDF, shape)).as(HTML)
  }

  /**
   * Builds a result for the given graph and MIME Type using the serialisation function provided.
   *
   * @param resource the resource URI
   * @param graph the RDF graph of the resource
   * @param mimeType the MIME type of the response
   * @param asString the serialisation function
   * @return the Result for the HTTP response
   */
  private def buildResult(resource: String, graph: Graph, mimeType: String, asString: (Graph, String) => Try[String]): Result = {

    val strRDF = ResourceSerialiser.asTurtle(graph, resource).get
    val rdf = RDFTriples.parse(strRDF).get

    val shexShape = ShapeMatcher.matchWithShex(rdf, resource)
//    val shaclShape = ShapeMatcher.matchWithShacl(rdf)

    asString(graph, resource) match {
      case Failure(f) => InternalServerError
      case Success(c) =>
        val headers = linkHeaders() :: allowHeaders() :: etagHeader(c) :: Nil
        Result(
          header = ResponseHeader(200, Map(headers: _*)),
          body = Enumerator(c.getBytes)
        ).as(mimeType)
    }
  }

  private def nodeToString(node: RDFNode): String = {
    if (node.isIRI) node.toIRI.str
    else node.toString
  }

  def nodesToString(nodes: Set[RDFNode]): String = {
    val sb = new StringBuilder
    for (node <- nodes) {
      sb.append(nodeToString(node))
    }
    sb.append("\n")
    sb.toString
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

