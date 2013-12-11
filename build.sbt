name := "wesby"

version := "0.1.0-M1-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  filters,
  "org.apache.jena" % "jena-arq" % "2.11.0",
  "commons-configuration" % "commons-configuration" % "1.9",
  "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2"
)     

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2"

templatesImport += "es.weso.wfLodPortal.models._"

templatesImport += "es.weso.wfLodPortal.utils.CommonURIS._"

templatesImport += "views.html.helpers._"

templatesImport += "views.html.helpers.utils._"

play.Project.playScalaSettings

sourceGenerators in Compile <+= Def.task {
  val finder: PathFinder = (new File("app")/"views"/"lod") ** "*.scala.html"
  var sourceCode = """package es.weso.wesby
import scala.collection.Map
/* This Source file is autogenerated at compile time*/
object TemplateMapping {
  var templates : Map[String, String] = Map.empty
"""
  for( file <- finder.get){
  	sourceCode += "  templates+=\""+file.name+"\"->\""+file.toString+"\"\n"
  }
  sourceCode += "}"
  val file = (sourceManaged in Compile).value / "es" / "weso" / "wesby" / "TemplateMapping.scala"
  IO.write(file, sourceCode)
  Seq(file)
}