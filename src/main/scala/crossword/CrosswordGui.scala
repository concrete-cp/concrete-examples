package crossword;

import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import java.util.HashSet
import java.util.List
import java.util.Random
import java.util.Set
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextField
import cspfj.constraint.AbstractConstraint
import cspfj.exception.FailedGenerationException
import cspfj.filter.RevisionHandler
import cspfj.problem.Problem
import cspfj.problem.Variable
import cspfj.util.BitVector;
import javax.swing.SwingConstants

case class Cell(x: Int, y: Int)

class CrosswordGui(x: Int, y: Int) {
  val RAND = new Random
  val frame = new JFrame
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  frame.getContentPane().setLayout(
    new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

  val grid = new JPanel
  grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS))

  val cell = (0 until x) map { _ => (0 until y) map { _ => new JTextField } }

  cell.flatten.foreach { f =>
    f.setColumns(2)
    f.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
    f.setHorizontalAlignment(SwingConstants.CENTER);
    f.setFont(f.getFont().deriveFont(25F));

  }

  cell.foreach { r =>
    val row = new JPanel
    r.foreach(row.add)
    grid.add(row)
  }

  val black = (for (i <- 0 until x; j <- 0 until y; if RAND.nextFloat < .15) yield Cell(i, j)).toSet
  black.foreach(c => cell(c.x)(c.y).setBackground(Color.BLACK))

  frame.getContentPane().add(grid);

  val start = new JButton("start");

  start.addActionListener(new ActionListener() {
    override def actionPerformed(e: ActionEvent) { solve() }

  });

  frame.getContentPane().add(start);

  // Display the window.
  frame.pack();

  frame.setVisible(true)

  var problem: Problem = null
  var crossword: CrosswordGenerator = null

  def solve() {
    crossword = new CrosswordGenerator(x, y, black);
    this.problem = crossword.generate();
    problem.addConstraint(new VisuConstraint(problem.variables.toArray, this))
    new CrosswordResolver(problem).start();
  }

}

object CrosswordGui {

  def main(args: Array[String]) {

    new CrosswordGui(10, 10);

  }
}
