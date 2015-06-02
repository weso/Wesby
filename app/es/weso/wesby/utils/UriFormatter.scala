package es.weso.wesby.utils

import java.io.{BufferedWriter, FileWriter}
import java.nio.charset.{Charset, CodingErrorAction}

import es.weso.wesby.Configurable
import es.weso.wesby.models.{ShortUri, Uri}
import play.api.Logger
import play.api.libs.json.Json

import scala.Option.option2Iterable
import scala.io.Source

/**
 * Handles the URIs performing transformation between local and base uri, and
 * generating Uri objects.
 */
object UriFormatter extends Configurable {

  val actualUri = conf.getString("sparql.actualuri")
  val baseUri = conf.getString("sparql.baseuri")

  // namespace -> uri
  val namespaces = loadNamespaces()

  // uri -> namespace
  val inverseNamespaces = namespaces.map(n => n._2 -> n._1)

  protected implicit val charsetDecoder = Charset.forName("UTF-8").newDecoder()
  charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE)
  charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE)

  /**
   * Reads the namespaces from conf/wesby/prefixes.ttl
   */
  protected def loadNamespaces() = {
    val Matcher = "PREFIX[' '||\t]*(.*):[' '||\t]*<(.*)>[' '||\t]*".r

    Logger.info("Loading prefixes into memory: conf/wesby/prefixes.ttl")
    val prefixes: Map[String, String] = (for (line <- Source.fromFile("conf/wesby/prefixes.ttl").getLines) yield {
      line match {
        case Matcher(prefix, uri) =>
          Some(prefix -> uri)
        case _ =>
          Logger.warn(s"'${line}' could not be processed!")
          None
      }
    }).toList.flatten.toMap

    updatePubbyNamespaces(prefixes)

    Logger.info("Done loading.")

    prefixes
  }

  /**
   * Updates the Snorql's namespaces.js file
   */
  protected def updatePubbyNamespaces(prefixes: Map[String, String]): Unit = {

    Logger.info("Updating namespaces.js (Snorql) file")

    val json = Json.toJson(prefixes).toString

    val out = new BufferedWriter(new FileWriter("public/javascripts/snorql/namespaces.js"))
    
    try {
      out.write("namespaces=")
      out.write(json)
      out.write(";")
    } finally {
      out.close
    }

    Logger.info("Done updating.")
  }

  /**
   * Adds the base URI to the partial URI
   */
  def fullUri(uri: String) = {
    baseUri + uri
  }

  /**
   * Localizes the URI
   */
  def uriToLocalURI(uri: String) = {
    uri.replace(baseUri, actualUri)
  }

  /**
   * Transforms the URI into an absolute URI
   */
  def uriToBaseURI(uri: String) = {
    uri.replace(actualUri, baseUri)
  }

  /**
   * Generates an Uri object from a URI
   */
  def format(uri: String): Uri = {
    val index = math.max(uri.lastIndexOf("#"), uri.lastIndexOf("/")) + 1
    val prefix = uri.subSequence(0, index).toString
    val localUri = uriToLocalURI(uri)

    val shortUri = if (inverseNamespaces contains prefix) {
      val suffix = uri.substring(index).toString
      val localPrefix = uriToLocalURI(prefix)
      Some(ShortUri((localPrefix, inverseNamespaces.get(prefix).get), (localUri, suffix)))
    } else None

    Uri(localUri, uri, shortUri)
  }

  /**
   * Generates an Uri object from a URI split by base and Uri
   * @param base the base URI
   * @param uri the suffix URI
   */
  def format(base: String, uri: String): Uri = format(base + uri)

}