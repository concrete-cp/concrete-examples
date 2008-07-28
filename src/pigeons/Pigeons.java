package pigeons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cspfj.MGACIter;
import cspfj.ResultHandler;
import cspfj.Solver;
import cspfj.constraint.AbstractConstraint;
import cspfj.constraint.Constraint;
import cspfj.exception.FailedGenerationException;
import cspfj.problem.Problem;
import cspfj.problem.ProblemGenerator;
import cspfj.problem.Variable;

public class Pigeons implements ProblemGenerator {

	final private int size;

	final private List<Variable> variables;

	final private Collection<Constraint> constraints;

	public Pigeons(int size) {
		this.size = size;
		variables = new ArrayList<Variable>(size);
		constraints = new ArrayList<Constraint>();
	}

	public void generate() throws FailedGenerationException {
		final int[] domain = new int[size - 1];

		for (int i = size - 1; --i >= 0;) {
			domain[i] = i;
		}

		for (int i = size; --i >= 0;) {
			variables.add(new Variable(domain.clone(), "V" + i));
		}

		for (int i = size; --i >= 0;) {
			for (int j = size; --j >= i + 1;) {
				constraints.add(diff(variables.get(i), variables.get(j)));
			}
		}
	}

	private static Constraint diff(final Variable var1, final Variable var2)
			throws FailedGenerationException {
		return new DiffConstraint(new Variable[] { var1, var2 });
	}

	public List<Variable> getVariables() {
		return variables;
	}

	public Collection<Constraint> getConstraints() {
		return constraints;
	}

	public static void main(final String[] args) throws NumberFormatException,
			FailedGenerationException, IOException {
		final Problem problem = Problem.load(new Pigeons(Integer
				.parseInt(args[0])));

		final ResultHandler rh = new ResultHandler();

		final Solver solver = new MGACIter(problem, rh);
		final boolean result = solver.runSolver();
		System.out.println(result);
		if (result) {
			System.out.println(solver.getSolution());
		}
	}

	private static class DiffConstraint extends AbstractConstraint {
		public DiffConstraint(Variable[] scope) {
			super(scope);
		}

		@Override
		public boolean check() {
			return tuple[0] != tuple[1];
		}

	}

}
