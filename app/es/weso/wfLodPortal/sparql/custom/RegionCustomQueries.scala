package es.weso.wfLodPortal.sparql.custom

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.LazyDataStore
import es.weso.wfLodPortal.models.Model
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.sparql.ModelLoader
import es.weso.wfLodPortal.utils.CommonURIS.rdf
import es.weso.wfLodPortal.utils.CommonURIS.wfOnto
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import es.weso.wfLodPortal.models.Uri
import es.weso.wfLodPortal.models.Uri._
import es.weso.wfLodPortal.utils.UriFormatter
import es.weso.wfLodPortal.models.ResultQuery

object RegionCustomQueries extends CustomQuery with Configurable {

  case class Region(uri: Uri, label: String, countries: List[Country])
  case class Country(uri: Uri, label: String, code2: String, code3: String)

  val queryCountries = conf.getString("query.subject")

  implicit val countryReads = Json.reads[Country]
  implicit val countryWrites = Json.writes[Country]

  implicit val regionReads = Json.reads[Region]
  implicit val regionWrites = Json.writes[Region]

  def loadRegions(mode: String): List[Region] = {
    val param = mode match {
      case "webindex" => "http://data.webfoundation.org/webindex/v2013/"
      case "odb" => "http://data.webfoundation.org/odb/v2013/"
    }

    def inner(r: RdfResource): Option[Region] = {
      val uri = r.uri.absolute
      if (uri.contains(param)) {
        val label = loadLabel(r)
        val countries = loadCountries(r.dss.subject.get)
        Some(Region(UriFormatter.format(uri), label, countries))
      } else None
    }

    val rs : ResultQuery = ModelLoader.loadUri(wfOnto, "Region")

    handleResource[Option[Region]](rs.predicate.get, rdf, "type", inner _).flatten
  }

  protected def loadCountries(subject: Model): List[Country] = {

    def inner(r: RdfResource): Country = {
      val uri = UriFormatter.format(r.uri.absolute)
      val label = loadLabel(r)
      val dataStore = r.dss.subject.get
      val iso2 = firstNodeAsLiteral(dataStore, wfOnto, "has-iso-alpha2-code")
      val iso3 = firstNodeAsLiteral(dataStore, wfOnto, "has-iso-alpha3-code")
      Country(uri, label, iso2, iso3)
    }

    handleResource[Country](subject, wfOnto, "has-country", inner _)
  }
  
}