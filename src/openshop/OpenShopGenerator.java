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

import cspfj.constraint.Constraint;
import cspfj.constraint.DTPConstraint;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.ProblemGenerator;
import cspfj.problem.Variable;

public class OpenShopGenerator implements ProblemGenerator {

	private int[][] durations;

	private Map<Variable, Integer> durationsMap;

	private int ub;

	private int size;

	private Variable[][] variables;

	private final Collection<Constraint> constraints;

	// private final String filename;

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
				} else {
					final String[] values = line.split(" +");
					for (int i = 0; i < values.length; i++) {
						durations[ln - 1][i] = Integer.parseInt(values[i]);
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

		// for (int[] d :durations) {
		// System.out.println(Arrays.toString(d));
		// }

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
				sumC += durations[j][i];
			}
			max = max(max, max(sumL, sumC));
		}
		return max;
	}

	public final int getUB() {
		int maxL = 0;
		int maxC = 0;
		for (int i = size; --i >= 0;) {
			int sumL = 0;
			int sumC = 0;
			for (int j = size; --j >= 0;) {
				sumL += durations[i][j];
				sumC += durations[j][i];
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
				variables[i][j] = new Variable(intDomain(ub - durations[i][j]
						+ 1));
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
					constraints.add(new DTPConstraint(new Variable[] {
							variables[l][c1], variables[l][c2] },
							durations[l][c1], durations[l][c2], true));
				}
			}
		}

		// cpt = 0 ;

		for (int c = size; --c >= 0;) {
			for (int l1 = size; --l1 >= 0;) {
				for (int l2 = size; --l2 >= l1 + 1;) {
					constraints.add(new DTPConstraint(new Variable[] {
							variables[l1][c], variables[l2][c] },
							durations[l1][c], durations[l2][c], true));
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

		for (Variable v : solution.keySet()) {
			evaluation = Math.max(evaluation, solution.get(v)
					+ durationsMap.get(v));
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

}
