package views.helpers.wf

import scala.collection._

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models._
import es.weso.wfLodPortal.models._
import es.weso.wfLodPortal.sparql.Handlers._
import es.weso.wfLodPortal.sparql.custom.IndexValueCustomQuery.Index
import es.weso.wfLodPortal.utils.CommonURIS._
import play.api.cache.Cache
import play.api.Play.current
import views.helpers.Utils.label

object Utils extends Configurable {
  
  val cacheExpiration = conf.getInt("sparql.expiration")
  
  def loadObservations(rs: ResultQuery) = {
    import scala.collection.mutable.Map
    val observations: Map[String, Map[String, (String, String)]] = Map.empty

    def inner(r: RdfResource): Unit = {
      val uri = r.uri.relative

      val data = r.dss.subject.get

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

  def compareUri(options: mutable.Map[String, Object], iso3: String) = {
    "/" + options("mode").toString + "/compare?selectedCountries=" + iso3
  }

  def loadCountryRanking(options: scala.collection.mutable.Map[String, Object], iso3: String): scala.collection.immutable.Map[String, Object] = {
    val ranking = options("ranking.allCountries")
    ranking.asInstanceOf[scala.collection.immutable.Map[String, Object]]
  }

  def loadHierarchyValues(options: scala.collection.mutable.Map[String, Object]): Index = {
    val ranking = options("query.hierarchy")
    ranking.asInstanceOf[Index]
  }
}