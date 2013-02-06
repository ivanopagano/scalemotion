name := "scalemotion"

version := "1.1.1"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
	"net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
	"org.scalatest" %% "scalatest" % "1.8" % "test",
	"joda-time" % "joda-time" % "2.1",
	"org.joda" % "joda-convert" % "1.2" % "compile",
  "org.slf4j" % "slf4j-jdk14" % "1.7.2",
  "org.ocpsoft.prettytime" % "prettytime" % "2.1.2.Final"
)

//"org.scalafx" %% "scalafx-core" % "1.0-SNAPSHOT"

//The following is specific to setup JAVAFX use with SBT

fork in run := true

//gets the javafx runtime as a local dependency
unmanagedJars in Compile += Attributed.blank(file(scala.util.Properties.javaHome) / "lib" / "jfxrt.jar")

//settings for the one-jar plugin to generate single bundle
mainClass in oneJar := Some("it.pagoda5b.scalemotion.ui.GraphsApp")

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)