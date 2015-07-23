package models

import java.net.URL
import org.w3.banana
import org.w3.banana._
import org.w3.banana.diesel._
import org.w3.banana.io.{JsonLdExpanded, JsonLdFlattened, RDFWriter}
import org.w3.banana.jena.Jena
import play.{Logger, Play}

import scala.util.Try

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
    Logger.debug("Querying: " + resource)
    val selectQueryString = queryString.replace("$resource", resource)
    val query = parseSelect(selectQueryString).get

    val solutions: Rdf#Solutions = endpoint.executeSelect(query).get

    solutions
  }

  def construct(resource: String, queryString: String): Try[Rdf#Graph] = {
    Logger.debug("Querying: " + resource)
    Logger.debug("with: " + queryString)
    val constructQueryString = queryString.replace("$resource", resource)
    val query = parseConstruct(constructQueryString).get

    endpoint.executeConstruct(query)
  }
}

import org.w3.banana.jena.JenaModule

object QueryEngineWithJena extends QueryEngine with JenaModule