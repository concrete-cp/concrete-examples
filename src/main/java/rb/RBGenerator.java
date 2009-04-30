package rb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rb.randomlists.CoarseProportionRandomListGenerator;
import rb.randomlists.ProbabilityRandomListGenerator;
import rb.randomlists.ProportionRandomListGenerator;
import rb.randomlists.RandomListGenerator;
import rb.randomlists.RandomListGenerator.Structure;
import cspfj.constraint.Constraint;
import cspfj.constraint.extension.ExtensionConstraint2D;
import cspfj.constraint.extension.ExtensionConstraintGeneral;
import cspfj.constraint.extension.Matrix;
import cspfj.constraint.extension.Matrix2D;
import cspfj.constraint.extension.MatrixGeneral;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.ProblemGenerator;
import cspfj.problem.Variable;

/**
 * This class corresponds to explicit random problems, i.e., random problems
 * such that constraints are given in extension. <br>
 * 4 2 40 10 10 2 140 0 44 0 0 0 n n <br>
 * 25 10 2 200 0 15 0 0 0 n n 5 8 10 2 22 0 65 0 0 0 n n 25 0 5
 */
public class RBGenerator implements ProblemGenerator {
    /**
     * Nb of variables
     */
    final private int nbVariables;

    /**
     * Variable domain Size
     */
    final private int domainSize;

    /**
     * Constraint arity
     */
    final private int arity;

    /**
     * Nb of constraints
     */
    final private int nbConstraints;

    /**
     * Tightness Mode: NBNG, PROPORTION, PROBABILITY, NBSUP
     */
    public static enum Tightness {
        NBNG, PROPORTION, PROBABILITY, NBSUP
    }

    final private Tightness tightnessMode;

    /**
     * Tightness
     */
    final private double tightness;

    /**
     * Seed
     */
    final private long seed;

    /**
     * Constraint Graph Type
     */
    final private Structure constraintGraphType;

    /**
     * Incompatibility Graph Type
     */
    final private Structure incompatibilityGraphType;

    /**
     * Possibility of generating several constraints with same signature
     */
    final private boolean repetition;

    /**
     * Generation of satisfiable instances
     */
    final private boolean alwaysSatisfiable;

    final private List<Variable> variables;

    private final Collection<Constraint> constraints;

    public RBGenerator(int nbVariables, int domainSize, int arity,
            int nbConstraints, Tightness tightnessMode, double tightness,
            long seed, Structure constraintGraphType,
            Structure incompatibilityGraphType, boolean repetition,
            boolean alwaysSatisfiable) {
        this.nbVariables = nbVariables;
        this.domainSize = domainSize;
        this.arity = arity;
        this.nbConstraints = nbConstraints;
        this.tightnessMode = tightnessMode;
        this.tightness = tightness;
        this.seed = seed;
        this.constraintGraphType = constraintGraphType;
        this.incompatibilityGraphType = incompatibilityGraphType;
        this.repetition = repetition;
        this.alwaysSatisfiable = alwaysSatisfiable;

        constraints = new ArrayList<Constraint>();
        variables = new ArrayList<Variable>();
    }

    public String getName() {
        final StringBuffer sb = new StringBuffer();
        sb.append("rand-").append(arity);
        sb.append("-").append(nbVariables);
        sb.append("-").append(domainSize);
        sb.append("-").append(nbConstraints);
        final int coeff = tightnessMode == Tightness.PROPORTION
                || tightnessMode == Tightness.PROBABILITY ? 1000 : 1;
        sb.append("-").append(Math.round(coeff * tightness));
        if (alwaysSatisfiable) {
            sb.append("-fcd");
        }
        sb.append("-").append(seed);
        sb.append("_ext");

        return sb.toString();
    }

    public void generate() throws FailedGenerationException {
        final int[] domain = new int[domainSize];
        for (int k = domainSize; --k >= 0;) {
            domain[k] = k;
        }

        for (int i = nbVariables; --i >= 0;) {
            variables.add(new Variable(domain));
        }

        final Random random = new Random(seed);
        final Map<Variable, Integer> solution = (alwaysSatisfiable ? computeRandomSolution(random)
                : null);

        int[] forcedTuple = (alwaysSatisfiable ? new int[arity] : null);

        ProportionRandomListGenerator r = new CoarseProportionRandomListGenerator(
                nbVariables, arity, seed);

        int[][] activeConstraints = r.selectTuples(nbConstraints,
                constraintGraphType, repetition, false);
        for (int i = 0; i < activeConstraints.length; i++) {
            Variable[] involvedVariables = new Variable[arity];
            for (int j = 0; j < involvedVariables.length; j++) {
                involvedVariables[j] = variables.get(activeConstraints[i][j]);
                if (alwaysSatisfiable) {
                    forcedTuple[j] = solution.get(involvedVariables[j]);
                    // System.out.println("tuple["+j+"]="+solution[
                    // involvedVariables[j].getId()]);
                }
            }
            constraints.add(buildExplicitConstraint(involvedVariables,
                    tightnessMode, tightness, random.nextLong(),
                    incompatibilityGraphType, forcedTuple));
        }

    }

