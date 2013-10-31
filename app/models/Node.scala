package models

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel, Property => JenaProperty, Resource => JenaResource, RDFNode => JenaRDFNode }

sealed abstract class Node {
  val rdfNode: JenaRDFNode
}

case class Uri(val relative: String, val absolute: String, val short: Option[ShortUri])
case class ShortUri(val prefix: (String, String), val suffix: (String, String))

case class Anon(
  val label: Option[String],
  rdfNode: JenaResource) extends Node

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
  val dataType: Option[Uri],
  rdfNode: JenaRDFNode) extends Node {

  def literal: JenaRDFNode = rdfNode

  override def toString: String = {
    new StringBuilder("L[Value:'").append(value).append("', Data Type:'")
      .append(dataType).append("']").toString
  }

}

case class ResultQuery(subject: Model, predicate: InverseModel)

case class Model(
  val jenaModel: JenaModel) extends DataStore[Model] {

  def add(p: Property, r: Resource, lds: LazyDataStore[Model]) { addToDataStore(p, r, Some(lds)) }

  def add(p: Property, l: Literal) { addToDataStore(p, l, None) }
  
  def add(p: Property, a: Anon) { addToDataStore(p, a, None) }

  override def toString(): String = {
    new StringBuilder("Model[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }

}

case class InverseModel(
  val jenaModel: JenaModel) extends DataStore[InverseModel] {

  def add(r: Resource, p: Property, lds: LazyDataStore[InverseModel]) { addToDataStore(p, r, Some(lds)) }

  def add(l: Literal, p: Property, lds: Option[LazyDataStore[InverseModel]]) { addToDataStore(p, l, lds) }
  
  override def toString(): String = {
    new StringBuilder("InverseModel[nodes:{").append(map.mkString(", "))
      .append("}]").toString
  }
}

case class LazyDataStore[T](uri: Uri, method: (String) => T) {
  lazy val dataStore = method(uri.absolute)
  def data = dataStore
}

trait DataStore[T] {

  protected val map: HashMap[String, (Property, ListBuffer[(Node, Option[LazyDataStore[T]])])] = HashMap.empty

  protected def addToDataStore(p: Property, n: Node, lds: Option[LazyDataStore[T]]) {
    val m = map.getOrElse(p.uri.relative, (p, new ListBuffer[(Node, Option[LazyDataStore[T]])]()))
    val l = m._2
    l += ((n, lds))
    map += p.uri.relative -> (p, l)
  }

  def get(uri: String): Option[(Property, ListBuffer[(Node, Option[LazyDataStore[T]])])] = {
    map.get(uri)
  }

  def list = map.valuesIterator.toList

}