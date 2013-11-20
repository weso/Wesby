import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import es.weso.wfLodPortal.Configurable
import play.api.GlobalSettings
import play.api.Logger
import play.api.libs.ws.Response
import play.api.libs.ws.WS
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter) with GlobalSettings with Configurable {

  val actualUri = conf.getString("sparql.actualuri")

  val uris = Seq(
    "organization/WF",
    "organization/WebFoundation",
    "organization/WESO",
    "ontology/WESO",
    "ontology/Observation")

  precachedUris

  def precachedUris() = {
    for (uri <- uris) {
      val fullUri = actualUri + uri
      Logger.info("Pre-caching " + fullUri)
      val future: Future[Response] = WS.url(fullUri)
        .withHeaders("accept" -> "text/html")
        .withTimeout(300000)
        .withRequestTimeout(300000)
        .get
      future.onComplete(loaded)
    }
  }

  def loaded(response: Try[Response]) = {
    response match {
      case Success(v) =>
        Logger.info("Uri: '" + v.ahcResponse.getUri + "' Cached: " + v.statusText)
      case Failure(e) => Logger.warn("Uri Fail Caching: " + e.getMessage)
    }
  }
}