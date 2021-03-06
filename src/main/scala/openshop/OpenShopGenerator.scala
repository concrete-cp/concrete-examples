/**
 * CSPFJ Competitor - CSP solver using the CSPFJ API for Java
 * Copyright (C) 2006 Julien VION
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package openshop;

import math.max
import concrete.generator.FailedGenerationException
import cspom.CSPOM
import cspom.variable.CSPOMVariable
import scala.collection.mutable.HashMap
import CSPOM._
import cspom.variable.IntVariable
import concrete.CSPOMDriver._
import concrete.Solver

object OpenShopGenerator {
  def apply(filename: String) = {

    var ln = 0;
    var size: Int = -1
    var durations: Array[Array[Int]] = null

    for (
      line <- io.Source.fromFile(filename).getLines if (line.charAt(0) != '!' && line.trim.nonEmpty)
    ) {
      if (ln == 0) {
        size = Integer.parseInt(line);
        durations = Array.ofDim(size, size);
      } else if (ln > 1) {
        val values = line.split(" +");
        for (i <- 0 until values.length) {
          durations(ln - 2)(i) = values(i).toInt
        }
      }
      ln += 1;
    }

    val shuffle = Array.ofDim[Int](size, size)

    for (i <- 0 until size; j <- 0 until size) {
      shuffle(i)(j) = j;
    }

    val js = false;

    new OpenShopGenerator(size, durations, shuffle, js, ub(size, durations, shuffle))
  }

  def apply(size: Int, durSeed: Int, machSeed: Int, js: Boolean) = {
    val durations = BratleyGenerator.randMatrix(durSeed, size, size);

    val shuffle = BratleyGenerator.randShuffle(machSeed, size, size);

    new OpenShopGenerator(size, durations, shuffle, js, ub(size, durations, shuffle))
  }

  def ub(size: Int, durations: Array[Array[Int]], shuffle: Array[Array[Int]]) = {
    val maxL: Int = durations.map(_.sum).max
    val maxC: Int = (0 until size).map(i => (0 until size).map(j => durations(j)(shuffle(j).indexOf(i))).sum).max
    maxL + maxC;
  }

  def flowshop(size: Int, d: Array[Array[Int]],
    ub: Int) = CSPOM { implicit problem =>
    val s = for (i <- 0 until size) yield {
      for (j <- 0 until size) yield {
        IntVariable(0 to ub) as s"V$i.$j"
      }
    }

    val end = IntVariable(0 to ub) as "end"

    for (i <- 0 until size) {
      ctr(s(i)(size - 1) + d(i)(size - 1) <= end)
    }

    for (i <- 0 until size; j <- 1 until size) {
      ctr(s(i)(j) - s(i)(j - 1) >= d(i)(j - 1))
    }

    for (i <- 0 until size; j <- 0 until size; k <- 0 until j) {
      ctr(
        s(j)(i) - s(k)(i) >= d(k)(i) | s(k)(i) - s(j)(i) >= d(j)(i))
    }
  }

  def main(args: Array[String]): Unit = {
    val d = matrix"""
	     4,  8,  6
	     2,  5,  4
	     3,  1,  7
	  """
    val p = flowshop(3, d, 40)
    val s = Solver(p).get
    s.minimize("end")
    val sol = s.toSeq.last
    println("end: " + sol("end"))
    for (i <- 0 until 3) {
      for (j <- 0 until 3) {
        print(sol(s"V$i.$j") + " ")
      }
      println()
    }
  }
}

final class OpenShopGenerator(
  val size: Int,
  val durations: Array[Array[Int]],
  val shuffle: Array[Array[Int]],
  val js: Boolean,
  var ub: Int) {

  var durationsMap: HashMap[String, Int] = _
  var variableNames: Array[Array[String]] = _

  def getLB: Int = {
    val sumL = durations.map(_.sum).max
    val sumC = (0 until size).map(i => (0 until size).map(j => durations(j)(shuffle(j).indexOf(i))).sum).max
    max(sumL, sumC)
  }

  private def dtConstraint(v0: IntVariable, v1: IntVariable, d0: Int, d1: Int)(implicit problem: CSPOM) = {
    diffGe(v0, v1, d0) | diffGe(v1, v0, d1)
  }

  private def diffGe(v0: IntVariable, v1: IntVariable, d0: Int)(implicit problem: CSPOM) = {
    (v1 - v0) >= d0
  }

  def generate(): CSPOM = {
    val variables = Array.ofDim[IntVariable](size, size)
    variableNames = Array.ofDim[String](size, size)
    durationsMap = new HashMap()

    CSPOM { implicit problem =>
      for (i <- 0 until size; j <- 0 until size) {
        val name = s"V$i.$j"
        variableNames(i)(j) = name
        if (i == 0 && j == 0 && !js) {
          variables(0)(0) = IntVariable(0 to (ub - durations(i)(j)) / 2) as name
        } else {
          variables(i)(j) = IntVariable(0 to (ub - durations(i)(j))) as name
        }
        durationsMap.put(name, durations(i)(j));
      }

      // L'op�ration j du job i
      // se fait sur la machine machines[i][j]
      // et dure durations[i][j]
      // Les machines ne font qu'une chose � la fois

      for (l <- 0 until size; c1 <- 0 until size; c2 <- 0 until c1) {
        ctr(dtConstraint(variables(l)(c1), variables(l)(c2),
          durations(l)(c1), durations(l)(c2)))
      }

      // cpt = 0 ;

      if (js) {
        for (c <- 0 until size; l1 <- 1 until size) {
          ctr(diffGe(variables(l1 - 1)(shuffle(l1 - 1).indexOf(c)),
            variables(l1)(shuffle(l1).indexOf(c)),
            durations(l1 - 1)(shuffle(l1 - 1).indexOf(c))))

        }

      } else {
        for (c <- 0 until size; l1 <- 0 until size; l2 <- 0 until l1) {
          ctr(dtConstraint(variables(l1)(shuffle(l1).indexOf(c)),
            variables(l2)(shuffle(l2).indexOf(c)),
            durations(l1)(shuffle(l1).indexOf(c)),
            durations(l2)(shuffle(l2).indexOf(c))))
        }
      }
    }
  }

  def evaluate(solution: Map[String, Any]): Int = {
    durationsMap.map {
      case (variable, duration) => solution(variable).asInstanceOf[Int] + duration
    } max
  }

  def display(solution: Map[String, Int]) {
    for (l <- variableNames) {
      println(l.map(solution).mkString(" "))
    }
  }

  override def toString = {
    val stb = new StringBuilder();
    stb.append("Durations:\n");
    for (d <- durations) {
      d.addString(stb, "[", ", ", "]\n")
    }
    stb.append("Machines:\n");
    for (m <- shuffle) {
      m.addString(stb, "[", ", ", "]\n")
    }
    stb.toString
  }

}
