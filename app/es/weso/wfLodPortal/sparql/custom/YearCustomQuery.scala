package es.weso.wfLodPortal.sparql.custom

import scala.Option.option2Iterable
import scala.collection.mutable.ListBuffer

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.Model
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.sparql.Handlers.handleFirstLiteralAsValue
import es.weso.wfLodPortal.sparql.Handlers.handleResourceAs
import es.weso.wfLodPortal.sparql.ModelLoader
import es.weso.wfLodPortal.utils.CommonURIS.cex
import es.weso.wfLodPortal.utils.CommonURIS.rdf
import es.weso.wfLodPortal.utils.CommonURIS.time

object YearsCustomQuery extends CustomQuery with Configurable {

  def loadYears(mode: String): List[Int] = {

    val param = checkMode(mode)

    def inner(r: RdfResource): Option[List[Int]] = {
      val uri = r.uri.absolute
      if (uri.contains(param)) {
        Some(loadIndicatorInterval(r.dss.subject.get))
      } else None
    }

    val rs = ModelLoader.loadUri(cex, "Indicator")
    val foo = handleResourceAs[Option[List[Int]]](rs.predicate.get, rdf, "type", inner _).flatten
    foo.foldLeft(new ListBuffer[Int]())((a, b) => a ++ b).toSet.toList

  }

  protected def loadIndicatorInterval(subject: Model): List[Int] = {
    val starts = handleFirstLiteralAsValue(subject, time, "intervalStarts").toInt
    val intervalFinishes = handleFirstLiteralAsValue(subject, time, "intervalFinishes").toInt
    starts to intervalFinishes toList
  }

}