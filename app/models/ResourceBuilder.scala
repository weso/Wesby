package models

import java.util.regex.Pattern

import org.w3.banana._
import org.w3.banana.io.{RDFWriter, Turtle}
import play.{Logger, Play}

import scala.util.Try

trait ResourceBuilderDependencies
  extends RDFModule
  with RDFOpsModule

/**
 * Created by jorge on 6/10/15.
 */
trait ResourceBuilder extends ResourceBuilderDependencies {

  import ops._

  def rewrite(uri: Rdf#URI) = {
    val host = Play.application().configuration().getString("wesby.host")
    val datasetBase = Play.application().configuration().getString("wesby.datasetBase")
    val dereferencedUri = uri.toString.replaceFirst(Pattern.quote(datasetBase), host)
    URI(dereferencedUri)
  }

  def getProperties(graph: Rdf#Graph, uri: Rdf#URI): Map[Rdf#URI, Iterable[Rdf#Node]] = {
    import ops._
    val triples = graph.triples.filter(_._1.equals(uri))
    val l = for(Triple(s, p, o) <- triples) yield {

      if (o.isURI)
        (rewrite(p), rewrite(o.asInstanceOf[Rdf#URI]).asInstanceOf[Rdf#Node])
      else (rewrite(p), o)
    }

    l.groupBy(e => e._1).mapValues(e => e.map(x => x._2))
  }

  def getInverseProperties(graph: Rdf#Graph, uri: Rdf#URI): Iterable[(Rdf#URI, Rdf#URI)] = {
    val inverseTriples = graph.triples.filter(_._3.equals(uri))
    for(Triple(s, p, o) <- inverseTriples) yield {
      (rewrite(URI(s.toString)), rewrite(p))
    }
  }

  def build(uriString: String, graph: Rdf#Graph, shapes: List[String]) = {
    val mainLabelProp = Play.application().configuration().getString("wesby.altLabelProperty")

    val uri = URI(uriString)
    val pg = PointedGraph(uri, graph)

//    val ncname = uri.lastPathSegment

    val rdfs: RDFSPrefix[Rdf] = RDFSPrefix[Rdf]

    val rdfsLabelsPg = pg / rdfs.label
    val mainLabelsPg = pg / URI(mainLabelProp)

    val rdfsLabels = for (label <- rdfsLabelsPg.map(_.pointer)) yield label match {
      case l: Rdf#Literal => l
    }
    val defaultLabels = for (label <- mainLabelsPg.map(_.pointer)) yield label match {
      case l: Rdf#Literal => l
    }

    val labels = defaultLabels ++ rdfsLabels
    val properties = getProperties(graph, uri)
    val inverseProperties = getInverseProperties(graph, uri)

    val resource = new Resource[Rdf](uri, labels, shapes, properties, inverseProperties)

    resource
  }

}

import org.w3.banana.jena.JenaModule

object ResourceBuilderWithJena extends ResourceBuilder with JenaModule