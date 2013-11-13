package es.weso.wfLodPortal

import play.api._
import play.api.mvc._
import org.apache.commons.configuration.PropertiesConfiguration
import models.ResultQuery
import es.weso.wfLodPortal.utils.CommonURIS._
import es.weso.wfLodPortal.sparql._

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
    				"mode" -> mode, "uri" -> uri)

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

}