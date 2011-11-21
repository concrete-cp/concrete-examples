package allDiffDec;

import scala.Option;
import scala.collection.immutable.Map;
import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.generator.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspom.CSPOM;
import cspom.compiler.ProblemCompiler;
import cspom.variable.CSPOMVariable;

public final class AllDiffDec {
  private AllDiffDec(final int size) {
  }

  public static CSPOM generate() {
    final CSPOM problem = new CSPOM();

    final CSPOMVariable x1 = problem.interVar("X1", 3, 4);
    final CSPOMVariable x2 = problem.interVar("X2", 1, 5);
    final CSPOMVariable x3 = problem.interVar("X3", 3, 4);
    final CSPOMVariable x4 = problem.interVar("X4", 2, 5);
    final CSPOMVariable x5 = problem.interVar("X5", 1, 1);

    problem.ctr("allDifferent(" + x1 + ", " + x2 + ", " + x3 + ", " + x4
        + ", " + x5 + ")");
    return problem;
  }

  public static void main(String[] args) throws FailedGenerationException {
    final CSPOM problem = generate();

    ProblemCompiler.compile(problem);

    final Solver solver = new MGACIter(ProblemGenerator.generate(problem));

    Option<Map<String, Object>> solution = solver.nextSolution();
    while (solution.isDefined()) {
      System.out.println(solution);
      solution = solver.nextSolution();
    }

  }
}
