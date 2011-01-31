package queens;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import cspfj.AbstractSolver;
import cspfj.ParameterManager;
import cspfj.Solver;
import cspfj.StatisticsManager;
import cspfj.exception.FailedGenerationException;
import cspom.CSPOM;
import cspom.compiler.PredicateParseException;
import cspom.compiler.ProblemCompiler;
import cspom.variable.CSPOMVariable;

public final class Queens {
    private final int size;
    private final CSPOMVariable[] variables;

    private Queens(final int size) {
        this.size = size;
        variables = new CSPOMVariable[size];
    }

    public CSPOM generate() throws PredicateParseException {
        final CSPOM problem = new CSPOM();

        for (int i = size; --i >= 0;) {
            variables[i] = problem.var("Q" + i, 1, size);
        }

        for (int j = size; --j >= 0;) {
            for (int i = j; --i >= 0;) {
                problem.ctr("ne(" + variables[i] + ", " + variables[j] + ")");
                problem.ctr("ne(abs(sub(" + variables[i] + ", " + variables[j]
                        + ")), " + (j - i) + ")");
            }
        }

        return problem;
    }

    public static void main(String[] args) throws FailedGenerationException,
            NumberFormatException, PredicateParseException, IOException,
            ClassNotFoundException {
        ParameterManager.parameterParse("logger.level", "INFO");
        ParameterManager.parameterParse("heuristic.variable",
                "cspfj.heuristic.WDegFixedOnDom");
        for (int i : Arrays.asList(4, 8, 12, 15, 20, 30, 50, 80, 100, 120, 150)) {
        	StatisticsManager.reset();
            System.out.println(i + " :");
            long time = -System.currentTimeMillis();
            final Queens queens = new Queens(i);
            final CSPOM problem = queens.generate();
            ProblemCompiler.compile(problem);
            
            final Solver solver = AbstractSolver.factory(problem);

            Map<String, Integer> solution = solver.nextSolution();
            int count = 0;
            System.out.println((System.currentTimeMillis() + time) / 1000f);
            System.out.println(StatisticsManager.digest());
            // while (solution != null) {
            // solution = solver.nextSolution();
            // count++;
            // }
            // System.out.println(count + " in "
            // + (System.currentTimeMillis() + time) / 1e3f);
        }
    }
}
