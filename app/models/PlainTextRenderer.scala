package models

import com.hp.hpl.jena.graph.Graph
import org.w3.banana.jena.JenaModule

/**
 * Created by jorge on 15/7/15.
 */
trait PlainTextRendererTrait extends QueryEngineDependencies {
  import ops._
  import sparqlOps._
  import sparqlHttp.sparqlEngineSyntax._

  def render(solutions: Rdf#Solutions, header: String): String = {
    val sb = new StringBuilder

    sb.append(header)
    sb.append("\n\n")

    val result = for (row <- solutions.iterator) yield {
      row("r1").get.as[Rdf#URI].get + "\t" + row("r2").get.as[Rdf#Node].get
    }

    sb.append(result.mkString("\n"))
    sb.mkString
  }

  def renderConstruct(graph: Rdf#Graph, header: String): String = {
    val sb = new StringBuilder

    sb.append(header)
    sb.append("\n\n")

    val result = graph.triples.collect {
      case Triple(URI(s), p, o) => s"$p\t$o"
    } // collect should drop the failed items

    sb.append(result.mkString("\n"))
    sb.mkString
  }

}

object PlainTextRenderer extends PlainTextRendererTrait with JenaModule

