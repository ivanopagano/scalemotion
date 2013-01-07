name := "scalemotion"

version := "0.2"

scalaVersion := "2.9.2"

libraryDependencies ++= Seq(
	"net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
	"org.scalatest" %% "scalatest" % "1.8" % "test",
	"joda-time" % "joda-time" % "2.1",
	"org.joda" % "joda-convert" % "1.2" % "compile",
  "org.slf4j" % "slf4j-jdk14" % "1.7.2"
)

//The following is specific to setup JAVAFX use with SBT

fork in run := true

//---
// Note: Wouldn't 'sbt' be able to provide us a nice default for this (the following logic
//      would deserve to be automatic, not in a project build script). AK 4-Jan-2013
//
javaHome := {
  var s = System.getenv("JAVA_HOME")
  if (s==null) {
    // OS X library location
    s= "/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home"
  }
  //
  val dir = new File(s)
  if (!dir.exists) {
    throw new RuntimeException( "No JDK found - try setting 'JAVA_HOME'." )
  }
  //
  Some(dir)  // 'sbt' 'javaHome' value is ': Option[java.io.File]'
}

//---
// JavaFX
//
// Note: We shouldn't even need to say this at all. Part of Java 7 RT (since 7u06) and should come from there (right)
//      The downside is that now this also gets into the 'one-jar' .jar package (where it would not need to be,
//      and takes 15MB of space - of the 17MB package!) AKa 1-Nov-2012
//
unmanagedJars in Compile <+= javaHome map { jh /*: Option[File]*/ =>
  val dir: File = jh.getOrElse(null)    // unSome
  //
  val jfxJar = new File(dir, "/jre/lib/jfxrt.jar")
  if (!jfxJar.exists) {
    throw new RuntimeException( "JavaFX not detected (needs Java runtime 7u06 or later): "+ jfxJar.getPath )  // '.getPath' = full filename
  }
  Attributed.blank(jfxJar)
} 