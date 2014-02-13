package golombruler

import concrete.CSPOMDriver._
import concrete.Solver
import concrete.generator.ProblemGenerator
import concrete.generator.cspompatterns.AllDiff
import cspom.CSPOM
import cspom.CSPOM._
import cspom.StatisticsManager
import cspom.compiler.ProblemCompiler
import cspom.compiler.StandardCompilers
import concrete.generator.cspompatterns.SubToAdd
import cspom.compiler.MergeEq

object GolombRuler extends App {
  val TICKS = 10
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

  val statistics = new StatisticsManager()
  statistics.register("problemCompiler", ProblemCompiler)

  //ProblemCompiler.compile(problem, StandardCompilers() ++ Seq(AllDiff, SubToAdd))
  ProblemCompiler.compile(problem, Seq(MergeEq, SubToAdd))

  println(problem)

  //println(statistics)

  val solver = Solver(ProblemGenerator.generate(problem))
  //
  //  println(solver.problem)

  solver.hasNext

  println(solver.statistics) //.digest.foreach(println)

}