package es.weso.wesby.models

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.__
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.functional.syntax._

/**
 * Wesby's representation of a Uri.
 * @param relative the relative URI to the current machine
 * @param absolute the absolute URI to the triple store
 * @param short the shortened version of the URI
 */
case class Uri(val relative: String, val absolute: String, val short: Option[ShortUri]) {
  import Uri._
}

/**
 * Uri's companion object.
 */
object Uri {
  import ShortUri._

  /**
   * Implicit converter from Json to Uri
   */
  implicit val uriReads: Reads[Uri] = (
    (__ \ "relative").read[String] and
    (__ \ "absolute").read[String] and
    (__ \ "short").readNullable[ShortUri](shortUriReads))(Uri.apply _)

  /**
   * Implicit combinator from Uri to Json
   */
  implicit val uriWrites: Writes[Uri] = (
    (__ \ "relative").write[String] and
    (__ \ "absolute").write[String] and
    (__ \ "short").write[Option[ShortUri]])(unlift(Uri.unapply))
}

/**
 * Shortened URI comprised by a prefix and a suffix.
 * @param prefix the tuple comprised by the label and the URI
 * @param suffix the tuple comprised by the label and the URI
 */
case class ShortUri(val prefix: (String, String), val suffix: (String, String)) {
  import ShortUri._
}

/**
 * ShortUri's Companion Object
 */
object ShortUri {

  /**
   * Implicit combinator from Json to Tuple
   */
  implicit val tupleReads: Reads[(String, String)] = (
    ((__ \ "_1").read[String] and
      (__ \ "_2").read[String]).tupled: Reads[(String, String)])

  /**
   * Implicit combinator from Tuple to Json
   */
  implicit val tupleWrites = (
    (__ \ "_1").write[String] and
      (__ \ "_2").write[String] tupled)

  /**
   * Implicit combinator from Json to ShortUri
   */
  implicit val shortUriReads: Reads[ShortUri] = Json.reads[ShortUri]

  /**
   * Implicit combinator from ShortUri to Json
   */
  implicit val shortUriWrites: Writes[ShortUri] = Json.writes[ShortUri]
}