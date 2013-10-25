package models

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }

sealed abstract class Node {
  val uri: String
}

case class Resource(uri: String, val label: Option[String]) extends Node
case class Property(uri: String, val label: Option[String]) extends Node
case class Literal(uri: String, val value: String) extends Node

case class ResultQuery(subject: Model, predicate: InverseModel)

case class Model(val jenaModel: JenaModel) extends DataStore {

  def add(p: Property, n: Node) = addToDataStore(p, n)

  override def toString(): String = {
    new StringBuilder("Model[nodes:").append(map.toString).append("]").toString
  }

}

case class InverseModel(val jenaModel: JenaModel) extends DataStore {

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