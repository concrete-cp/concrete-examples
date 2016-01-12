package knapsack

import java.io.InputStream
import scala.annotation.tailrec
import scala.collection.mutable.HashMap
import scala.util.Random
import concrete.ParameterManager
import concrete.Variable
import concrete.heuristic.RevLexico
import concrete.runner.CSPOMRunner
import concrete.runner.ConcreteRunner
import cspom.CSPOM
import cspom.CSPOM._
import cspom.extension.MDD
import cspom.extension.MDDNode
import cspom.util.ContiguousIntRangeSet
import cspom.variable.CSPOMVariable
import cspom.variable.IntVariable
import scala.util.Try

object Knapsack extends CSPOMRunner with App {

  pm("logger.level") = "INFO"
  pm("heuristic.value") = classOf[RevLexico]
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
      case C(v)    => c = v.toInt
      case _       =>
    }

    (o, c)
  }

  def main2(args: Array[String]) = {
    val (o, c) = load(getClass.getResource("exnsd16.ukp").openStream())
    //println(c)
    //println(o)
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

    val cspom = CSPOM { implicit problem =>

      val variables = o.zipWithIndex map {
        case (O(w, _), i) => IntVariable(0 to (c / w)) as ("v" + i)
      }

      val wBound = IntVariable(0 to c) as "wBound"
      val pBound = IntVariable(lb to ub) as "pBound"

      println(variables.mkString("\n"))

      val mdd = zeroSum(wBound :: variables, -1 :: o.map(_.w))
      println(mdd)
    }
  }

  def controlCSPOM(solution: Map[String, Any]) = ???

  def description(args: List[String]) = {
    args.mkString("knapsack-", "-", "")
  }

  override def loadCSPOM(args: List[String], opt: Map[Symbol, Any]) = Try {
    val List(n, b, r, i, s) = args map (_.toInt)

    val (w, m, p) = ks(n, b, r, i, s)

    val c = (95 * w.zip(m).map(i => i._1 * i._2).sum) / 100
    val ub = p.zip(m).map(i => i._1 * i._2).sum

    CSPOM { implicit problem =>

      val variables = m.map(b => IntVariable(0 to b)).toList

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

      val wBound = IntVariable(weight to c) as "wBound"
      val pBound = IntVariable(lb to ub) as "pBound"

      val wMDD = zeroSum(wBound :: variables, -1 :: w.toList)
      //println(wMDD)
      ctr(wBound :: variables in wMDD)
      val pMDD = zeroSum(pBound :: variables, -1 :: p.toList)
      //println(pMDD)
      ctr(pBound :: variables in pMDD)
    }

    //val solver = Solver.factory(problem)
    //println(solver.nextSolution())
    //println(solver.bestSolution(problem.variable("pBound")))

    //    val variables = List.fill(10)(
    //      cspom.IntVariable.ofInterval(1, 30))
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
  def sum(domains: List[Iterable[Int]], factors: List[Int], f: Iterable[Int] => Int, currentSum: Int = 0): Int = {
    if (domains.isEmpty) {
      currentSum
    } else {
      sum(domains.tail, factors.tail, f, currentSum + f(domains.head) * factors.head)
    }
  }

  def zeroSum(variables: List[IntVariable], factors: List[Int]): MDD[Int] = {
    val domains = variables.map(v => new ContiguousIntRangeSet(v.domain))
    zeroSum(domains, factors, sum(domains, factors, _.min), sum(domains, factors, _.max))
  }

  def zeroSum(domains: List[Iterable[Int]], factors: List[Int], min: Int, max: Int, currentSum: Int = 0,
              nodes: HashMap[(Int, Int), MDD[Int]] = new HashMap()): MDD[Int] = {
    if (domains.isEmpty) {
      require(currentSum == min && currentSum == max)
      if (currentSum == 0) {
        MDD.leaf
      } else {
        MDD.empty
      }
    } else if (min > 0) {
      MDD.empty
    } else if (max < 0) {
      MDD.empty
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
          MDD.empty
        } else {
          new MDDNode(trie.toMap)
        }
      })

    }
  }

}
