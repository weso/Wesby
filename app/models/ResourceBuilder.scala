package models

import org.w3.banana._
import org.w3.banana.jena.JenaModule
import play.Logger

trait ResourceBuilderDependencies
  extends RDFModule
  with RDFOpsModule

/**
 * Created by jorge on 6/10/15.
 */
trait ResourceBuilder extends QueryEngineDependencies {

  import ops._

  def trimQuotes(s: String) = {
    s.substring(1, s.length - 1)
  }

  def getLabels(graph: Rdf#Graph): Option[Iterable[String]] = {
    val rdfs = RDFSPrefix[Rdf]
    val labels: Iterable[String] = for (Triple(s, rdfs.label, o) <- graph.triples) yield {
      trimQuotes(o.toString)
    }
    Logger.debug("Labels: " + labels)
    if (labels.isEmpty) None
    else Option(labels)
  }

  def build(uriString: String, graph: Rdf#Graph, shapes: List[String]) = {


    Logger.debug("Graph: " + graph)

    val labels = getLabels(graph).getOrElse(Iterable(uriString))


    //    val g = graph.toPointedGraph

    //    for (k <- g / rdfs.label) {
    //      println(k)
    //    }

    val resource = new Resource(uriString, labels.toList, shapes)

    resource
  }

}

import org.w3.banana.jena.JenaModule

object ResourceBuilderWithJena extends ResourceBuilder with JenaModule