package crossword;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cspfj.constraint.AbstractArcGrainedConstraint;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.RevisionHandler;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspfj.util.BitVector;

public class CrosswordGui {

	final JFrame frame;

	final JPanel grid;

	final JTextField[][] cell;

	final int x, y;

	final Set<Cell> black;

	private final static Random RAND = new Random();

	private CrosswordGenerator crossword;

	private Problem problem;

	public CrosswordGui(int x, int y) {
		this.x = x;
		this.y = y;
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(
				new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		grid = new JPanel();
		grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));

		cell = new JTextField[x][y];

		black = new HashSet<Cell>();

		for (int i = 0; i < x; i++) {
			JPanel row = new JPanel();
			row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
			for (int j = 0; j < y; j++) {
				cell[i][j] = new JTextField();
				cell[i][j].setColumns(2);
				cell[i][j].setBorder(BorderFactory.createMatteBorder(1, 1, 1,
						1, Color.BLACK));
				cell[i][j].setHorizontalAlignment(JTextField.CENTER);
				cell[i][j].setFont(cell[i][j].getFont().deriveFont(25F));
				row.add(cell[i][j]);

				if (RAND.nextFloat() < .15) {
					black.add(new Cell(i, j));
				}
			}
			grid.add(row);
		}

		for (Cell c : black) {
			cell[c.x][c.y].setBackground(Color.BLACK);
		}

		frame.getContentPane().add(grid);

		JButton start = new JButton("start");

		start.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					solve();
				} catch (FailedGenerationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		});

		frame.getContentPane().add(start);

		// Display the window.
		frame.pack();

		frame.setVisible(true);

	}

	public void solve() throws FailedGenerationException, IOException {
		crossword = new CrosswordGenerator(x, y, black);
		this.problem = crossword.generate();
		problem.addConstraint(new VisuConstraint(problem.getVariables()));
		problem.prepareConstraints();
		new CrosswordResolver(problem).start();
	}

	public static void main(String args[]) {

		new CrosswordGui(10, 10);

	}

	private class VisuConstraint extends AbstractArcGrainedConstraint {

		private int level;
		private final BitVector[] modified;

		public VisuConstraint(Variable[] variables) {
			super(variables);
			modified = new BitVector[variables.length];
			modified[0] = BitVector.factory(getArity(), false);
		}

		private void setCell(String name, int value) {
			final Cell varCell = crossword.whatCell(name);
			if (value < 0) {
				cell[varCell.x][varCell.y].setText("");
			} else {
				cell[varCell.x][varCell.y].setText(String
						.valueOf((char) (value + 65)));
			}
			cell[varCell.x][varCell.y].setForeground(Color.getHSBColor(2f
					* level / getArity(), 1, 1));
		}

		private void setCellNum(String name, int value) {
			final Cell varCell = crossword.whatCell(name);
			if (value < 0) {
				cell[varCell.x][varCell.y].setText("");
			} else {
				cell[varCell.x][varCell.y].setText(String.valueOf(value));
			}
			cell[varCell.x][varCell.y].setForeground(Color.getHSBColor(2f
					* level / getArity(), 1, 1));
		}

		@Override
		public boolean check() {
			return true;
		}

		@Override
		public int getEvaluation(int reviseCount) {
			return Integer.MAX_VALUE;
		}

		@Override
		public void setLevel(int level) {
			this.level = level;
			if (modified[level] == null) {
				modified[level] = BitVector.factory(getArity(), false);
			}
		}

		@Override
		public void restore(int level) {
			for (int l = this.level; --l >= level;) {
				final BitVector modified = this.modified[l];
				this.level = level;
				for (int i = modified.nextSetBit(0); i >= 0; i = modified
						.nextSetBit(i + 1)) {
					final Variable v = getVariable(i);
					if (v.getDomainSize() == 1) {
						setCell(v.getName(), v.getValue(v.getFirst()));
					} else {
						setCellNum(v.getName(), v.getDomainSize());
					}
				}
				modified.fill(false);
			}

		}

		@Override
		public boolean revise(RevisionHandler revisator, int reviseCount) {
			for (int i = getArity(); --i >= 0;) {
				if (getRemovals(i) >= reviseCount) {
					modified[level].set(i);
					final Variable v = getVariable(i);
					if (v.getDomainSize() == 1) {
						setCell(v.getName(), v.getValue(v.getFirst()));
					} else {
						setCellNum(v.getName(), v.getDomainSize());
					}
				}
			}

			return true;
		}
	}

	public static class Cell {
		private final int x, y;

		public Cell(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int hashCode() {
			return 1000 * y + x;
		}

		public boolean equals(Object object) {
			if (object instanceof Cell) {
				final Cell cell = (Cell) object;
				return cell.x == x && cell.y == y;
			}
			return false;
		}

		public String toString() {
			return "(" + x + ", " + y + ")";
		}

	}
}
