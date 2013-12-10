package es.weso.wesby.models

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.functional.syntax._

case class Uri(val relative: String, val absolute: String, val short: Option[ShortUri]) {
  import Uri._
}

object Uri {
  import ShortUri._

  implicit val uriReads: Reads[Uri] = (
    (__ \ "relative").read[String] and
    (__ \ "absolute").read[String] and
    (__ \ "short").readNullable[ShortUri](shortUriReads))(Uri.apply _)

  implicit val uriWrites: Writes[Uri] = (
    (__ \ "relative").write[String] and
    (__ \ "absolute").write[String] and
    (__ \ "short").write[Option[ShortUri]])(unlift(Uri.unapply))
}

case class ShortUri(val prefix: (String, String), val suffix: (String, String)) {
  import ShortUri._
}

object ShortUri {

  implicit val tupleReads: Reads[(String, String)] = (
    ((__ \ "_1").read[String] and
      (__ \ "_2").read[String]).tupled: Reads[(String, String)])

  implicit val tupleWrites = (
    (__ \ "_1").write[String] and
      (__ \ "_2").write[String] tupled)

  implicit val shortUriReads: Reads[ShortUri] = Json.reads[ShortUri]
  implicit val shortUriWrites: Writes[ShortUri] = Json.writes[ShortUri]
}