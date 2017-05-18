
name := "oververse-account"

version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += jdbc
libraryDependencies += cache
libraryDependencies += ws
libraryDependencies += filters
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "2.7.17" % Test
libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.12.1"

coverageExcludedPackages := "filters.*;router.*;controllers.javascript.*;views.*;<empty>;Reverse.*;connectors.*;config.*"
coverageMinimum := 100
coverageFailOnMinimum := true

fork := true
javaOptions += "-Xmx4G"

