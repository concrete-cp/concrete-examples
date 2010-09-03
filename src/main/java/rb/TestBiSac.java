package rb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import rb.RBGenerator.Tightness;
import rb.randomlists.RandomListGenerator.Structure;
import cspfj.constraint.Constraint;
import cspfj.constraint.extension.ExtensionConstraint2D;
import cspfj.exception.FailedGenerationException;
import cspfj.filter.BiSAC;
import cspfj.filter.DC1;
import cspfj.generator.ProblemGenerator;
import cspfj.problem.Problem;
import cspfj.problem.Variable;
import cspom.CSPOM;
import cspom.DuplicateVariableException;
import cspom.constraint.CSPOMConstraint;
import cspom.extension.Extension;
import cspom.extension.ExtensionConstraint;
import cspom.variable.CSPOMVariable;

public class TestBiSac {
    // private final static Random RAND = new Random();
    private final static int NB_VARS = 30;
    private final static int NB_VALS = 15;

    // private final static int NB_CONS = 6;

    public static void drawVar(Variable var, String orientation,
            String position, String labelPos) {
        final int first = var.getFirst();
        System.out.println("\\node (" + var.getName() + var.getValue(first)
                + ") [" + position + "] {" + var.getValue(first) + "};");
        for (int i = var.getNext(first); i >= 0; i = var.getNext(i)) {
            System.out.println("\\node (" + var.getName() + var.getValue(i)
                    + ") [" + orientation + "=.5em of " + var.getName()
                    + var.getValue(var.getPrev(i)) + "] {" + var.getValue(i)
                    + "};");
        }
        System.out.println("\\begin{pgfonlayer}{background}");
        System.out.print("\\node(" + var.getName() + ") [draw,ellipse,fit=");
        for (int i = first; i >= 0; i = var.getNext(i)) {
            System.out.print("(" + var.getName() + var.getValue(i) + ")");
        }
        System.out.println(",label=" + labelPos + ":$" + var.getName()
                + "$] {};");
        System.out.println("\\end{pgfonlayer}");
    }

    public static void draw(Problem problem) {
        for (Variable x : problem.getVariables()) {
            for (Constraint c : x.getInvolvingConstraints()) {
                if (c.getScope()[0] != x) {
                    continue;
                }
                final int[] tuple = c.getTuple();
                final Variable y = c.getScope()[1];
                for (tuple[0] = x.getFirst(); tuple[0] >= 0; tuple[0] = x
                        .getNext(tuple[0])) {
                    for (tuple[1] = y.getFirst(); tuple[1] >= 0; tuple[1] = y
                            .getNext(tuple[1])) {
                        if (!c.check()) {
                            System.out
                                    .println("\\draw (" + x.getName()
                                            + x.getValue(tuple[0]) + ")--("
                                            + y.getName()
                                            + y.getValue(tuple[1]) + ");");
                        }
                    }
                }
            }
        }
    }

    private static boolean fullDomains(Problem problem) {
        for (Variable v : problem.getVariables()) {
            if (v.getDomainSize() < NB_VALS) {
                return false;
            }
        }
        return true;
    }

    private static boolean controlDomains(Problem problem,
            Map<Variable, Integer> domainSizes) {
        for (Variable v : problem.getVariables()) {
            if (v.getDomainSize() != domainSizes.get(v)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws DuplicateVariableException,
            FailedGenerationException, InterruptedException {
        main1(args);
    }

    public static void main2(String[] args) throws DuplicateVariableException,
            FailedGenerationException, InterruptedException {
        Logger.getLogger("").setLevel(Level.WARNING);

        final CSPOM cspom = new CSPOM();
        final CSPOMVariable xi = cspom.var("Xi", 1, 2);
        final CSPOMVariable xj = cspom.var("Xj", 1, 3);
        final CSPOMVariable xk = cspom.var("Xk", 1, 3);
        final CSPOMVariable xl = cspom.var("Xl", 1, 3);
        final CSPOMVariable xm = cspom.var("Xm", 1, 3);
        final CSPOMVariable xn = cspom.var("Xn", 1, 3);

        final Extension<Integer> extIJ = new Extension<Integer>(2, true);
        extIJ.addTuple(1, 1);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extIJ, xi, xj));

        final Extension<Integer> extIK = new Extension<Integer>(2, true);
        extIK.addTuple(1, 1);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extIK, xi, xk));

