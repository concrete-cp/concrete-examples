package xcsp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import cspfj.MGACIter;
import cspfj.constraint.Constraint;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.AC3;
import cspfj.filter.AC3Constraint;
import cspfj.generator.ProblemGenerator;
import cspfj.heuristic.CrossHeuristic;
import cspfj.heuristic.DDegOnDom;
import cspfj.heuristic.Lexico;
import cspfj.priorityqueues.BinaryHeap;
import cspfj.priorityqueues.BinomialHeap;
import cspfj.priorityqueues.FibonacciHeap;
import cspfj.priorityqueues.Fifo;
import cspfj.priorityqueues.Key;
import cspfj.priorityqueues.SoftHeap;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspom.CSPParseException;
import cspom.compiler.ProblemCompiler;
import cspom.constraint.CSPOMConstraint;

public class XCSPSolver {
	private static int count(final String name) throws CSPParseException,
			IOException, FailedGenerationException {
		final cspom.CSPOM cspomProblem = cspom.CSPOM.load(name);
		ProblemCompiler.compile(cspomProblem);
		final Problem problem = ProblemGenerator.generate(cspomProblem);
		// System.out.println(problem);

		final MGACIter solver = new MGACIter(problem);

		int count = 0;
		for (;; count++) {
			final Map<String, Integer> solution = solver.nextSolution();

			if (solution == null) {
				break;
			}
			final Map<String, Number> numSolution = new LinkedHashMap<String, Number>(
					solution.size());
			for (Entry<String, Integer> e : solution.entrySet()) {
				numSolution.put(e.getKey(), e.getValue());
			}
			final Collection<CSPOMConstraint> failed = cspomProblem
					.control(numSolution);
			if (!failed.isEmpty()) {
				throw new IllegalStateException(1 + count + "th solution: "
						+ failed.toString());
			}

		}

		return count;
	}

	private static long solveVar(final String name, final Queue<Variable> queue)
			throws CSPParseException, IOException, FailedGenerationException {
		final cspom.CSPOM cspomProblem = cspom.CSPOM.load(name);
		ProblemCompiler.compile(cspomProblem);
		final Problem problem = ProblemGenerator.generate(cspomProblem);
		// System.out.println(problem);

		final MGACIter solver = new MGACIter(problem, new CrossHeuristic(
				new DDegOnDom(problem), new Lexico(false)), new AC3(problem,
				queue));
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		long time = -System.currentTimeMillis();
		final Map<String, Integer> solution = solver.nextSolution();
		time += System.currentTimeMillis();
		if (solution == null) {
			return time;
		}
		final Map<String, Number> numSolution = new LinkedHashMap<String, Number>(
				solution.size());
		for (Entry<String, Integer> e : solution.entrySet()) {
			numSolution.put(e.getKey(), e.getValue());
		}
		final Collection<CSPOMConstraint> failed = cspomProblem
				.control(numSolution);
		if (!failed.isEmpty()) {
			throw new IllegalStateException(failed.toString());
		}

		return time;
	}

	private static long solveCons(final String name,
			final Queue<Constraint> queue) throws CSPParseException,
			IOException, FailedGenerationException {
		final cspom.CSPOM cspomProblem = cspom.CSPOM.load(name);
		ProblemCompiler.compile(cspomProblem);
		final Problem problem = ProblemGenerator.generate(cspomProblem);
		// System.out.println(problem);

		final MGACIter solver = new MGACIter(problem, new CrossHeuristic(
				new DDegOnDom(problem), new Lexico(false)), new AC3Constraint(
				problem, queue));

		System.gc();
		System.gc();
		System.gc();
		System.gc();
		System.gc();
		long time = -System.currentTimeMillis();
		final Map<String, Integer> solution = solver.nextSolution();
		time += System.currentTimeMillis();
		if (solution == null) {
			return time;
		}
		final Map<String, Number> numSolution = new LinkedHashMap<String, Number>(
				solution.size());
		for (Entry<String, Integer> e : solution.entrySet()) {
			numSolution.put(e.getKey(), e.getValue());
		}
		final Collection<CSPOMConstraint> failed = cspomProblem
				.control(numSolution);
		if (!failed.isEmpty()) {
			throw new IllegalStateException(failed.toString());
		}

		return time;
	}

	private static String format(int number) {
		if (number < 1000) {
			return "\\np{" + number + "}";
		}
		if (number < 10000) {
			return "\\np[k]{" + Math.round(number / 100d) / 10d + "}";
		}
		if (number < 1000000) {
			return "\\np[k]{" + Math.round(number / 1000d) + "}";
		}
		if (number < 10000000) {
			return "\\np[M]{" + Math.round(number / 100000d) / 10d + "}";
		}
		return "\\np[M]{" + Math.round(number / 1000000d) + "}";
	}

	private static void nbC(final String problem) throws CSPParseException,
			IOException, FailedGenerationException {
		final cspom.CSPOM cspomProblem = cspom.CSPOM.load(problem);
		ProblemCompiler.compile(cspomProblem);
		final Problem csp = ProblemGenerator.generate(cspomProblem);
		System.out.print(csp.getNbConstraints());
		final MGACIter solver = new MGACIter(csp, new CrossHeuristic(
				new DDegOnDom(csp), new Lexico(false)));
		solver.nextSolution();
	}

	public static void main(String[] args) throws CSPParseException,
			IOException, FailedGenerationException, IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		Logger.getLogger("").setLevel(Level.WARNING);

		final Key<Constraint> key = new Key<Constraint>() {
			@Override
			public float getKey(final Constraint object) {
				// double size = 1;
				// for (Variable v : object.getScope()) {
				// size *= v.getDomainSize();
				// }
				return 10000 * object.getEvaluation() + object.getId();
			}
		};

		for (String prob : Arrays.asList("bqwh-18-141/bqwh-18-141-0_ext",
				"bqwh-18-141_glb/bqwh-18-141-2_glb", "frb40-19/frb40-19-1_ext",
				"allIntervalSeries/series-15",
				"golombRulerArity3/ruler-25-7-a3", "langford3/langford-3-10",
				"small/patat-02-small-3", "bmc/bmc-ibm-02-02",
				"lexHerald/crossword-m1-lex-15-04", "schurrLemma/lemma-23-3",
				"os-taillard-5/os-taillard-5-100-3", "rlfapScens/scen4")) {
			System.out.print("{\\em " + prob + "} & ");
			final String problem = "/home/vion/CPAI08/" + prob + ".xml.bz2";
			nbC(problem);

			System.out.print(" & ");
			for (Class<? extends AbstractQueue> clazz : Arrays.asList(
					Fifo.class, BinaryHeap.class, BinomialHeap.class,
					FibonacciHeap.class, SoftHeap.class)) {
				final Queue<Constraint> queue = clazz.getConstructor(Key.class)
						.newInstance(key);
				System.out.print(" & \\np[s]{"
						+ Math.round(solveCons(problem, queue) / 100d) / 10d
						+ "}");

			}
			System.out.println();
		}
	}
}
