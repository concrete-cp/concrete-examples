name := "Concrete-Examples"

organization := "fr.univ-valenciennes.concrete"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.0-RC4"

resolvers += "Concrete repository" at "http://concrete-cp.github.io/concrete/repository"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

scalacOptions += "-Xdisable-assertions"

libraryDependencies ++= Seq(
	"commons-cli" % "commons-cli" % "1.1"
	)

publishTo := Some(
	Resolver.file("Concrete local repository",
		new File(Path.userHome.absolutePath+"/concrete/repository")))

