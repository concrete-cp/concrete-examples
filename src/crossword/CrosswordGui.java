package crossword;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;

public class CrosswordGui {

	final JFrame frame;

	final JPanel grid;

	final JTextField[][] cell;

	final int x, y;

	final Set<Cell> black;

	private final static Random RAND = new Random();

	private CrosswordGenerator crossword;

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
				cell[i][j].setColumns(1);
				cell[i][j].setBorder(BorderFactory.createMatteBorder(1, 1, 1,
						1, Color.BLACK));
				cell[i][j].setHorizontalAlignment(JTextField.CENTER);
				cell[i][j].setFont(cell[i][j].getFont().deriveFont(25F));
				row.add(cell[i][j]);

				if (RAND.nextFloat() < .1) {
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
		Logger.getLogger("").addHandler(new Visualize());
		Logger.getLogger("").setLevel(Level.FINE);
		crossword = new CrosswordGenerator(x, y, black);
		final Problem problem = Problem.load(crossword);
		new CrosswordResolver(problem).start();
	}

	public static void main(String args[]) {

		new CrosswordGui(10, 10);

	}

	private class Visualize extends Handler {

		private final Pattern pattern = Pattern
				.compile("(\\d*) : X(\\d*)\\[\\d*\\] <- (\\d*)");

		private final int[] history;
		private int last = -1;

		public Visualize() {
			history = new int[x * y];
		}

		@Override
		public void close() throws SecurityException {
			// TODO Auto-generated method stub

		}

		@Override
		public void flush() {
			// TODO Auto-generated method stub

		}

		@Override
		public void publish(LogRecord arg0) {
			if ("mac".equals(arg0.getSourceMethodName())) {
				Matcher m = pattern.matcher(arg0.getMessage());
				if (m.find()) {
					int level = Integer.valueOf(m.group(1));
					int var = Integer.valueOf(m.group(2));
					int val = Integer.valueOf(m.group(3));

					for (int i = level; i <= last; i++) {
						setCell(history[i], -1);
					}
					setCell(var, val);
					last = level;
					history[last] = var;
				}
			}
		}

		private void setCell(int number, int value) {
			final Cell varCell = crossword.whatCell(number);
			if (value < 0) {
				cell[varCell.x][varCell.y].setText("");
			} else {
				cell[varCell.x][varCell.y].setText(String
						.valueOf((char) (value + 65)));
			}
			// cell[number / y][number % y].repaint();
			// frame.repaint();
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
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
