package views.helpers.wf

import es.weso.wfLodPortal.models._
import views.helpers.Utils._
import views.helpers.Handlers._
import es.weso.wfLodPortal.utils.CommonURIS._
import es.weso.wfLodPortal.models._
import views.html.helpers._
import views.html.helpers.utils._
import es.weso.wfLodPortal.sparql.custom.IndexValueCustomQuery.Index

object Utils {

  def loadObservations(rs: ResultQuery) = {
    import scala.collection.mutable.Map
    val observations: Map[String, Map[String, (String, String)]] = Map.empty

    def inner(r: RdfResource): Unit = {
      val uri = r.uri.relative

      val data = r.dss.subject.get
      val indicator = data.get(cex, "indicator").getOrElse("")

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
      val indicator = data.get(cex, "indicator").getOrElse("")

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

  def compareUri(options: scala.collection.mutable.Map[String,Object], iso3: String) = {
    "/" + options("mode") + "/compare?selectedCountries=" + iso3
  }
  
  def loadCountryRanking(options: scala.collection.mutable.Map[String,Object], iso3: String): scala.collection.immutable.Map[String, Object] = {
  	val ranking = options("ranking.allCountries")
  	ranking.asInstanceOf[scala.collection.immutable.Map[String, Object]]
  }
  
  def loadHierarchyValues(options: scala.collection.mutable.Map[String,Object]): Index = {
  	val ranking = options("query.hierarchy")
  	ranking.asInstanceOf[Index]
  }
}