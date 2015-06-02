package es.weso.wesby.sparql

import es.weso.wesby.models.{DataStore, RdfLiteral, RdfNode, RdfResource}

import scala.Option.option2Iterable
import scala.reflect.ClassTag

/**
 * Handles information retrieval from a DataStore.
 * 
 * Includes a set of handlers that perform information retrieval in a safe
 * way (Taking care of Options, Empty Lists and so forth).
 * 
 * @author César Luis Alvargonzález (cesarla)
 */
object Handlers {

  val Empty = ""
  val BlankSpace = " "

   /**
   * Finds the Us (Sub-types of RdfNode) in the DataStore for a given URI, and then, returns 
   * the results of the applied callbacks as a List of T.
   * @tparam U Sub-types of RdfNode
   * @tparam T the value of the returned value
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when an 'U' is retrieved
   */
  def handleAs[U <: RdfNode: ClassTag, T](dataStore: DataStore, prefix: String,
    suffix: String, callback: (U) => T): List[T] = {
    val nodes = dataStore.get(prefix, suffix) match {
      case Some(propertyResult) =>
        for {
          node <- propertyResult.nodes
        } yield {
          node match {
            case r: U => Some(callback(r))
            case _ => None
          }
        }
      case None => List.empty
    }
    nodes.toList.flatten
  }

  /**
   * Finds the first Us (Sub-types of RdfNode) in the DataStore for a given URI, and then, returns 
   * the result of the applied callback encapsulated as an Option of T
   * @tparam U Sub-types of RdfNode
   * @tparam T the value of the returned value
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when an 'U' is retrieved
   */
  def handleFirstAs[U <: RdfNode: ClassTag, T](dataStore: DataStore,
    prefix: String, suffix: String, callback: (U) => T): Option[T] = {
    dataStore.get(prefix, suffix) match {
      case Some(propertyResult) =>
        propertyResult.nodes.toList match {
          case r :: _ => r match {
            case r: U => Some(callback(r))
            case _ => None
          }
          case Nil => None
        }
      case None => None
    }
  }

  /**
   * Finds the RdfResources in the DataStore for a given URI, and then, returns 
   * the results of the applied callbacks as a List of T.
   * @tparam T the value of the returned value
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when a RdfResource is retrieved
   */
  def handleResourceAs[T](dataStore: DataStore, prefix: String, suffix: String,
    callback: (RdfResource) => T): List[T] = {
    handleAs[RdfResource, T](dataStore, prefix, suffix, callback)
  }

  /**
   * Finds the RdfResources in the DataStore for a given URI, and then, returns 
   * the results of the applied callbacks as a serialized String.
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when a RdfResource is retrieved
   * @param sep the separator string
   */
  def handleResourceAsString(dataStore: DataStore, prefix: String, suffix: String,
    callback: (RdfResource) => String, sep: String = BlankSpace): String = {
    handleResourceAs[String](dataStore, prefix, suffix, callback).mkString(sep)
  }
  
  
  /**
   * Finds the first RdfResource in the DataStore for a given URI, and then, returns 
   * the result of the applied callback encapsulated as an Option of T
   * @tparam T the value of the returned value
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when a RdfResource is retrieved
   */
  def handleFirstResourceAs[T](dataStore: DataStore, prefix: String, suffix: String,
    callback: (RdfResource) => T): Option[T] = {
    handleFirstAs[RdfResource, T](dataStore, prefix, suffix, callback)
  }

   /**
   * Finds the first RdfResource in the DataStore for a given URI, and then, returns 
   * the result of the applied callback as a String.
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when a RdfLiteral is retrieved
   * @param default a default value when no RdfResource is found (Empty 
   * String as default)
   */
  def handleFirstResourceAsString(dataStore: DataStore, prefix: String, suffix: String,
    callback: (RdfResource) => String, default: String = Empty): String = {
    handleFirstResourceAs[String](dataStore, prefix, suffix, callback).getOrElse(default)
  }

  /**
   * Finds the RdfLiterals in the DataStore for a given URI, and then, returns 
   * the results of the applied callbacks as a List of T.
   * @tparam T the value of the returned value
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when a RdfLiteral is retrieved
   */
  def handleLiteralAs[T](dataStore: DataStore, prefix: String, suffix: String,
    callback: (RdfLiteral) => T): List[T] = {
    handleAs[RdfLiteral, T](dataStore, prefix, suffix, callback)
  }

  /**
   * Finds the RdfLiterals in the DataStore for a given URI, and then, returns 
   * the results of the applied callbacks as a serialized String.
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when a RdfLiteral is retrieved
   * @param sep the separator string
   */
  def handleLiteralAsString(dataStore: DataStore, prefix: String, suffix: String,
    callback: (RdfLiteral) => String, sep: String = BlankSpace): String = {
    handleLiteralAs[String](dataStore, prefix, suffix, callback).mkString(sep)
  }
  
  /**
   * Finds the first RdfLiteral in the DataStore for a given URI, and then, returns the 
   * result of the applied callback encapsulated as an Option of T.
   * @tparam T the value of the returned value
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when a RdfLiteral is retrieved
   */
  def handleFirstLiteralAs[T](dataStore: DataStore, prefix: String, suffix: String,
    callback: (RdfLiteral) => T): Option[T] = {
    handleFirstAs[RdfLiteral, T](dataStore, prefix, suffix, callback)
  }

  /**
   * Finds the first RdfLiteral in the DataStore for a given URI, and then, returns the 
   * result of the applied callback as a String.
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param callback Function to be applied when a RdfLiteral is retrieved
   * @param default a default value when no RdfLiteral is found (Empty 
   * String as default)
   */
  def handleFirstLiteralAsString(dataStore: DataStore, prefix: String, suffix: String,
    callback: (RdfLiteral) => String, default: String = Empty): String = {
    handleFirstLiteralAs[String](dataStore, prefix, suffix, callback).getOrElse(default)
  }

  /**
   * Finds the RdfLiterals in the DataStore for a given URI, and then, returns the 
   * values from the literals.
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param sep the separator string
   */
  def handleLiteralAsValue(dataStore: DataStore, prefix: String, suffix: String,
    sep: String = BlankSpace): String = {
    val inner = (l: RdfLiteral) => l.value
    handleLiteralAsString(dataStore, prefix, suffix, inner, sep)
  }

  /**
   * Finds the first RdfLiteral in the DataStore for a given URI, and then, returns 
   * its value.
   * @param dataStore the DataStore to perform the pseudo-query
   * @param prefix the URI's prefix (Whole URI)
   * @param suffix the URI's suffix
   * @param default a default value when no RdfLiteral is found (Empty 
   * String as default)
   */
  def handleFirstLiteralAsValue(dataStore: DataStore, prefix: String,
    suffix: String, default: String = Empty): String = {
    val inner = (l: RdfLiteral) => l.value
    handleFirstLiteralAsString(dataStore, prefix, suffix, inner, default)
  }
}