package xcsp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bigleq.BigLeq;
import cspfj.MGACIter;
import cspfj.constraint.Constraint;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.AC3;
import cspfj.filter.AC3Constraint;
import cspfj.generator.ProblemGenerator;
import cspfj.heuristic.CrossHeuristic;
import cspfj.heuristic.DDegOnDom;
import cspfj.heuristic.Lexico;
import cspfj.heuristic.WDegOnDom;
import cspfj.priorityqueues.BinaryHeap;
import cspfj.priorityqueues.BinomialHeap;
import cspfj.priorityqueues.FibonacciHeap;
import cspfj.priorityqueues.Fifo;
import cspfj.priorityqueues.Key;
import cspfj.priorityqueues.SoftHeap;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspom.CSPOM;
import cspom.CSPParseException;
import cspom.compiler.ProblemCompiler;
import cspom.constraint.CSPOMConstraint;

public class XCSPSolver {
    private static int count(final String name) throws CSPParseException,
            IOException, FailedGenerationException {
        final cspom.CSPOM cspomProblem = cspom.CSPOM.load(name);
        ProblemCompiler.compile(cspomProblem);
        final Problem problem = ProblemGenerator.generate(cspomProblem);
        // System.out.println(problem);

        final MGACIter solver = new MGACIter(problem);

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

    private final static Pattern PATT = Pattern
            .compile("bigleq\\-(\\d*)\\.xml");

    private static Problem load(final String name)
            throws FailedGenerationException, CSPParseException, IOException {
        final Matcher matcher = PATT.matcher(name);
        if (matcher.find()) {
            final int nb = Integer.valueOf(matcher.group(1));
            return BigLeq.bigleq(nb, nb);

        }

        final CSPOM cspomProblem = cspom.CSPOM.load(name);
        ProblemCompiler.compile(cspomProblem);
        return ProblemGenerator.generate(cspomProblem);

    }

    private static long solveVar(final String name, final Queue<Variable> queue)
            throws CSPParseException, IOException, FailedGenerationException {

        final Problem problem = load(name);

        final MGACIter solver = new MGACIter(problem, new CrossHeuristic(
                new WDegOnDom(problem), new Lexico(false)), new AC3(problem,
                queue));
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        long time = -System.currentTimeMillis();
        solver.nextSolution();
        time += System.currentTimeMillis();
        return time;
    }

    private static long solveCons(final String name,
            final Queue<Constraint> queue) throws CSPParseException,
            IOException, FailedGenerationException {
        final Problem problem = load(name);
        final MGACIter solver = new MGACIter(problem, new CrossHeuristic(
                new DDegOnDom(problem), new Lexico(false)), new AC3Constraint(
                problem, queue));

        System.gc();
        System.gc();
        System.gc();
        System.gc();
        System.gc();
        long time = -System.currentTimeMillis();
        solver.nextSolution();
        time += System.currentTimeMillis();
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

    private static void nbC(final String problem) throws CSPParseException,
            IOException, FailedGenerationException {
        final Problem csp = load(problem);
        System.out.print(csp.getNbConstraints());
        final MGACIter solver = new MGACIter(csp, new CrossHeuristic(
                new WDegOnDom(csp), new Lexico(false)));
        solver.nextSolution();
    }

    public static void main(String[] args) throws CSPParseException,
            IOException, FailedGenerationException, IllegalArgumentException,
            SecurityException, InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        Logger.getLogger("").setLevel(Level.WARNING);

        final Key<Constraint> key = new Key<Constraint>() {

            @Override
            public float getKey(Constraint object) {
                return object.getEvaluation();
            }

        };

        for (String prob : Arrays.asList(
//                "bqwh-18-141/normalized-bqwh-18-141-0_ext",
//                "bqwh-18-141_glb/normalized-bqwh-18-141-2_glb",
//                "frb40-19/normalized-frb40-19-1_ext",
//                "allIntervalSeries/normalized-series-15",
//                "golombRulerArity3/normalized-ruler-25-7-a3",
                "langford3/langford-3-10",
                "small/normalized-patat-02-small-3",
                "bmc/normalized-bmc-ibm-02-02",
                "lexHerald/normalized-crossword-m1-lex-15-04",
                "schurrLemma/normalized-lemma-23-3",
                "os-taillard-5/normalized-os-taillard-5-100-3",
                "rlfapScens/normalized-scen4", "normalized-bigleq-70")) {
            System.out.print("{\\em "
                    + prob.substring(prob.indexOf("/normalized-") + 12)
                    + "} & ");
            final String problem = "/home/vion/CPAI08/" + prob + ".xml.bz2";
            nbC(problem);
            // System.out.print("\\np[s]{"
            // + Math.round(solveVar(problem, new FibonacciHeap<Variable>(
            // new Key<Variable>() {
            // @Override
            // public float getKey(Variable object) {
            // return object.getDomainSize();
            // }
            // })) / 100d) / 10d + "}");
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveVar(problem, new FibonacciHeap<Variable>(
            // new Key<Variable>() {
            // @Override
            // public float getKey(Variable object) {
            // return object.getDomainSize()
            // / wDeg(object);
            // }
            // })) / 100d) / 10d + "}");
            //
            // /*
            // * Fifo Cons
            // */
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveCons(problem, new Fifo<Constraint>(
            // new Key<Constraint>() {
            // @Override
            // public float getKey(Constraint object) {
            // float size = 1;
            // for (Variable v : object.getScope()) {
            // size *= v.getDomainSize();
            // }
            // return size;
            // }
            // })) / 100d) / 10d + "}");
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveCons(problem, new Fifo<Constraint>(
            // new Key<Constraint>() {
            // @Override
            // public float getKey(Constraint object) {
            // float size = 1;
            // for (Variable v : object.getScope()) {
            // size *= v.getDomainSize();
            // }
            // return size / object.getWeight();
            // }
            // })) / 100d) / 10d + "}");
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveCons(problem, new Fifo<Constraint>(
            // new Key<Constraint>() {
            // @Override
            // public float getKey(Constraint object) {
            // return object.getEvaluation();
            // }
            // })) / 100d) / 10d + "}");
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveCons(problem, new Fifo<Constraint>(
            // new Key<Constraint>() {
            // @Override
            // public float getKey(Constraint object) {
            // return object.getEvaluation()
            // / object.getWeight();
            // }
            // })) / 100d) / 10d + "}");
            //
            // /*
            // * Binomial !
            // */
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveCons(problem,
            // new FibonacciHeap<Constraint>(
            // new Key<Constraint>() {
            // @Override
            // public float getKey(Constraint object) {
            // float size = 1;
            // for (Variable v : object.getScope()) {
            // size *= v.getDomainSize();
            // }
            // return size;
            // }
            // })) / 100d) / 10d + "}");
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveCons(problem,
            // new FibonacciHeap<Constraint>(
            // new Key<Constraint>() {
            // @Override
            // public float getKey(Constraint object) {
            // float size = 1;
            // for (Variable v : object.getScope()) {
            // size *= v.getDomainSize();
            // }
            // return size / object.getWeight();
            // }
            // })) / 100d) / 10d + "}");
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveCons(problem,
            // new FibonacciHeap<Constraint>(
            // new Key<Constraint>() {
            // @Override
            // public float getKey(Constraint object) {
            // return object.getEvaluation();
            // }
            // })) / 100d) / 10d + "}");
            // System.out.print(" & \\np[s]{"
            // + Math.round(solveCons(problem,
            // new FibonacciHeap<Constraint>(
            // new Key<Constraint>() {
            // @Override
            // public float getKey(Constraint object) {
            // return object.getEvaluation()
            // / object.getWeight();
            // }
            // })) / 100d) / 10d + "}");
//            System.out
//                    .print(" & \\np[s]{"
//                            + Math.round(solveCons(problem,
//                                    new Fifo<Constraint>(key)) / 100d) / 10d
//                            + "}");
            System.out.print(" & \\np[s]{"
                    + Math.round(solveCons(problem, new BinaryHeap<Constraint>(
                            key)) / 100d) / 10d + "}");
            System.out.print(" & \\np[s]{"
                    + Math.round(solveCons(problem,
                            new BinomialHeap<Constraint>(key)) / 100d) / 10d
                    + "}");
            System.out.print(" & \\np[s]{"
                    + Math.round(solveCons(problem,
                            new FibonacciHeap<Constraint>(key)) / 100d) / 10d
                    + "}");
            System.out.print(" & \\np[s]{"
                    + Math.round(solveCons(problem, new SoftHeap<Constraint>(
                            key)) / 100d) / 10d + "}");

            System.out.println("\\\\");
        }

    }

    public static float wDeg(final Variable variable) {
        float count = 0;

        for (Constraint c : variable.getInvolvingConstraints()) {
            // count += (nbUnboundVariables(c) - 1) * c.getWeight();
            if (!c.isEntailed()) {
                count += c.getWeight();
            }
        }
        return count;
    }
}
