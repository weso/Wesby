package es.weso.wfLodPortal.utils

import scala.collection.mutable.HashMap

import es.weso.wfLodPortal.Configurable
import es.weso.wfLodPortal.models.ShortUri
import es.weso.wfLodPortal.models.Uri

object UriFormatter extends Configurable {

  val actualUri = conf.getString("sparql.actualuri")
  val baseUri = conf.getString("sparql.baseuri")

  val prefixes = HashMap[String, String]();

  val it = conf.getKeys

  while (it.hasNext) {
    val label = it.next()
    val prefix = conf.getString(label).replace("<HOST>", baseUri)

    prefixes(prefix) = label
  }

  def uRIToLocalURI(uri: String) = {
    uri.replace(baseUri, actualUri)
  }
  
  def uRIToBaseURI(uri:String) = {
    uri.replace(actualUri, baseUri)
  }

  def format(uri: String): Uri = {
    val index = math.max(uri.lastIndexOf("#"), uri.lastIndexOf("/")) + 1
    val prefix = uri.subSequence(0, index).toString
    val localUri=  uRIToLocalURI(uri)
    val shortUri = if (prefixes contains prefix) {
      val suffix = uri.substring(index).toString
      val localPrefix =  uRIToLocalURI(prefix)  
      Some(ShortUri((localPrefix, prefixes.get(prefix).get), (localUri, suffix)))
    } else None
    Uri(localUri, uri, shortUri)
  }

}