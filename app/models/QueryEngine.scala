package models

import java.net.URL
import org.w3.banana
import org.w3.banana._
import org.w3.banana.diesel._
import org.w3.banana.io.{JsonLdExpanded, JsonLdFlattened, RDFWriter}
import org.w3.banana.jena.Jena
import play.{Logger, Play}

import scala.util.{Failure, Success, Try}

trait QueryEngineDependencies
  extends RDFModule
  with RDFOpsModule
  with SparqlOpsModule
  with SparqlHttpModule
  with RDFXMLWriterModule
  with TurtleWriterModule

/**
 * Created by jorge on 5/6/15.
 */
trait QueryEngine extends QueryEngineDependencies { self =>

  import ops._
  import sparqlOps._
  import sparqlHttp.sparqlEngineSyntax._

  val endpoint = new URL(Play.application().configuration().getString("wesby.endpoint"))

  def replace(resource: String, queryString: String) = {

  }

  def select(resource: String, queryString: String): Rdf#Solutions = {
//    Logger.debug("Querying: " + resource)
    val selectQueryString = queryString.replace("$resource", resource)
    val query = parseSelect(selectQueryString).get

    val solutions: Rdf#Solutions = endpoint.executeSelect(query).get

    solutions
  }

  def construct(resource: String, queryString: String): Try[Rdf#Graph] = {
//    Logger.debug("Querying: " + resource)
    val constructQueryString = queryString.replace("$resource", resource)
    val query = parseConstruct(constructQueryString).get

    endpoint.executeConstruct(query)
  }

  def pagedConstruct(resource: String, queryString: String, limit: Int, offset: Int): Try[Rdf#Graph] = {
    //    Logger.debug("Querying: " + resource)
    val constructQueryString = queryString
      .replace("$resource", resource)
      .replace("$limit", limit.toString)
      .replace("$offset", offset.toString)
    val query = parseConstruct(constructQueryString).get

    endpoint.executeConstruct(query)
  }

  def listWords(letter: String) = {
    val listWordsQuery = Play.application().configuration().getString("queries.listWords")

    val selectQueryString = listWordsQuery.replace("$letter", letter)
    val query = parseSelect(selectQueryString).get

    parseSelect(selectQueryString) match {
      case Success(q) => Success(endpoint.executeSelect(q).get)
      case Failure(f) => Failure(f)
    }
  }

  def textSearchSelect(searchQuery: String) = {
    val simpleSearchQuery = Play.application().configuration().getString("queries.textSearch")
    val labelProp = Play.application().configuration().getString("wesby.altLabelProperty")
    val selectQueryString =
      getPrefixesString +
        simpleSearchQuery
          .replace("$query", searchQuery)
          .replace("$labelProp", labelProp)

    parseSelect(selectQueryString) match {
      case Success(q) => Success(endpoint.executeSelect(q).get)
      case Failure(f) => Failure(f)
    }
  }

  def simpleSearchSelect(searchQuery: String, resourceType: String) = {
    val simpleSearchQuery = Play.application().configuration().getString("queries.simpleSearch")
    val selectQueryString =
      getPrefixesString +
      simpleSearchQuery
      .replace("$query", searchQuery)
      .replace("$type", resourceType)

    parseSelect(selectQueryString) match {
      case Success(q) => Success(endpoint.executeSelect(q).get)
      case Failure(f) => Failure(f)
    }
  }

  def labelPropSearchSelect(searchQuery: String, resourceType: String, labelProperty: String) = {
    val simpleSearchQuery = Play.application().configuration().getString("queries.labelPropSearch")

    val selectQueryString =
      getPrefixesString +
      simpleSearchQuery
      .replace("$query", searchQuery)
      .replace("$type", resourceType)
      .replace("$labelProperty", labelProperty)
    val query = parseSelect(selectQueryString).get

    val solutions: Rdf#Solutions = endpoint.executeSelect(query).get

    solutions
  }

  def getLabel(uri: String): Option[Rdf#Literal] = {
    val getLabelQuery = Play.application().configuration().getString("queries.getLabel")

    val queryString = getPrefixesString + getLabelQuery
      .replace("$resource", uri)
      .replace("$lang", "es")
    val query = parseSelect(queryString).get
    val labels = endpoint.executeSelect(query).get.iterator map { row =>
      row("label").get.as[Rdf#Literal].get
    }

    if(labels.isEmpty)
      None
    else
      Some(labels.toList.head)
  }

  def getType(uri: String): Option[String] = {
    val getLabelQuery = Play.application().configuration().getString("queries.getType")

    val queryString = getPrefixesString + getLabelQuery
      .replace("$resource", uri)

    val query = parseSelect(queryString).get
    val types = endpoint.executeSelect(query).get.iterator map { row =>
      row("type").get.as[Rdf#Node].get.toString
    }

    if(types.isEmpty)
      None
    else
      Some(types.toList.head)
  }

  private def getPrefixesString: String = {
    val prefixes = PrefixMapping.prefixToUri
    val prefixesString = (
      prefixes.map {
        case (key, value) => s"""PREFIX $key: <$value>\n"""
      }
        mkString
      )
    prefixesString
  }
}

import org.w3.banana.jena.JenaModule

object QueryEngineWithJena extends QueryEngine with JenaModule