package mdd

import scala.collection.mutable.HashMap
import scala.util.Random
import scala.util.Try
import org.scalameter.MeasureBuilder
import org.scalameter.api._
import org.scalameter.picklers.noPickler._
import concrete.runner.CSPOMRunner
import concrete.runner.ConcreteRunner
import cspom.CSPOM
import cspom.CSPOM._
import cspom.StatisticsManager
import cspom.extension.MDD
import cspom.extension.MDDNode
import rb.randomlists.CoarseProportionRandomListGenerator
import rb.randomlists.Structure
import cspom.variable.IntVariable
import org.scalameter.Key
import org.scalameter.Measurer.OutlierElimination
import org.scalameter.Measurer.PeriodicReinstantiation
import org.scalameter.Keys
import concrete.generator.ProblemGenerator
import concrete.constraint.Constraint
import concrete.constraint.extension.ExtensionConstraint
import concrete.constraint.extension.ReduceableExt
import concrete.Problem
import cspom.UNSATException
import concrete.ProblemState
import concrete.Outcome
import concrete.Contradiction
import concrete.Variable
import scala.collection.mutable.ArrayBuffer
import concrete.constraint.extension.MDD0
import concrete.IntDomain
import concrete.constraint.extension.TupleTrieSet
import concrete.constraint.extension.MDDRelation
import concrete.constraint.extension.STR
import concrete.constraint.Removals
import org.scalameter.Measurer.IterationBasedValue
import org.scalameter.Quantity
import org.scalameter.Measurer.Timer
import scala.collection.mutable.ListBuffer
import scala.compat.Platform
import java.io.PrintWriter
import java.io.File
import java.util.Locale
import concrete.constraint.extension.MDDCd
import concrete.constraint.extension.MDDCnu
import concrete.constraint.extension.MDDGenerator
import concrete.constraint.extension.MDDLink
import concrete.constraint.extension.MDDLinkRelation
import concrete.constraint.extension.MDDCLink

object RandomMDD extends CSPOMRunner with App {

  override def loadCSPOM(args: List[String], opt: Map[Symbol, Any]) = Try {

    val (n, d, k, e, l, q, s) = params(args(0))

    val rand = new Random(s)

    CSPOM { implicit problem =>
      val vars = List.tabulate(n)(i => IntVariable(0 until d) as s"V$i")

      val r = new CoarseProportionRandomListGenerator(n, k, s);

      for (scope <- r.selectTuples(e, Structure.UNSTRUCTURED, false, false)) {
        val mdd = MDDGenerator(d, k, l, q, rand)

        ctr(scope.toSeq.map(vars).in(mdd))
      }
    }
  }

  def description(args: List[String]) = {
    val (n, d, k, e, l, q, s) = params(args(0))
    s"mdd-$n-$d-$k-$e-$l-$q-$s"
  }

  def controlCSPOM(solution: Map[String, Any]) = ???

  def params(args: String) = {
    val Array(nbVariables, domainSize, arity, nbConstraints,
      looseness, mddProb, seed) = args.split(":")
    val n = nbVariables.toInt
    val d = domainSize.toInt
    val k = arity.toInt
    val e = nbConstraints.toInt
    val l = looseness.toDouble match {
      case l if l > 0 => l
      case l          => math.exp(-n * math.log(d) / e)
    }
    val q = mddProb.toDouble
    val s = seed.toLong

    (n, d, k, e, l, q, s)
  }

  run(args)
}

object TestMDD extends App {

  val points = Seq(
    (12 to 250 by 17, Seq(5), Seq(0.0), Seq(200000)),
    (Seq(15), Seq(5), Seq(0.0), 0 to 759375 by 50625),
    (Seq(15), 8 to 31 by 3, Seq(0.0), Seq(200000)))

  val it = 1000

  val varProb = .2
  val valProb = .2
  val maxDepth = 20

  val algos: Seq[(String, Function2[Array[Variable], concrete.constraint.extension.MDD, Removals])] = Seq(
    "MDDCd" -> { case (vars, data) => new MDDCd(vars, new MDDRelation(data.reduce())) },
    "MDDCnu" -> { case (vars, data) => new MDDCnu(vars, new MDDRelation(data.reduce())) },
    "MDDCLink" -> { case (vars, data) => new MDDCLink(vars, new MDDLinkRelation(MDDLink(data.map(_.toList)).reduce())) },
    "MDDF" -> { case (vars, data) => new ReduceableExt(vars, new MDDRelation(data.reduce())) },
    "MDDF2" -> { case (vars, data) => new ReduceableExt(vars, new MDDLinkRelation(MDDLink(data.map(_.toList)).reduce())) },
    "STR2" -> { case (vars, data) => new ReduceableExt(vars, STR(data)) })

  def generate(d: Int, k: Int, lambda: Int, q: Double, rand: Random, g: (Array[Variable], concrete.constraint.extension.MDD) => Removals): (Outcome, Removals) = {
    val data = MDDGenerator(d, k, lambda, rand)

    val vars = Array.tabulate(k)(i => new Variable(s"V$i", IntDomain(0 until d)))
    val problem = new Problem(vars)
    val constraint = g(vars, data)

    problem.addConstraint(constraint)
    (problem.initState, constraint)
  }

