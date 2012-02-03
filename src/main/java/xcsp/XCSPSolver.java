package xcsp;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import cspfj.Pair;
import cspfj.ParameterManager;
import cspfj.Solver;
import cspfj.filter.AC3;
import cspfj.filter.Filter;
import cspfj.generator.FailedGenerationException;
import cspfj.heuristic.DDegOnDom;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspom.CSPOM;
import cspom.CSPParseException;
import cspom.compiler.ProblemCompiler;
import cspfj.filter.AC3Constraint;

public class XCSPSolver {

  public static void main(String[] args) throws CSPParseException,
      IOException, FailedGenerationException, InterruptedException {

    final CSPOM cspomProblem = cspom.CSPOM.load(new URL(
        "file:///home/vion/CPAI08/langford3/langford-3-12.xml.bz2"));

    // ParameterManager.parameter("logger.level", "INFO");
    ParameterManager.update("heuristic.variable", DDegOnDom.class);
    ParameterManager.update("mac.filter", AC3Constraint.class);

    ProblemCompiler.compile(cspomProblem);

    final Solver solver = Solver.factory(cspomProblem);
    System.out.println(solver.problem().stats());
    System.out.println(solver.XMLConfig());
    System.out.println(solver.nextSolution());
    System.out.println(solver.statistics().display());
  }

  public static boolean control(Problem problem) throws InterruptedException {
    final Filter ac = new AC3(problem);
    if (!ac.reduceAll()) {
      return false;
    }

    for (Variable vi : problem.getVariables()) {
      for (int a = vi.dom().first(); a >= 0; a = vi.dom().next(a)) {

        final Set<Pair> domain = new HashSet<Pair>();
        for (Variable vj : problem.getVariables()) {
          for (int b = vj.dom().first(); b >= 0; b = vj.dom().next(b)) {

            problem.push();
            vj.dom().setSingle(b);

            if (ac.reduceAfter(vj) && vi.dom().present(a)) {
              domain.add(new Pair(vj, b));
            }

            problem.pop();
          }
        }

        problem.push();
        for (Variable v : problem.getVariables()) {
          for (int i = v.dom().first(); i >= 0; i = v.dom().next(i)) {
            if (!domain.contains(new Pair(v, i))) {
              v.dom().remove(i);
            }
          }
          if (v.dom().size() == 0) {
            return false;
          }
        }
        if (!ac.reduceAll()) {
          return false;
        }
        problem.pop();
      }
    }

    return true;
  }

}
