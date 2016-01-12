package tsp

import concrete.CSPOMDriver._
import cspom.CSPOM
import CSPOM._
import cspom.variable.IntVariable
import cspom.variable.CSPOMSeq
import cspom.extension.Table
import concrete.Solver
import concrete.CSPOMSolution

object TSP extends App {

  val problem = CSPOM { implicit cspom =>
    val nCities = 4;
    val d = matrix""" 
      0, 9, 10, 2,
      9, 0,  3, 7,
     10, 3,  0, 4,
      2, 7,  4, 0"""

    val dists = for (i <- 0 until nCities; j <- 0 until nCities) yield Seq(i, j, d(i)(j))

    val visit = (0 until nCities).map(c => IntVariable(0 until nCities))

    visit as "visit"

    val distVar = for (Seq(v0, v1) <- visit.iterator.sliding(2)) yield {
      val v = IntVariable(0 to 10)
      ctr(Seq(v0, v1, v) in dists)
      v
    }

    val dist = sum(distVar.toSeq: _*) as "dist"

    ctr(allDifferent(visit: _*))

  }

  val solver = Solver(problem).get
  solver.minimize("dist")
  solver.next()
  for (s: CSPOMSolution <- solver) println(s.get("visit"))
}