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
import org.scalatest.path
import org.w3.banana.jena.Jena
import play.api.cache.{CacheApi, Cached}
import play.api.libs.iteratee.Enumeratee
import play.api.libs.json.{JsObject, JsValue}
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

class Application @Inject() (val messagesApi: MessagesApi) (cache: CacheApi) (cached: Cached)
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
  def index = cached("index") {
    Action { implicit request =>
      Redirect(Play.application().configuration().getString("wesby.index"))
    }
  }

  /**
   * It shows the default welcome page that is set as the index after the installation.
   * You can change the index page in `application.conf`.
   */
  def welcome = Action { implicit request =>
    Ok(views.html.welcome()).as(HTML)
  }

  def presentacion = Action { implicit request =>
    Ok(views.html.presentacion()).as(HTML)
  }

  def descripcion = Action { implicit request =>
    Ok(views.html.descripcion()).as(HTML)
  }

  def words(letter: String) = Action { implicit request =>
    QueryEngineWithJena.listWords(letter) match {
      case Success(solutions) => {
        val results = SearchResultsBuilder.build(solutions)
        Ok(views.html.words(results, letter)).as(HTML)
      }
      case Failure(f) => NotFound("Not found")
    }
  }

  def search(searchQuery: String, a: Option[String],
             labelProperty: Option[String], typeProperty: Option[String],
             typeObject: Option[String]) = Action { implicit request =>

    val upperCaseQuery = searchQuery.toUpperCase()
    a match {
      case Some(resourceType) => {
        labelProperty match {
          case Some(lp) => {
            val solutions: ResultSet = QueryEngineWithJena.labelPropSearchSelect(upperCaseQuery, resourceType, lp)
            val results = SearchResultsBuilder.build(solutions)
            Ok(views.html.search(results)).as(HTML)
          }
          case None =>
            QueryEngineWithJena.simpleSearchSelect(upperCaseQuery, resourceType) match {
              case Success(solutions) => {
                val results = SearchResultsBuilder.build(solutions)
                Ok(views.html.search(results)).as(HTML)
              }
              case Failure(f) => NotFound(f.getMessage)
            }
        }
      }
      case None => {
        QueryEngineWithJena.textSearchSelect(upperCaseQuery) match {
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
  def dereference(path: String, page: Option[String]) = Action { implicit request =>

    val query = page match {
      case Some(p) => "?page=" + p
      case None => ""
    }

    render {
      case Accepts.Html() => Redirect(request.path + ".html" + query)
      case AcceptsTurtle() => Redirect(request.path + ".ttl" + query)
      case AcceptsJSONLD() => Redirect(request.path + ".jsonld" + query)
      case AcceptsPlainText() => Redirect(request.path + ".txt" + query)
      case AcceptsNTriples() => Redirect(request.path + ".nt" + query)
      case AcceptsN3() => Redirect(request.path + ".n3" + query)
      case AcceptsRdfXML() => Redirect(request.path + ".rdf" + query)
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
  def getLDPR(path: String, extension: String, page: Option[String]) = // cached(path + "." + extension + "." + page) {
      Action { implicit request =>
        Logger.debug(s"""Downloading: $path.$extension""")

        val queryPart: String = page match {
          case Some(p) => "?page=" + p
          case None => ""
        }
        val uriString = Play.application().configuration().getString("wesby.datasetBase") + path
        val dataPath = "/" + (path + ".jsonld" + queryPart)

//        val graph = page match {
//          case Some(p) =>  {
//            val constructQuery = Play.application().configuration().getString("queries.pagedConstruct")
//            val limit = 10
//            val offset = limit * p.toInt
//            QueryEngineWithJena.pagedConstruct(uriString, constructQuery, limit, offset)
//          }
//          case None => {
//            val constructQuery = Play.application().configuration().getString("queries.construct")
//            QueryEngineWithJena.construct(uriString, constructQuery)
//          }
//        }

        val graph: Try[Graph] = cache.getOrElse[Try[Graph]](uriString) {
          Logger.debug("not cached")
          page match {
            case Some(p) =>  {
              val constructQuery = Play.application().configuration().getString("queries.pagedConstruct")
              val limit = 10
              val offset = limit * p.toInt
              QueryEngineWithJena.pagedConstruct(uriString, constructQuery, limit, offset)
            }
            case None => {
              val constructQuery = Play.application().configuration().getString("queries.construct")
              QueryEngineWithJena.construct(uriString, constructQuery)
            }
          }
        }

        cache.set(uriString, graph)

        val resourceType = QueryEngineWithJena.getType(uriString).getOrElse("default")

        graph match {
          case Failure(f) => InternalServerError
          case Success(g) => if (g.isEmpty) NotFound(views.html.errors.error404("Not found")).as(HTML)
          else extension match {
            case "html" => buildHTMLResult(request.path, g, request2lang, dataPath, resourceType)
            case "ttl" => buildResult(uriString, g, TURTLE, ResourceSerialiser.asTurtle)
            case "txt" => Ok(ResourceSerialiser.asPlainText(g, Messages("wesby.title"))).as(TEXT)
            case "nt" => buildResult(uriString, g, NTRIPLES, ResourceSerialiser.asNTriples)
            //          case "jsonld" => buildResult(uriString, g, JSONLD, ResourceSerialiser.asJsonLd)
            case "jsonld" => buildTemplateResult(uriString, g, request2lang)
            // TODO      case "n3" =>
            case "rdf" => buildResult(uriString, g, RDFXML, ResourceSerialiser.asRdfXml)
            case _ => NotFound
          }
        }
      }
//  }

  /**
   * Renders the requested LDPC resource.
   *
   * @param pathWithoutSlash the resource path
   * @return the HTTP response
   */
  def getLDPC(pathWithoutSlash: String, format: Option[String]) = Action { implicit request =>
    val listings = Play.application().configuration().getBoolean("wesby.generateListings")
    val path = pathWithoutSlash + "/"
    val queryName = if (listings) "queries.listing" else "queries.construct"

      //    Logger.debug(s"""Downloading: $path""")
    val uriString = Play.application().configuration().getString("wesby.datasetBase") + path
    val dataPath = Play.application().configuration().getString("wesby.datasetBase") + path + ".jsonld"// + page

    val constructQuery = Play.application().configuration().getString(queryName)

    val graph: Try[Graph] = QueryEngineWithJena.construct(uriString, constructQuery)

//    val graph: Try[Graph] = cache.getOrElse[Try[Graph]](uriString) {
//      Logger.debug("not cached")
//      QueryEngineWithJena.construct(uriString, constructQuery)
//    }

//    cache.set(uriString, graph)

    val resourceType = QueryEngineWithJena.getType(uriString).getOrElse("default")

    graph match {
      case Failure(f) => InternalServerError
      case Success(g) =>
        if (g.isEmpty) NotFound(views.html.errors.error404("Not found")).as(HTML)
        else format match {
          case None => buildHTMLResult(uriString, g, request2lang, dataPath, resourceType)
          case Some(s) => s match {
            case "html" => buildHTMLResult(uriString, g, request2lang, dataPath, resourceType)
            case "jsonld" => buildTemplateResult(uriString, g, request2lang)
            case _ => UnsupportedMediaType
          }
        }
    }

  }

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
  def buildHTMLResult(resourceUri: String, graph: Graph, lang: Lang, dataPath: String, resourceType: String): Result = {
    val strRDF = ResourceSerialiser.asTurtle(graph, resourceUri).get
    //    val rdf = RDFTriples.parse(strRDF).get
    JenaUtils.str2Model(strRDF) match {
      case Parsed(model) => {
//        val shapes = ShapeMatcher.matchWithShacl(RDFAsJenaModel(model), resourceUri)
//        val resource = ResourceBuilderWithJena.build(resourceUri, graph, shapes)
        //        val template = Play.application().configuration().getString(shapes.head)
        //        template match {
        //          case "user" => Ok(views.html.user(resource)(lang.language)).as(HTML)
        //          case _ => Ok(views.html.resource(resource)(lang.language)).as(HTML)
        //        }
        //        Ok(views.html.resource(resource)(lang.language)).as(HTML)

        Ok(views.html.handlebars(resourceUri)(lang.language)(dataPath)(resourceType)).as(HTML)
      }
    }
  }

    def buildTemplateResult(resourceUri: String, graph: Graph, lang: Lang): Result = {
//      val strRDF = ResourceSerialiser.asTurtle(graph, resourceUri).get
//      //    val rdf = RDFTriples.parse(strRDF).get
//      JenaUtils.str2Model(strRDF) match {
//        case Parsed(model) => {
////          val shapes = ShapeMatcher.matchWithShacl(RDFAsJenaModel(model), resourceUri)
//          val resource = ResourceBuilderWithJena.build(resourceUri, graph, List(""))
////          val res = ResourceSerialiser.asTemplateData(resource)
////          Logger.debug(Json.toJson(resource).toString)
////          Ok(views.html.handlebars(resourceUri)(lang.language)).as(HTML)
//          val res: JsValue = Json.toJson(resource)
//          Ok(res)
//        }
//      }


      val resource = ResourceBuilderWithJena.build(resourceUri, graph, List(""))
      val res: JsValue = Json.toJson(resource)
      Ok(res)


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

  def context() = Action {
    Ok(Json.parse(scala.io.Source.fromFile("conf/context.json").mkString))
  }
}

