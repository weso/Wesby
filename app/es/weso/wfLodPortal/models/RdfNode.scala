package es.weso.wfLodPortal.models

import com.hp.hpl.jena.rdf.model.{ Property => JenaProperty }
import com.hp.hpl.jena.rdf.model.{ RDFNode => JenaRDFNode }
import com.hp.hpl.jena.rdf.model.{ Resource => JenaResource }

sealed abstract class RdfNode {
  val rdfNode: JenaRDFNode

  def r: JenaRDFNode = rdfNode

  def asRdfAnon: Option[RdfAnon] = {
    this match {
      case e: RdfAnon => Some(e)
      case _ => None
    }
  }

  def asRdfResource: Option[RdfResource] = this match {
    case e: RdfResource => Some(e)
    case _ => None
  }

  def asRdfProperty: Option[RdfProperty] = this match {
    case p: RdfProperty => Some(p)
    case _ => None
  }

  def asRdfLiteral: Option[RdfLiteral] = this match {
    case l: RdfLiteral => Some(l)
    case _ => None
  }

}

case class RdfAnon(
  rdfNode: JenaResource) extends RdfNode {

  def resource = rdfNode

  override def toString: String = {
    new StringBuilder("A[id:'").append(rdfNode.getId()).append("']").toString
  }
}

trait Resource {
  val dataStores: ResultQuery

  def dss: ResultQuery = dataStores

}

case class RdfResource(
  val uri: Uri,
  dataStores: ResultQuery,
  rdfNode: JenaResource) extends RdfNode with Resource {

  def u = uri
  def resource = rdfNode

  override def toString: String = {
    new StringBuilder("R[uri:'").append(uri).append("']").toString
  }
}

case class RdfProperty(
  val uri: Uri,
  dataStores: ResultQuery,
  rdfNode: JenaProperty) extends RdfNode with Resource {

  def u = uri
  def property: JenaProperty = rdfNode

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