name := "wfLodPortal"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  filters,
  "org.apache.jena" % "jena-arq" % "2.10.1",
  "commons-configuration" % "commons-configuration" % "1.9",
  "com.github.mumoshu" %% "play2-memcached" % "0.3.0.2"
)     

resolvers += "Spy Repository" at "http://files.couchbase.com/maven2"

templatesImport += "es.weso.wfLodPortal.models._"

templatesImport += "es.weso.wfLodPortal.utils.CommonURIS._"

templatesImport += "views.html.helpers._"

templatesImport += "views.html.helpers.utils._"

play.Project.playScalaSettings