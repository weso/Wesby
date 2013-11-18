import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import play.api.GlobalSettings
import play.api.libs.ws.WS
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter
import es.weso.wfLodPortal.Configurable
import play.api.Logger
import org.apache.commons.configuration.PropertiesConfiguration

object Global extends WithFilters(new GzipFilter) with GlobalSettings with Configurable {

  val baseUri = conf.getString("sparql.baseuri")

  println("BaseUri: " + baseUri)

  val uris = Seq(
    "organization/WF",
    "organization/WebFoundation",
    "organization/WESO",
    "ontology/WESO",
    "ontology/Observation")

  precachedUris

  def precachedUris() = {
    for (uri <- uris) {
      val fullUri = baseUri + uri
      Logger.info("Pre-caching " + fullUri)
      Await.result(WS.url(fullUri)
        .withHeaders("accept" -> "text/html")
        .withRequestTimeout(300000)
        .get, 5 minutes)
    }
  }
}