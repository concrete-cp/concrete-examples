import com.typesafe.sbt.SbtStartScript

name := "Concrete-Examples"

organization := "fr.univ-valenciennes.concrete"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.10.3"

resolvers += "Concrete repository" at "http://scand1sk.github.io/concrete/repository"

scalacOptions += "-Xdisable-assertions"

libraryDependencies ++= Seq(
	"commons-cli" % "commons-cli" % "1.1",
	"junit" % "junit" % "4.10" % "test"
	)

seq(SbtStartScript.startScriptForClassesSettings: _*)

publishTo := Some(
	Resolver.file("Concrete local repository",
		new File(Path.userHome.absolutePath+"/concrete/repository")))
