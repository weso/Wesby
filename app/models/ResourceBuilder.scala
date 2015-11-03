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

//  val turtleWriter: RDFWriter[Rdf, Try, Turtle]

  def getProperties(graph: Rdf#Graph, uri: Rdf#URI): Iterable[(WURI[RDF], Rdf#Node)] = {
    import ops._
    val triples = graph.triples.filter(_._1.equals(uri))
    for(Triple(s, p, o) <- triples) yield {
      (WURI[RDF](p), o)
    }
  }

  def getInverseProperties(graph: Rdf#Graph, uri: Rdf#URI): Iterable[(Rdf#Node, WURI[RDF])] = {
    val inverseTriples = graph.triples.filter(_._3.equals(uri))
    for(Triple(s, p, o) <- inverseTriples) yield {
//      (s,p)
      (s, WURI[RDF](p))
    }
  }

  def build(uriString: String, graph: Rdf#Graph, shapes: List[String]) = {

    Logger.debug("Graph: " + graph)
    val rdfs = RDFSPrefix[Rdf]

    val uri = URI(uriString)
    val ncname = uri.lastPathSegment
    val prefix = uriString.dropRight(ncname.length)

    Logger.debug("URI: " + uri.getString)
    Logger.debug("NCNAME: " + ncname)
    Logger.debug("PREFIX: " + prefix)
    Logger.debug("URI: " + PrefixMapping.getNsURIPrefix(prefix))

    val pg = PointedGraph(uri, graph)
    val labelsPg = pg / rdfs.label
    val labels = for (label <- labelsPg.map(_.pointer)) yield label match {
      case l: Rdf#Literal => l
    }

    val properties = getProperties(graph, uri)
    val inverseProperties = getInverseProperties(graph, uri)

    val resource = new Resource[RDF](uri, labels, shapes, properties, inverseProperties)

    resource
  }

}

import org.w3.banana.jena.JenaModule

object ResourceBuilderWithJena extends ResourceBuilder with JenaModule