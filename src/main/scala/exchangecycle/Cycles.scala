package exchangecycle

import cspom.CSPOM
import cspom.CSPOM._
import concrete.CSPOMDriver._
import scala.xml.Node
import cspom.extension.Table
import concrete.Solver
import cspom.compiler.ProblemCompiler
import cspom.variable.CSPOMTrue
import concrete.ParameterManager
import cspom.variable.CSPOMVariable
import cspom.CSPOMConstraint
import scala.util.Random
import cspom.variable.CSPOMFalse
import cspom.variable.BoolVariable
import cspom.variable.BoolExpression

object Cycles extends App {

  val n = 3
  val t = 1
  val d = 1
  val rand = new Random(0)

  val problem = CSPOM {
    // Génération des arcs
    val variables =
      for (i <- 0 until n) yield {
        for (j <- 0 until n) yield {
          if (rand.nextDouble() < t) {
            new BoolVariable() as s"V$i-$j"
          } else {
            CSPOMFalse
          }
        }
      }

    // Assure la connectivité du graphe (algorithme de Floyd-Marshall)
    //    val F =
    //      for (i <- 0 until n) yield {
    //        for (j <- 0 until n) yield {
    //          for (k <- 0 until n) yield {
    //
    //            //ctr(variables(i)(j) ==> v)
    //            //if (i == j) {
    //            val v = auxBool()
    //            ctr(variables(i)(j) ==> v)
    //            v
    //            //            } else {
    //            //              CSPOMTrue
    //            //            }
    //
    //          }
    //
    //        }
    //      }

    val F =
      for (
        i <- 0 until n
      ) yield for (
        j <- 0 until n
      ) yield for (
        k <- 0 until n
      ) yield {
        if (k == 0 || k == 0) {
          variables(i)(j)
        } else if (k == n - 1 && i != j) {
          CSPOMTrue
        } else {
          new BoolVariable() as s"F$i-$j-$k"
        }
      }

    for (i <- 0 until n; j <- 0 until n; k <- 1 until n) {
      ctr(F(i)(j)(k) === (F(i)(j)(k - 1) | (F(k)(j)(k - 1) & F(i)(k)(k - 1))))
    }

    for (i <- 0 until n) yield {
      ctr(occurrence(true, variables(i).filter(_.isInstanceOf[CSPOMVariable]): _*) === d)
      ctr(occurrence(true, variables.map(_(i)).filter(_.isInstanceOf[CSPOMVariable]): _*) === d)
    }
  }

//  println(problem)
//  ProblemCompiler.compile(problem, Patterns())

  println(problem)

  ParameterManager("heuristic.value") = classOf[concrete.heuristic.RevLexico]
  //  ParameterManager("logger.level") = "INFO"
  val solver = Solver(problem)

  println("Searching…")

  for (sol <- solver.toIterable.headOption) {
    println("graph [")
    println("directed 1")
    for (v <- 0 until n) {
      println("node [")
      println("id \"" + v + "\"")
      println("]")
    }

    for (i <- 0 until n; j <- 0 until n) {
      val r = sol.getOrElse(s"V$i-$j", 0)

      if (r == 1) {
        println("edge [")
        println("source \"" + i + "\"")
        println("target \"" + j + "\"")
        println("graphics [ targetArrow \"standard\" ]")
        println("]")
      }
    }

    println("]")
  }

  //  def cycle[A](s: Seq[A]): Iterator[(A, A)] = {
  //    s.sliding(2).map { case Seq(a, b) => (a, b) } ++ Iterator((s.last, s.head))
  //  }
}