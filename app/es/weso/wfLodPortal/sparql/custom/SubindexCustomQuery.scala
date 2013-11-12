package es.weso.wfLodPortal.sparql.custom

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.DataStore
import es.weso.wfLodPortal.models.LazyDataStore
import es.weso.wfLodPortal.models.Model
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.sparql.ModelLoader
import es.weso.wfLodPortal.utils.CommonURIS.cex
import es.weso.wfLodPortal.utils.CommonURIS.rdf
import es.weso.wfLodPortal.utils.CommonURIS.wfOnto
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import es.weso.wfLodPortal.models.DataStore
import es.weso.wfLodPortal.models.InverseModel
import es.weso.wfLodPortal.models.Uri
import es.weso.wfLodPortal.models.Uri._
import es.weso.wfLodPortal.utils.UriFormatter

object SubindexCustomQuery extends CustomQuery with Configurable {

  case class Subindex(uri: Uri, label: String, components: List[Component])
  case class Component(uri: Uri, label: String, indicator: List[Indicator])
  case class Indicator(uri: Uri, label: String)

  implicit val indicatorReads = Json.reads[Indicator]
  implicit val indicatorWrites = Json.writes[Indicator]

  implicit val componentReads = Json.reads[Component]
  implicit val componentWrites = Json.writes[Component]

  implicit val subindexReads = Json.reads[Subindex]
  implicit val subindexWrites = Json.writes[Subindex]

  def loadSubindexes(mode: String): List[Subindex] = {
    val param = mode match {
      case "webindex" => "http://data.webfoundation.org/webindex/v2013/"
      case "odb" => "http://data.webfoundation.org/odb/v2013/"
    }

    def inner(r: RdfResource): Option[Subindex] = {
      val uri = r.uri.absolute
      if (uri.contains(param)) {
        val label = loadLabel(r)
        val components = loadComponents(r.dataStores.subject.get)
        Some(Subindex(UriFormatter.format(uri), label, components))
      } else None
    }

    val rs = ModelLoader.loadUri(cex, "SubIndex")

    handleResource[Option[Subindex]](rs.predicate.get, rdf, "type", inner _).flatten
  }

  protected def loadComponents(dataStore: Model): List[Component] = {
    def inner(r: RdfResource): Component = {
      val uri = r.uri.absolute
      val label = loadLabel(r)
      val indicators = loadIndicators(r.dataStores.subject.get)
      Component(UriFormatter.format(uri), label, indicators)
    }
    handleResource[Component](dataStore, cex, "element", inner _)
  }

  protected def loadIndicators(dataStore: Model): List[Indicator] = {

    def inner(r: RdfResource): Indicator = {
      val uri = r.uri.absolute
      val label =  loadLabel(r)
      Indicator(UriFormatter.format(uri), label)
    }
    handleResource[Indicator](dataStore, cex, "element", inner _)
  }

}