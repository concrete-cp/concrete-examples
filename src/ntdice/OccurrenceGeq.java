package ntdice;

import cspfj.constraint.AbstractConstraint;
import cspfj.problem.Variable;

public class OccurrenceGeq extends AbstractConstraint {

	private final int value, count;

	private final boolean[] nothing;

	private final boolean[] avail;

	public OccurrenceGeq(final Variable[] scope, final int value,
			final int count) {
		super(scope, false);
		this.value = value;
		this.count = count;

		this.nothing = new boolean[scope.length];
		this.avail = new boolean[scope.length];
	}

	public boolean[] revise(final int level) {
		int available = 0;

		for (int i = getArity(); --i >= 0;) {
			final Variable v = getVariable(i);
			final int[] d = v.getDomain();
			avail[i] = false;
			for (int index = v.getFirst(); index >= 0; index = v.getNext(index)) {
				if (d[index] == value) {

					if (++available > this.count) {
						return nothing;
					}
					avail[i] = true;
					break;
				}
			}
		}

		if (available == this.count) {
			assign(level);
			return avail;
		}
		return new boolean[0];

	}



	private void assign(final int level) {
		for (int i = getArity(); --i >= 0;) {
			if (!avail[i] || getVariable(i).isAssigned()) {
				avail[i] = false;
				continue;
			}
			avail[i] = false;
			final Variable v = getVariable(i);
			for (int index = v.getFirst(); index != -1; index = v
					.getNext(index)) {
				if (v.getDomain()[index] != value) {
					v.remove(index, level);
					avail[i] = true;
				}
			}

		}
	}

	@Override
	public boolean check() {
		int count = this.count;
		for (int i = getArity(); --i >= 0;) {
			if (getValue(i) == value) {
				if (--count < 0) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public boolean isSlow() {
		return false;
	}

}
