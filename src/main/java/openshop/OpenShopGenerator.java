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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cspfj.exception.FailedGenerationException;
import cspfj.problem.Variable;
import cspom.CSPOM;
import cspom.compiler.PredicateParseException;
import cspom.variable.CSPOMVariable;
import cspom.variable.Interval;

public class OpenShopGenerator {

	private int[][] durations;

	private final int[][] shuffle;

	private CSPOMVariable[][] variables;
	private Map<CSPOMVariable, Integer> durationsMap;
	private CSPOM cspom;

	private int ub;

	private int size;

	private final boolean js;

	public OpenShopGenerator(final String filename) {
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
					durationsMap = new HashMap<CSPOMVariable, Integer>(size
							* size);
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
		durations = randMatrix(durSeed, size, size);
		durationsMap = new HashMap<CSPOMVariable, Integer>(size * size);

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

	private void dtConstraint(CSPOMVariable v0, CSPOMVariable v1, int d0, int d1)
			throws PredicateParseException {
		// final Interval<Integer> dom0 = (Interval<Integer>) v0.getDomain();
		// final Interval<Integer> dom1 = (Interval<Integer>) v1.getDomain();
		//
		// final CSPOMVariable diff = cspom.var(dom0.getLb() - dom1.getUb(),
		// dom0
		// .getUb()
		// - dom1.getLb());
		// cspom.ctr("eq(" + diff + ", sub(" + v0 + ", " + v1 + "))");
		cspom.ctr("or(ge(sub(" + v1 + ", " + v0 + "), " + d0 + "), ge(sub("
				+ v0 + ", " + v1 + "), " + d1 + "))");
	}

	private void diffConstraint(CSPOMVariable v0, CSPOMVariable v1, int d0)
			throws PredicateParseException {
		cspom.ctr("ge(sub(" + v1 + ", " + v0 + "), " + d0 + ")");
	}

	public CSPOM generate() throws FailedGenerationException,
			PredicateParseException {
		final int size = getSize();

		variables = new CSPOMVariable[size][size];
		cspom = new CSPOM();

		durationsMap.clear();
		for (int i = size; --i >= 0;) {
			for (int j = size; --j >= 0;) {
				if (i == 0 && j == 0 && !js) {
					variables[0][0] = cspom.var(0, (ub - durations[i][j]) / 2);
				} else {
					variables[i][j] = cspom.var(0, ub - durations[i][j]);
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
				for (int c2 = c1; --c2 >= 0;) {
					dtConstraint(variables[l][c1], variables[l][c2],
							durations[l][c1], durations[l][c2]);
				}
			}
		}

		// cpt = 0 ;

		if (js) {
			for (int c = size; --c >= 0;) {
				for (int l1 = size; --l1 >= 1;) {
					diffConstraint(variables[l1 - 1][find(c, shuffle[l1 - 1])],
							variables[l1][find(c, shuffle[l1])],
							durations[l1 - 1][find(c, shuffle[l1 - 1])]);

				}

			}
		} else {
			for (int c = size; --c >= 0;) {
				for (int l1 = size; --l1 >= 0;) {
					for (int l2 = l1; --l2 >= 0;) {
						dtConstraint(variables[l1][find(c, shuffle[l1])],
								variables[l2][find(c, shuffle[l2])],
								durations[l1][find(c, shuffle[l1])],
								durations[l2][find(c, shuffle[l2])]);
					}
				}
			}
		}
		return cspom;
	}

	public int evaluate(final Map<String, Integer> solution) {
		int evaluation = 0;

		for (Entry<String, Integer> e : solution.entrySet()) {
			final Integer duration = durationsMap.get(cspom.getVariable(e
					.getKey()));
			if (duration != null) {
				evaluation = Math.max(evaluation, e.getValue() + duration);
			}
		}

		return evaluation;
	}

	public void display(final Map<String, Integer> solution) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				System.out.print(solution.get(variables[i][j].getName()));
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
		stb.append("Machines:\n");
		for (int[] m : shuffle) {
			stb.append(Arrays.toString(m)).append("\n");
		}
		return stb.toString();
	}

	private final static int A = 16807;
	private final static int B = 127773;
	private final static int C = 2836;
	private final static int M = (0x1 << 31) - 1;
	private static int seed = 1;
	private static final int MAX = 99;

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
				matrix[i][j] = nextRand(1, MAX);
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
