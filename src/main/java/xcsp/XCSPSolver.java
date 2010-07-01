package xcsp;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import cspfj.MGACIter;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.AC3;
import cspfj.filter.AC3Constraint;
import cspfj.generator.ProblemGenerator;
import cspfj.heuristic.CrossHeuristic;
import cspfj.heuristic.DDegOnDom;
import cspfj.heuristic.Lexico;
import cspfj.priorityqueues.Fifo;
import cspfj.problem.Problem;
import cspom.CSPParseException;
import cspom.compiler.ProblemCompiler;
import cspom.constraint.CSPOMConstraint;

public class XCSPSolver {
    private static int count(final String name) throws CSPParseException,
            IOException, FailedGenerationException {
        final cspom.CSPOM cspomProblem = cspom.CSPOM.load(XCSPSolver.class
                .getResource(name));
        ProblemCompiler.compile(cspomProblem);
        final Problem problem = ProblemGenerator.generate(cspomProblem);
        // System.out.println(problem);

        final MGACIter solver = new MGACIter(problem, new CrossHeuristic(
                new DDegOnDom(problem), new Lexico(false)));

        int count = 0;
        for (;; count++) {
            final Map<String, Integer> solution = solver.nextSolution();

            if (solution == null) {
                break;
            }
            final Map<String, Number> numSolution = new LinkedHashMap<String, Number>(
                    solution.size());
            for (Entry<String, Integer> e : solution.entrySet()) {
                numSolution.put(e.getKey(), e.getValue());
            }
            final Collection<CSPOMConstraint> failed = cspomProblem
                    .control(numSolution);
            if (!failed.isEmpty()) {
                throw new IllegalStateException(1 + count + "th solution: "
                        + failed.toString());
            }

        }

        return count;
    }

    private static long solve(final String name) throws CSPParseException,
            IOException, FailedGenerationException {
        final cspom.CSPOM cspomProblem = cspom.CSPOM.load(name);
        ProblemCompiler.compile(cspomProblem);
        final Problem problem = ProblemGenerator.generate(cspomProblem);
        // System.out.println(problem);

        final MGACIter solver = new MGACIter(problem, new CrossHeuristic(
                new DDegOnDom(problem), new Lexico(false)), new AC3(problem));

        long time = -System.currentTimeMillis();
        final Map<String, Integer> solution = solver.nextSolution();
        time += System.currentTimeMillis();
        if (solution == null) {
            return time;
        }
        final Map<String, Number> numSolution = new LinkedHashMap<String, Number>(
                solution.size());
        for (Entry<String, Integer> e : solution.entrySet()) {
            numSolution.put(e.getKey(), e.getValue());
        }
        final Collection<CSPOMConstraint> failed = cspomProblem
                .control(numSolution);
        if (!failed.isEmpty()) {
            throw new IllegalStateException(failed.toString());
        }

        return time;
    }

    private static long solve2(final String name) throws CSPParseException,
            IOException, FailedGenerationException {
        final cspom.CSPOM cspomProblem = cspom.CSPOM.load(name);
        ProblemCompiler.compile(cspomProblem);
        final Problem problem = ProblemGenerator.generate(cspomProblem);
        // System.out.println(problem);

        final MGACIter solver = new MGACIter(problem, new CrossHeuristic(
                new DDegOnDom(problem), new Lexico(false)), new AC3Constraint(
                problem));

        long time = -System.currentTimeMillis();
        final Map<String, Integer> solution = solver.nextSolution();
        time += System.currentTimeMillis();
        if (solution == null) {
            return time;
        }
        final Map<String, Number> numSolution = new LinkedHashMap<String, Number>(
                solution.size());
        for (Entry<String, Integer> e : solution.entrySet()) {
            numSolution.put(e.getKey(), e.getValue());
        }
        final Collection<CSPOMConstraint> failed = cspomProblem
                .control(numSolution);
        if (!failed.isEmpty()) {
            throw new IllegalStateException(failed.toString());
        }

        return time;
    }

    private static String format(int number) {
        if (number < 1000) {
            return "\\np{" + number + "}";
        }
        if (number < 10000) {
            return "\\np[k]{" + Math.round(number / 100d) / 10d + "}";
        }
        if (number < 1000000) {
            return "\\np[k]{" + Math.round(number / 1000d) + "}";
        }
        if (number < 10000000) {
            return "\\np[M]{" + Math.round(number / 100000d) / 10d + "}";
        }
        return "\\np[M]{" + Math.round(number / 1000000d) + "}";
    }

    public static void main(String[] args) throws CSPParseException,
            IOException, FailedGenerationException {
        final String problem = "/home/vion/CPAI08/schurrLemma/normalized-lemma-12-9-mod.xml.bz2";

        Logger.getLogger("").setLevel(Level.WARNING);
        solve(problem);
        Fifo.insert = 0;
        Fifo.update = 0;
        Fifo.remove = 0;
        System.out.print("{\\em " + problem + "} & \\np[s]{");

        System.out.print(Math.round(solve(problem) / 100d) / 10d + "} & "
                + format(Fifo.insert) + " & " + format(Fifo.update) + " & "
                + format(Fifo.remove) + " & \\np[s]{");

        Fifo.insert = 0;
        Fifo.update = 0;
        Fifo.remove = 0;
        System.out.print(Math.round(solve2(problem) / 100d) / 10d + "} & "
                + format(Fifo.insert) + " & " + format(Fifo.update) + " & "
                + format(Fifo.remove) + " \\\\");

    }
}
