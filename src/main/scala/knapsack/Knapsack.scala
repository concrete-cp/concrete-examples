package knapsack
import cspfj.problem.Problem
import cspfj.problem.Variable
import cspfj.problem.IntervalDomain
import cspfj.constraint.semantic.SumLeq
import cspfj.constraint.semantic.Eq
import cspfj.MAC
import cspfj.Solver
import cspfj.problem.BitVectorDomain

object Knapsack {

  val c = 2900

  case class O(val w: Int, val p: Int)

  val o = List(
    O(120, 300),
    O(245, 580),
    O(130, 301),
    O(260, 601),
    O(310, 605),
    O(194, 322),
    O(190, 310))

  def main(args: Array[String]) = {
    //Solver.loggerLevel = "FINE"
    var u = 0
    var r = c
    for (obj <- o.sortBy(t => t.w.toDouble / t.p)) {
      val m = r / obj.w
      if (m > 0) {
        u += obj.p * m
        r -= obj.w * m
      }
    }

    println(u)

    var lb = u

    var ub = u - o.maxBy(t => t.w.toDouble / t.p).w + o.maxBy(_.p).w //lb + best._2
    while (ub > lb) {
      val test = (ub + lb + 1) / 2
      println("Testing " + test)

      val problem = new Problem

      val variables = o.zipWithIndex map {
        case (O(w, _), i) =>
          problem.addVariable("v" + i, new BitVectorDomain(0 to (c / w): _*))
      }

      val (capacities, values) = (o, variables).zipped map {
        case (O(w, p), variable) =>
          val vc = problem.addVariable("c" + variable.name, new BitVectorDomain(variable.dom.values map (_ * w) toSeq: _*))
          problem.addConstraint(new Eq(w, variable, 0, vc))
          val vv = problem.addVariable("v" + variable.name, new BitVectorDomain(variable.dom.values.map(_ * -p).toSeq.reverse: _*))
          problem.addConstraint(new Eq(-p, variable, 0, vv))
          (vc, vv)
      } unzip

      problem.addConstraint(new SumLeq(c, capacities.toArray))
      problem.addConstraint(new SumLeq(-test, values.toArray))

      val solver = Solver.factory(problem)
      solver.nextSolution() match {
        case Some(sol) => {
          (variables, capacities, values).zipped.foreach { (variable, capa, value) =>
            if (sol(variable.name) > 0) {
              println(variable.name + "\t" + sol(variable.name) + "\t" + sol(capa.name) + "\t" + sol(value.name))
            }
          }
          lb = -(values map (v => sol(v.name)) sum)
          println(lb)
        }
        case None => ub = test - 1

      }
    }
    println("Optimal : " + lb)
  }

}