package models

import org.w3.banana._
import org.w3.banana.io.{Turtle, RDFWriter}
import org.w3.banana.jena.JenaModule

import scala.util.Try


/**
 * Created by jorge on 6/10/15.
 */
class Resource[Rdf<:RDF](
  val uri: Rdf#URI,
  val labels: Iterable[Rdf#Literal],
  val shapes: List[String],//List[Rdf#URI],
  val properties: Map[Rdf#URI, Iterable[Rdf#Node]],
  val inverseProperties: Iterable[(Rdf#URI, Rdf#URI)]
//  val inverseProperties: Iterable[(Rdf#Node, Rdf#URI)]
  ) extends RDFModule with RDFOpsModule with JenaModule{
  import ops._
  def label = labels.headOption
}


