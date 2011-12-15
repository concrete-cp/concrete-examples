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

  val o = List(
    (120, 300),
    (245, 580),
    (130, 301),
    (260, 601),
    (310, 605),
    (194, 322),
    (190, 310))

  def main(args: Array[String]) = {
    Solver.loggerLevel = "FINE"
    val best = o.maxBy(t => t._2.toDouble / t._1)
    var lb = best._2 * (c / best._1)
    var ub = 7237//lb + best._2
    while (ub > lb) {
      val test = (ub + lb) / 2
      println("Testing " + test)

      val problem = new Problem

      val variables = o.zipWithIndex map {
        case ((size, _), i) =>
          problem.addVariable("v" + i, new BitVectorDomain(0 to (c / size): _*))
      }

      val capacities = (o, variables).zipped map {
        case ((size, _), variable) =>
          val v = problem.addVariable("c" + variable.name, new BitVectorDomain(variable.dom.values map (_ * size) toSeq: _*))
          problem.addConstraint(new Eq(size, variable, 0, v))
          v
      }

      val values = (o, variables).zipped map {
        case ((_, value), variable) =>
          val v = problem.addVariable("v" + variable.name, new BitVectorDomain(variable.dom.values.map(_ * -value).toSeq.reverse: _*))
          problem.addConstraint(new Eq(-value, variable, 0, v))
          v
      }

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
  }

}