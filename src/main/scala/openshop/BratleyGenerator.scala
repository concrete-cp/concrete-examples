package openshop

object BratleyGenerator {
  private val MAX = 99;
  def randMatrix(seed: Int, x: Int, y: Int): Array[Array[Int]] = {
    val gen = new BratleyGenerator(seed)
    val matrix = Array.ofDim[Int](x, y)

    for (i <- 0 until x; j <- 0 until y) {
      matrix(i)(j) = gen.nextRand(1, MAX)
    }
    matrix;
  }

  def randShuffle(seed: Int, x: Int, y: Int): Array[Array[Int]] = {
    val gen = new BratleyGenerator(seed)
    val matrix = Array.fill(x)((0 until y).toArray)

    for (i <- 0 until x; j <- 0 until y) {
      val s = gen.nextRand(j, y - 1);
      val t = matrix(i)(j);
      matrix(i)(j) = matrix(i)(s);
      matrix(i)(s) = t;
    }

    matrix;
  }
}

final class BratleyGenerator(var seed: Int = 1) {

  private val A = 16807;
  private val B = 127773;
  private val C = 2836;
  private val M = (0x1 << 31) - 1;

  private def nextRand(): Double = {
    var rand = A * (seed % B) - (seed / B) * C;
    if (rand < 0) {
      rand += M;
    }
    seed = rand;
    rand.toDouble / M;
  }

  private def nextRand(a: Int, b: Int): Int = {
    math.floor(a + nextRand() * (b - a + 1)).toInt
  }

}