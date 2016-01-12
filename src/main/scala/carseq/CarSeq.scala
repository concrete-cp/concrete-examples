package carseq

import java.net.URL
import concrete.CSPOMDriver._
import concrete.Variable
import concrete.runner.CSPOMRunner
import cspom.CSPOM
import cspom.CSPOM._
import cspom.CSPOMConstraint
import cspom.variable.CSPOMExpression
import cspom.variable.CSPOMVariable
import cspom.variable.IntVariable
import cspom.variable.CSPOMSeq
import scala.util.Try

object CarSeq extends CSPOMRunner with App {
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

  var carNames: IndexedSeq[String] = _

  var optionNames: IndexedSeq[IndexedSeq[String]] = _

  override def loadCSPOM(args: List[String], opt: Map[Symbol, Any]) = Try {
    //val url = new URL(args(0))
    val url = getClass().getResource(args(0))
    val source = io.Source.fromURL(url)
    val itr = source.getLines

    val Array(nbCars, nbOptions, nbClasses) = itr.next().split(" ").map(_.toInt)

    val maxCars = itr.next().split(" ").map(_.toInt)
    val blockSizes = itr.next().split(" ").map(_.toInt)

    var classes = List[Seq[Int]]()
    val quantities = Array.ofDim[Int](nbClasses)
    for (options <- itr.map(_.split(" "))) {
      val i = options(0).toInt
      quantities(i) = options(1).toInt
      classes ::= (i +: options.drop(2).map(_.toInt))
    }

    CSPOM { implicit problem =>
      val (cars, cn) = (0 until nbCars).map(c => IntVariable(0 until nbClasses) withName s"car$c").unzip

      carNames = cn
      val oc = cars.zipWithIndex map {
        case (cv, c) =>
          val vars = (0 until nbOptions) map (o => IntVariable(0, 1) withName s"car${c}option$o")
          ctr((cv +: vars.map(_._1)) in classes)
          vars
      }
      val (options, on) = oc.map(_.unzip).unzip

      optionNames = on

      for (i <- 0 until nbOptions) {
        val cardinality = classes.map(c => quantities(c(0)) * c(i + 1)).sum
        //println(cardinality)
        ctr(CSPOMConstraint('slidingSum)(0, maxCars(i), blockSizes(i), CSPOMSeq(options.map(o => o(i)), 0 until options.length)))
        //ctr(sum(options.map(_(i)): _*) === cardinality)
        //sequenceBDD(options.map(_(i)), maxCars(i), blockSizes(i), cardinality)
      }

      ctr(gcc(quantities.zipWithIndex.map {
        case (q, i) => (i, q, q)
      }, cars: _*))
    }
  }

  def sequence(cp: CSPOM, vars: IndexedSeq[IntVariable], u: Int, q: Int, cardinality: Int)(implicit problem: CSPOM) {
    for (i <- 0 to vars.size - q) {
      ctr(sum(vars.slice(i, i + q): _*) <= u)
    }
  }

  def controlCSPOM(solution: Map[String, Any]): Option[String] = ???
  def description(args: List[String]) = args.head

  override def outputCSPOM(solution: Map[String, Any]): String = {
    (carNames zip optionNames) map {
      case (c, o) => solution(c) + " " + o.map(p => solution(p)).mkString(" ")
    } mkString ("\n")
  }

  run(args)

}
