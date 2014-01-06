package app

import es.weso.wesby.models.ResultQuery
import es.weso.wesby.Configurable
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import es.weso.wesby.models.Options

/**
 * TemplateEngineHelper avoids the use of the built-in template engine. 
 * It selects the template to be shown in a programmatically way (overriding 
 * the default system).
 * 
 * In order to override the template, just add "OVERRIDE.TEMPLATE" as the
 * template to a given URI.
 * 
 */
object TemplateEngineHelper extends Controller with Configurable {

  /**
   * Renders a template based on its rdf:type overriding the built-in template
   * routing system.
   * @param resultQuery the target ResultQuery
   * @param uri the target URI
   * @param currentType
   * @param request the RequestHeader
   * @param options
   */
  def renderAsTemplate(resultQuery: ResultQuery, uri: String,
    currentType: String)(implicit request: RequestHeader, options: Options) = currentType match {
    //Add here your custom handlers
    case _ => Ok(views.html.lod.fallback(resultQuery))

  }
}