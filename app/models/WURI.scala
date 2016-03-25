package models

import org.w3.banana._

/**
 * Created by jorge on 27/10/15.
 */

case class WURI[Rdf <: RDF](val uri: Rdf#URI) {
  def getNamespace = uri.toString.dropRight(getLocalname.length)
  def getPrefix = PrefixMapping.getNsURIPrefix(getNamespace)
  def getLocalname: String = {
    val segments = if (uri.toString.contains("#")) { // TODO temporal solution, not really a canonical XML localname
      uri.toString.split("#")
    } else {
      uri.toString.split("/")
    }

    segments(segments.length - 1)
  }
}


