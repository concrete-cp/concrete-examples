package knapsack

import java.io.InputStream
import scala.collection.mutable.HashMap
import concrete.IntDomain
import concrete.Problem
import concrete.Solver
import concrete.Variable
import concrete.constraint.semantic.Eq
import concrete.constraint.semantic.ZeroSum
import cspom.CSPOM
import cspom.extension.EmptyMDD
import cspom.extension.MDD
import cspom.extension.MDDLeaf
import cspom.extension.MDDNode
import cspom.extension.ExtensionConstraint
import cspom.variable.CSPOMDomain
import concrete.generator.ProblemGenerator
import cspom.variable.CSPOMVariable
import scala.annotation.tailrec
import scala.util.Random
import concrete.ParameterManager
import concrete.heuristic.RevLexico
import cspom.extension.LazyMDD
import concrete.runner.ConcreteRunner

object Knapsack extends ConcreteRunner with App {
  run(args)
  case class O(val w: Int, val p: Int)

  def load(is: InputStream): (List[O], Int) = {
    val lines = io.Source.fromInputStream(is).getLines().filter(!_.startsWith("#"))

    val R = """^(\d+)\s(\d+)$""".r
    val C = """^c: (\d+)\s*$""".r

    var o: List[O] = List()
    var c = 0

    lines foreach {
      case R(w, p) => o +:= O(w.toInt, p.toInt)
      case C(v) => c = v.toInt
      case _ =>
    }

    (o, c)
  }

  def main2(args: Array[String]) = {
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

    val cspom = new CSPOM

    val variables = o.zipWithIndex map {
      case (O(w, _), i) =>
        cspom.interVar("v" + i, 0, c / w)
    }

    val wBound = cspom.interVar("wBound", 0, c)
    val pBound = cspom.interVar("pBound", lb, ub)

    println(variables.mkString("\n"))

    val mdd = zeroSum(wBound :: variables, -1 :: o.map(_.w))
    println(mdd)
    //
    //    problem.addConstraint(new ZeroSum(Array(-1) ++ o.map(_.w), Array(wBound) ++ variables))
    //    problem.addConstraint(new ZeroSum(Array(-1) ++ o.map(_.p), Array(pBound) ++ variables))
    //
    //    val solver = Solver.factory(problem)
    //    val sol = solver.bestSolution(pBound).get
    //
    //    (variables, o).zipped.foreach { (variable, o) =>
    //      if (sol(variable.name) > 0) {
    //        println(variable.name + "\t" + sol(variable.name) + "\t" + o.w + "\t" + o.p) //+ "\t" + sol(capa.name) + "\t" + sol(value.name))
    //      }
    //    }
    //    val bound = sol("pBound")
    //
    //    println("Optimal : " + bound)
  }

  ParameterManager("logger.level") = "INFO"
  ParameterManager("heuristic.value") = classOf[RevLexico]

  def control(solution: Map[String, Int]) = None

  def output(solution: Map[String, Int]) = solution.toString

  def description(args: List[String]) = {
    args.mkString("knapsack-", "-", "")
  }

