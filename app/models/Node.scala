package models

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel, Property => JenaProperty, Resource => JenaResource, RDFNode => JenaRDFNode }

sealed abstract class Node {
  val rdfNode: JenaRDFNode
}

case class Uri(val relative: String, val absolute: String, val short: Option[ShortUri])
case class ShortUri(val prefix: (String, String), val suffix: (String, String))

case class Resource(
  val uri: Uri,
  val label: Option[String],
  rdfNode: JenaResource) extends Node {

  def resource: JenaResource = rdfNode

  override def toString: String = {
    new StringBuilder("R[uri:'").append(uri).append("', Label:'")
      .append(label).append("']").toString
  }
}

case class Property(
  val uri: Uri,
  val label: Option[String],
  rdfNode: JenaProperty) extends Node {

  def property: JenaProperty = rdfNode

  override def toString: String = {
    new StringBuilder("P[uri:'").append(uri).append("', Label:'")
      .append(label).append("']").toString
  }
}

case class Literal(
  val value: String,
  val dataType: Option[String],
  rdfNode: JenaRDFNode) extends Node {

  def literal: JenaRDFNode = rdfNode

  override def toString: String = {
    new StringBuilder("L[Value:'").append(value).append("', Data Type:'")
      .append(dataType).append("']").toString
  }

}

case class ResultQuery(subject: Model, predicate: InverseModel)

case class Model(
  val jenaModel: JenaModel) extends DataStore {

  def add(p: Property, r: Resource, m: Option[Model] = None) { addToDataStore(p, r, m) }

  def add(p: Property, l: Literal) { addToDataStore(p, l) }

  override def toString(): String = {
    new StringBuilder("Model[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }

}

case class InverseModel(
  val jenaModel: JenaModel) extends DataStore {

  def add(r: Resource, p: Property) { addToDataStore(p, r) }

  def add(r: Resource, m: InverseModel, p: Property) { addToDataStore(p, r, Some(m)) }

  def add(l: Literal, p: Property) { addToDataStore(p, l) }

  override def toString(): String = {
    new StringBuilder("InverseModel[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }
}

trait DataStore {

  protected val map: HashMap[String, (Property, ListBuffer[(Node, Option[DataStore])])] = HashMap.empty

  protected def addToDataStore(p: Property, n: Node, d: Option[DataStore] = None) {
    val m = map.getOrElse(p.uri.relative, (p, new ListBuffer[(Node, Option[DataStore])]()))
    val l = m._2
    l += ((n, d))
    map += p.uri.relative -> (p, l)
  }

  def get(uri: String): Option[(Property, ListBuffer[(Node, Option[DataStore])])] = {
    map.get(uri)
  }

  def list = map.valuesIterator.toList

}