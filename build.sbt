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
import es.weso.wesby.models.ResultQuery
import es.weso.wesby.models.Options
import play.api.mvc.RequestHeader
import play.templates.BaseScalaTemplate
import play.templates.Format
import play.api.templates.HtmlFormat.Appendable
import play.api.templates.Template3
/* This Source file is auto-generated at compile time*/
object TemplateMapping {
  type LodTemplate = BaseScalaTemplate[Appendable, Format[Appendable]] with Template3[ResultQuery,RequestHeader,Options,Appendable]
  var templates : Map[String, LodTemplate] = Map.empty 
"""
  /*I could not employ regular expressions due to an SBT-13.0.0's internal bug*/
  for( file <- finder.get){
    val chunks = file.name.split('.')
    if(!chunks.isEmpty){
       sourceCode += "  templates+=\""+chunks(0)+"\"->views.html.lod."+chunks(0)+"\n"
    }
  }
  sourceCode += "}"
  val file = (sourceManaged in Compile).value / "es" / "weso" / "wesby" / "TemplateMapping.scala"
  IO.write(file, sourceCode)
  Seq(file)
}