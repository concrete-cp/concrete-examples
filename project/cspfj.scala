import sbt._
import Keys._

object cspfj extends Build {
  
  lazy val root = Project(id = "Examples", base = file(".")) dependsOn (cspfj)

  lazy val cspfj = RootProject(file("../cspfj"))
}