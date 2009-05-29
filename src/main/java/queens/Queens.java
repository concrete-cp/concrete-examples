package queens;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cspfj.MGACIter;
import cspfj.Solver;
import cspfj.constraint.AbstractAC3Constraint;
import cspfj.constraint.Constraint;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;
import cspfj.problem.ProblemGenerator;
import cspfj.problem.Variable;

public class Queens implements ProblemGenerator {

    private final int size;
    private final Collection<Constraint> constraints;
    private final List<Variable> variables;

    public Queens(int size) {
        this.size = size;
        constraints = new ArrayList<Constraint>();
        variables = new ArrayList<Variable>();
    }

    @Override
    public void generate() {
        final int[] domain = new int[size];
        for (int i = 0; i < size; i++) {
            domain[i] = i;
        }
        for (int i = 0; i < size; i++) {
            variables.add(new Variable(domain));
        }
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                final int diff = Math.abs(i - j);
                constraints.add(new AbstractAC3Constraint(variables
                        .get(i), variables.get(j)) {
                    @Override
                    public boolean check() {
                        return getValue(0) != getValue(1)
                                && Math
                                        .abs(getValue(0)
                                                - getValue(1)) != diff;
                    }
                });
            }
        }
    }

    @Override
    public Collection<Constraint> getConstraints() {
        return constraints;
    }

    @Override
    public List<Variable> getVariables() {
        return variables;
    }

    public static void main(String[] args)
            throws FailedGenerationException, IOException {
        final ProblemGenerator generator = new Queens(Integer
                .valueOf(args[0]));
        final Solver solver = new MGACIter(Problem.load(generator));
        solver.runSolver();
        for (Variable v : generator.getVariables()) {
            System.out
                    .println(v + ": " + solver.getSolution().get(v));
        }
    }

}
