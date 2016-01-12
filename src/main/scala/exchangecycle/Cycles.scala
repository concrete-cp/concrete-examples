package exchangecycle

import scala.util.Random
import concrete.CSPOMDriver.CSPOMBoolExpressionOperations
import concrete.CSPOMDriver.occurrence
import concrete.ParameterManager
import concrete.Solver
import concrete.heuristic.RevLexico
import cspom.CSPOM
import cspom.CSPOM.constant
import cspom.CSPOM.ctr
import cspom.variable.BoolVariable
import cspom.variable.CSPOMConstant
import cspom.variable.IntVariable
import concrete.SolverFactory

object Cycles extends App {

  val n = 3
  val t = 1
  val d = 1
  val rand = new Random(0)

  val problem = CSPOM { implicit problem =>
    // Génération des arcs
    val variables =
      for (i <- 0 until n) yield {
        for (j <- 0 until n) yield {
          if (rand.nextDouble() < t) {
            new BoolVariable()
          } else {
            CSPOMConstant(false)
          } as s"V$i-$j"
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
    //            //              CSPOMConstant(true)
    //            //            }
    //
    //          }
    //
    //        }
    //      }

//    val F =
//      for (
//        i <- 0 until n
//      ) yield for (
//        j <- 0 until n
//      ) yield for (
//        k <- 0 until n
//      ) yield {
//        if (k == 0 || k == 0) {
//          variables(i)(j)
//        } else if (k == n - 1 && i != j) {
//          CSPOMConstant(true)
//        } else {
//          new BoolVariable() as s"F$i-$j-$k"
//        }
//      }
//
//    for (i <- 0 until n; j <- 0 until n; k <- 1 until n) {
//      ctr(F(i)(j)(k) === (F(i)(j)(k - 1) | (F(k)(j)(k - 1) & F(i)(k)(k - 1))))
//    }

    for (i <- 0 until n) yield {
      ctr(occurrence(true)(variables(i): _*) === d)
      ctr(occurrence(true)(variables.map(_(i)): _*) === d)
    }
  }

  //  println(problem)
  //  CSPOMCompiler.compile(problem, Patterns())

  println(problem)

  val pm = new ParameterManager
  pm("heuristic.value") = classOf[concrete.heuristic.RevLexico]
  //  ParameterManager("logger.level") = "INFO"
  val solver = new SolverFactory(pm)(problem).get

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

      val r: Int = sol(s"V$i-$j").asInstanceOf[Int]

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