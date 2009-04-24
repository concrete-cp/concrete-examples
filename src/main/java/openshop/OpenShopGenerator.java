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

import static java.lang.Math.max;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cspfj.constraint.Constraint;
import cspfj.constraint.semantic.DTConstraint;
import cspfj.constraint.semantic.DiffConstraint;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.ProblemGenerator;
import cspfj.problem.Variable;

public class OpenShopGenerator implements ProblemGenerator {

	private int[][] durations;

	private final int[][] shuffle;

	private Map<Variable, Integer> durationsMap;

	private int ub;

	private int size;

	private Variable[][] variables;

	private final Collection<Constraint> constraints;

	private final boolean js;

	public OpenShopGenerator(final String filename) {
		constraints = new ArrayList<Constraint>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));

			int ln = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.charAt(0) == '!' || line.trim().isEmpty()) {
					continue;
				}

				if (ln == 0) {
					size = Integer.parseInt(line);
					durations = new int[size][size];
					durationsMap = new HashMap<Variable, Integer>(size * size);
				} else if (ln > 1) {
					final String[] values = line.split(" +");
					for (int i = 0; i < values.length; i++) {
						durations[ln - 2][i] = Integer.parseInt(values[i]);
					}
				}
				ln++;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		shuffle = new int[size][size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				shuffle[i][j] = j;
			}
		}

		ub = getUB();
		js = false;
	}

	public OpenShopGenerator(final int size, final int durSeed,
			final int machSeed, final boolean js) {
		constraints = new ArrayList<Constraint>();

		durations = randMatrix(durSeed, size, size);
		durationsMap = new HashMap<Variable, Integer>(size * size);

		shuffle = randShuffle(machSeed, size, size);

		this.js = js;

		this.size = size;

		ub = getUB();
	}

	public int[][] getDurations() {
		return durations;
	}

	public void setUB(final int ub) {
		this.ub = ub;
	}

	public void factor(final float factor) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				durations[i][j] = (int) Math.ceil(durations[i][j] * factor);
			}
		}
	}

	public int getLB() {
		int max = 0;
		for (int i = size; --i >= 0;) {
			int sumL = 0;
			int sumC = 0;
			for (int j = size; --j >= 0;) {
				sumL += durations[i][j];
				sumC += durations[j][find(i, shuffle[j])];
			}
			max = max(max, max(sumL, sumC));
		}
		return max;
	}

	public static int find(int value, int[] array) {
		for (int i = array.length; --i >= 0;) {
			if (array[i] == value) {
				return i;
			}
		}
		return -1;
	}

	public final int getUB() {
		int maxL = 0;
		int maxC = 0;
		for (int i = size; --i >= 0;) {
			int sumL = 0;
			int sumC = 0;
			for (int j = size; --j >= 0;) {
				sumL += durations[i][j];
				sumC += durations[j][find(i, shuffle[j])];
			}
			maxL = max(maxL, sumL);
			maxC = max(maxC, sumC);
		}
		return maxL + maxC;
	}

	public int getSize() {
		return size;
	}

	public static int[] intDomain(final int end) {
		final int[] domain = new int[end];
		for (int k = end; --k >= 0;) {
			domain[k] = k;
		}
		return domain;
	}

	public void generate() throws FailedGenerationException {
		final int size = getSize();

		variables = new Variable[size][size];
		constraints.clear();

		durationsMap.clear();
		for (int i = size; --i >= 0;) {
			for (int j = size; --j >= 0;) {
				if (i == 0 && j == 0 && js) {
					variables[0][0] = new Variable(intDomain((ub
							- durations[i][j] + 2) / 2));
				} else {
					variables[i][j] = new Variable(intDomain(ub
							- durations[i][j] + 1));
				}
				durationsMap.put(variables[i][j], durations[i][j]);
			}
		}

		// L'op�ration j du job i
		// se fait sur la machine machines[i][j]
		// et dure durations[i][j]
		// Les machines ne font qu'une chose � la fois

		for (int l = size; --l >= 0;) {
			for (int c1 = size; --c1 >= 0;) {
				for (int c2 = size; --c2 >= c1 + 1;) {
					constraints.add(new DTConstraint(new Variable[] {
							variables[l][c1], variables[l][c2] },
							durations[l][c1], durations[l][c2], true));
				}
			}
		}

		// cpt = 0 ;

		if (js) {
			for (int c = size; --c >= 0;) {
				for (int l1 = size; --l1 >= 1;) {
					constraints.add(new DiffConstraint(new Variable[] {
							variables[l1 - 1][find(c, shuffle[l1 - 1])],
							variables[l1][find(c, shuffle[l1])] },
							durations[l1 - 1][find(c, shuffle[l1 - 1])], true));

				}

			}
		} else {
			for (int c = size; --c >= 0;) {
				for (int l1 = size; --l1 >= 0;) {
					for (int l2 = size; --l2 >= l1 + 1;) {
						constraints.add(new DTConstraint(new Variable[] {
								variables[l1][find(c, shuffle[l1])],
								variables[l2][find(c, shuffle[l2])] },
								durations[l1][find(c, shuffle[l1])],
								durations[l2][find(c, shuffle[l2])], true));
					}
				}
			}
		}
	}

	public List<Variable> getVariables() {
		final List<Variable> vars = new ArrayList<Variable>(size * size);
		for (Variable[] v : variables) {
			vars.addAll(Arrays.asList(v));
		}
		return vars;
	}

	public Collection<Constraint> getConstraints() {
		return constraints;
	}

	public int evaluate(final Map<Variable, Integer> solution) {
		int evaluation = 0;

		// final StringBuffer sb = new StringBuffer();

		for (Entry<Variable, Integer> e : solution.entrySet()) {
			evaluation = Math.max(evaluation, e.getValue()
					+ durationsMap.get(e.getKey()));
			// sb.append(v).append(" (").append(durationsMap.get(v))
			// .append(") : ").append(solution.get(v)).append('-').append(
			// solution.get(v) + durationsMap.get(v)).append('\n');
		}
		// logger.info(sb.toString());

		return evaluation;
	}

	public void display(final Map<Variable, Integer> solution) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				System.out.print(solution.get(variables[i][j]));
				System.out.print(" ");
			}
			System.out.println();
		}
	}

	public String toString() {
		final StringBuilder stb = new StringBuilder();
		stb.append("Durations:\n");
		for (int[] d : durations) {
			stb.append(Arrays.toString(d)).append("\n");
		}
		return stb.toString();
	}

	private final static int A = 16807;
	private final static int B = 127773;
	private final static int C = 2836;
	private final static int M = (0x1 << 31) - 1;
	private static int seed = 1;

	private static double nextRand() {
		int rand = A * (seed % B) - (seed / B) * C;
		if (rand < 0) {
			rand += M;
		}
		seed = rand;
		return (double) rand / M;
	}

	private static int nextRand(int a, int b) {
		return (int) Math.floor(a + nextRand() * (b - a + 1));
	}

	private static int[][] randMatrix(int seed, int x, int y) {
		OpenShopGenerator.seed = seed;
		int[][] matrix = new int[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				matrix[i][j] = nextRand(1, 99);
			}
		}
		return matrix;
	}

	private static int[][] randShuffle(int seed, int x, int y) {
		OpenShopGenerator.seed = seed;
		int[][] matrix = new int[x][y];

		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				matrix[i][j] = j;
			}
			for (int j = 0; j < y; j++) {
				final int s = nextRand(j, y - 1);
				final int t = matrix[i][j];
				matrix[i][j] = matrix[i][s];
				matrix[i][s] = t;
			}
		}
		return matrix;
	}
}
