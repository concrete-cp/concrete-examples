package ntdice;

import java.util.SortedSet;
import java.util.TreeSet;

import cspfj.constraint.AbstractConstraint;
import cspfj.problem.Variable;

public class Continuous extends AbstractConstraint {

	private final boolean[] nothing;

	public Continuous(final Variable[] scope) {
		super(scope);
		this.nothing = new boolean[scope.length];
	}

	private final SortedSet<Integer> values = new TreeSet<Integer>();

	public boolean[] revise(final int level) {
		values.clear();
		for (int i = getArity(); --i >= 0;) {
			final Variable v = getVariable(i);
			final int[] domain = v.getDomain();
			for (int index = v.getFirst(); index >= 0; index = v.getNext(index)) {
				values.add(domain[index]);
			}
		}

		for (int v : values) {
			if (!values.contains(v + 1) && v != values.last()) {
				return new boolean[0];
			}
		}
		return nothing;

	}

	@Override
	public boolean check() {
		values.clear();
		for (int i = getArity(); --i >= 0;) {
			values.add(getValue(i));
		}
		for (int v : values) {
			if (!values.contains(v + 1)) {
				for (int w : values) {
					if (w > v) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean isSlow() {
		return true;
	}

}
