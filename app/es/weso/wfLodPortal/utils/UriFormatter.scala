package es.weso.wfLodPortal.utils

import java.io.BufferedWriter
import java.io.FileWriter
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

import scala.Option.option2Iterable
import scala.io.Source

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.ShortUri
import es.weso.wfLodPortal.models.Uri
import play.api.Logger
import play.api.libs.json.Json

object UriFormatter extends Configurable {

  val actualUri = conf.getString("sparql.actualuri")
  val baseUri = conf.getString("sparql.baseuri")

  // namespace -> uri
  val namespaces = loadNamespaces()

  // uri -> namespace
  val inverseNamespaces = namespaces.map(a => a._2 -> a._1)

  protected implicit val charsetDecoder = Charset.forName("UTF-8").newDecoder()
  charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE)
  charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE)

  protected def loadNamespaces() = {
    val Matcher = "PREFIX[' '||\t]*(.*):[' '||\t]*<(.*)>[' '||\t]*".r

    Logger.info("Loading prefixes into memory: conf/prefixes.ttl")
    val prefixes: Map[String, String] = (for (line <- Source.fromFile("conf/prefixes.ttl").getLines) yield {
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

  protected def updatePubbyNamespaces(prefixes: Map[String, String]): Unit = {

    Logger.info("Updating namespaces.js (Snorql) file")

    val json = Json.toJson(prefixes).toString

    val out = new BufferedWriter(new FileWriter("public/javascripts/snorql/namespaces.json"))

    try {
      out.write(json)
    } finally {
      out.close
    }

    Logger.info("Done updating.")
  }

  def fullUri(uri: String) = {
    baseUri + uri
  }

  def uriToLocalURI(uri: String) = {
    uri.replace(baseUri, actualUri)
  }

  def uriToBaseURI(uri: String) = {
    uri.replace(actualUri, baseUri)
  }

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

  def format(base: String, uri: String): Uri = format(base + uri)

}