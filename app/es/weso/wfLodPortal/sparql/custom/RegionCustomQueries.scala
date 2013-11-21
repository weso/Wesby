package es.weso.wfLodPortal.sparql.custom

import scala.Option.option2Iterable

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.Model
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.sparql.Handlers.handleFirstLiteralAsValue
import es.weso.wfLodPortal.sparql.Handlers.handleResourceAs
import es.weso.wfLodPortal.sparql.ModelLoader
import es.weso.wfLodPortal.utils.CommonURIS.rdf
import es.weso.wfLodPortal.utils.CommonURIS.wfOnto
import es.weso.wfLodPortal.models.Uri
import es.weso.wfLodPortal.models.Uri._
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import views.helpers.Utils

object RegionCustomQueries extends CustomQuery with Configurable {

  case class Region(uri: Uri, label: String, children: List[Country])
  case class Country(uri: Uri, label: String, code2: String, code: String)

  val queryCountries = conf.getString("query.subject")

  implicit val countryReads = Json.reads[Country]
  implicit val countryWrites = Json.writes[Country]

  implicit val regionReads = Json.reads[Region]
  implicit val regionWrites = Json.writes[Region]

  def loadRegions(mode: String, version: String): List[Region] = {
    val param = checkMode(mode, version)
    println(param)

    def inner(r: RdfResource): Option[Region] = {
      val uri = r.uri
      if (uri.absolute.contains(param)) {
        val label = Utils.label(r.dss)
        val countries = loadCountries(r.dss.subject.get)
        Some(Region(uri, label, countries))
      } else None
    }

    val rs = ModelLoader.loadUri(wfOnto, "Region")
    handleResourceAs[Option[Region]](rs.predicate.get, rdf, "type", inner _).flatten
  }

  protected def loadCountries(subject: Model): List[Country] = {

    def inner(r: RdfResource): Country = {
      val uri = r.uri
      val name = Utils.label(r.dss)
      val dataStore = r.dss.subject.get
      val iso2 = handleFirstLiteralAsValue(dataStore, wfOnto, "has-iso-alpha2-code")
      val iso3 = handleFirstLiteralAsValue(dataStore, wfOnto, "has-iso-alpha3-code")
      Country(uri, name, iso2, iso3)
    }

    handleResourceAs[Country](subject, wfOnto, "has-country", inner _)
  }

}