package controllers
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import app.models.JsonBuilder
import com.hp.hpl.jena.rdf.model.{ Model => JenaModel }
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.weso.wesby.TemplateEngine
import es.weso.wesby.sparql.ModelLoader
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.Accepting
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import play.api.libs.ws.WS
import play.api.libs.json._
import es.weso.wesby.models._
import views.helpers.Utils._

import scala.collection.mutable.ListBuffer

/**
 * Wesby's Controllers which Handles the different Web Services.
 */
object Application extends Controller with TemplateEngine {

  val Html = Accepting("text/html")
  val PlainText = Accepting("text/plain")
  val N3 = Accepting("text/n3")
  val RdfN3 = Accepting("text/rdf+n3")
  val Turtle = Accepting("text/turtle")
  val XTurtle = Accepting("application/x-turtle")
  val Xml = Accepting("application/xml")
  val RdfXML = Accepting("application/rdf+xml")
  val RdfJSON = Accepting("application/rdf+json")

  val charsetDecoder = Charset.forName("UTF-8").newDecoder();
  charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
  charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  val endpointPath = conf.getString("sparql.endpoint")

  /**
   * Redirects to the default page.
   */
  def index() = Action {
    implicit request =>
      val default = Options.host_r + conf.getString("sparql.index")
      Redirect(default)
  }

  /**
   * Renders the built-in Snorql
   */
  def snorql() = Action {
    implicit request => Ok(views.html.snorql())
  }

  /**
   * Proxies the endpoint in order to be able to query it avoiding
   * cross domain limitations. 
   */
  def endpoint() = Action.async {
    implicit request =>
      val params = request.body.asFormUrlEncoded.get
      var nMap = Map.empty[String,Seq[String]]
      nMap += "output" -> Seq("json")
      nMap += "query" -> Seq(params("query").head.replace("[object Object]", ""))
      WS.url(endpointPath).post(nMap).map { response =>
        Ok(response.body).as("application/json; charset=UTF8")
      }
  }

  /**
   * Performs a redirect to the supplied URI
   * @param to the URI to be redirected
   */
  def redirect(to: String) = Action {
    implicit request => Redirect(to)
  }

  /**
   * Intercepts the partial URI and renders the result
   * @param uri the supplied partial URI
   */
  def fallback(uri: String) = Action {
    implicit request =>
      val resultQuery = ModelLoader.loadUri(uri)
      val subjectModel = resultQuery.subject.get.jenaModel
      val predicateModel = resultQuery.predicate.get.jenaModel
      val models = List(subjectModel, predicateModel)

      request.getQueryString("format") match {
        case Some(format) => downloadAs(uri, format, models)
        case None =>
          render {
            case Html() => renderAsTemplate(resultQuery, uri)
            case N3() =>
              Redirect(request.path + "?format=n3")
            case RdfN3() =>
              Redirect(request.path + "?format=n3")
            case Turtle() =>
              Redirect(request.path + "?format=turtle")
            case XTurtle() =>
              Redirect(request.path + "?format=turtle")
            case RdfXML() =>
              Redirect(request.path + "?format=rdfxml")
            case Xml() =>
              Redirect(request.path + "?format=rdfxml")
            case RdfJSON() =>
              Redirect(request.path + "?format=rdfjson")
            case PlainText() =>
              Redirect(request.path + "?format=n-triples")
          }
      }
  }

  def templateJsonData(uri: String) = Action {
    implicit request =>
      val resultQuery = ModelLoader.loadUri(uri)
      val json: JsValue = JsonBuilder.toJson(resultQuery)

      Ok(json)
  }

  /**
   * Download the resource in a given format
   * @param uri the supplied URI
   * @param format the format to be downloaded
   * @param models the models to be merged
   * @param request the implicit request
   */
  protected def downloadAs(uri: String, format: String, models: Seq[JenaModel])(implicit request: RequestHeader) = {
    format match {
      case "n3" =>
        renderModelsAs(models, ("N3", "utf-8", N3.mimeType))
      case "turtle" =>
        renderModelsAs(models, ("TURTLE", "utf-8", Turtle.mimeType))
      case "rdfxml" =>
        renderModelsAs(models, ("RDF/XML", "utf-8", RdfXML.mimeType))
      case "rdfjson" =>
        renderModelsAs(models, ("RDF/JSON", "utf-8", RdfJSON.mimeType))
      case "n-triples" =>
        renderModelsAs(models, ("N-Triples", "utf-8", PlainText.mimeType))
      case _ => BadRequest
    }
  }

  /**
   * Merges the models and renders it in the supplied contentType.
   * @param models the models to be rendered
   * @param contentType the target content type
   * @param request the implicit request
   */
  protected def renderModelsAs(models: Seq[JenaModel], contentType: (String, String, String))(implicit request: RequestHeader) = {
    val out = new ByteArrayOutputStream
    val mergedModel: JenaModel = ModelFactory.createDefaultModel

    for (model <- models) {
      mergedModel.add(model)
    }
    mergedModel.write(out, contentType._1)

    Ok(out.toString).as {
      (new StringBuilder(contentType._3)).append(" ; charset=")
        .append(contentType._2).toString
    }
  }
}