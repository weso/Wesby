package es.weso.wfLodPortal

import play.api._
import play.api.mvc._
import org.apache.commons.configuration.PropertiesConfiguration
import models.ResultQuery

trait TemplateEgine extends Controller with Configurable {
  conf.append(new PropertiesConfiguration("conf/templates.properties"))

  protected val country = conf.getString("country.template")

  protected val RdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

  protected val Undefined = "UNDEFINED"

  def renderAsTemplate(resultQuery: ResultQuery) = {
    val result = resultQuery.subject.get(RdfType)

    val currentType = if (result.isDefined) {
      val r = result.get
      if (!r._2.isEmpty) {
        r._2.head._1.rdfNode.asResource.getURI()
      } else Undefined
    } else Undefined
    
    Ok(
      currentType match {
        case e if currentType == country => views.html.country(resultQuery)
        case _ => views.html.fallback(resultQuery)

      })
  }
}