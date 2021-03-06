package controllers

import java.util.regex.Pattern
import javax.inject.Inject
import javax.naming.directory.SearchResult

import com.hp.hpl.jena.graph.Graph
import com.hp.hpl.jena.query.ResultSet
import com.hp.hpl.jena.rdf.model.RDFReader
import es.weso.rdf.jena.RDFAsJenaModel
import es.weso.rdf.validator.jena.JenaValidator
import es.weso.utils.{JenaUtils, Parsed}
//import es.weso.rdf.RDFTriples
import es.weso.rdf.nodes.RDFNode
import models.http.{CustomContentTypes, CustomHeaderNames, CustomMimeTypes}
import models._
import org.jboss.resteasy.spi.metadata.ResourceBuilder
import org.w3.banana.jena.JenaModule
import play.Play
import play.api.Logger
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.libs.Codecs
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import views.ResourceSerialiser
import org.w3.banana._
import play.api.libs.json.Json
import views.html.resource

import scala.util.{Failure, Success, Try}

//object Application extends JenaModule

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
    Ok(views.html.welcome()).as(HTML)
  }

  def search(searchQuery: String, a: Option[String],
             labelProperty: Option[String], typeProperty: Option[String],
             typeObject: Option[String]) = Action { implicit request =>

    a match {
      case Some(resourceType) => {
        labelProperty match {
          case Some(lp) => {
            val solutions: ResultSet = QueryEngineWithJena.labelPropSearchSelect(searchQuery, resourceType, lp)
            val results = SearchResultsBuilder.build(solutions)
            Ok(views.html.search(results)).as(HTML)
          }
          case None =>
            QueryEngineWithJena.simpleSearchSelect(searchQuery, resourceType) match {
              case Success(solutions) => {
                val results = SearchResultsBuilder.build(solutions)
                Ok(views.html.search(results)).as(HTML)
              }
              case Failure(f) => NotFound(f.getMessage)
            }
        }
      }
      case None => {
        QueryEngineWithJena.textSearchSelect(searchQuery) match {
          case Success(solutions) => {
            val results = SearchResultsBuilder.build(solutions)
            Ok(views.html.search(results)).as(HTML)
          }
          case Failure(f) => NotFound(f.getMessage)
        }
      }
    }
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
    Logger.debug(s"""Downloading: $path.$extension""")
    val uriString = Play.application().configuration().getString("wesby.datasetBase") + path
    val constructQuery = Play.application().configuration().getString("queries.construct")
    val graph: Try[Graph] = QueryEngineWithJena.construct(uriString, constructQuery)

    graph match {
      case Failure(f) => InternalServerError
      case Success(g) => if (g.isEmpty) NotFound(views.html.errors.error404("Not found")).as(HTML)
    else extension match {
          case "html" => buildHTMLResult(uriString, g, request2lang)
          case "ttl" => buildResult(uriString, g, TURTLE, ResourceSerialiser.asTurtle)
          case "txt" => Ok(ResourceSerialiser.asPlainText(g, Messages("wesby.title"))).as(TEXT)
          case "nt" => buildResult(uriString, g, NTRIPLES, ResourceSerialiser.asNTriples)
          case "jsonld" => buildResult(uriString, g, JSONLD, ResourceSerialiser.asJsonLd)
          // TODO      case "n3" =>
          case "rdf" => buildResult(uriString, g, RDFXML, ResourceSerialiser.asRdfXml)
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
  def getLDPC(path: String) = getLDPR(path + "/", "html")
//    Action { implicit request =>
//    Logger.debug("Container: " + path)
//    val resource = Play.application().configuration().getString("wesby.datasetBase") + path + "/"
//    val constructQuery = Play.application().configuration().getString("queries.construct.s")
//    val graph = QueryEngineWithJena.construct(resource, constructQuery)
//
//    //    val query = Play.application().configuration().getString("queries.s")
//    //    val solutions = QueryEngineWithJena.select(resource, query)
//
//    graph match {
//      case Failure(f) => InternalServerError
//      case Success(g) => if (g.isEmpty) NotFound
//      else buildResult(resource, g, TURTLE, ResourceSerialiser.asTurtle)
//    }
//  }

  def rewrite(uri: String) = {
    val host = Play.application().configuration().getString("wesby.host")
    val datasetBase = Play.application().configuration().getString("wesby.datasetBase")
    val dereferencedUri = uri.replaceFirst(Pattern.quote(host), datasetBase)
    dereferencedUri
  }

  def label(uri: String) = Action { implicit request =>
    QueryEngineWithJena.getLabel(rewrite(uri)) match {
//      case None => NotFound TODO Find a way to prevent 404 logging in browser
      case None => Ok(Json.toJson(Json.parse(
        """
          |{
          | "status": "error",
          | "message": "Label not found"
          |}
        """.stripMargin
      )))
      case Some(s) => Ok(Json.toJson(Json.parse(
        s"""
           |{
           |  "status": "success",
           |  "data": {"label": "${s.getLiteralValue.toString}"}
           |}
         """.stripMargin
      )))
    }
  }

  /**
   * TODO temporary
   */
  def buildHTMLResult(resourceUri: String, graph: Graph, lang: Lang): Result = {
    val strRDF = ResourceSerialiser.asTurtle(graph, resourceUri).get
//    val rdf = RDFTriples.parse(strRDF).get
    JenaUtils.str2Model(strRDF) match {
      case Parsed(model) => {
        val shapes = ShapeMatcher.matchWithShacl(RDFAsJenaModel(model), resourceUri)
        val resource = ResourceBuilderWithJena.build(resourceUri, graph, shapes)
//        val template = Play.application().configuration().getString(shapes.head)
//        template match {
//          case "user" => Ok(views.html.user(resource)(lang.language)).as(HTML)
//          case _ => Ok(views.html.resource(resource)(lang.language)).as(HTML)
//        }
        Ok(views.html.resource(resource)(lang.language)).as(HTML)
      }
    }






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
//    val rdf = RDFTriples.parse(strRDF).get

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
    sb.toString()
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

