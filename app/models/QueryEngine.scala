package models

import java.net.URL
import org.w3.banana._
import org.w3.banana.diesel._
import play.{Logger, Play}

trait QueryEngineDependencies
  extends RDFModule
  with RDFOpsModule
  with SparqlOpsModule
  with SparqlHttpModule

/**
 * Created by jorge on 5/6/15.
 */
trait QueryEngine extends QueryEngineDependencies { self =>

  import ops._
  import sparqlOps._
  import sparqlHttp.sparqlEngineSyntax._

  val endpoint = new URL(Play.application().configuration().getString("wesby.endpoint"))

  def queryTest(resource: String): String = {
    Logger.debug("Querying: " + resource)
    val query = parseSelect(
      s"""
        |SELECT DISTINCT ?v ?o WHERE {
        |  <$resource> ?v ?o
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

}

import org.w3.banana.jena.JenaModule

object QueryEngineWithJena extends QueryEngine with JenaModule
