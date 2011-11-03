package groups;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.exception.FailedGenerationException;
import cspfj.generator.ProblemGenerator;
import cspfj.problem.Problem;
import cspom.CSPOM;
import cspom.compiler.PredicateParseException;
import cspom.compiler.ProblemCompiler;
import cspom.variable.CSPOMVariable;

public class Groups {
    private static final int NB_GROUPS = 2;

    private static final Collection<Etud> etuds = Arrays.asList(new Etud(
            "berling", 12.25), new Etud("quinzin", 9.68), new Etud("quintana",
            13.07), new Etud("rosin", 14.25), new Etud("wagnier", 7.21),
            new Etud("chaste", 13.04), new Etud("dewil", 10.50), new Etud(
                    "foulon", 13.57), new Etud("pruvost", 15.96), new Etud(
                    "valentin", 10.71), new Etud("stolies", 9.89), new Etud(
                    "dubrulle", 10.46), new Etud("houdelette", 10.68),
            new Etud("lecaillon", 10.75), new Etud("masclef", 10.36), new Etud(
                    "camberlin", 14.57), new Etud("vicogne", 14.54), new Etud(
                    "koma", 9.86));// , new Etud("koprowski", 13.82), new Etud(

    // "laigle", 10.68), new Etud("ledrogoff", 10.61), new Etud(
    // "dupire", 12.50), new Etud("lemmen", 10.50), new Etud(
    // "magniez", 13.79), new Etud("zaboitzeff", 13.75), new Etud(
    // "burillon", 12.39), new Etud("digeon", 15.61), new Etud(
    // "disoard", 11.79), new Etud("clerfayt", 12.54), new Etud(
    // "leroy", 10.14), new Etud("piette", 15.93));

    public static void main(String[] args) throws PredicateParseException,
            FailedGenerationException {
        final CSPOM cspom = new CSPOM();

        final Map<Etud, CSPOMVariable> groups = new HashMap<Etud, CSPOMVariable>();

        for (Etud e : etuds) {
            groups.put(e, cspom.var(e.nom, 1, NB_GROUPS));
        }

        final Iterator<CSPOMVariable> itr = groups.values().iterator();
        CSPOMVariable prev = itr.next();
        for (int i = NB_GROUPS; --i >= 0;) {
            final CSPOMVariable cur = itr.next();
            cspom.le(prev, cur);
            prev = cur;
        }

        final int minGroupSize = etuds.size() / NB_GROUPS;
        final int maxGroupSize;
        if (etuds.size() % NB_GROUPS == 0) {
            maxGroupSize = minGroupSize;
        } else {
            maxGroupSize = minGroupSize + 1;
        }

        cspom.ctr(gcc(minGroupSize, maxGroupSize, groups.values()));

        // final Collection<Collection<CSPOMVariable>> groupsMoys = new
        // ArrayList<Collection<CSPOMVariable>>();
        // for (int g = 1; g <= NB_GROUPS; g++) {
        // final Collection<CSPOMVariable> groupMoys = new
        // ArrayList<CSPOMVariable>();
        // groupsMoys.add(groupMoys);
        // for (Entry<Etud, CSPOMVariable> e : groups.entrySet()) {
        // final CSPOMVariable note = new CSPOMVariable(Arrays.asList(0,
        // e.getKey().moy));
        // cspom.addVariable(note);
        // }
        // }

        ProblemCompiler.compile(cspom);
        final Problem problem = ProblemGenerator.generate(cspom);
        int count = 0;
        final Solver s = new MGACIter(problem);
        for (;;) {
            Map<String, Integer> sol = s.nextSolution();
            if (sol == null) {
                break;
            }
            System.out.println(sol);
            count++;
        }

        System.out.println(count);
    }

    private static String gcc(int minGroupSize, int maxGroupSize,
            Collection<CSPOMVariable> groups) {
        final StringBuilder gcc = new StringBuilder().append("gcc{");
        for (int i = 1; i <= NB_GROUPS; i++) {
            gcc.append(i).append(", ").append(minGroupSize).append(", ")
                    .append(maxGroupSize);
            if (i < NB_GROUPS) {
                gcc.append(", ");
            }
        }
        gcc.append("}(");
        final Iterator<CSPOMVariable> e = groups.iterator();
        gcc.append(e.next());
        while (e.hasNext()) {
            gcc.append(", ").append(e.next());
        }
        return gcc.append(")").toString();
    }

    private static class Etud {
        String nom;
        double moy;

        public Etud(String nom, double moy) {
            this.nom = nom;
            this.moy = moy;
        }

    }
}
