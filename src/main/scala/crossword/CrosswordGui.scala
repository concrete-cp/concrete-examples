package crossword;

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.util.Random
import java.util.Set

import cspfj.constraint.AbstractConstraint
import cspfj.generator.FailedGenerationException
import cspfj.problem.Problem
import cspfj.problem.Variable
import cspfj.ParameterManager
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants

case class Cell(x: Int, y: Int)

class CrosswordGui(x: Int, y: Int) {
  ParameterManager("logger.level") = "INFO"
  val RAND = new Random(0)
  val frame = new JFrame
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  frame.getContentPane().setLayout(new BorderLayout());

  val grid = new JPanel
  grid.setLayout(new GridLayout(x, y))

  val cell = (0 until x) map { _ => (0 until y) map { _ => new JTextField } }

  cell.flatten.foreach { f =>
    f.setColumns(2)
    f.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
    f.setHorizontalAlignment(SwingConstants.CENTER);
    f.setFont(f.getFont().deriveFont(25F));

  }

  cell.flatten.foreach(grid.add)

  val black = (for (i <- 0 until x; j <- 0 until y; if RAND.nextFloat < .15) yield Cell(i, j)).toSet
  black.foreach(c => cell(c.x)(c.y).setBackground(Color.BLACK))

  frame.getContentPane().add(grid, BorderLayout.CENTER);

  val start = new JButton("start");

  start.addActionListener(new ActionListener() {
    override def actionPerformed(e: ActionEvent) { solve() }

  });

  frame.getContentPane().add(start, BorderLayout.SOUTH);

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

    new CrosswordGui(12, 12);

  }
}
