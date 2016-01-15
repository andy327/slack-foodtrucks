name := """slack-foodtrucks"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.4.2" % "compile"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )
