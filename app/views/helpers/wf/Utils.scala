package views.helpers.wf

import es.weso.wfLodPortal.models._
import views.helpers.Utils._
import views.helpers.Handlers._
import es.weso.wfLodPortal.utils.CommonURIS._
import es.weso.wfLodPortal.models._
import views.html.helpers._
import views.html.helpers.utils._
object Utils {

  def loadObservations(rs: ResultQuery) = {
    import scala.collection.mutable.Map
    val observations: Map[String, Map[String, (String, String)]] = Map.empty

    def inner(r: RdfResource): Unit = {
      val uri = r.uri.relative

      val data = r.dss.subject.get
      val indicator = data.get(cex, "indicator").get

      val name = handleResourceAsString(data, cex,
        "indicator", (r: RdfResource) => r.uri.short match {
          case Some(s) => s.suffix._2
          case None => ""
        })

      val year = handleLiteralAsValue(data, wfOnto, "ref-year")
      val value = handleLiteralAsValue(data, cex, "value")

      if (!name.isEmpty) {
        val map = observations.getOrElse(name, Map.empty)

        map(year) = (uri, value)

        observations += name -> map
      }
    }

    handleResourceAs[Unit](rs.predicate.get, wfOnto, "ref-area", inner)

    observations.toSeq.sortBy(_._1)
  }
  def loadIndicators(rs: ResultQuery): Seq[(String, String)] = {
    import scala.collection.mutable.Map
    def inner(r: RdfResource) = {
      val data = r.dss.subject.get
      val indicator = data.get(cex, "indicator").get

      val name = handleResourceAsString(data,
        cex, "indicator",
        (r: RdfResource) => r.uri.short match {
          case Some(s) => s.suffix._2
          case None => ""
        })

      val indicatorLabel = handleResourceAsString(data,
        cex, "indicator",
        (r: RdfResource) => label(r.dataStores))
      name -> indicatorLabel
    }

    handleResourceAs[(String, String)](rs.predicate.get,
      wfOnto, "ref-area", inner).toMap.toSeq.sortBy(_._1)

  }

  def compareUri(options: Map[String, String], iso3: String) = {
    "/" + options("mode") + "/compare?selectedCountries=" + iso3
  }
}