package crossword;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import crossword.CrosswordGui.Cell;
import cspfj.exception.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspfj.problem.Problem;
import cspom.CSPOM;
import cspom.extension.Extension;
import cspom.extension.ExtensionConstraint;
import cspom.variable.CSPOMVariable;

/*
 * Created on 20 mai 08
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class CrosswordGenerator {
	private final CSPOMVariable[][] variables;

	private final int x;

	private final int y;

	private final Set<Cell> black;

	private final Map<Integer, Extension<Integer>> dicts;

	private final Map<String, Cell> map;

	private final CSPOM problem;

	public CrosswordGenerator(final int x, final int y, final Set<Cell> black)
			throws FailedGenerationException {
		variables = new CSPOMVariable[x][y];
		this.x = x;
		this.y = y;
		this.black = black;
		dicts = loadDicts(getClass().getResource("french"), Math.max(x, y));
		map = new HashMap<String, Cell>();
		problem = new CSPOM();

	}

	private static Map<Integer, Extension<Integer>> loadDicts(final URL file,
			int max) throws FailedGenerationException {

		final Map<Integer, Extension<Integer>> dicts = new HashMap<Integer, Extension<Integer>>();

		try {
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(file.openStream()));

			String line;

			while ((line = reader.readLine()) != null) {
				final String word = Normalizer.normalize(line.toUpperCase(),
						Normalizer.Form.NFD).replaceAll("[^A-Z]", "");

				if (word.length() < 2 || word.length() > max) {
					continue;
				}

				Extension<Integer> ths = dicts.get(word.length());

				if (ths == null) {
					ths = new Extension<Integer>(word.length(), false);
					dicts.put(word.length(), ths);
				}

				final Integer[] tuple = new Integer[word.length()];

				for (int i = word.length(); --i >= 0;) {
					tuple[i] = word.charAt(i) - 65;
					assert tuple[i] >= 0 : word;
				}
				ths.addTuple(tuple);

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

	public Problem generate() throws FailedGenerationException {

		for (int i = x; --i >= 0;) {
			for (int j = y; --j >= 0;) {
				if (!black.contains(new Cell(i, j))) {
					variables[i][j] = problem.var(0, 25);
					map.put(variables[i][j].getName(), new Cell(i, j));
				}
			}
		}

		final Collection<CSPOMVariable> currentWord = new ArrayList<CSPOMVariable>(
				Math.max(x, y));

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

		return ProblemGenerator.generate(problem);
	}

	private void newWord(Collection<CSPOMVariable> word)
			throws FailedGenerationException {
		if (word.size() >= 2) {
			problem.addConstraint(new ExtensionConstraint<Integer>(dicts
					.get(word.size()), word.toArray(new CSPOMVariable[word
					.size()])));
		}
		word.clear();
	}

	public Cell whatCell(String varName) {
		return map.get(varName);
	}

}
