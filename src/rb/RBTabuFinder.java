package rb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import ml.options.Options;
import ml.options.Options.Multiplicity;
import ml.options.Options.Separator;
import analyzer.Analyzer;

import competitor.AbstractLauncher;
import competitor.ResultWriter;

import cspfj.AbstractLocalSolver;
import cspfj.MCRW;
import cspfj.Tabu;
import cspfj.WMC;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;

public final class RBTabuFinder extends AbstractLauncher {

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

	final double p;

	final int t;

	final int f;

	public RBTabuFinder(String[] argv) {
		super(argv, 10);
		nbVariables = Integer.valueOf(getData(0));

		domainSize = Integer.valueOf(getData(1));

		arity = Integer.valueOf(getData(2));

		nbConstraints = (int) (nbVariables * (nbVariables - 1)
				* Double.valueOf(getData(3)) / 2);

		logger.info(nbConstraints + " constraints");

		tightnessMode = Integer.valueOf(getData(4));

		tightness = Double.valueOf(getData(5));

		constraintGraphType = Integer.valueOf(getData(6));

		incompatibilityGraphType = Integer.valueOf(getData(7));

		repetition = Boolean.valueOf(getData(8));

		alwaysSatisfiable = Boolean.valueOf(getData(9));

		p = isSet("p") ? Double.parseDouble(getOption("p").getResultValue(0))
				: -1;

		t = isSet("t") ? Integer.parseInt(getOption("t").getResultValue(0))
				: 50;

		f = isSet("f") ? Integer.parseInt(getOption("f").getResultValue(0))
				: 100000;

	}

	@Override
	public void run() throws IOException {
		final RBGenerator rb = new RBGenerator(nbVariables, domainSize, arity,
				nbConstraints, tightnessMode, tightness, 0,
				constraintGraphType, incompatibilityGraphType, repetition,
				alwaysSatisfiable);

		Problem problem = null;
		try {
			problem = Problem.load(rb);
		} catch (FailedGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final List<Integer> list = new ArrayList<Integer>();

		for (int i = t; --i >= 0;) {
			writer = new ResultWriter(true);

			final AbstractLocalSolver solver;
			if ("MCRW".equals(getSolverName())) {
				if (p >= 0) {
					solver = new MCRW(problem, writer, (float) p, true);
				} else {
					solver = new MCRW(problem, writer, true);
				}
			} else if ("Tabu".equals(getSolverName())){
				if (p >= 0) {
					solver = new Tabu(problem, writer, (int) p, true);
				} else {
					solver = new Tabu(problem, writer, true);
				}
			} else {
				solver = new WMC(problem,writer, true);
			}
			solver.setMaxBacktracks(f);
			solver.setMaxTries(1);
			writer.load(solver, 0);

			Tabu.setSeed(i);
			solve(solver);
			list.add(writer.getBestSatisfied());
			logger.warning(Integer.toString(writer.getBestSatisfied()));
		}

		System.out.println("minf : " + (nbConstraints - Analyzer.max(list)));
		System.out.println("maxf : " + (nbConstraints - Analyzer.min(list)));
		System.out
				.println("avgf : " + (nbConstraints - Analyzer.average(list)));
		System.out.println("medf : " + (nbConstraints - Analyzer.median(list)));

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new RBTabuFinder(args).run();
		} catch (IOException e) {
			logger.severe(e.toString());
		}
	}

	public final String usage() {

		return super.usage()
				+ " {nb variables} {domain size} {arity} {density}"
				+ " {tght mode} {tght} {cons graph type} {inc graph type}"
				+ " {repetition} {force}";
	}

	@Override
	public void setOptions(Options options) {
		super.setOptions(options);
		options
				.addOptionAllSets("p", Separator.BLANK,
						Multiplicity.ZERO_OR_ONE);
		options
				.addOptionAllSets("t", Separator.BLANK,
						Multiplicity.ZERO_OR_ONE);
		options
				.addOptionAllSets("f", Separator.BLANK,
						Multiplicity.ZERO_OR_ONE);
	}

}
