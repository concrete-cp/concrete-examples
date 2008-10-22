package crossword;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import crossword.CrosswordGui.Cell;
import cspfj.constraint.Constraint;
import cspfj.constraint.extension.ExtensionConstraintDynamic;
import cspfj.constraint.extension.TupleHashSet;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.ProblemGenerator;
import cspfj.problem.Variable;

/*
 * Created on 20 mai 08
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class CrosswordGenerator implements ProblemGenerator {

	private final Collection<Constraint> constraints;

	private final Variable[][] variables;

	private final int x;

	private final int y;

	private final Set<Cell> black;

	private final Map<Integer, TupleHashSet> dicts;

	private final Map<Integer, Cell> map;

	public CrosswordGenerator(final int x, final int y, final Set<Cell> black)
			throws FailedGenerationException {
		variables = new Variable[x][y];
		this.x = x;
		this.y = y;
		constraints = new ArrayList<Constraint>();
		this.black = black;
		dicts = loadDicts("crossword/french", Math.max(x, y));
		map = new HashMap<Integer, Cell>();
	}

	private static Map<Integer, TupleHashSet> loadDicts(final String file,
			int max) throws FailedGenerationException {

		final Map<Integer, TupleHashSet> dicts = new HashMap<Integer, TupleHashSet>();

		try {
			final BufferedReader reader = new BufferedReader(new FileReader(
					file));

			String line;

			while ((line = reader.readLine()) != null) {
				final String word = Normalizer.normalize(line.toUpperCase(),
						Normalizer.Form.NFD).replaceAll("[^A-Z]", "");

				if (word.length() < 2 || word.length() > max) {
					continue;
				}

				TupleHashSet ths = dicts.get(word.length());

				if (ths == null) {
					ths = new TupleHashSet(false);
					dicts.put(word.length(), ths);
				}

				final int[] tuple = new int[word.length()];

				for (int i = word.length(); --i >= 0;) {
					tuple[i] = word.charAt(i) - 65;
					assert tuple[i] >= 0 : word;
				}
				ths.set(tuple, true);

			}

		} catch (FileNotFoundException e) {
			throw new FailedGenerationException(e);
		} catch (IOException e) {
			throw new FailedGenerationException(e);
		}

		return dicts;
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
				if (!black.contains(new Cell(i, j))) {
					variables[i][j] = new Variable(domain);
					map.put(variables[i][j].getId(), new Cell(i, j));
				}
			}
		}

		final Collection<Variable> currentWord = new ArrayList<Variable>(Math
				.max(x, y));

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				if (variables[i][j] == null) {
					newWord(currentWord);
				} else {
					currentWord.add(variables[i][j]);
				}
			}
			newWord(currentWord);
		}

		for (int j = 0; j < y; j++) {
			for (int i = 0; i < x; i++) {
				if (variables[i][j] == null) {
					newWord(currentWord);
				} else {
					currentWord.add(variables[i][j]);
				}
			}
			newWord(currentWord);
		}

	}

	private void newWord(Collection<Variable> word)
			throws FailedGenerationException {
		if (word.size() >= 2) {
			constraints.add(new ExtensionConstraintDynamic(word
					.toArray(new Variable[word.size()]),
					dicts.get(word.size()), true));
		}
		word.clear();
	}

	@Override
	public Collection<Constraint> getConstraints() {
		return constraints;
	}

	@Override
	public List<Variable> getVariables() {
		final List<Variable> variables = new ArrayList<Variable>();
		for (Variable[] vs : this.variables) {
			for (Variable v : vs) {
				if (v != null) {
					variables.add(v);
				}

			}
		}
		return variables;
	}

	public Cell whatCell(int varId) {
		return map.get(varId);
	}

}
