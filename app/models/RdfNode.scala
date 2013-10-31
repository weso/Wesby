package models

import com.hp.hpl.jena.rdf.model.{ Property => JenaProperty, Resource => JenaResource, RDFNode => JenaRDFNode }

sealed abstract class RdfNode {
  val rdfNode: JenaRDFNode
}

case class RdfAnon(
  val label: Option[String],
  rdfNode: JenaResource) extends RdfNode

case class RdfResource(
  val uri: Uri,
  val label: Option[String],
  rdfNode: JenaResource) extends RdfNode {

  def resource: JenaResource = rdfNode

  override def toString: String = {
    new StringBuilder("R[uri:'").append(uri).append("', Label:'")
      .append(label).append("']").toString
  }
}

case class RdfProperty(
  val uri: Uri,
  val label: Option[String],
  rdfNode: JenaProperty) extends RdfNode {

  def property: JenaProperty = rdfNode

  override def toString: String = {
    new StringBuilder("P[uri:'").append(uri).append("', Label:'")
      .append(label).append("']").toString
  }
}

case class RdfLiteral(
  val value: String,
  val dataType: Option[Uri],
  rdfNode: JenaRDFNode) extends RdfNode {

  def literal: JenaRDFNode = rdfNode

  override def toString: String = {
    new StringBuilder("L[Value:'").append(value).append("', Data Type:'")
      .append(dataType).append("']").toString
  }

}