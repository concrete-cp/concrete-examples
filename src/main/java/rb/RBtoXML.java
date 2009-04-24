package rb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

import competitor.AbstractLauncher;

import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspfj.constraint.Constraint;

public final class RBtoXML extends AbstractLauncher {

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

	public RBtoXML(String[] argv) {
		super(argv, 8);
		nbVariables = Integer.valueOf(getData(0));

		domainSize = Integer.valueOf(getData(1));

		arity = Integer.valueOf(getData(2));

		nbConstraints = Integer.valueOf(getData(3));

		tightnessMode = 1;// Integer.valueOf(getData(4));

		tightness = Double.valueOf(getData(4));

		constraintGraphType = 0;// Integer.valueOf(getData(6));

		incompatibilityGraphType = 0;// Integer.valueOf(getData(7));

		repetition = false;// Boolean.valueOf(getData(8));

		alwaysSatisfiable = Boolean.valueOf(getData(5));

		nbInstances = Long.valueOf(getData(6));

		firstSeed = Long.valueOf(getData(7));
	}

	@Override
	public void run() throws IOException {

		for (long seed = nbInstances; --seed >= 0;) {
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
			final String name = (alwaysSatisfiable ? "f" : "") + "rand_"
					+ nbVariables + "_" + domainSize + "_" + arity + "_"
					+ nbConstraints + "_" + tightness + "." + seed;
			final OutputStreamWriter out = new FileWriter(name + ".xml");

			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			out.write("<instance>\n");
			out.write("<presentation name=\"" + name
					+ "\" maxConstraintArity=\"" + arity
					+ "\"  format=\"XCSP 2.0\"/>\n");

			out.write("<domains nbDomains=\"1\">");
			out.write("<domain name=\"D0\" nbValues=\"" + domainSize + "\">0.."
					+ (domainSize - 1) + "</domain>");
			out.write("</domains>\n");

			out.write("<variables nbVariables='" + nbVariables + "'>\n");
			for (Variable v : problem.getVariables()) {
				out.write("<variable name='" + v + "' domain='D0' />\n");
			}
			out.write("</variables>\n");

			out.write("<relations nbRelations='" + nbConstraints + "'>\n");

			for (Constraint c : problem.getConstraints()) {
				final StringBuffer tuple = new StringBuffer();
				int nbTuples = 0;

				for (int position = c.getArity(); --position >= 0;) {
					for (int i = c.getInvolvedVariables()[position]
							.getDomainSize(); --i >= 0;) {
						c.getMatrix().setFirstTuple(position, i);

						do {
							nbTuples++;
							//tuple.append(nbTuples).append('-');
							for (int j : c.getTuple()) {
								tuple.append(j).append(' ');
							}
							tuple.deleteCharAt(tuple.length() - 1).append('|');

						} while (c.getMatrix().next());
					}
				}

				tuple.deleteCharAt(tuple.length() - 1);
				
				out.write("<relation name='R" + (c.getId()) + "' arity='"
						+ c.getArity() + "' nbTuples='" + nbTuples
						+ "' semantics='supports'>");
				out.write(tuple.toString());
				out.write("</relation>\n");
			}

			out.write("</relations>\n");

			out.write("<constraints nbConstraints='" + nbConstraints + "'>\n");

			for (Constraint c : problem.getConstraints()) {
				final StringBuffer scope = new StringBuffer();
				for (int p = 0; p < c.getArity(); p++) {
					scope.append(c.getInvolvedVariables()[p]).append(" ");
				}
				scope.deleteCharAt(scope.length() - 1);

				out.write("<constraint name='C" + (c.getId()) + "' arity='"
						+ c.getArity() + "' scope='" + scope
						+ "' reference ='R" + (c.getId()) + "' />\n");

			}

			out.write("</constraints>\n");

			out.write("</instance>\n");
			out.close();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new RBtoXML(args).run();
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
