object regex {
  val Regex = """.*/(.*).scala.html""".r          //> Regex  : scala.util.matching.Regex = .*/(.*).scala.html
  val text = "/Wesby/app/views/lod/fallback.scala.html"
                                                  //> text  : String = /Wesby/app/views/lod/fallback.scala.html
  text match {
    case Regex(name) =>
     "  templates+=\"" + name + "\"->" + name + "\n"
    case _ => {}
  }                                               //> res0: Any = "  templates+="fallback"->fallback
                                                  //| "
}