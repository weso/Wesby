package models

import java.util.regex.Pattern

import org.w3.banana._
import org.w3.banana.io.{RDFWriter, Turtle}
import play.{Logger, Play}

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

  def rewrite(uri: Rdf#URI) = { // TODO extract to utils
  val host = Play.application().configuration().getString("wesby.host")
    val datasetBase = Play.application().configuration().getString("wesby.datasetBase")
    val dereferencedUri = uri.toString.replaceFirst(Pattern.quote(datasetBase), host)
    URI(dereferencedUri)
  }

  def build(solutions: Rdf#Solutions) = {
    val resources = solutions.iterator map { row =>

      (row("label").get.as[Rdf#Literal].get, rewrite(row("resource").get.as[Rdf#URI].get).toString)
    }

    resources
  }

}

import org.w3.banana.jena.JenaModule

object SearchResultsBuilder extends SearchResultsBuilder with JenaModule