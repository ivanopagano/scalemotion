name := "scalemotion"

version := "0.0.1"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
	"net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
	"org.scalatest" %% "scalatest" % "1.8" % "test",
	"joda-time" % "joda-time" % "2.1",
	"org.joda" % "joda-convert" % "1.2" % "compile"
)
