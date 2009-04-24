package ntdice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cspfj.MGACIter;
import cspfj.ResultHandler;
import cspfj.constraint.AbstractAC3Constraint;
import cspfj.constraint.Constraint;
import cspfj.constraint.Constraint;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.AC3WSlowQueue;
import cspfj.heuristic.CrossHeuristic;
import cspfj.heuristic.DDegOnDom;
import cspfj.heuristic.Heuristic;
import cspfj.heuristic.Lexico;
import cspfj.heuristic.VariableHeuristic;
import cspfj.problem.Problem;
import cspfj.problem.ProblemGenerator;
import cspfj.problem.Variable;

public class NTDice implements ProblemGenerator {

	private final int nbDies, nbFaces, nbValues, minOcc;

	// private final boolean equalProb;

	private final Variable[][] dies;
	private final Variable[][][] matrices;

	private final Collection<Constraint> constraints;

	public NTDice(int nbDies, int nbFaces, int nbValues, int minOcc) {
		this.nbDies = nbDies;
		this.nbFaces = nbFaces;
		this.nbValues = nbValues;

		this.minOcc = minOcc;// (int)Math.ceil(minProb*nbFaces*nbFaces);

		// System.out.println(minOcc + " occurrences");

		dies = new Variable[nbDies][nbFaces];
		matrices = new Variable[nbDies][nbFaces][nbFaces];
		constraints = new ArrayList<Constraint>();
	}

	@Override
	public void generate() throws FailedGenerationException {

		final int[] values = new int[nbValues];
		for (int i = nbValues; --i >= 0;) {
			values[i] = i + 1;
		}

		final Variable[] allFaces = new Variable[nbDies * nbFaces];

		for (int i = nbDies; --i >= 0;) {
			for (int j = nbFaces; --j >= 0;) {
				if (i == 0 && j == 0) {
					dies[i][j] = new Variable(new int[] { 1 });
				} else {
					dies[i][j] = new Variable(values);
				}
				allFaces[i * nbFaces + j] = dies[i][j];
			}
		}

		// for (int i = nbDies ; --i>=0;) {
		// for (int j = nbFaces-1;--j>=0;) {
		// constraints.add(
		// new Leq(dies[i][j], dies[i][j+1]));
		// }
		// }

		for (int i = nbDies; --i >= 1;) {
			constraints.add(new LexLeq(dies[0], dies[i]));
		}

		final int[] binary = { 0, 1 };
		for (int k = nbDies; --k >= 0;) {
			for (int i = nbFaces; --i >= 0;) {
				for (int j = nbFaces; --j >= 0;) {
					matrices[k][i][j] = new Variable(binary);
				}
			}
		}

		for (int k = nbDies - 1; --k >= 0;) {
			for (int i = nbFaces; --i >= 0;) {
				for (int j = nbFaces; --j >= 0;) {
					constraints.add(new AbstractAC3Constraint(new Variable[] {
							dies[k][i], dies[k + 1][j], matrices[k][i][j] }) {

						public boolean check() {
							return getValue(2) == (getValue(0) > getValue(1) ? 1
									: 0);
						}
						
						public boolean isSlow() {
							return false;
						}
						
					});
				}
			}
		}

		for (int i = nbFaces; --i >= 0;) {
			for (int j = nbFaces; --j >= 0;) {
				constraints.add(new AbstractAC3Constraint(new Variable[] {
						dies[nbDies - 1][i], dies[0][j],
						matrices[nbDies - 1][i][j] }) {

					public boolean check() {
						return getValue(2) == (getValue(0) > getValue(1) ? 1
								: 0);
					}
					
					public boolean isSlow() {
						return false;
					}

				});
			}
		}

		for (int k = nbDies; --k >= 0;) {
			final Variable[] scope = new Variable[nbFaces * nbFaces];
			for (int i = nbFaces; --i >= 0;) {
				for (int j = nbFaces; --j >= 0;) {
					scope[i * nbFaces + j] = matrices[k][i][j];
				}
			}
			constraints.add(new OccurrenceGeq(scope, 1, minOcc));

			for (int i = nbFaces; --i >= 0;) {
				for (int j = nbFaces - 1; --j >= 0;) {
					constraints.add(new Leq(matrices[k][i][j + 1],
							matrices[k][i][j]));
				}
			}

			for (int i = nbFaces; --i >= 0;) {
				for (int j = nbFaces - 1; --j >= 0;) {
					constraints.add(new Leq(matrices[k][j][i],
							matrices[k][j + 1][i]));
				}
			}
		}
	}

	@Override
	public Collection<Constraint> getConstraints() {
		return constraints;
	}

	@Override
	public List<Variable> getVariables() {
		final List<Variable> vars = new ArrayList<Variable>(nbDies * nbFaces
				* (1 + nbFaces));
		for (Variable[] d : dies) {
			vars.addAll(Arrays.asList(d));
		}
		for (Variable[][] m1 : matrices) {
			for (Variable[] m2 : m1) {
				vars.addAll(Arrays.asList(m2));
			}
		}

		return vars;
	}

	public String solution(Map<Variable, Integer> solution) {
		final StringBuilder stb = new StringBuilder();

		for (int i = 0; i < nbDies; i++) {
			stb.append("Die ").append(i + 1).append(" : ");

			for (Variable v : dies[i]) {
				stb.append(solution.get(v)).append(" ");
			}

			stb.append("\n");

			// for (Variable[] m : matrices[i]) {
			// for (Variable v : m) {
			// stb.append(solution.get(v)).append(" ");
			// }
			// stb.append("\n");
			// }
		}

		return stb.toString();
	}

	public static void main(String[] args) throws FailedGenerationException,
			IOException {
		Logger.getLogger("").getHandlers()[0].setLevel(Level.WARNING);
		Logger.getLogger("").setLevel(Level.WARNING);

		final int nbFaces = Integer.valueOf(args[1]);

		int ub = nbFaces * nbFaces;
		int lb = 1 + ub / 2;

		int totalNodes = 0;
		float totalTime = 0;

		while (ub > lb) {
			int test = 1 + (ub + lb) / 2;
			System.out.println("Testing " + test + " occurrences ("
					+ (100 * test / (nbFaces * nbFaces) + " %)"));
			final NTDice ntdice = new NTDice(Integer.valueOf(args[0]), Integer
					.valueOf(args[1]), Integer.valueOf(args[2]), test);
			final Problem problem = Problem.load(ntdice, 0);
			final VariableHeuristic variableHeuristic = new DDegOnDom(problem);
			final Heuristic heuristic = new CrossHeuristic(variableHeuristic, new Lexico(
					problem, false));
			final MGACIter solver = new MGACIter(problem, new ResultHandler(),
					heuristic, new AC3WSlowQueue(problem, variableHeuristic));
			// solver.setAllSolutions(true);

			final boolean result = solver.runSolver();

			System.out.println(solver.getNbAssignments() + " nodes");
			System.out.println(solver.getUserTime() + " s");
			totalNodes += solver.getNbAssignments();
			totalTime += solver.getUserTime();
			if (!result) {
				System.out.println("No solution found");
				ub = test - 1;
			} else {
				System.out.println(ntdice.solution(solver.getSolution()));
				lb = test;
			}

		}

		System.out.println("Best : " + lb + " occurrences ("
				+ (100 * (float) lb / (nbFaces * nbFaces)) + " %)");
		System.out.println("Total nodes : " + totalNodes);
		System.out.println("Total time : " + totalTime);
	}
}
