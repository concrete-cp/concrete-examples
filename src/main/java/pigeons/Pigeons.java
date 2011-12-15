package pigeons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.generator.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspom.CSPOM;
import cspom.compiler.ProblemCompiler;
import cspom.variable.CSPOMVariable;

public final class Pigeons {

  private Pigeons() {

  }

  public static CSPOM generate(int size) throws FailedGenerationException {
    final CSPOM problem = new CSPOM();
    final List<CSPOMVariable> variables = new ArrayList<CSPOMVariable>(size);
    for (int i = size; --i >= 0;) {
      variables.add(problem.interVar("V" + i, 1, size - 1));
    }

    for (int i = size; --i >= 0;) {
      for (int j = i; --j >= 0;) {
        problem.ctr("ne(" + variables.get(i) + ", " + variables.get(j)
            + ")");
      }
    }
    return problem;
  }

  public static void main(final String[] args) throws NumberFormatException,
      FailedGenerationException, IOException {

    for (int i : Arrays.asList(4, 8, 12, 15, 20, 30, 50, 80, 100, 120, 150)) {
      System.out.println(i + " :");
      long time = -System.currentTimeMillis();
      final CSPOM problem = generate(i);

      ProblemCompiler.compile(problem);

      final Solver solver = new MGACIter(
          ProblemGenerator.generate(problem));

      solver.nextSolution();

      System.out.println((System.currentTimeMillis() + time) / 1000f);
      // while (solution != null) {
      // solution = solver.nextSolution();
      // count++;
      // }
      // System.out.println(count + " in "
      // + (System.currentTimeMillis() + time) / 1e3f);
    }
  }

}
