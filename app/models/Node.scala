package models

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel, Property => JenaProperty, Resource => JenaResource, RDFNode => JenaRDFNode }

sealed abstract class Node {
  val rdfNode :  JenaRDFNode
}

case class Resource(val uri: String, 
    val label: Option[String], 
    rdfNode: JenaResource) extends Node {
  
  def resource: JenaResource = rdfNode
  
}
case class Property(val uri: String, 
    val label: Option[String], 
    rdfNode: JenaProperty) extends Node {
  
  def property : JenaProperty = rdfNode
  
}
case class Literal(val value: String, 
    val dataType: Option[String], 
    rdfNode: JenaRDFNode) extends Node {
  
  def literal: JenaRDFNode = rdfNode
  
}

case class ResultQuery(subject: Model, predicate: InverseModel)

case class Model(val jenaModel: JenaModel) extends DataStore {
  
  def add(p: Property, r: Resource) { addToDataStore(p, r) }
  
  def add(p: Property, l: Literal) { addToDataStore(p, l) }
  
  def add(p: Property, n: Node) { addToDataStore(p, n) }

  override def toString(): String = {
    new StringBuilder("Model[nodes:").append(map.toString).append("]").toString
  }

}

case class InverseModel(val jenaModel: JenaModel) extends DataStore {

  def add(r: Resource, p: Property) { addToDataStore(p, r) }
  
  def add(l: Literal, p: Property) { addToDataStore(p, l) }
  
  def add(n: Node, p: Property) { addToDataStore(p, n) }

  override def toString(): String = {
    new StringBuilder("InverseModel[nodes:").append(map.toString).append("]").toString
  }
}

trait DataStore {
  protected val map: HashMap[String, (Property, ListBuffer[Node])] = HashMap.empty

  protected def addToDataStore(p: Property, n: Node) {
    val m = map.getOrElse(p.uri, (p, new ListBuffer[Node]()))
    val l = m._2
    l += n
    map += p.uri -> (p, l)
  }

  protected def get(uri: String): Option[(Property, ListBuffer[Node])] = {
    map.get((uri))
  }

  def list = map.valuesIterator.toList

}