package es.weso.wfLodPortal

import scala.collection.mutable.ListBuffer

import org.apache.commons.configuration.PropertiesConfiguration

import es.weso.wfLodPortal.sparql.QueryEngine
import es.weso.wfLodPortal.sparql.custom.IndexValueCustomQuery
import es.weso.wfLodPortal.sparql.custom.IndicatorCustomQuery
import es.weso.wfLodPortal.sparql.custom.IndicatorCustomQuery.Indicator
import es.weso.wfLodPortal.sparql.custom.IndicatorCustomQuery.indicatorWrites
import es.weso.wfLodPortal.sparql.custom.RankingCustomQuery
import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries
import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries.Region
import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries.regionWrites
import es.weso.wfLodPortal.sparql.custom.RootQueries
import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery
import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery.Subindex
import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery.subindexWrites
import es.weso.wfLodPortal.sparql.custom.YearsCustomQuery
import es.weso.wfLodPortal.sparql.custom.ObservationCustomQuery
import es.weso.wfLodPortal.utils.CommonURIS.p
import es.weso.wfLodPortal.utils.CommonURIS.rdf
import es.weso.wfLodPortal.utils.CommonURIS.rdfs
import models.ResultQuery
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.api.mvc.RequestHeader

trait TemplateEgine extends Controller with Configurable {

  conf.append(new PropertiesConfiguration("conf/templates.properties"))

  protected val country = conf.getString("country.template")
  protected val indicator = conf.getString("indicator.template")
  protected val observation = conf.getString("observation.template")
  protected val dataset = conf.getString("dataset.template")
  protected val countryConcept = conf.getString("countryConcept.template")

  protected val currentVersion = conf.getString("application.version")

  protected val RdfType = p(rdf, "type")
  protected val RdfLabel = p(rdfs, "label")

  protected val Undefined = "UNDEFINED"

  def renderAsTemplate(resultQuery: ResultQuery, uri: String, mode: String)(implicit request: RequestHeader) = {
    val typeResult = resultQuery.subject.get.get(RdfType)

    val currentType = if (typeResult.isDefined) {
      val r = typeResult.get
      if (!r.nodes.isEmpty) {
        r.nodes.head.rdfNode.asResource.getURI()
      } else Undefined
    } else Undefined

    val options = scala.collection.mutable.Map[String, Object]("endpoint" -> conf.getString("sparql.endpoint"),
      "query" -> QueryEngine.applyFilters(conf.getString("query.show.fallback"), Seq("<" + uri + ">")),
      "mode" -> mode, "host" -> conf.getString("sparql.actualuri"), "version" -> conf.getString("application.version"))

    currentType match {
      case e if currentType == country => renderCountry(uri, mode, resultQuery, options)
      case e if currentType == indicator => Ok(views.html.indicator(resultQuery, options))
      case e if currentType == observation => renderObservation(uri, mode, resultQuery, options)
      case e if currentType == dataset => Ok(views.html.dataset(resultQuery, options))
      case e if currentType == countryConcept => Ok(views.html.countryConcept(resultQuery, options))
      case _ => Ok(views.html.fallback(resultQuery, options))

    }
  }

  def renderObservation(uri: String, mode: String, resultQuery: ResultQuery, options: scala.collection.mutable.Map[String, Object])(implicit request: RequestHeader) = {
  	val observations = ObservationCustomQuery.loadObservations(uri, mode)
  	
  	options("observation.history") = observations
  	
  	Ok(views.html.observation(resultQuery, options))
  }

  def renderCountry(uri: String, mode: String, resultQuery: ResultQuery,
    options: scala.collection.mutable.Map[String, Object])(implicit request: RequestHeader) = {
    val countries = RankingCustomQuery.loadRanking(mode)

    options("ranking.allCountries") = countries

    val hierarchy = IndexValueCustomQuery.loadHierarchy(uri, mode)

    options("query.hierarchy") = hierarchy

    Ok(views.html.country(resultQuery, options))
  }

  def renderHome()(implicit request: RequestHeader) = {
    val version = this.conf.getString("application.version")
    Ok(views.html.home(version))
  }

  def renderRoot(mode: String, version: String)(implicit request: RequestHeader) = {
    import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries._
    import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery._
    import es.weso.wfLodPortal.sparql.custom.RootQueries._

    val title = if (mode == "odb") "OPEN DATA BAROMETER"; else "WEB INDEX"

    val c = RegionCustomQueries.loadRegions(mode, version)
    val s = SubindexCustomQuery.loadSubindexes(mode, version)
    val queries: ListBuffer[scala.collection.mutable.Map[String, Object]] = RootQueries.loadQueries

    Ok(views.html.root(version, mode, title, request.host, c, s, queries))
  }

  def renderPreCompare(mode: String, selectedCountries: Option[String], selectedIndicators: Option[String], host: String)(implicit request: RequestHeader) = {
    import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries._
    import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery._
    import es.weso.wfLodPortal.sparql.custom.YearsCustomQuery._

    val c = Json.toJson[List[Region]](RegionCustomQueries.loadRegions(mode, currentVersion))
    val y = Json.toJson[List[Int]](YearsCustomQuery.loadYears(mode, currentVersion))
    val s = Json.toJson[List[Subindex]](SubindexCustomQuery.loadSubindexes(mode, currentVersion))

    Ok(views.html.compare(c, y, s, selectedCountries, selectedIndicators, mode, host, currentVersion))
  }

  def renderCompare(mode: String, countries: String, years: String, indicators: String, host: String)(implicit request: RequestHeader) = {
    import es.weso.wfLodPortal.sparql.custom.IndicatorCustomQuery._

    val c = countries.split(",")
    val y = years.split(",")
    val i = indicators.split(",")
    val observations = IndicatorCustomQuery.loadObservations(c, y, i)
    val json = Json.toJson[Map[String, Indicator]](observations)
    
    Ok(views.html.comparison(json, mode, host, currentVersion))
  }

}