  def simulate(ps: Outcome, vars: Array[Variable], varProb: Double, valProb: Double, rand: Random): (Outcome, Seq[Int]) = {
    var mod = new ListBuffer[Int]
    var s = ps
    while (s.isState && mod.isEmpty) {
      for ((v, i) <- vars.zipWithIndex) {
        if (rand.nextDouble() < varProb) {
          val n = s.filterDom(v)(_ => rand.nextDouble() > valProb)
          if (n ne s) {
            s = n
            mod += i
          }
        }
      }
    }

    (s, mod)
  }

  val aggregator: Aggregator[(Double, Double)] = Aggregator("tuple-median") { xs =>
    val units = xs.head.units
    val m1 = StatisticsManager.median(xs.map(_.value._1))
    val m2 = StatisticsManager.median(xs.map(_.value._2))
    Quantity((m1, m2), units)
  }

  val measurer = new MultiRun[(Outcome, Removals)]({
    case (state, constraint) =>
      var max = maxDepth
      var ps = List(state)
      var time = 0L

      while (max > 0) {
        simulate(ps.head, constraint.scope, varProb, valProb, rand) match {
          case (Contradiction, _) => max = 0
          case (s: ProblemState, mod) =>
            val start = System.nanoTime()
            ps ::= constraint.revise(s, mod)
            val end = System.nanoTime()
            time += end - start
            max -= 1
        }
      }
      val runtime = Runtime.getRuntime
      Platform.collectGarbage()
      Platform.collectGarbage()
      val mem = runtime.totalMemory - runtime.freeMemory
      (ps, (time, mem))
  }) with PeriodicReinstantiation[(Double, Double)]

  def d(s: Seq[_], or: String): String =
    if (s.length == 1) s.head.toString else or

  val rand = new Random(0)

  Locale.setDefault(Locale.US)

  for ((ds, ks, qs, lambdas) <- points) {

    val file = new File(s"mdd_${d(ds, "d")}_${d(ks, "k")}_${d(qs, "q")}_${d(lambdas, "lambda")}_${varProb}x${valProb}")
    if (file.exists()) {
      System.out.println(s"$file exists, skipping")
    } else {

      val out = new PrintWriter(file)

      out.println(s"d\tk\tq\tlambda\t${algos.map(_._1).map(n => s"$n-time\t$n-mem").mkString("\t")}")

      try {

        for (d <- ds; k <- ks; q <- qs; lambda <- lambdas) {
          val params = f"$d%d\t$k%d\t$q%f\t$lambda%d"
          println(params)
          out.print(params)

          for ((name, algo) <- algos if name == "MDDCLink") {

            println(name)

            val searchMeasurer = new MeasureBuilder[(Outcome, Removals), (Double, Double)](
              Context.inlineBenchmarking,
              new Warmer.Default,
              measurer,
              (() => generate(d, k, lambda, q, rand, algo)),
              _ => (),
              _ => (),
              aggregator)
              .config(
                Key.exec.benchRuns -> it,
                Key.verbose -> false,
                Key.exec.reinstantiation.frequency -> 20)

            //println(searchMeasurer.ctx(Key.exec.outliers.suspectPercent))
            //println(searchMeasurer.ctx.get(Key.exec.benchRuns))
            //Key.exec.outliers.suspectPercent")// -> 25

            val r = searchMeasurer.measureWith { _ => Unit }

            val result = f"\t${r.value._1}%f\t${r.value._2}%f"
            println(result)
            out.print(result)
            out.flush()

          }
          out.println()
        }
      } finally {
        out.close()
      }
    }
  }

}

class MultiRun[S](val realSnippet: S => (Any, (Long, Long))) extends Measurer[(Double, Double)] with IterationBasedValue {
  def name = "Measurer.MultiRun"

  //  def numeric = new Numeric[(Double, Double)] {
  //    def fromInt(x: Int): (Double, Double) = ???
  //    def minus(x: (Double, Double), y: (Double, Double)): (Double, Double) = (x._1 - y._1, x._2 - y._2)
  //    def negate(x: (Double, Double)): (Double, Double) = (-x._1, -x._2)
  //    def plus(x: (Double, Double), y: (Double, Double)): (Double, Double) = (x._1 + y._1, x._2 + y._2)
  //    def times(x: (Double, Double), y: (Double, Double)): (Double, Double) = (x._1 * y._1, x._2 * y._2)
  //    def toDouble(x: (Double, Double)): Double = ???
  //    def toFloat(x: (Double, Double)): Float = ???
  //    def toInt(x: (Double, Double)): Int = ???
  //    def toLong(x: (Double, Double)): Long = ???
  //    def compare(x: (Double, Double), y: (Double, Double)): Int = ???
  //
  //  }

  def measure[T](context: Context, measurements: Int, setup: T => Any,
                 tear: T => Any, regen: () => T, snippet: T => Any): Seq[Quantity[(Double, Double)]] = {
    var iteration = 0
    val tm = collection.mutable.ListBuffer.empty[Quantity[(Double, Double)]]
    var value = regen()

    while (iteration < measurements) {
      if (iteration % 20 == 0) org.scalameter.log.info(s"Iteration $iteration")
      value = valueAt(context, iteration, regen, value)
      setup(value)

      val (_, (t, m)) = realSnippet(value.asInstanceOf[S])

      tear(value)

      tm += Quantity((t * 1e-9, m.toDouble / (1024 * 1024)), "(s, MiB)")
      iteration += 1
    }

    org.scalameter.log.verbose(s"measurements: ${tm.mkString(", ")}")

    tm.result() //.zip(mems.result())
  }

}
