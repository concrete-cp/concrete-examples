package rb;

import java.io.IOException;
import java.util.logging.Logger;

import rb.RBGenerator.Tightness;
import rb.randomlists.RandomListGenerator.Structure;
import concrete.AbstractLauncher;
import concrete.AbstractResultWriter;
import concrete.FullResultWriter;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspfj.problem.Problem;

public final class RB extends AbstractLauncher {

	private static final Logger logger = Logger.getLogger("cspfj");

	private final int nbVariables;

	private final int domainSize;

	private final int arity;

	private final int nbConstraints;

	private final Tightness tightnessMode;

	private final double tightness;

	private final Structure constraintGraphType;

	private final Structure incompatibilityGraphType;

	private final boolean repetition;

	private final boolean alwaysSatisfiable;

	private final long nbInstances;

	private final long firstSeed;
	private final AbstractResultWriter writer;

	public RB(String[] argv) throws IOException {
		super(12, 12, argv);
		nbVariables = Integer.valueOf(getArgs().get(0));

		domainSize = Integer.valueOf(getArgs().get(1));

		arity = Integer.valueOf(getArgs().get(2));

		nbConstraints = Integer.valueOf(getArgs().get(3));

		tightnessMode = Tightness.valueOf(getArgs().get(4));

		tightness = Double.valueOf(getArgs().get(5));

		constraintGraphType = Structure.valueOf(getArgs().get(6));

		incompatibilityGraphType = Structure.valueOf(getArgs().get(7));

		repetition = Boolean.valueOf(getArgs().get(8));

		alwaysSatisfiable = Boolean.valueOf(getArgs().get(9));

		nbInstances = Long.valueOf(getArgs().get(10));

		firstSeed = Long.valueOf(getArgs().get(11));

		writer = new FullResultWriter();
	}

	@Override
	public void run() throws IOException {

		for (long seed = nbInstances; --seed >= 0;) {
			long time = -System.currentTimeMillis();
			final RBGenerator rb = new RBGenerator(nbVariables, domainSize,
					arity, nbConstraints, tightnessMode, tightness, seed
							+ firstSeed, constraintGraphType,
					incompatibilityGraphType, repetition, alwaysSatisfiable);

			final Problem problem;
			try {
				problem = ProblemGenerator.generate(rb.generate());
			} catch (FailedGenerationException e) {
				throw new IllegalStateException(e);
			}

			final long loadTime = System.currentTimeMillis() + time;
			writer.problem(rb.toString());

			final Solver solver = getSolver(problem, writer);
			writer.load(solver, loadTime);

			solve(solver, writer);
			writer.nextProblem();

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
