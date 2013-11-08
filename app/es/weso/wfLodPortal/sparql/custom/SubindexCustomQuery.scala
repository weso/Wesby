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
import es.weso.wfLodPortal.models.OptionalResultQuery

case class Subindex(uri: String, label: String, components: List[Component])
case class Component(uri: String, label: String, indicator: List[Indicator])
case class Indicator(uri: String, label: String)

object SubindexCustomQuery extends CustomQuery with Configurable {

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

    def inner(r: RdfResource, orq: OptionalResultQuery): Option[Subindex] = {
      val uri = r.uri.absolute
      if (uri.contains(param)) {
        val label = r.label.getOrElse("Undefined Label")
        val components = loadComponents(orq.s.get)
        Some(Subindex(uri, label, components))
      } else None
    }

    val rs = ModelLoader.loadUri(cex, "SubIndex")

    handleResource[Option[Subindex]](rs.predicate, rdf, "type", inner _).flatten
  }

  protected def loadComponents(ls: LazyDataStore[Model]): List[Component] = {
    def inner(r: RdfResource, orq: OptionalResultQuery): Component = {
      val uri = r.uri.absolute
      val label = r.label.getOrElse("Undefined Label")
      val indicators = loadIndicators(orq.s.get)
      Component(uri, label, indicators)
    }
    handleResource[Component](ls.data, cex, "element", inner _)
  }

  protected def loadIndicators(ls: LazyDataStore[Model]): List[Indicator] = {

    def inner(r: RdfResource, orq: OptionalResultQuery): Indicator = {
      val uri = r.uri.absolute
      val label = r.label.getOrElse("Undefined Label")
      Indicator(uri, label)
    }
    handleResource[Indicator](ls.data, cex, "element", inner _)
  }

}