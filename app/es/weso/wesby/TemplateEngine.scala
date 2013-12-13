package es.weso.wesby

import org.apache.commons.configuration.PropertiesConfiguration

import es.weso.wesby.models.Options
import es.weso.wesby.utils.CommonURIS.rdf
import es.weso.wesby.utils.CommonURIS.rdfs
import models.ResultQuery
import play.api.mvc.Controller
import play.api.mvc.RequestHeader

trait TemplateEgine extends Controller with Configurable {

  conf.append(new PropertiesConfiguration("conf/wesby/templates.properties"))

  protected val currentVersion = conf.getString("application.version")

  protected val RdfType = rdf + "type"

  protected val Undefined = "UNDEFINED"

  def renderAsTemplate(resultQuery: ResultQuery, uri: String)(implicit request: RequestHeader) = {
    implicit val options = new Options(uri)
    val currentType = rdfType(resultQuery)
    val templateName = conf.getString(currentType, Undefined)
    val template = TemplateMapping.templates.getOrElse(templateName, views.html.lod.fallback)
    Ok(template.render(resultQuery, request, options))
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