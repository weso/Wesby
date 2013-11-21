package es.weso.wfLodPortal

import scala.collection.mutable.ListBuffer

import org.apache.commons.configuration.PropertiesConfiguration
import es.weso.wfLodPortal.models.Options
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
  protected val skosConcept = conf.getString("skosConcept.template")

  protected val currentVersion = conf.getString("application.version")

  protected val RdfType = p(rdf, "type")
  protected val RdfLabel = p(rdfs, "label")

  protected val Undefined = "UNDEFINED"

  def renderAsTemplate(resultQuery: ResultQuery, uri: String)(implicit request: RequestHeader) = {

    implicit val options = new Options(uri)

    val currentType = rdfType(resultQuery)
    currentType match {
      case e if currentType == country => Ok(views.html.lod.country(resultQuery))
      case e if currentType == indicator => Ok(views.html.lod.indicator(resultQuery))
      case e if currentType == observation => Ok(views.html.lod.observation(resultQuery))
      case e if currentType == dataset => Ok(views.html.lod.dataset(resultQuery))
      case e if currentType == skosConcept => {
        if(request.path.contains("/Country"))
        	Ok(views.html.lod.countryConcept(resultQuery))
    	else
    	    Ok(views.html.lod.fallback(resultQuery))        	  
    	}
      case _ => Ok(views.html.lod.fallback(resultQuery))
    }
  }

  def renderRootWI(mode: String, version: String)(implicit request: RequestHeader) = {
    import es.weso.wfLodPortal.sparql.custom._
    import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries._
    import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery._
    import es.weso.wfLodPortal.sparql.custom.RootQueries._

    implicit val regions = loadRegions(mode, version)
    implicit val subindexes = loadSubindexes(mode, version)
    implicit val queries = RootQueries.loadQueries

    Ok(views.html.custom.WIroot(version, mode))
  }
  
  def renderRootODB(mode: String, version: String)(implicit request: RequestHeader) = {
    import es.weso.wfLodPortal.sparql.custom._
    import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries._
    import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery._
    import es.weso.wfLodPortal.sparql.custom.RootQueries._

    implicit val regions = loadRegions(mode, version)
    implicit val subindexes = loadSubindexes(mode, version)
    implicit val queries = RootQueries.loadQueries

    Ok(views.html.custom.ODBroot(version, mode))
  }

  def renderPreCompare(mode: String, selectedCountries: Option[String], selectedIndicators: Option[String])(implicit request: RequestHeader) = {
    import es.weso.wfLodPortal.sparql.custom._
    import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries._
    import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery._
    import es.weso.wfLodPortal.sparql.custom.YearsCustomQuery._
    
    val regions = Json.toJson[List[Region]](loadRegions(mode, currentVersion))
    val years = Json.toJson[List[Int]](loadYears(mode, currentVersion))
    val subindexes = Json.toJson[List[Subindex]](loadSubindexes(mode, currentVersion))
    
    Ok(views.html.custom.compare(currentVersion, mode)(request, regions, years, subindexes, selectedCountries, selectedIndicators))
  }

  def renderCompare(mode: String, countries: String, years: String, indicators: String)(implicit request: RequestHeader) = {
    import es.weso.wfLodPortal.sparql.custom.IndicatorCustomQuery._

    val c = countries.split(",")
    val y = years.split(",")
    val i = indicators.split(",")
    val observations = IndicatorCustomQuery.loadObservations(c, y, i)
    implicit val json = Json.toJson[Map[String, Indicator]](observations)

    Ok(views.html.custom.comparison(currentVersion, mode)(request, json))
  }

  protected def rdfType(resultQuery: ResultQuery): String = {
    val typeResult = resultQuery.subject.get.get(RdfType)

    val currentType = if (typeResult.isDefined) {
      val r = typeResult.get
      if (!r.nodes.isEmpty) {
        r.nodes.head.rdfNode.asResource.getURI
      } else Undefined
    } else Undefined
    currentType
  }

}