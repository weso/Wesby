name := "wfLodPortal"

version := "0.1-M1-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  filters,
  "org.apache.jena" % "jena-arq" % "2.10.1",
  "commons-configuration" % "commons-configuration" % "1.9"
)     

play.Project.playScalaSettings
