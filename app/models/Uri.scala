package models

case class Uri(val relative: String, val absolute: String, val short: Option[ShortUri])
case class ShortUri(val prefix: (String, String), val suffix: (String, String))