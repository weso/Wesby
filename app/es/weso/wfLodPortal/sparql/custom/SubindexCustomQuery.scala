package es.weso.wfLodPortal.sparql.custom

import scala.Option.option2Iterable
import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.Model
import es.weso.wfLodPortal.models.RdfResource
import es.weso.wfLodPortal.models.ShortUri
import es.weso.wfLodPortal.sparql.Handlers.handleResourceAs
import es.weso.wfLodPortal.sparql.ModelLoader
import es.weso.wfLodPortal.utils.CommonURIS.cex
import es.weso.wfLodPortal.utils.CommonURIS.rdf
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

object SubindexCustomQuery extends CustomQuery with Configurable {

  case class Subindex(uri: Uri, label: String, children: List[Component])
  case class Component(uri: Uri, label: String, children: List[Indicator])
  case class Indicator(uri: Uri, label: String, code: String)

  implicit val indicatorReads = Json.reads[Indicator]
  implicit val indicatorWrites = Json.writes[Indicator]

  implicit val componentReads = Json.reads[Component]
  implicit val componentWrites = Json.writes[Component]

  implicit val subindexReads = Json.reads[Subindex]
  implicit val subindexWrites = Json.writes[Subindex]

  def loadSubindexes(mode: String): List[Subindex] = {
    val param = checkMode(mode)

    def inner(r: RdfResource): Option[Subindex] = {
      val uri = r.uri
      if (uri.absolute.contains(param)) {
        val label = Utils.label(r.dss)
        val components = loadComponents(r.dss.subject.get)
        Some(Subindex(uri, label, components))
      } else None
    }

    val rs = ModelLoader.loadUri(cex, "SubIndex")
    handleResourceAs[Option[Subindex]](rs.predicate.get, rdf, "type", inner _).flatten
  }

  protected def loadComponents(dataStore: Model): List[Component] = {
    def inner(r: RdfResource): Component = {
      val uri = r.uri
      val label = Utils.label(r.dss)
      val indicators = loadIndicators(r.dataStores.subject.get)
      Component(uri, label, indicators)
    }
    handleResourceAs[Component](dataStore, cex, "element", inner _)
  }

  protected def loadIndicators(dataStore: Model): List[Indicator] = {

    def inner(r: RdfResource): Indicator = {
      val uri = r.uri
      val label = Utils.label(r.dss)
      val code = r.uri.short match {
        case Some(s: ShortUri) => s.suffix._2
        case None => label
      }
      Indicator(uri, label, code)
    }
    handleResourceAs[Indicator](dataStore, cex, "element", inner _)
  }

}