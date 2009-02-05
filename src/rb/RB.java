package rb;

import java.util.logging.Logger;

import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;
import cspfj.util.CpuMonitor;

public final class RB {

	private static final Logger logger = Logger.getLogger("cspfj");

	final int nbVariables;

	final int domainSize;

	final int arity;

	final int nbConstraints;

	final int tightnessMode;

	final double tightness;

	final int constraintGraphType;

	final int incompatibilityGraphType;

	final boolean repetition;

	final boolean alwaysSatisfiable;

	final long nbInstances;

	final long firstSeed;

	public RB(String[] argv) {
		nbVariables = Integer.valueOf(argv[0]);

		domainSize = Integer.valueOf(getData(1));

		arity = Integer.valueOf(getData(2));

		nbConstraints = Integer.valueOf(getData(3));

		tightnessMode = Integer.valueOf(getData(4));

		tightness = Double.valueOf(getData(5));

		constraintGraphType = Integer.valueOf(getData(6));

		incompatibilityGraphType = Integer.valueOf(getData(7));

		repetition = Boolean.valueOf(getData(8));

		alwaysSatisfiable = Boolean.valueOf(getData(9));

		nbInstances = Long.valueOf(getData(10));

		firstSeed = Long.valueOf(getData(11));
	}

	@Override
	public void run() throws IOException {

		for (long seed = nbInstances; --seed >= 0;) {
			long loadTime = -CpuMonitor.getCpuTimeNano();

			final RBGenerator rb = new RBGenerator(nbVariables, domainSize,
					arity, nbConstraints, tightnessMode, tightness, seed
							+ firstSeed, constraintGraphType,
					incompatibilityGraphType, repetition, alwaysSatisfiable);

			Problem problem = null;
			try {
				problem = Problem.load(rb);
			} catch (FailedGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writer.problem(problem.getName());
			loadTime += CpuMonitor.getCpuTimeNano();

			final Solver solver = getSolver(problem);
			writer.load(solver, 0);

			solve(solver);
			
		}
		writer.close();

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new RB(args).run();
		} catch (IOException e) {
			logger.severe(e.toString());
		}
	}

	public final String usage() {

		return super.usage()
				+ " {nb variables} {domain size} {arity} {nb constraints}"
				+ " {tght mode} {tght} {cons graph type} {inc graph type}"
				+ " {repetition} {force} {nb instances} {seed}";
	}
}
