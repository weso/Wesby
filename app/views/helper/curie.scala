package views.helper

import models.PrefixMapping

/**
  * Created by jorge on 16/3/16.
  */

case class Curie(namespace: String, prefix: String, reference: String) {
  override def toString = prefix + ":" + reference
}

object Curie {
  def apply(uri: String): Curie = new Curie(getNamespace(uri), getPrefix(uri), getReference(uri))

  def getNamespace(uri: String) = uri.dropRight(getReference(uri).length)
  def getPrefix(uri: String) = PrefixMapping.getNsURIPrefix(getNamespace(uri))
  def getReference(uri: String) = {
    val segments =  if (uri.contains("#")) uri.split("#")
                    else uri.split("/")

    segments(segments.length - 1)
  }
}
