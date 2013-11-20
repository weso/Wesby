package es.weso.wfLodPortal.models

case class ResultQuery(
  sub: Option[LazyDataStore[Model]],
  pred: Option[LazyDataStore[InverseModel]]) {

  lazy val subject = sub match {
    case Some(dt) => Some(dt.data)
    case None => None
  }

  lazy val predicate = pred match {
    case Some(dt) => Some(dt.data)
    case None => None
  }

}

object ResultQuery {
  def apply(subject: LazyDataStore[Model], predicate: LazyDataStore[InverseModel]) = new ResultQuery(Some(subject), Some(predicate))
}