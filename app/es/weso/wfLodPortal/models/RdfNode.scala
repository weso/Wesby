package es.weso.wfLodPortal.models

import com.hp.hpl.jena.rdf.model.{ Property => JenaProperty, Resource => JenaResource, RDFNode => JenaRDFNode }

sealed abstract class RdfNode {
  val rdfNode: JenaRDFNode
  val r: JenaRDFNode = rdfNode
}

trait Resource {
  val dataStores: ResultQuery
  val dss = dataStores
}

case class RdfAnon(
  rdfNode: JenaResource) extends RdfNode {
  val resource = rdfNode
}

case class RdfResource(
  val uri: Uri,
  dataStores: ResultQuery,
  rdfNode: JenaResource) extends RdfNode with Resource {

  val u = uri
  val resource = rdfNode

  override def toString: String = {
    new StringBuilder("R[uri:'").append(uri).append("']").toString
  }
}

case class RdfProperty(
  val uri: Uri,
  dataStores: ResultQuery,
  rdfNode: JenaProperty) extends RdfNode with Resource {

  val u = uri
  val property: JenaProperty = rdfNode

  override def toString: String = {
    new StringBuilder("P[uri:'").append(uri).append("']").toString
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