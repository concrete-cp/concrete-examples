package knapsack
import cspfj.Problem
import cspfj.Variable
import cspfj.constraint.semantic.Eq
import cspfj.MAC
import cspfj.Solver
import cspfj.constraint.semantic.ZeroSum
import java.io.InputStream
import cspfj.IntDomain

object Knapsack {

  case class O(val w: Int, val p: Int)

  def load(is: InputStream): (Seq[O], Int) = {
    val lines = io.Source.fromInputStream(is).getLines().filter(!_.startsWith("#"))

    val R = """^(\d+)\s(\d+)$""".r
    val C = """^c: (\d+)\s*$""".r

    var o: Seq[O] = Seq()
    var c = 0

    lines foreach {
      case R(w, p) => o +:= O(w.toInt, p.toInt)
      case C(v) => c = v.toInt
      case _ =>
    }

    (o, c)
  }

  def main(args: Array[String]) = {
    val (o, c) = load(getClass.getResource("exnsd16.ukp").openStream())
    //println(c)
    //println(o)
    Solver.loggerLevel = "INFO"
    var u = 0
    var r = c
    for (obj <- o.sortBy(t => t.w.toDouble / t.p)) {
      val m = r / obj.w
      if (m > 0) {
        u += obj.p * m
        r -= obj.w * m
      }
    }

    var lb = u

    var ub = u - o.maxBy(t => t.w.toDouble / t.p).w + o.maxBy(_.p).w //lb + best._2

    println("p = [" + lb + ", " + ub + "], c = " + c)

    val problem = new Problem

    val variables = o.zipWithIndex map {
      case (O(w, _), i) =>
        problem.addVariable("v" + i, IntDomain(0 to (c / w)))
    }

    val wBound = problem.addVariable("wBound", IntDomain(0 to c))
    val pBound = problem.addVariable("pBound", IntDomain(lb to ub))

    problem.addConstraint(new ZeroSum(Array(-1) ++ o.map(_.w), Array(wBound) ++ variables))
    problem.addConstraint(new ZeroSum(Array(-1) ++ o.map(_.p), Array(pBound) ++ variables))

    val solver = Solver.factory(problem)
    val sol = solver.bestSolution(pBound).get

    (variables, o).zipped.foreach { (variable, o) =>
      if (sol(variable.name) > 0) {
        println(variable.name + "\t" + sol(variable.name) + "\t" + o.w + "\t" + o.p) //+ "\t" + sol(capa.name) + "\t" + sol(value.name))
      }
    }
    val bound = sol("pBound")

    println("Optimal : " + bound)
  }

}