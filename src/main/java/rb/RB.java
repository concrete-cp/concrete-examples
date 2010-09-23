package rb;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import rb.RBGenerator.Tightness;
import rb.randomlists.RandomListGenerator.Structure;
import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.Filter;
import cspfj.generator.ProblemGenerator;
import cspfj.problem.Problem;

public final class RB {
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

    // private final AbstractResultWriter writer;

    public RB(int nbVariables, int domainSize, int arity, int nbConstraints,
            Tightness tightnessMode, double tightness,
            Structure constraintGraphType, Structure incompatibilityGraphType,
            boolean repetition, boolean alwaysSatisfiable, long nbInstances,
            long firstSeed) throws IOException {
        this.nbVariables = nbVariables;

        this.domainSize = domainSize;

        this.arity = arity;

        this.nbConstraints = nbConstraints;

        this.tightnessMode = tightnessMode;

        this.tightness = tightness;

        this.constraintGraphType = constraintGraphType;

        this.incompatibilityGraphType = incompatibilityGraphType;

        this.repetition = repetition;

        this.alwaysSatisfiable = alwaysSatisfiable;

        this.nbInstances = nbInstances;

        this.firstSeed = firstSeed;
    }

    public int run() throws IOException {
        int unsat = 0;
        for (long seed = nbInstances; --seed >= 0;) {

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

            final Solver solver = new MGACIter(problem);
            if (solver.nextSolution() == null) {
                unsat++;
            }
        }
        return unsat;
    }

    public int runFilter(Class<? extends Filter> clazz) throws IOException {
        int unsat = 0;
        for (long seed = nbInstances; --seed >= 0;) {

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

            problem.prepare();

            final Filter filter;
            try {
                filter = clazz.getConstructor(Problem.class).newInstance(
                        problem);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            try {
                if (!filter.reduceAll()) {
                    unsat++;
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return unsat;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.WARNING);

        try {
            new RB(Integer.valueOf(args[0]), Integer.valueOf(args[1]),
                    Integer.valueOf(args[2]), Integer.valueOf(args[3]),
                    Tightness.PROPORTION, Double.valueOf(args[4]),
                    Structure.UNSTRUCTURED, Structure.UNSTRUCTURED, false,
                    false, 20, 0).run();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(usage());

        }
    }

    public final static String usage() {

        return " {nb variables} {domain size} {arity} {nb constraints}"
                + " {tght mode} {tght} {cons graph type} {inc graph type}"
                + " {repetition} {force} {nb instances} {seed}";
    }
}
