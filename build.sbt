name := """Wesby"""

version := "2.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += "Bintray" at "http://dl.bintray.com/weso/weso-releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// Banana RDF
libraryDependencies += "org.w3" %% "banana-rdf" % "0.8.1"

libraryDependencies += "org.w3" %% "banana-jena" % "0.8.1"

libraryDependencies += "org.w3" %% "banana-sesame" % "0.8.1"

libraryDependencies += "org.w3" %% "banana-plantain" % "0.8.1"

libraryDependencies += "org.w3" % "ldp-testsuite" % "0.1.1"

// code coverage
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 80

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false

// ShExcala
libraryDependencies += "es.weso" % "shexcala_2.11" % "0.5.4"
