package models

import org.w3.banana._
import org.w3.banana.io.{RDFWriter, Turtle}
import play.Logger

import scala.util.Try

trait ResourceBuilderDependencies
  extends RDFModule
  with RDFOpsModule

/**
 * Created by jorge on 6/10/15.
 */
trait ResourceBuilder extends ResourceBuilderDependencies {

  import ops._

  def getProperties(graph: Rdf#Graph, uri: Rdf#URI): Map[Rdf#URI, Iterable[Rdf#Node]] = {
    import ops._
    val triples = graph.triples.filter(_._1.equals(uri))
    val l = for(Triple(s, p, o) <- triples) yield {
      (p, o)
    }

    l.groupBy(e => e._1).mapValues(e => e.map(x => x._2))
  }

  def getInverseProperties(graph: Rdf#Graph, uri: Rdf#URI): Iterable[(Rdf#URI, Rdf#URI)] = {
    val inverseTriples = graph.triples.filter(_._3.equals(uri))
    for(Triple(s, p, o) <- inverseTriples) yield {
      (URI(s.toString), p)
    }
  }

  def build(uriString: String, graph: Rdf#Graph, shapes: List[String]) = {

    Logger.debug("Graph: " + graph)
    val rdfs = RDFSPrefix[Rdf]

    val uri = URI(uriString)
    val ncname = uri.lastPathSegment
    val prefix = uriString.dropRight(ncname.length)

    val pg = PointedGraph(uri, graph)
    val labelsPg = pg / rdfs.label
    val labels = for (label <- labelsPg.map(_.pointer)) yield label match {
      case l: Rdf#Literal => l
    }

    val properties = getProperties(graph, uri)
    val inverseProperties = getInverseProperties(graph, uri)

    val resource = new Resource[Rdf](uri, labels, shapes, properties, inverseProperties)

    resource
  }

}

import org.w3.banana.jena.JenaModule

object ResourceBuilderWithJena extends ResourceBuilder with JenaModule