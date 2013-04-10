package carseq

import java.io.ByteArrayInputStream
import java.net.URL
import scala.collection.immutable.Queue
import scala.collection.mutable.HashMap
import concrete.Concrete
import concrete.SQLWriter
import cspfj.generator.ProblemGenerator
import cspom.CSPOM
import cspom.extension.ExtensionConstraint
import cspom.extension.Table
import cspom.variable.CSPOMVariable
import cspom.extension.EmptyMDD
import cspom.extension.MDDLeaf
import cspom.extension.MDDNode
import cspom.extension.MDD
import cspom.extension.LazyMDD

object CarSeq extends Concrete with App {
  /**
   * The format of the data files is as follows:
   *
   * First line: number of cars; number of options; number of classes.
   * Second line: for each option, the maximum number of cars with that option in a block.
   * Third line: for each option, the block size to which the maximum number refers.
   * Then for each class: index no.; no. of cars in this class; for each option, whether or not this class requires it (1 or 0).
   *
   * This is the example given in (Dincbas et al., ECAI88):
   *
   *
   * 10 5 6
   * 1 2 1 2 1
   * 2 3 3 5 5
   * 0 1 1 0 1 1 0
   * 1 1 0 0 0 1 0
   * 2 2 0 1 0 0 1
   * 3 2 0 1 0 1 0
   * 4 2 1 0 1 0 0
   * 5 2 1 1 0 0 0
   *
   * A valid sequence for this set of cars is:
   * Class   Options req.
   * 0   1 0 1 1 0
   * 1   0 0 0 1 0
   * 5   1 1 0 0 0
   * 2   0 1 0 0 1
   * 4   1 0 1 0 0
   * 3   0 1 0 1 0
   * 3   0 1 0 1 0
   * 4   1 0 1 0 0
   * 2   0 1 0 0 1
   * 5   1 1 0 0 0
   */

  var cProblem: Option[CSPOM] = None

  var cars: IndexedSeq[CSPOMVariable] = _
  var options: IndexedSeq[IndexedSeq[CSPOMVariable]] = _

  def load(args: List[String]) = {
    //val url = new URL(args(0))
    val url = getClass().getResource(args(0))
    val source = io.Source.fromURL(url)
    val itr = source.getLines

    val Array(nbCars, nbOptions, nbClasses) = itr.next().split(" ").map(_.toInt)

    val maxCars = itr.next().split(" ").map(_.toInt)
    val blockSizes = itr.next().split(" ").map(_.toInt)

    var classes = List[Array[Int]]()
    val quantities = Array.ofDim[Int](nbClasses)
    for (options <- itr.map(_.split(" "))) {
      val i = options(0).toInt
      quantities(i) = options(1).toInt
      classes ::= (i +: options.drop(2).map(_.toInt))
    }

    val problem = new CSPOM()

    cars = (0 until nbCars) map (c => problem.interVar(s"car$c", 0, nbClasses - 1))
    options = cars.zipWithIndex map {
      case (cv, c) =>
        val vars = (0 until nbOptions) map (o => problem.varOf(s"car${c}option$o", 0, 1))
        problem.addConstraint(new ExtensionConstraint(new Table(classes), false, cv +: vars))
        vars
    }

    for (i <- 0 until nbOptions) {
      val cardinality = classes.map(c => quantities(c(0)) * c(i + 1)).sum
      //println(cardinality)
      sequenceBDD(problem, options.map(_(i)), maxCars(i), blockSizes(i), cardinality)
    }

    problem.ctr(s"gcc{${
      quantities.zipWithIndex.map {
        case (q, i) => s"$i, $q, $q"
      } mkString (", ")
    }}(${cars.mkString(", ")})")

    cProblem = Some(problem)
    val cp = ProblemGenerator.generate(problem)
    //problem.closeRelations()
    //println(cp)

    cp
  }

  def sequence(cp: CSPOM, vars: IndexedSeq[CSPOMVariable], u: Int, q: Int, cardinality: Int) {
    for (i <- 0 to vars.size - q) {
      val ub = cp.interVar(-u, 0)
      cp.ctr(s"zerosum(${vars.slice(i, i + q).mkString(", ")}, $ub)")
    }
    val ub = cp.interVar(-cardinality, -cardinality)
    cp.ctr(s"zerosum(${vars.mkString(", ")}, $ub)")
  }

  def sequenceBDD(cp: CSPOM, vars: IndexedSeq[CSPOMVariable], u: Int, q: Int, cardinality: Int) {
    val b = new LazyMDD(Unit => bdd(u, q, Queue.empty, vars.size, cardinality))
    //println(s"${args(0)} ${b.lambda} ${b.edges}")
    cp.addConstraint(new ExtensionConstraint(b, false, vars))
  }

  def bdd(capa: Int, block: Int, queue: Queue[Int], k: Int, cardinality: Int,
    nodes: HashMap[(Int, Int, Queue[Int], Int, Int), MDD] = new HashMap()): MDD = {
    if (capa < 0 || cardinality < 0 || cardinality > k) {
      EmptyMDD
    } else if (k == 0) {
      MDDLeaf
    } else {
      nodes.getOrElseUpdate((capa, block, queue, k, cardinality), {
        val (newCapa, newQueue) =
          if (queue.nonEmpty) {
            val (front, tail) = queue.dequeue

            if (front == k + block) {
              (capa + 1, tail)
            } else {
              (capa, queue)
            }
          } else {
            (capa, queue)
          }

        val l = bdd(newCapa, block, newQueue, k - 1, cardinality, nodes)
        val r = bdd(newCapa - 1, block, newQueue.enqueue(k), k - 1, cardinality - 1, nodes)

        if (l.isEmpty && r.isEmpty) {
          EmptyMDD
        } else if (l.isEmpty) {
          new MDDNode(Map(1 -> r))
        } else if (r.isEmpty) {
          new MDDNode(Map(0 -> l))
        } else {
          new MDDNode(Map(0 -> l, 1 -> r))
        }
      })
    }

  }

  def control(solution: Map[String, Int]): Option[String] = ???
  def description(args: List[String]) = args.head

  def output(solution: Map[String, Int]): String = {
    (cars zip options) map {
      case (c, o) => solution(c.name) + " " + o.map(p => solution(p.name)).mkString(" ")
    } mkString ("\n")
  }

  run(args)
}