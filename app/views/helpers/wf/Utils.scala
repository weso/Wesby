package views.helpers.wf

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.RdfProperty
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.models.ResultQuery
import es.weso.wfLodPortal.sparql.Handlers.handleLiteralAsValue
import es.weso.wfLodPortal.sparql.Handlers.handleResourceAs
import es.weso.wfLodPortal.sparql.Handlers.handleResourceAsString
import es.weso.wfLodPortal.utils.CommonURIS.cex
import es.weso.wfLodPortal.utils.CommonURIS.wfOnto
import play.api.Play.current
import play.api.cache.Cache
import views.helpers.Utils.label

object Utils extends Configurable {
  val cacheExpiration = conf.getInt("sparql.expiration")

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

  def cachedLabel(r: RdfProperty): String = {
    val key = r.uri.absolute.hashCode.toString
    Cache.getOrElse(key, cacheExpiration)(label(r.dss))
  }

  def cachedLabel(r: RdfResource): String = {
    val key = r.uri.absolute.hashCode.toString
    Cache.getOrElse(key, cacheExpiration)(label(r.dss))
  }

  def cachedLabel(rs: ResultQuery): String = {
    val key = rs.pred.get.uri.absolute.hashCode.toString
    Cache.getOrElse(key, cacheExpiration)(label(rs))
  }

  def compareUri(options: Map[String, String], iso3: String) = {
    "/" + options("mode") + "/compare?selectedCountries=" + iso3
  }
}