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

  protected val RdfType = rdf + "type"
  protected val RdfLabel = rdfs + "label"

  protected val Undefined = "UNDEFINED"

  def renderAsTemplate(resultQuery: ResultQuery, uri: String)(implicit request: RequestHeader) = {

    implicit val options = new Options(uri)

    val currentType = rdfType(resultQuery)
    currentType match {
      case e if currentType == country => Ok(views.html.lod.country(resultQuery))
      case e if currentType == indicator => Ok(views.html.lod.indicator(resultQuery))
      case e if currentType == observation => Ok(views.html.lod.observation(resultQuery))
      case e if currentType == dataset => Ok(views.html.lod.dataset(resultQuery))
      case e if currentType == skosConcept =>
        if (request.path.contains("/Country")) {
          Ok(views.html.lod.countryConcept(resultQuery))
        } else { Ok(views.html.lod.fallback(resultQuery)) }
      case _ => Ok(views.html.lod.fallback(resultQuery))
    }

  }

  protected def rdfType(resultQuery: ResultQuery): String = {
    resultQuery.subject.get.get(RdfType) match {
      case Some(r) => if (!r.nodes.isEmpty) {
        r.nodes.head.rdfNode.asResource.getURI
      } else Undefined
      case None => Undefined
    }
  }

}