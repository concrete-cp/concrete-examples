import sbt._
import Keys._

object Concrete extends Build {
  
  lazy val root = Project(id = "Concrete-Examples", base = file(".")) dependsOn (concrete)

  lazy val concrete = RootProject(file("../concrete"))
}