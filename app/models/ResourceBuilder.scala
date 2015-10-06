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

  def build(uriString: String, graph: Rdf#Graph) = {

    import ops._


    val uri = URI(uriString)

    val rdfs = RDFSPrefix[Rdf]

    Logger.debug("Graph: " + graph)
    Logger.debug("Label: " + graph.resolveAgainst(rdfs.label))

//    graph.triples.foreach {
//      case (s, p, o) => {
//        Logger.debug("P: " + p)
//      }
//      case _ => Logger.debug("x")
//    }
    val g = graph.toPointedGraph

    for (k <- g / rdfs.label) {
      println(k)
    }

    val resource = new Resource(uri, "test")

    resource
  }

}

import org.w3.banana.jena.JenaModule

object ResourceBuilderWithJena extends ResourceBuilder with JenaModule