package xcsp;

import java.io.IOException;
import java.net.URL;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

import cspfj.Pair;
import cspfj.ParameterManager;
import cspfj.Solver;
import cspfj.filter.AC3;
import cspfj.filter.Filter;
import cspfj.generator.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspom.CSPOM;
import cspom.CSPParseException;
import cspom.compiler.ProblemCompiler;

public class XCSPSolver {

  public static void main(String[] args) throws CSPParseException,
      IOException, FailedGenerationException, InterruptedException {
    // System.out.println(Arrays.toString(args));
    final URL url = new URL(args[1]);
    final CSPOM cspomProblem = cspom.CSPOM.load(url);

    // ParameterManager.parse("logger.level", "INFO");

    final String[] parameters = args[0].split(":");

    for (String p : parameters) {
      String[] v = p.split("=");
      ParameterManager.parse(v[0], v[1]);
    }

    ProblemCompiler.compile(cspomProblem);

    final Problem p = ProblemGenerator.generate(cspomProblem);
    System.out.println(p);

    // ParameterManager.checkPending();

    // final Formatter f = new Formatter();
    //
    // System.out
    // .println(f.format("update problems set"
    // + "(nbvars, nbcons) = (%d,%d) " + "where name = '%s';",
    // p.variables().size(), p.constraints().size(),
    // url.getFile()));
    
    final Solver solver = Solver.factory(p);
    solver.nextSolution();

    System.out
        .println(f.format("update problems set"
            + "(nbvars, nbcons) = (%d,%d) " + "where name = '///%s';",
            p.variables().size(), p.constraints().size(),
            url.getFile()));
    // solver.nextSolution();

    // System.out.println(solver.statistics().toString());
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
