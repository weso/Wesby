name := "wfLodPortal"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  filters,
  "org.apache.jena" % "jena-arq" % "2.10.1",
  "commons-configuration" % "commons-configuration" % "1.9"
)     

templatesImport ++= Seq(
	"models._",
	"es.weso.wfLodPortal.utils.CommonURIS._",
	"views.html.helpers._",
	"views.html.helpers.utils._"
)

play.Project.playScalaSettings
