package models

import play.api.Logger

import scala.io.Source

/**
 * Created by jorge on 26/10/15.
 */
object PrefixMapping {
  val prefixToUri: Map[String, String] = loadPrefixes()
  val uriToPrefix: Map[String, String] = prefixToUri.map(_.swap)

  def setNsPreffix(prefix: String, uri: String) = ???

  def getNsURIPrefix(uri: String) = uriToPrefix.getOrElse(uri, "unknown")


  private def loadPrefixes() = {
    val Matcher = "@prefix[' '||\t]*(.*):[' '||\t]*<(.*)>[' '||\t]*\\.".r

    Logger.info("Loading prefixes into memory: conf/wesby/prefixes.ttl")
    val prefixes: Map[String, String] = (for (line <- Source.fromFile("conf/prefixes.ttl").getLines) yield {
      line match {
        case Matcher(prefix, uri) =>
          Some(prefix -> uri)
        case _ =>
          Logger.warn(s"'${line}' could not be processed!")
          None
      }
    }).toList.flatten.toMap // TODO quitar el tolist

    prefixes
  }
}