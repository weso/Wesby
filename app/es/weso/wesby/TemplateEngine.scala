package es.weso.wesby

import org.apache.commons.configuration.PropertiesConfiguration
import es.weso.wesby.models.Options
import es.weso.wesby.utils.CommonURIS.rdf
import models.ResultQuery
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import views.html

/**
 * Adds the built-in rdf:type based template engine.
 */
trait TemplateEngine extends Controller with Configurable {

  conf.append(new PropertiesConfiguration("conf/wesby/templates.properties"))

  protected val RdfType = rdf + "type"

  protected val Undefined = "UNDEFINED"

  /**
   * Renders a template based on its rdf:type
   * @param resultQuery the target ResultQuery
   * @param uri the target URI
   * @param request the RequestHeader
   */
  def renderAsTemplate(resultQuery: ResultQuery, uri: String)(implicit request: RequestHeader) = {
    implicit val options = new Options(uri)
    val currentType = rdfType(resultQuery)
    Ok(html.lod.mustache(request, options, uri))
  }

  /**
   * Returns the rdf:type for a given ResultQuery
   * @param resultQuery the target resultQuery
   */
  protected def rdfType(resultQuery: ResultQuery): String = {
    resultQuery.subject.get.get(RdfType) match {
      case Some(r) => if (!r.nodes.isEmpty) {
        r.nodes.head.rdfNode.asResource.getURI
      } else Undefined
      case None => Undefined
    }
  }

}