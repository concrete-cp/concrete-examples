package crossword;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.constraint.Constraint;
import cspfj.constraint.extension.ExtensionConstraintDynamic;
import cspfj.constraint.extension.TupleHashSet;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;
import cspfj.problem.ProblemGenerator;
import cspfj.problem.Variable;

/*
 * Created on 20 mai 08
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class Crossword implements ProblemGenerator {

	private final Collection<Constraint> constraints;

	private final Variable[][] variables;

	private final int x;

	private final int y;

	public Crossword(final int x, final int y) {
		variables = new Variable[x][y];
		this.x = x;
		this.y = y;
		constraints = new ArrayList<Constraint>();
	}

	private static Set<String> getDict(final String file, final int x,
			final int y) throws FailedGenerationException {

		final Set<String> dict = new HashSet<String>();

		try {
			final BufferedReader reader = new BufferedReader(new FileReader(
					file));

			String line;

			while ((line = reader.readLine()) != null) {
				final String word = Normalizer.normalize(line.toUpperCase(),
						Normalizer.Form.NFD).replaceAll("[^A-Z]", "");
				if (word.length() == x || word.length() == y) {
					dict.add(word);
				}
			}

		} catch (FileNotFoundException e) {
			throw new FailedGenerationException(e);
		} catch (IOException e) {
			throw new FailedGenerationException(e);
		}

		return dict;
	}

	public static <E> E[][] transpose(final E[][] matrix, final E[][] transposed) {
		for (int i = matrix.length; --i >= 0;) {
			for (int j = matrix[i].length; --j >= 0;) {
				transposed[j][i] = matrix[i][j];
			}
		}

		return transposed;
	}

	@Override
	public void generate() throws FailedGenerationException {
		final int[] domain = new int[26];
		for (int i = domain.length; --i >= 0;) {
			domain[i] = i;
		}

		for (int i = x; --i >= 0;) {
			for (int j = y; --j >= 0;) {
				variables[i][j] = new Variable(domain);
			}
		}

		final TupleHashSet tl = new TupleHashSet(false);
		for (String s : getDict("crossword/french", x, y)) {
			int[] tuple = new int[s.length()];

			for (int i = s.length(); --i >= 0;) {
				tuple[i] = s.charAt(i) - 65;
				assert tuple[i] >= 0 : s;
			}
			tl.set(tuple, true);
		}

		for (Variable[] v : variables) {
			constraints.add(new ExtensionConstraintDynamic(v, tl));
		}

		for (Variable[] v : transpose(variables, new Variable[y][x])) {
			constraints.add(new ExtensionConstraintDynamic(v, tl));
		}
	}

	@Override
	public Collection<Constraint> getConstraints() {
		return constraints;
	}

	@Override
	public List<Variable> getVariables() {
		final List<Variable> variables = new ArrayList<Variable>();
		for (Variable[] v : this.variables) {
			variables.addAll(Arrays.asList(v));
		}
		return variables;
	}

	/**
	 * @param args
	 * @throws FailedGenerationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FailedGenerationException,
			IOException {
		Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
		//Logger.getLogger("").setLevel(Level.FINE);
		final Crossword crossword = new Crossword(Integer.valueOf(args[0]),
				Integer.valueOf(args[1]));
		final Problem problem = Problem.load(crossword);
		final Solver solver = new MGACIter(problem, new ResultDisplayer(crossword.x,
				crossword.y, crossword.variables));
		// solver.setAllSolutions(true);
		if (!solver.runSolver()) {
			System.out.println("No crossword found");
		}
	}

}
