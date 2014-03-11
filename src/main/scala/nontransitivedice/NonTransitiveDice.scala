package nontransitivedice

import cspom.CSPOM
import CSPOM._
import cspom.variable.IntVariable
import concrete.CSPOMDriver._
import concrete.Solver

object NonTransitiveDice extends App {
  val NB_DICE = 4
  val NB_FACES = 6
  val MAX_VALUE = 7

  def slide[A](s: Seq[A]): Iterator[(A, A)] = {
    Iterator((s.last, s.head)) ++ s.sliding(2).map {
      case Seq(i, j) => (i, j)
    }
  }

  val problem = CSPOM {
    val dice = for (i <- 0 until NB_DICE) yield {
      for (j <- 0 until NB_FACES) yield {
        IntVariable.ofInterval(1, MAX_VALUE) as s"D${i}F$j"
      }
    }

    for (d <- dice) {
      for (Seq(i, j) <- d.sliding(2)) {
        ctr(i <= j)
      }
    }

    val nbs = IntVariable.free() as "NBS"

    for ((d1, d2) <- slide(dice)) {
      ctr(nbs === nbSup(d1, d2))

      for (f1 <- d1; f2 <- d2) {
        ctr(f1 !== f2)
      }
    }

    ctr(nbs > (NB_FACES * NB_FACES) / 2)
  }

  def nbSup(d1: Seq[IntVariable], d2: Seq[IntVariable]) = {
    val counts = for (i <- d1; j <- d2) yield {
      i > j
    }
    occurrence(true, counts: _*)
  }

  val solver = Solver(problem)
  solver.maximize("NBS")

  for (solution <- solver) {
    println(solution("NBS"))
    for (i <- 0 until NB_DICE) {
      println((0 until NB_FACES).map(j => solution(s"D${i}F$j")))
    }

  }
}