package es.weso.wfLodPortal.utils

import scala.collection.mutable.HashMap
import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.ShortUri
import es.weso.wfLodPortal.models.Uri
import scala.io.Source
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.CodingErrorAction
import play.api.libs.json.Json
import scalax.io.Resource
import scalax.io.Output
import scalax.file.Path
import play.api.Logger
import java.io.File
import sys.process._
import play.api.Play
import java.io.BufferedWriter
import java.io.FileWriter

object UriFormatter extends Configurable {

  val actualUri = conf.getString("sparql.actualuri")
  val baseUri = conf.getString("sparql.baseuri")

  val prefixes = loadPrefixes()

  protected implicit val charsetDecoder = Charset.forName("UTF-8").newDecoder()
  charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE)
  charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE)

  protected def loadPrefixes() = {
    val Matcher = "PREFIX[' '||\t]*(.*):[' '||\t]*<(.*)>[' '||\t]*".r

    Logger.info("Loading prefixes into memory:")
    val prefixes = (for (line <- Source.fromFile("conf/prefixes.ttl").getLines) yield {
      println(line)
      line match {
        case Matcher(prefix, uri) =>
          Some(prefix -> uri)
        case _ =>
          Logger.warn(s"'${line}' could not be processed!")
          None
      }
    }).toList.flatten.toMap

    updateSnorqlNamespaces(prefixes)

    Logger.info("Prefixes loaded into memory")
    prefixes
  }

  protected def updateSnorqlNamespaces(prefixes: Map[String, String]): Unit = {

    Logger.info("Updating namespaces.js (Snorql) file")

    val json = Json.toJson(prefixes).toString

    val out = new BufferedWriter(new FileWriter("public/javascripts/snorql/namespaces.json"))

    try {
      out.write(json)
    } finally {
      out.close
    }

    Logger.info("namespaces.js (Snorql) file updated")
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
    val shortUri = if (prefixes contains prefix) {
      val suffix = uri.substring(index).toString
      val localPrefix = uriToLocalURI(prefix)
      Some(ShortUri((localPrefix, prefixes.get(prefix).get), (localUri, suffix)))
    } else None
    Uri(localUri, uri, shortUri)
  }

}