        final Extension<Integer> extIN = new Extension<Integer>(2, true);
        extIN.addTuple(1, 1);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extIN, xi, xn));

        final Extension<Integer> extJK = new Extension<Integer>(2, true);
        extJK.addTuple(1, 1);
        extJK.addTuple(2, 2);
        extJK.addTuple(3, 3);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extJK, xj, xk));

        final Extension<Integer> extJM = new Extension<Integer>(2, true);
        extJM.addTuple(2, 3);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extJM, xj, xm));

        final Extension<Integer> extJN = new Extension<Integer>(2, true);
        extJN.addTuple(1, 1);
        extJN.addTuple(2, 2);
        extJN.addTuple(3, 3);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extJN, xj, xn));

        final Extension<Integer> extKL = new Extension<Integer>(2, true);
        extKL.addTuple(2, 2);
        extKL.addTuple(2, 3);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extKL, xk, xl));

        final Extension<Integer> extKM = new Extension<Integer>(2, true);
        extKM.addTuple(2, 1);
        extKM.addTuple(3, 1);
        extKM.addTuple(3, 2);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extKM, xk, xm));

        final Extension<Integer> extLM = new Extension<Integer>(2, true);
        extLM.addTuple(1, 2);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extLM, xl, xm));

        final Extension<Integer> extMN = new Extension<Integer>(2, true);
        extMN.addTuple(3, 2);
        cspom.addConstraint(new ExtensionConstraint<Integer>(extMN, xm, xn));

        final Problem problem = ProblemGenerator.generate(cspom);
        problem.prepare();

        System.out.println("\\begin{tikzpicture}");
        drawVar(problem.getVariable(xj.getName()), "below right", "", "above");
        drawVar(problem.getVariable(xi.getName()), "below",
                "right=10em of Xj3", "left");
        drawVar(problem.getVariable(xn.getName()), "right",
                "above=10em of Xj1", "right");
        drawVar(problem.getVariable(xk.getName()), "above right",
                "below=10em of Xj1", "below");
        drawVar(problem.getVariable(xm.getName()), "above right",
                "left=10em of Xj1", "right");
        drawVar(problem.getVariable(xl.getName()), "above left",
                "below=10em of Xm1", "right");

        draw(problem);
        System.out.println("\\end{tikzpicture}");

        problem.prepare();
        System.out.println(new BiSAC(problem).control());

        System.out.println("\\begin{tikzpicture}");
        drawVar(problem.getVariable(xj.getName()), "below right", "", "above");
        drawVar(problem.getVariable(xi.getName()), "below",
                "right=10em of Xj3", "left");
        drawVar(problem.getVariable(xn.getName()), "right",
                "above=10em of Xj1", "right");
        drawVar(problem.getVariable(xk.getName()), "above right",
                "below=10em of Xj1", "below");
        drawVar(problem.getVariable(xm.getName()), "above right",
                "left=10em of Xj1", "right");
        drawVar(problem.getVariable(xl.getName()), "above left",
                "below=10em of Xm1", "right");

        draw(problem);
        System.out.println("\\end{tikzpicture}");

    }

    private static class Params {
        int nbCons;
        double tight;
        long seed;

        public Params(int nbCons, double tight, long seed) {
            this.nbCons = nbCons;
            this.tight = tight;
            this.seed = seed;
        }

        public String toString() {
            return "<" + nbCons + ", " + tight + " ("
                    + Long.toHexString(Double.doubleToLongBits(tight)) + "), "
                    + seed + ">";
        }
    }

    private static Params findProblem() throws FailedGenerationException,
            InterruptedException {

        for (int nbCons = NB_VARS; nbCons <= (NB_VARS * (NB_VARS - 1)) / 2; nbCons += 1) {
            for (double tight = .03; tight < 1; tight += .005) {
                System.out.println(nbCons + ", " + tight);

                for (long seed = 200; --seed >= 0;) {
                    final Params params = new Params(nbCons, tight, seed);

                    final Problem problem = ProblemGenerator
                            .generate(new RBGenerator(NB_VARS, NB_VALS, 2,
                                    nbCons, Tightness.PROBABILITY, tight, seed,
                                    Structure.UNSTRUCTURED,
                                    Structure.UNSTRUCTURED, false, false)
                                    .generate());
                    problem.prepare();
                    if (!new DC1(problem).reduceAll()) {
                        continue;
                    }
                    if (!new BiSAC(problem).control()) {
                        return params;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Generates the constraint network graph in the GML format. N-ary
     * constraints are represented as nodes.
     * 
     * @return a String containing the GML representation of the constraint
     *         network.
     */
    public static String toGML(Problem problem) {
        final StringBuilder stb = new StringBuilder();
        stb.append("graph [\n");
        stb.append("directed 0\n");
        for (Variable v : problem.getVariables()) {
            stb.append("node [\n");
            stb.append("id \"").append(v.getName()).append("\"\n");
            stb.append("label \"").append(v.getName()).append("\"\n");
            stb.append("]\n");
        }

        for (Constraint c : problem.getConstraints()) {
            if (c.isEntailed()) {
                continue;
            }
            stb.append("edge [\n");
            stb.append("source \"").append(c.getScope()[0].getName())
                    .append("\"\n");
            stb.append("target \"").append(c.getScope()[1].getName())
                    .append("\"\n");
            // stb.append("label \"").append(c.getDescription()).append("\"\n");
            stb.append("]\n");

        }
        stb.append("]\n");

        return stb.toString();
    }

    private static class Pair {
        final String var;
        final int i;

        public Pair(String v, int i) {
            this.var = v;
            this.i = i;
        }

        public String toString() {
            return var + ", " + i;
        }
    }

    private static class Good {
        final List<String> scope;
        final int[] tuple;

        public Good(Constraint c, int i1, int i2) {
            scope = Arrays.asList(c.getScope()[0].getName(),
                    c.getScope()[1].getName());
            this.tuple = new int[] { i1, i2 };
        }

        public String toString() {
            return scope + ": (" + tuple[0] + ", " + tuple[1] + ")";
        }

    }

    private static Problem obtainProblem(Params params,
            Collection<String> keepCol, Set<Pair> removeVal,
            Set<Good> removeGood) throws FailedGenerationException {
        final CSPOM cspom = new RBGenerator(NB_VARS, NB_VALS, 2, params.nbCons,
                Tightness.PROBABILITY, params.tight, params.seed,
                Structure.UNSTRUCTURED, Structure.UNSTRUCTURED, false, false)
                .generate();

        final Set<String> keep = new HashSet<String>(keepCol);

        for (CSPOMVariable var : new ArrayList<CSPOMVariable>(
                cspom.getVariables())) {
            if (keep.contains(var.getName())) {
                continue;
            }

            for (CSPOMConstraint c : new ArrayList<CSPOMConstraint>(
                    var.getConstraints())) {
                cspom.removeConstraint(c);
            }
            cspom.removeVariable(var);
        }
        final Problem problem = ProblemGenerator.generate(cspom);
        problem.prepare();
        for (Pair p : removeVal) {
            problem.getVariable(p.var).remove(p.i);
        }
        for (Good g : removeGood) {
            ((ExtensionConstraint2D) cons(problem, g.scope))
                    .removeTuple(g.tuple);
        }
        return problem;
    }

    private static Constraint cons(Problem problem, List<String> scope) {
        for (Constraint c : problem.getConstraints()) {
            if (c.getScope()[0].getName().equals(scope.get(0))
                    && c.getScope()[1].getName().equals(scope.get(1))) {
                return c;
            }
        }
        return null;
    }

    public static void main1(String[] args) throws FailedGenerationException,
            InterruptedException {
        Logger.getLogger("").setLevel(Level.WARNING);

        final Params params = new Params(30, Double.longBitsToDouble(Long
                .decode("0x3fe947ae147ae14d")), 149);

        // System.out.println(params);

        if (params != null) {
            final Collection<String> keep = Arrays.asList("X4", "X6", "X14",
                    "X15", "X18", "X19", "X21", "X24");
            final Set<Pair> removeVals = new HashSet<Pair>();
            final Set<Good> removeGoods = new HashSet<Good>();

            final List<Pair> allPairs = new ArrayList<Pair>();
            {
                final Problem problem = obtainProblem(params, keep, removeVals,
                        removeGoods);

                problem.prepare();
                if (!new DC1(problem).reduceAll()) {
                    throw new IllegalStateException();
                }
                for (Variable v : problem.getVariables()) {
                    for (int i = v.getFirst(); i >= 0; i = v.getNext(i)) {
                        allPairs.add(new Pair(v.getName(), i));
                    }
                }

                Collections.shuffle(allPairs, new Random(1));
            }
            for (Pair p : allPairs) {
                removeVals.add(p);
                final Problem prob = obtainProblem(params, keep, removeVals,
                        removeGoods);
                if (!new DC1(prob).reduceAll()) {
                    System.out.println(p + ": NOK");
                    removeVals.remove(p);
                } else if (new BiSAC(prob).control()) {
                    System.out.println(p + ": NOK");
                    removeVals.remove(p);
                } else {
                    System.out.println(p + ": OK");
                }
            }

            final List<Good> goods = new ArrayList<Good>();
            {
                final Problem problem = obtainProblem(params, keep, removeVals,
                        removeGoods);
                new DC1(problem).reduceAll();
                for (Constraint c : problem.getConstraints()) {
                    for (int i0 = c.getScope()[0].getFirst(); i0 >= 0; i0 = c
                            .getScope()[0].getNext(i0)) {
                        for (int i1 = c.getScope()[1].getFirst(); i1 >= 0; i1 = c
                                .getScope()[1].getNext(i1)) {
                            c.getTuple()[0] = i0;
                            c.getTuple()[1] = i1;
                            if (c.check()) {
                                goods.add(new Good(c, i0, i1));
                            }
                        }
                    }
                }
            }

            for (Good g : goods) {
                removeGoods.add(g);
                final Problem prob = obtainProblem(params, keep, removeVals,
                        removeGoods);

                if (!new DC1(prob).reduceAll()) {
                    System.out.println(g + ": NOK");
                    removeGoods.remove(g);
                } else if (new BiSAC(prob).control()) {
                    System.out.println(g + ": NOK");
                    removeGoods.remove(g);
                } else {
                    System.out.println(g + ": OK");
                }
            }
            // for (Constraint c : problem.getConstraints()) {
            // problem.push();
            // c.entail();
            // if (new BiSAC(problem).control()) {
            // System.out.println(c + ": NOK");
            // problem.pop();
            // } else {
            // problem.pop();
            // System.out.println(c + ": OK");
            // c.entail();
            // }
            // }

            // if (!new BiSAC(problem).control()) {
            // System.out.println("OK");
            // }
            final Problem problem = obtainProblem(params, keep, removeVals,
                    removeGoods);
            new DC1(problem).reduceAll();
            problem.push();
            if (!new BiSAC(problem).control()) {
                System.out.println("OK");
            }
            problem.pop();

            for (Variable v : problem.getVariables()) {
                System.out.print("<domain name='D" + v.getName()
                        + "' nbValues='" + v.getDomainSize() + "'>");
                for (int i = v.getFirst(); i >= 0; i = v.getNext(i)) {
                    System.out.print(i + " ");
                }
                System.out.println("</domain>");
            }
            for (Variable v : problem.getVariables()) {
                System.out.println("<variable name='" + v.getName()
                        + "' domain='D" + v.getName() + "'/>");
            }
            for (Constraint c : problem.getConstraints()) {
                final List<int[]> tuples = new ArrayList<int[]>();
                for (int i0 = c.getScope()[0].getFirst(); i0 >= 0; i0 = c
                        .getScope()[0].getNext(i0)) {
                    for (int i1 = c.getScope()[1].getFirst(); i1 >= 0; i1 = c
                            .getScope()[1].getNext(i1)) {
                        c.getTuple()[0] = i0;
                        c.getTuple()[1] = i1;
                        if (c.check()) {
                            tuples.add(c.getTuple().clone());
                        }
                    }
                }

                System.out.println("<relation name='R" + c.getId()
                        + "' arity='2' nbTuples='" + tuples.size()
                        + "' semantics='supports'>");
                boolean first = true;
                for (int[] t : tuples) {
                    if (!first) {
                        System.out.print("|");
                    }
                    System.out.print(t[0] + " " + t[1]);
                    first = false;
                }
                System.out.println();
                System.out.println("</relation>");
            }

            for (Constraint c : problem.getConstraints()) {
                System.out.println("<constraint name='C" + c.getId()
                        + "' arity='2' scope='" + c.getScope()[0].getName()
                        + " " + c.getScope()[1].getName() + "' reference='R"
                        + c.getId() + "'/>");
            }
            // System.out.println(toGML(problem));
        }
        // System.out.println("\\begin{tikzpicture}");
        // drawVar(problem.getVariable("_0"), "right", "", "above");
        // drawVar(problem.getVariable("_1"), "below",
        // "below left=10em of X0", "left");
        // drawVar(problem.getVariable("_2"), "below",
        // "below right=10em of X0" + (NB_VALS - 1), "right");
        // // drawVar("X3", "right", "below right=10em of X1" + (NB_VALS -
        // 1),
        // // "below");
        //
        // draw(problem);
        // System.out.println("\\end{tikzpicture}");

    }
}
