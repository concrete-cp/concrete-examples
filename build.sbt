name := "CSPFJ Examples"

organization := "fr.univ-valenciennes.cspfj"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.9.2"

resolvers += "CSP4J repository" at "http://cspfj.sourceforge.net/repository"

libraryDependencies ++= Seq(
	"commons-cli" % "commons-cli" % "1.1",
	"junit" % "junit" % "4.10" % "test"
	)

