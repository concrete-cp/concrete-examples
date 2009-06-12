package queens;

import java.io.IOException;

import javax.script.ScriptException;

import concrete.CspOM;
import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspom.predicate.Predicate;
import cspom.predicate.PredicateConstraint;
import cspom.variable.Domain;
import cspom.variable.IntervalDomain;

public class QueensConcrete {

    public static void main(String[] args) throws FailedGenerationException,
            IOException, ScriptException {

        final int size = Integer.valueOf(args[0]);
        final cspom.Problem problem = new cspom.Problem("Queens");
        final Domain domain = new IntervalDomain("", 0, size - 1);
        for (int i = 0; i < size; i++) {
            problem.addVariable(new cspom.variable.Variable("Q" + i, domain));
        }
        final Predicate predicate = new Predicate("", "int X int Y int diff",
                "X!=Y && abs(X-Y)!=diff");

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                final int diff = Math.abs(i - j);
                problem.addConstraint(new PredicateConstraint("C" + i + j, "Q"
                        + i + " Q" + j + " " + diff, predicate, problem
                        .getVariables().get(i), problem.getVariables().get(j)));
            }
        }

        final Problem cspfjProblem = Problem.load(new CspOM(problem, true));
        final Solver solver = new MGACIter(cspfjProblem);
        solver.runSolver();
        for (Variable v : cspfjProblem.getVariables()) {
            System.out.println(v + ": " + solver.getSolution().get(v));
        }
    }

}
