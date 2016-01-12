package sudoku

import concrete.IntDomain
import concrete.Problem
import concrete.Variable
import concrete.constraint.semantic.Neq
import cspom.CSPOM._
import concrete.filter.ACC
import concrete.ParameterManager
import concrete.MAC
import cspom.CSPOM
import cspom.variable.IntVariable
import concrete.CSPOMDriver._
import concrete.Solver

object Sudoku extends App {

  val hints = matrix"""
    4, 0, 8,  0, 0, 0,  0, 0, 0
    0, 0, 0,  1, 7, 0,  0, 0, 0
    0, 0, 0,  0, 8, 0,  0, 3, 2
  
    0, 0, 6,  0, 0, 8,  2, 5, 0  
    0, 9, 0,  0, 0, 0,  0, 8, 0
    0, 3, 7,  6, 0, 0,  9, 0, 0
  
    2, 7, 0,  0, 5, 0,  0, 0, 0
    0, 0, 0,  0, 1, 4,  0, 0, 0
    0, 0, 0,  0, 0, 0,  6, 0, 4  
  """

  val sudoku = CSPOM { implicit problem =>
    val variables = for (i <- 0 until 9) yield for (j <- 0 until 9) yield IntVariable(1 to 9) as s"$i.$j"

    for (i <- 0 until 9) {
      // Rows
      ctr(allDifferent(variables(i): _*))

      // Columns
      ctr(allDifferent(variables.map(_(i)): _*))

    }

    // Squares
    for (i <- 0 until 3; j <- 0 until 3) {
      ctr(allDifferent((
        for (p <- 0 until 3; q <- 0 until 3) yield variables(3 * i + p)(3 * j + q)): _*))
    }

    // Hints
    for (i <- 0 until 9; j <- 0 until 9) {
      if (hints(i)(j) > 0) {
        ctr(variables(i)(j) === hints(i)(j))
      }
    }

  }

  val pm = new ParameterManager

  val solver = Solver(sudoku).get

  val problem = solver.concreteProblem
  var ps = problem.initState.toState
  println(problem)
  for (c <- problem.constraints) {
    ps = c.revise(ps).toState
  }

  val vars = for (i <- 0 until 9) yield for (j <- 0 until 9) yield problem.variableMap.get(s"$i.$j").getOrElse(null)

  for (i <- 0 until 9; j <- 0 until 9) {
    if (hints(i)(j) == 0) {
      println(s"\\hintcell{${i + 1}}{${j + 1}}{${ps.dom(vars(i)(j)).mkString(", ")}}")
    }
  }

  println("------")
  new ACC(problem, pm).reduceAll(ps) andThen { ps =>

    for (i <- 0 until 9; j <- 0 until 9) {
      if (hints(i)(j) == 0) {
        println(s"\\hintcell{${i + 1}}{${j + 1}}{${ps.dom(vars(i)(j)).mkString(", ")}}")
      }
    }

    ps
  }

  MAC(problem, pm).nextSolution()
  println(problem.variables)

}