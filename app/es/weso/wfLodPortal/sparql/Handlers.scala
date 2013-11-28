package es.weso.wfLodPortal.sparql

import scala.Option.option2Iterable
import scala.reflect.ClassTag

import es.weso.wfLodPortal.models.DataStore
import es.weso.wfLodPortal.models.RdfLiteral
import es.weso.wfLodPortal.models.RdfNode
import es.weso.wfLodPortal.models.RdfResource

object Handlers {

  val Empty = ""
  val BlankSpace = " "

  def handleAs[U <: RdfNode: ClassTag, T](dataStore: DataStore, prefix: String,
    suffix: String, fallback: (U) => T): List[T] = {
    val nodes = dataStore.get(prefix, suffix) match {
      case Some(propertyResult) =>
        for {
          node <- propertyResult.nodes
        } yield {
          node match {
            case r: U => Some(fallback(r))
            case _ => None
          }
        }
      case None => List.empty
    }
    nodes.toList.flatten
  }

  def handleFirstAs[U <: RdfNode: ClassTag, T](dataStore: DataStore,
    prefix: String, suffix: String, fallback: (U) => T): Option[T] = {
    dataStore.get(prefix, suffix) match {
      case Some(propertyResult) =>
        propertyResult.nodes.toList match {
          case r :: _ => r match {
            case r: U => Some(fallback(r))
            case _ => None
          }
          case Nil => None
        }
      case None => None
    }
  }

  def handleResourceAs[T](dataStore: DataStore, prefix: String, suffix: String,
    fallback: (RdfResource) => T): List[T] = {
    handleAs[RdfResource, T](dataStore, prefix, suffix, fallback)
  }

  def handleResourceAsString(dataStore: DataStore, prefix: String, suffix: String,
    fallback: (RdfResource) => String, escape: String = BlankSpace): String = {
    handleResourceAs[String](dataStore, prefix, suffix, fallback).mkString(escape)
  }

  def handleFirstResourceAs[T](dataStore: DataStore, prefix: String, suffix: String,
    fallback: (RdfResource) => T): Option[T] = {
    handleFirstAs[RdfResource, T](dataStore, prefix, suffix, fallback)
  }

  def handleFirstResourceAsString(dataStore: DataStore, prefix: String, suffix: String,
    fallback: (RdfResource) => String, default: String = Empty): String = {
    handleFirstResourceAs[String](dataStore, prefix, suffix, fallback).getOrElse(default)
  }

  def handleLiteralAs[T](dataStore: DataStore, prefix: String, suffix: String,
    fallback: (RdfLiteral) => T): List[T] = {
    handleAs[RdfLiteral, T](dataStore, prefix, suffix, fallback)
  }

  def handleLiteralAsString(dataStore: DataStore, prefix: String, suffix: String,
    fallback: (RdfLiteral) => String, escape: String = BlankSpace): String = {
    handleLiteralAs[String](dataStore, prefix, suffix, fallback).mkString(escape)
  }

  def handleFirstLiteralAs[T](dataStore: DataStore, prefix: String, suffix: String,
    fallback: (RdfLiteral) => T): Option[T] = {
    handleFirstAs[RdfLiteral, T](dataStore, prefix, suffix, fallback)
  }

  def handleFirstLiteralAsString(dataStore: DataStore, prefix: String, suffix: String,
    fallback: (RdfLiteral) => String, default: String = Empty): String = {
    handleFirstLiteralAs[String](dataStore, prefix, suffix, fallback).getOrElse(default)
  }

  def handleLiteralAsValue(dataStore: DataStore, prefix: String, suffix: String,
    escape: String = BlankSpace): String = {
    val inner = (l: RdfLiteral) => l.value
    handleLiteralAsString(dataStore, prefix, suffix, inner, escape)
  }

  def handleFirstLiteralAsValue(dataStore: DataStore, prefix: String,
    suffix: String, default: String = Empty): String = {
    val inner = (l: RdfLiteral) => l.value
    handleFirstLiteralAsString(dataStore, prefix, suffix, inner, default)
  }
}