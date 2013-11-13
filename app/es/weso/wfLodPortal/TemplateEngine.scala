package es.weso.wfLodPortal

import play.api._
import play.api.mvc._
import org.apache.commons.configuration.PropertiesConfiguration
import models.ResultQuery
import es.weso.wfLodPortal.utils.CommonURIS._
import es.weso.wfLodPortal.sparql._

import play.api.libs.json.Json
import es.weso.wfLodPortal.sparql.custom._

trait TemplateEgine extends Controller with Configurable {
  conf.append(new PropertiesConfiguration("conf/templates.properties"))

  protected val country = conf.getString("country.template")
  protected val indicator = conf.getString("indicator.template")
  protected val observation = conf.getString("observation.template")
  protected val dataset = conf.getString("dataset.template")
  protected val countryConcept = conf.getString("countryConcept.template")

  protected val RdfType = p(rdf, "type")
  protected val RdfLabel = p(rdfs, "label")

  protected val Undefined = "UNDEFINED"

  def renderAsTemplate(resultQuery: ResultQuery, uri: String, mode: String) = {
    val typeResult = resultQuery.subject.get(RdfType)

    val currentType = if (typeResult.isDefined) {
      val r = typeResult.get
      if (!r.nodes.isEmpty) {
        r.nodes.head.node.rdfNode.asResource.getURI()
      } else Undefined
    } else Undefined

    val options = Map("endpoint" -> conf.getString("sparql.endpoint"), 
    				"query" -> QueryEngine.applyFilters(conf.getString("query.show.fallback"), Seq("<" + uri + ">")),
    				"mode" -> mode, "uri" -> uri, "host" -> conf.getString("sparql.actualuri"), "version" -> conf.getString("application.version"))

    Ok(
      currentType match {
        case e if currentType == country => views.html.country(resultQuery, options)
        case e if currentType == indicator => views.html.indicator(resultQuery, options)
        case e if currentType == observation => views.html.observation(resultQuery, options)
        case e if currentType == dataset => views.html.dataset(resultQuery, options)
        case e if currentType == countryConcept => views.html.countryConcept(resultQuery, options)
        case _ => views.html.fallback(resultQuery, options)

      })
  }
  
  def renderHome() = {
  	val version = this.conf.getString("application.version")
  	Ok(views.html.home(version))
  }
  
  def renderPreCompare(mode: String, selectedCountries: Option[String], selectedIndicators: Option[String], host: String) = {
    import es.weso.wfLodPortal.sparql.custom.RegionCustomQueries._
    import es.weso.wfLodPortal.sparql.custom.SubindexCustomQuery._
    import es.weso.wfLodPortal.sparql.custom.YearsCustomQuery._

    val version = this.conf.getString("application.version")

    val c = Json.toJson[List[Region]](RegionCustomQueries.loadRegions(mode))
    val y = Json.toJson[List[Int]](YearsCustomQuery.loadYears(mode))
    val s = Json.toJson[List[Subindex]](SubindexCustomQuery.loadSubindexes(mode))
    Ok(views.html.compare(c, y, s, selectedCountries, selectedIndicators, mode, host, version))
  }

  def renderCompare(mode: String, countries: String, years: String, indicators: String, host: String) = {
    import es.weso.wfLodPortal.sparql.custom.IndicatorCustomQuery._
    
    val version = this.conf.getString("application.version")
    
    val c = countries.split(",")
    val y = years.split(",")
    val i = indicators.split(",")
    val observations = IndicatorCustomQuery.loadObservations(c, y, i)
    val json = Json.toJson[Map[String, Indicator]](observations)
    Ok(views.html.comparison(json, mode, host, version))
  }

}