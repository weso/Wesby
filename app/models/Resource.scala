package models

import org.w3.banana._

/**
 * Created by jorge on 6/10/15.
 */
class Resource[Rdf<:RDF](
  val uri: Rdf#URI,
  val labels: Iterable[Rdf#Literal],
  val shapes: List[String],//List[Rdf#URI],
  val properties: Iterable[(Rdf#URI, Rdf#Node)]//,
  //  inverseProperties: List[Rdf#Node]
  ) {

  def label = {
    val literal = labels.head.toString
    literal.substring(1, literal.length - 1)
  }
}

