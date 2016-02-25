package models

import java.net.URI

import org.w3.banana._
import org.w3.banana.io.{RDFWriter, Turtle}
import play.Logger

import scala.util.Try

trait SparqlSolutionsDependencies
  extends RDFModule
  with RDFOpsModule
  with SparqlOpsModule
  with SparqlHttpModule

/**
 * Created by jorge on 6/10/15.
 */
trait SearchResultsBuilder extends SparqlSolutionsDependencies {

  import ops._
  import sparqlOps._
  import sparqlHttp.sparqlEngineSyntax._

  def build(solutions: Rdf#Solutions) = {
    val resources = solutions.iterator map { row =>

      (row("label").get.as[String].get, row("resource").get.as[Rdf#URI].get.toString)
    }

    resources
  }

}

import org.w3.banana.jena.JenaModule

object SearchResultsBuilder extends SearchResultsBuilder with JenaModule