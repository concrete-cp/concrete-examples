/* $Header: /home/vion/migration/examples/src/rb/RandomExtensionConstraint.java,v 1.1 2009-02-05 16:15:03 scand1sk Exp $ */

package rb;

import rb.randomlists.CoarseProportionRandomListGenerator;
import rb.randomlists.ProbabilityRandomListGenerator;
import rb.randomlists.ProportionRandomListGenerator;
import rb.randomlists.RandomListGenerator;
import cspfj.constraint.ExtensionConstraint;
import cspfj.exception.FailedGenerationException;
import cspfj.exception.MatrixTooBigException;
import cspfj.problem.Variable;

public final class RandomExtensionConstraint extends ExtensionConstraint {

	public RandomExtensionConstraint(Variable[] involvedVariables,
			int nbTuples, long seed, int type, int[] requiredSupport,
			boolean supports) throws FailedGenerationException {
		super(involvedVariables);
		int[] nbValues = new int[involvedVariables.length];
		for (int i = 0; i < nbValues.length; i++) {
			nbValues[i] = involvedVariables[i].getDomain().length;
		}

		ProportionRandomListGenerator r = new CoarseProportionRandomListGenerator(
				nbValues, seed); // new
		// FineProportionRandomListGenerator(nbValues,
		// seed);
		int[][] tuples = r.selectTuples(nbTuples, type, false, true,
				requiredSupport, supports);

		intersect(involvedVariables, supports, tuples);
	}

	public RandomExtensionConstraint(Variable[] involvedVariables,
			double nbUnallowedTuples, long seed, int type, int[] requiredSupport)
			throws FailedGenerationException {
		super(involvedVariables);
		int[] nbValues = new int[involvedVariables.length];
		for (int i = 0; i < nbValues.length; i++) {
			nbValues[i] = involvedVariables[i].getDomain().length;
		}

		double nbAllowedTuples = RandomListGenerator
				.computeNbArrangementsFrom(nbValues)
				- nbUnallowedTuples;

		// System.out.println("nbAllowedc = " + nbAllowedTuples + " nbUnaloowed
		// = " + nbUnallowedTuples);
		if (nbAllowedTuples > Integer.MAX_VALUE
				&& nbUnallowedTuples > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"The number of allowed and unallowed tuples is greater than Interger.MAX_INT");
		}
		int nbTuples = (int) (Math.min(nbAllowedTuples, nbUnallowedTuples));

		ProportionRandomListGenerator r = new CoarseProportionRandomListGenerator(
				nbValues, seed); // new
		// FineProportionRandomListGenerator(nbValues,
		// seed);
		int[][] tuples = r.selectTuples(nbTuples, type, false, true,
				requiredSupport, nbAllowedTuples < nbUnallowedTuples);

		intersect(involvedVariables, tuples.length < nbUnallowedTuples, tuples);
	}

	/**
	 * Builds an <ExplicitRandomConstraint </code> object.
	 * 
	 * @param problem
	 *            the problem to which the constraint is attached
	 * @param variables
	 *            the variables involved in the constraint
	 * @param nbUnallowedTuples
	 *            the number of conflicts (unallowed tuples)
	 * @param seed
	 *            the seed used to generate random numbers
	 * @param type
	 *            the type of the generated list of random constraints which can
	 *            be UNSTRUCTURED, CONNECTED or BALANCED
	 * @throws FailedGenerationException
	 */
	public RandomExtensionConstraint(Variable[] variables,
			long nbUnallowedTuples, long seed, int type)
			throws FailedGenerationException {
		this(variables, nbUnallowedTuples, seed, type, null);
	}

	/**
	 * Builds an <code>RandomExtensionConstraint</code> object.
	 * 
	 * @param problem
	 *            the problem to which the constraint is attached
	 * @param variables
	 *            the variables involved in the constraint
	 * @param tightness
	 *            the tightness of the constraint
	 * @param seed
	 *            the seed used to generate random numbers
	 * @param requiredSupport
	 *            a particular tuple, which if not <code> null </code>, must be
	 *            considered as a support
	 * @throws FailedGenerationException
	 */
	public RandomExtensionConstraint(Variable[] variables, double tightness,
			long seed, int[] requiredSupport) throws FailedGenerationException {
		super(variables);
		int nbValues = variables[0].getDomain().length; // we assume
		// that each
		// variable
		// has the
		// same
		// domain
		int tupleLength = variables.length;

		double selectionLimit = Math.min(tightness, 1 - tightness);
		ProbabilityRandomListGenerator r = new ProbabilityRandomListGenerator(
				nbValues, tupleLength, seed);
		int[][] tuples = r.selectTuples(selectionLimit, true, requiredSupport,
				tightness > 0.5);
		intersect(variables, tightness > 0.5, tuples);
	}

	/**
	 * Builds an <ExplicitRandomConstraint </code> object.
	 * 
	 * @param problem
	 *            the problem to which the constraint is attached
	 * @param variables
	 *            the variables involved in the constraint the tightness of the
	 *            constraint
	 * @param seed
	 *            the seed used to generate random numbers
	 * @throws FailedGenerationException
	 */
	public RandomExtensionConstraint(Variable[] variables, double tightness,
			long seed) throws FailedGenerationException {
		this(variables, tightness, seed, null);
	}
}
