package amphi

import cspom.CSPOM
import cspom.variable.IntVariable
import CSPOM._
import concrete.Solver
import concrete.ParameterManager
import concrete.heuristic.RevLexico
import concrete.heuristic.LexVar
import concrete.CSPOMDriver._

object Amphi extends App {

  val NBETU = 37
  val MINDIST = 3

  val (problem, (etuI, etuJ)) = CSPOM withResult {

    val (etuI, etuJ) = Array.fill(NBETU)((interVar(0, 8), interVar(0, 15))).unzip

    for (e <- 1 until NBETU) {
      val i0 = etuI(e - 1)
      val i1 = etuI(e)
      val j0 = etuJ(e - 1)
      val j1 = etuJ(e)
      ctr(lexLeq(Seq(i0, j0), Seq(i1, j1)))
    }

    for (e <- 0 until NBETU; j <- (0 to 3) ++ (12 to 15)) {
      ctr((etuI(e) !== 0) | (etuJ(e) !== j))
    }

    for (e0 <- 0 until NBETU; e1 <- e0 + 1 until NBETU) {
      val i0 = etuI(e0)
      val i1 = etuI(e1)
      val j0 = etuJ(e0)
      val j1 = etuJ(e1)
      //      //ctr('absdiff.defInt(i0, i1) + 'absdiff.defInt(j0, j1) >= MINDIST)
      ctr(sq(i0 - i1) + sq(j0 - j1) >= MINDIST)
      //    }
      //      ctr('nevec(Seq(i0, j0), Seq(i1, j1)))
    }
    (etuI, etuJ)

  }

  Solver.loggerLevel = "INFO"
  ParameterManager("heuristic.variable") = classOf[LexVar]
  val solver = Solver(problem)

  solver.toStream.headOption match {
    case None => println("UNSAT")
    case Some(solution) =>
      val places = (for (e <- 0 until NBETU)
        yield ((solution(etuI(e).name), solution(etuJ(e).name)))) toSet

      for (i <- 7 to 0 by -1) {
        for (j <- 0 until 15) {
          if (places((i, j))) print("*") else print(".")
        }
        println()
      }

  }

}