package rb;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import rb.RBGenerator.Tightness;
import rb.randomlists.RandomListGenerator.Structure;
import cspfj.AbstractSolver;
import cspfj.filter.AC3;
import cspfj.filter.DC1;
import cspfj.filter.Filter;
import cspfj.filter.SAC1;

public class FindThreshold {
    private static final int INST = 100;

    public static void main(String[] args) throws NumberFormatException,
            IOException {
        Logger.getLogger("").setLevel(Level.WARNING);
        final int nbVars = Integer.valueOf(args[0]);
//        final List<Class<? extends Filter>> list = Arrays.asList(AC3.class,
//                SAC1.class, DC1.class);
        final FileWriter fw = new FileWriter("threshold_50.dat");
        fw.write("constraints");
//        for (Class<? extends Filter> c : list) {
//            fw.write("\t" + c.getSimpleName());
//        }
        fw.write("\tGlobal");
        fw.write("\n");

        for (int e = 25; e <= (nbVars * (nbVars - 1)) / 2; e += 20) {
            fw.write(Integer.toString(e));
            // for (Class<? extends Filter> clazz : list) {
            //
            // double lb = .08;
            // double ub = 1;
            //
            // while (ub - lb > .0001) {
            // final double test = (ub + lb) / 2;
            // System.out.print("[" + lb + ", " + ub + "] " + test + ": ");
            // final int unsat = new RB(nbVars, Integer.valueOf(args[1]),
            // Integer.valueOf(args[2]), e, Tightness.PROBABILITY,
            // test, Structure.UNSTRUCTURED,
            // Structure.UNSTRUCTURED, false, false, INST, 0)
            // .runFilter(clazz);
            // System.out.println(unsat);
            // if (unsat <= INST / 2) {
            // lb = test;
            // } else {
            // ub = test;
            // }
            //
            // }
            // fw.write("\t" + 1000 * (lb + ub) / 2);
            // fw.flush();
            // }
            // AbstractSolver.PARAMETERS.put("dc.addConstraints", "EXT");
            // {
            // double lb = .08;
            // double ub = 1;
            //
            // while (ub - lb > .00001) {
            // final double test = (ub + lb) / 2;
            // System.out.print("[" + lb + ", " + ub + "] " + test + ": ");
            // final int unsat = new RB(Integer.valueOf(args[0]), Integer
            // .valueOf(args[1]), Integer.valueOf(args[2]), e,
            // Tightness.PROBABILITY, test,
            // Structure.UNSTRUCTURED, Structure.UNSTRUCTURED,
            // false, false, INST, 0).runFilter(DC1.class);
            // System.out.println(unsat);
            // if (unsat <= INST / 2) {
            // lb = test;
            // } else {
            // ub = test;
            // }
            //
            // }
            //
            // fw.write("\t" + 1000 * (lb + ub) / 2);
            // fw.flush();
            //
            // }
            // AbstractSolver.PARAMETERS.remove("dc.addConstraints");
            double lb = 0;
            double ub = 1;

            while (ub - lb > .00001) {
                final double test = (ub + lb) / 2;
                System.out.print("[" + lb + ", " + ub + "] " + test + ": ");
                final int unsat = new RB(Integer.valueOf(args[0]), Integer
                        .valueOf(args[1]), Integer.valueOf(args[2]), e,
                        Tightness.PROBABILITY, test, Structure.UNSTRUCTURED,
                        Structure.UNSTRUCTURED, false, false, INST, 0).run();
                System.out.println(unsat);
                if (unsat < INST / 2) {
                    lb = test;
                } else {
                    ub = test;
                }

            }

            fw.write("\t" + 1000 * (lb + ub) / 2);
            fw.flush();

            fw.write("\n");
        }

        fw.close();
    }

}
