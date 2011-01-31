package allDiffDec;

import java.io.IOException;
import java.util.Map;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspom.CSPOM;
import cspom.compiler.PredicateParseException;
import cspom.compiler.ProblemCompiler;
import cspom.variable.CSPOMVariable;

public final class AllDiffDec {
    private AllDiffDec(final int size) {
    }

    public static CSPOM generate() throws PredicateParseException {
        final CSPOM problem = new CSPOM();

        final CSPOMVariable x1 = problem.var("X1", 3, 4);
        final CSPOMVariable x2 = problem.var("X2", 1, 5);
        final CSPOMVariable x3 = problem.var("X3", 3, 4);
        final CSPOMVariable x4 = problem.var("X4", 2, 5);
        final CSPOMVariable x5 = problem.var("X5", 1, 1);

        problem.ctr("allDifferent(" + x1 + ", " + x2 + ", " + x3 + ", " + x4
                + ", " + x5 + ")");
        return problem;
    }

    public static void main(String[] args) throws FailedGenerationException,
            NumberFormatException, PredicateParseException, IOException {
        final CSPOM problem = generate();

        ProblemCompiler.compile(problem);

        final Solver solver = new MGACIter(ProblemGenerator.generate(problem));

        Map<String, Integer> solution = solver.nextSolution();
        while (solution != null) {
            System.out.println(solution);
            solution = solver.nextSolution();
        }

    }
}
