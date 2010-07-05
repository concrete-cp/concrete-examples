package bigleq;
import java.io.IOException;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.constraint.Constraint;
import cspfj.constraint.semantic.AllDifferent;
import cspfj.constraint.semantic.Gt;
import cspfj.filter.AC3Constraint;
import cspfj.priorityqueues.FibonacciHeap;
import cspfj.priorityqueues.Key;
import cspfj.problem.BitVectorDomain;
import cspfj.problem.Problem;
import cspfj.problem.Variable;

public class BigLeq {
    private static final int NB_VALS = 1000;
    private static final int NB_VARS = 1000;

    public static Problem bigleq(final int nbVars, final int nbVals) {
        final Problem problem = new Problem();
        final int[] vals = new int[nbVals];
        for (int i = nbVals; --i >= 0;) {
            vals[i] = i;
        }
        final Variable[] vars = new Variable[nbVars];
        for (int i = nbVars; --i >= 0;) {
            vars[i] = problem.addVariable("X" + i, new BitVectorDomain(vals));
        }
        problem.prepareVariables();
        for (int i = nbVars - 1; --i >= 0;) {
            problem.addConstraint(new Gt(vars[i + 1], vars[i], false));
        }

        problem.addConstraint(new AllDifferent(vars));
        problem.prepareConstraints();
        return problem;
    }

    public static void main(String[] args) throws IOException {

        final Problem problem = new Problem();
        final int[] vals = new int[NB_VALS];
        for (int i = NB_VALS; --i >= 0;) {
            vals[i] = i;
        }
        final Variable[] vars = new Variable[NB_VARS];
        for (int i = NB_VARS; --i >= 0;) {
            vars[i] = problem.addVariable("X" + i, new BitVectorDomain(vals));
        }
        problem.prepareVariables();
        for (int i = NB_VARS - 1; --i >= 0;) {
            problem.addConstraint(new Gt(vars[i + 1], vars[i], false));
        }

        problem.addConstraint(new AllDifferent(vars));
        problem.prepareConstraints();
        vars[0].remove(0);

        {
            final Solver s = new MGACIter(problem, new AC3Constraint(problem,
                    new FibonacciHeap<Constraint>(new Key<Constraint>() {
                        @Override
                        public float getKey(Constraint object) {
                            return object.getEvaluation();
                        }
                    })));
            long time = -System.currentTimeMillis();
            s.nextSolution();
            time += System.currentTimeMillis();

            System.out.println(time);
        }

    }
}
