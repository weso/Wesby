package es.weso.wesby.models

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Contains the result from a Query
 * @param sub the children nodes
 * @param pred the parents nodes
 */
case class ResultQuery(
  sub: Option[LazyDataStore[Model]],
  pred: Option[LazyDataStore[InverseModel]]) {

  /**
   * If sub is defined returns the Model encapsulated in an Option, otherwise
   * returns a None.
   */
  lazy val subject = sub match {
    case Some(dt) => Some(dt.data)
    case None => None
  }

  /**
   * If sub is defined returns the InverseModel encapsulated in an Option, 
   * otherwise returns a None.
   */
  lazy val predicate = pred match {
    case Some(dt) => Some(dt.data)
    case None => None
  }

}

/**
 * ResultQuery Companion Object
 */
object ResultQuery {

  //  implicit val resultQueryWrites: Writes[ResultQuery] = (
  //    (JsPath \ "sub").write[Option[LazyDataStore[Model]]] and
  //    (JsPath \ "pred").write[Option[LazyDataStore[InverseModel]]]
  //  )(unlift(ResultQuery.unapply))

  /**
   * Returns the result of applying a LazyDataStore[Model]
   * and a LazyDataStore[InverseModel].
   * @param subject the LazyDataStore from Model.
   * @param predicate the LazyDataStore from InverseModel.
   */
  def apply(subject: LazyDataStore[Model],
    predicate: LazyDataStore[InverseModel]) = new ResultQuery(Some(subject), Some(predicate))

  /**
   * Returns the result of applying an Option[LazyDataStore[Model]]
   * and a LazyDataStore[InverseModel].
   * @param subject the LazyDataStore from Model encapsulated in an Option.
   * @param predicate the LazyDataStore from InverseModel.
   */
  def apply(subject: Option[LazyDataStore[Model]],
    predicate: LazyDataStore[InverseModel]) = new ResultQuery(subject, Some(predicate))

  /**
   * Returns the result of applying a LazyDataStore[Model] and
   * an Option[LazyDataStore[InverseModel].
   * @param subject the LazyDataStore from Model.
   * @param predicate the LazyDataStore from InverseModel encapsulated in an Option.
   */
  def apply(subject: LazyDataStore[Model],
    predicate: Option[LazyDataStore[InverseModel]]) = new ResultQuery(Some(subject), predicate)
}