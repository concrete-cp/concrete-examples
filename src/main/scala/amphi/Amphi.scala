package amphi

import cspom.CSPOM
import cspom.variable.IntVariable
import CSPOM._
import concrete.Solver
import concrete.ParameterManager
import concrete.heuristic.RevLexico
import concrete.heuristic.LexVar

object Amphi extends App {

  val NBETU = 2
  val MINDIST = 1

  val (problem, (etuI, etuJ)) = CSPOM withResult {

    val (etuI, etuJ) = Array.fill(NBETU)((interVar(0, 8), interVar(0, 15))).unzip

    for (e <- 1 until NBETU) {
      val i0 = etuI(e - 1)
      val i1 = etuI(e)
      val j0 = etuJ(e - 1)
      val j1 = etuJ(e)
      ctr('lexleq(Seq(i0, j0), Seq(i1, j1)))
    }

    for (e <- 0 until NBETU; j <- (0 to 3) ++ (12 to 15)) {
      ctr((etuI(e) ne 0) | (etuJ(e) ne j))
    }

    for (e0 <- 0 until NBETU; e1 <- e0 + 1 until NBETU) {
      val i0 = etuI(e0)
      val i1 = etuI(e1)
      val j0 = etuJ(e0)
      val j1 = etuJ(e1)
      //      //ctr('absdiff.defInt(i0, i1) + 'absdiff.defInt(j0, j1) >= MINDIST)
      ctr('sq.defInt(i0 - i1) + 'sq.defInt(j0 - j1) >= MINDIST)
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
      for (e <- 0 until NBETU) {
        println((solution(etuI(e).name), solution(etuJ(e).name)))
      }
  }

}