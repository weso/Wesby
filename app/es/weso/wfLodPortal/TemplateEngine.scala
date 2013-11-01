package es.weso.wfLodPortal

import play.api._
import play.api.mvc._
import org.apache.commons.configuration.PropertiesConfiguration
import models.ResultQuery

trait TemplateEgine extends Controller with Configurable {
  conf.append(new PropertiesConfiguration("conf/templates.properties"))

  protected val country = conf.getString("country.template")

  protected val RdfType = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  protected val RdfLabel = "http://www.w3.org/2000/01/rdf-schema#label"

  protected val Undefined = "UNDEFINED"

  def renderAsTemplate(resultQuery: ResultQuery) = {
    val typeResult = resultQuery.subject.get(RdfType)
    val labelResult = resultQuery.subject.get(RdfLabel)

    val currentType = if (typeResult.isDefined) {
      val r = typeResult.get
      if (!r.nodes.isEmpty) {
        r.nodes.head.node.rdfNode.asResource.getURI()
      } else Undefined
    } else Undefined
    
    val currentTypeUri : Tuple2[String, String] = if (typeResult.isDefined) {
      val r = typeResult.get    
      if (!r.nodes.isEmpty) {
       (r.nodes.head.node.asInstanceOf[models.RdfResource].uri.relative, 
       		r.nodes.head.node.asInstanceOf[models.RdfResource].label getOrElse "")
      } else ("", "")
    } else ("", "")
    
    val currentLabel = if (labelResult.isDefined) {
      val r = labelResult.get     
      if (!r.nodes.isEmpty) {
        r.nodes.head.node.asInstanceOf[models.RdfLiteral].value
      } else ""
    } else ""
    
    Ok(
      currentType match {
        case e if currentType == country => views.html.country(resultQuery, currentLabel, currentTypeUri)
        case _ => views.html.fallback(resultQuery, currentLabel, currentTypeUri)

      })
  }
}