    private Map<Variable, Integer> computeRandomSolution(Random random) {
        Map<Variable, Integer> solution = new HashMap<Variable, Integer>(
                nbVariables);
        for (Variable v : variables) {
            solution.put(v, random.nextInt(v.getDomain().maxSize()));
        }
        return solution;
    }

    private long computeNbUnallowedTuplesFrom(Variable[] variables,
            double tightness) {
        long cpt = 1;
        for (int i = variables.length; --i >= 0;) {
            cpt *= variables[i].getDomain().maxSize();
        }
        return (long) (tightness * cpt);
    }

    private Constraint buildExplicitConstraint(Variable[] variables,
            Tightness tightnessMode, double tightness, long seed,
            Structure incompatibilityGraphType, int[] forcedTuple)
            throws FailedGenerationException {
        // System.out.println(tightnessMode);

        final int[] sizes = new int[variables.length];
        for (int i = variables.length; --i >= 0;) {
            sizes[i] = variables[i].getDomain().maxSize();
        }

        Matrix matrix;

        switch (tightnessMode) {
        case NBNG:
            matrix = randomMatrix(sizes, (int) tightness, seed,
                    incompatibilityGraphType, forcedTuple, false);
            break;

        case NBSUP:
            matrix = randomMatrix(sizes, (int) tightness, seed,
                    incompatibilityGraphType, forcedTuple, true);
            break;

        case PROBABILITY:
            matrix = randomMatrix(sizes, tightness, seed, forcedTuple);
            break;

        default:
            matrix = randomMatrix(sizes, computeNbUnallowedTuplesFrom(
                    variables, tightness), seed, incompatibilityGraphType,
                    forcedTuple);
        }

        if (matrix instanceof Matrix2D) {
            return new ExtensionConstraint2D(variables, (Matrix2D) matrix,
                    false);
        }
        return new ExtensionConstraintGeneral(variables, matrix, false);
    }

    private static Matrix randomMatrix(int[] sizes, int nbTuples, long seed,
            Structure type, int[] forcedSupport, boolean supports)
            throws FailedGenerationException {

        ProportionRandomListGenerator r = new CoarseProportionRandomListGenerator(
                sizes, seed); // new
        // FineProportionRandomListGenerator(nbValues,
        // seed);
        return tuplesToMatrix(sizes, r.selectTuples(nbTuples, type, false,
                true, forcedSupport, supports), supports);
    }

    public static Matrix randomMatrix(int[] sizes, double nbUnallowedTuples,
            long seed, Structure type, int[] requiredSupport)
            throws FailedGenerationException {

        double nbAllowedTuples = RandomListGenerator
                .computeNbArrangementsFrom(sizes)
                - nbUnallowedTuples;

        // System.out.println("nbAllowedc = " + nbAllowedTuples + " nbUnaloowed
        // = " + nbUnallowedTuples);
        if (nbAllowedTuples > Integer.MAX_VALUE
                && nbUnallowedTuples > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "The number of allowed and unallowed tuples is greater than Interger.MAX_INT");
        }

        final boolean supports = nbAllowedTuples < nbUnallowedTuples;

        int nbTuples = supports ? (int) nbAllowedTuples
                : (int) nbUnallowedTuples;

        ProportionRandomListGenerator r = new CoarseProportionRandomListGenerator(
                sizes, seed); // new
        // FineProportionRandomListGenerator(nbValues,
        // seed);

        return tuplesToMatrix(sizes, r.selectTuples(nbTuples, type, false,
                true, requiredSupport, supports), supports);
    }

    public static Matrix randomMatrix(int[] sizes, double tightness, long seed,
            int[] requiredSupport) throws FailedGenerationException {
        // we assume that each variable has the same domain
        int nbValues = sizes[0];
        int tupleLength = sizes.length;

        double selectionLimit = Math.min(tightness, 1 - tightness);
        ProbabilityRandomListGenerator r = new ProbabilityRandomListGenerator(
                nbValues, tupleLength, seed);
        final boolean supports = tightness > 0.5;
        return tuplesToMatrix(sizes, r.selectTuples(selectionLimit, true,
                requiredSupport, supports), supports);

    }

    private static Matrix tuplesToMatrix(int[] sizes, int[][] tuples,
            boolean supports) {
        final Matrix matrix;
        if (sizes.length == 2) {
            matrix = new Matrix2D(sizes[0], sizes[1], !supports);
        } else {
            matrix = new MatrixGeneral(sizes, !supports);
        }

        for (int[] tuple : tuples) {
            matrix.set(tuple, supports);
        }
        return matrix;
    }

    @Override
    public Collection<Constraint> getConstraints() {
        return constraints;
    }

    @Override
    public List<Variable> getVariables() {
        return variables;
    }

}