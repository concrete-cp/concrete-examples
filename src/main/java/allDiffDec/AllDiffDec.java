package allDiffDec;

import concrete.JCSPOMDriver;
import concrete.Solver;
import concrete.SolverResult;
import concrete.generator.FailedGenerationException;
import cspom.CSPOM;
import cspom.variable.IntVariable;
import static cspom.CSPOM.*;
import static concrete.JCSPOMDriver.*;

public final class AllDiffDec {
  private AllDiffDec(final int size) {
  }

  public static CSPOM generate() {
    final CSPOM problem = new CSPOM();

    final IntVariable x1 = problem.nameExpression(interVar(3, 4), "X1");
    final IntVariable x2 = problem.nameExpression(interVar(1, 5), "X2");
    final IntVariable x3 = problem.nameExpression(interVar(3, 4), "X3");
    final IntVariable x4 = problem.nameExpression(interVar(2, 5), "X4");
    final IntVariable x5 = problem.nameExpression(interVar(1, 1), "X5");

    problem.ctr(allDifferent(x1, x2, x3, x4, x5));
    return problem;
  }

  public static void main(String[] args) throws FailedGenerationException {
    final CSPOM problem = generate();

    final Solver solver = Solver.apply(problem);

    SolverResult solution = solver.nextSolution();
    while (solution.isSat()) {
      System.out.println(solution);
      solution = solver.nextSolution();
    }

  }
}