  override def load(args: List[String]) = {
    val List(n, b, r, i, s) = args map (_.toInt)

    val (w, m, p) = ks(n, b, r, i, s)

    val c = (95 * w.zip(m).map(i => i._1 * i._2).sum) / 100
    val ub = p.zip(m).map(i => i._1 * i._2).sum

    val cspom = new CSPOM

    val variables = m.map(b => cspom.interVar(0, b)).toList

    var lb = 0
    var weight = 0

    util.control.Breaks.breakable {
      val l = (w, m, p).zipped.toList.sortBy {
        case (wi, mi, pi) => wi / pi
      }
      for ((wi, mi, pi) <- l) {
        val nw = weight + mi * wi
        if (nw > c) {
          util.control.Breaks.break
        }
        weight = nw
        lb += mi * pi
      }
    }

    val wBound = cspom.interVar("wBound", weight, c)
    val pBound = cspom.interVar("pBound", lb, ub)

    val wMDD = new LazyMDD(Unit => zeroSum(wBound :: variables, -1 :: w.toList))
    //println(wMDD)
    cspom.addConstraint(new ExtensionConstraint(wMDD, false, wBound :: variables))
    val pMDD = new LazyMDD(Unit => zeroSum(pBound :: variables, -1 :: p.toList))
    //println(pMDD)
    cspom.addConstraint(new ExtensionConstraint(pMDD, false, pBound :: variables))

    ProblemGenerator.generate(cspom)
    //val solver = Solver.factory(problem)
    //println(solver.nextSolution())
    //println(solver.bestSolution(problem.variable("pBound")))

    //    val variables = List.fill(10)(
    //      cspom.interVar(1, 30))
    //
    //    val mdd = zeroSum(variables, (-5 to 4).toList)
    //    println(mdd)
    //    //println(mdd.iterator.map(_.toSeq).mkString("\n"))
    //    cspom.addConstraint(new ExtensionConstraint(mdd, false, variables))
    //
    //    //val problem = ProblemGenerator.generate(cspom)
    //    val solver = Solver.factory(cspom)
    //    val s = solver.nextSolution().get
    //    println(variables.map(v => s(v.name)))
  }

  /**
   * n: number of variables
   * m: bound
   * r: maximum weight/profit
   * i: seed
   * s: last seed
   */
  def ks(n: Int, m: Int, r: Int, i: Int, s: Int) = {
    var w = 0
    val rand = new Random(i)
    val wt = new Array[Int](n)
    val mt = new Array[Int](n)
    val pt = new Array[Int](n)
    for (j <- 0 until n) {
      wt(j) = rand.nextInt(r) + 1
      mt(j) = rand.nextInt(m / 2) + m / 2
      pt(j) = rand.nextInt(r) + 1
      w += mt(j) * wt(j)
    }
    var c = (i / w) / (s + 1)
    if (c <= r) {
      c = r + 1
    }
    for (j <- 0 until n) {
      if (mt(j) * wt(j) > c) {
        mt(j) = c / wt(j)
      }
    }
    (wt.toSeq, mt.toSeq, pt.toSeq)
  }

  @tailrec
  def sum(domains: List[Seq[Int]], factors: List[Int], f: Seq[Int] => Int, currentSum: Int = 0): Int = {
    if (domains.isEmpty) {
      currentSum
    } else {
      sum(domains.tail, factors.tail, f, currentSum + f(domains.head) * factors.head)
    }
  }

  def zeroSum(variables: List[CSPOMVariable], factors: List[Int]): MDD = {
    val domains = variables.map(_.domain.asInstanceOf[CSPOMDomain[Int]].values)
    zeroSum(domains, factors, sum(domains, factors, _.min), sum(domains, factors, _.max))
  }

  def zeroSum(domains: List[Seq[Int]], factors: List[Int], min: Int, max: Int, currentSum: Int = 0,
    nodes: HashMap[(Int, Int), MDD] = new HashMap()): MDD = {
    if (domains.isEmpty) {
      require(currentSum == min && currentSum == max)
      if (currentSum == 0) {
        MDDLeaf
      } else {
        EmptyMDD
      }
    } else if (min > 0) {
      EmptyMDD
    } else if (max < 0) {
      EmptyMDD
    } else {
      nodes.getOrElseUpdate((domains.size, currentSum), {
        val head :: tail = domains
        val hf :: tf = factors
        val newMin = min - head.min * hf
        val newMax = max - head.max * hf
        val trie = head.iterator.
          map(i => i -> zeroSum(tail, tf, newMin + i * hf, newMax + i * hf, currentSum + i * hf, nodes)).
          filter(_._2.nonEmpty)

        if (trie.isEmpty) {
          EmptyMDD
        } else {
          new MDDNode(trie.toMap)
        }
      })

    }
  }

}