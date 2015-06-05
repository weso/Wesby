package models

import java.net.URL
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
  with JsonLDWriterModule

/**
 * Created by jorge on 5/6/15.
 */
trait QueryEngine extends QueryEngineDependencies { self =>

  import ops._
  import sparqlOps._
  import sparqlHttp.sparqlEngineSyntax._

  val endpoint = new URL(Play.application().configuration().getString("wesby.endpoint"))

  def queryTestSelect(resource: String): String = {
    Logger.debug("Querying: " + resource)
    val query = parseSelect(
      s"""
        |SELECT DISTINCT ?s ?v ?o WHERE {
        |  {<$resource> ?v ?o .}
        |  UNION
        |  {?s <$resource> ?o .}
        |  UNION
        |  {?s ?v <$resource> .}
        |}
      """.stripMargin).get

    val answers: Rdf#Solutions = endpoint.executeSelect(query).get

    val languages: Iterator[Rdf#URI] = answers.iterator map { row =>
      /* row is an Rdf#Solution, we can get an Rdf#Node from the variable name */
      /* both the #Rdf#Node projection and the transformation to Rdf#URI can fail in the Try type, hence the flatMap */
      row("?v").get.as[Rdf#URI].get
    }

    languages.to[List].toString()
  }

  def queryTestConstructXml(resource: String) = {
    Logger.debug("Querying: " + resource)
    val query = parseConstruct(
      s"""
         |CONSTRUCT { ?s ?v ?o } WHERE {
         |  	<$resource> ?v ?o .
         |	?s ?v ?o.
         |}
      """.stripMargin).get

    val resultGraph = endpoint.executeConstruct(query).get
    val graphAsString = rdfXMLWriter.asString(resultGraph, base = "") getOrElse sys.error("coudn't serialize the graph")
    graphAsString // TODO base?
  }

  def queryTestConstructTurtle(resource: String) = {
    Logger.debug("Querying: " + resource)
    val query = parseConstruct(
      s"""
         |CONSTRUCT { ?s ?v ?o } WHERE {
         |  	<$resource> ?v ?o .
        |	?s ?v ?o.
        |}
      """.stripMargin).get

    val resultGraph = endpoint.executeConstruct(query).get
    val graphAsString = turtleWriter.asString(resultGraph, base = "") getOrElse sys.error("coudn't serialize the graph")
    graphAsString
  }
}

import org.w3.banana.jena.JenaModule

object QueryEngineWithJena extends QueryEngine with JenaModule {
  override implicit val jsonldExpandedWriter: RDFWriter[Jena, Try, JsonLdExpanded] = _
  override implicit val jsonldFlattenedWriter: RDFWriter[Jena, Try, JsonLdFlattened] = _
}
