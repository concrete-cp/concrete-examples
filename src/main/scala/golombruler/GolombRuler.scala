package golombruler

import cspom.CSPOM
import CSPOM._
import concrete.CSPOMDriver._
import concrete.Solver
import cspom.compiler.ProblemCompiler
import concrete.generator.ProblemGenerator
import cspom.compiler.StandardCompilers
import concrete.generator.cspompatterns.AllDiff
import concrete.generator.cspompatterns.SubToAdd
import concrete.MAC

object GolombRuler extends App {
  val TICKS = 8
  val MAX = TICKS * TICKS

  val problem = CSPOM {
    val variables = for (i <- 1 to TICKS) yield interVar(1, MAX) as s"T$i"

    for (Seq(xi, xj) <- variables.sliding(2)) {
      ctr(xi < xj)
    }

    for (
      xi <- variables; xj <- variables if xi != xj;
      xk <- variables; xl <- variables if xk != xl && (xi != xk || xj != xl)
    ) {
      ctr((xi - xj) !== (xk - xl))
    }
  }

  println(problem.constraints.size + " constraints")

  ProblemCompiler.compile(problem, StandardCompilers() ++ Seq(AllDiff))

  println(problem)

  //  val solver = Solver(ProblemGenerator.generate(problem))
  //
  //  println(solver.problem)

  ProblemCompiler.statistics.digest.foreach(println)

}