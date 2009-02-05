package ntdice;

import cspfj.constraint.AbstractPVRConstraint;
import cspfj.problem.Variable;

public class Leq extends AbstractPVRConstraint {

	private final Variable v0, v1;

	private int lastMin, lastMax;

	public Leq(final Variable v1, final Variable v2) {
		super(new Variable[] { v1, v2 });
		this.v0 = v1;
		this.v1 = v2;
		lastMin = Integer.MAX_VALUE;
		lastMax = Integer.MIN_VALUE;
	}

	public boolean revise(final int position, final int level) {
		boolean changed = false;

		if (position == 0) {
			final int[] domain = v0.getDomain();
			final int max = v1.getDomain()[v1.getLast()];
			if (lastMax == max) {
				return false;
			}
			for (int index = v0.getLast(); index >= 0; index = v0
					.getPrev(index)) {
				if (domain[index] <= max) {
					break;
				}
				v0.remove(index, level);
				changed = true;
			}
			lastMax = max;
		} else {
			final int[] domain = v1.getDomain();
			final int min = v0.getDomain()[v0.getFirst()];
			if (lastMin == min) {
				return false;
			}
			for (int index = v1.getFirst(); index >= 0; index = v1
					.getNext(index)) {
				if (domain[index] >= min) {
					break;
				}
				v1.remove(index, level);
				changed = true;

			}
			lastMin = min;
		}
		return changed;
	}

	@Override
	public boolean check() {
		return getValue(0) <= getValue(1);
	}

	@Override
	public void restore(int level) {
		super.restore(level);
		lastMin = Integer.MAX_VALUE;
		lastMax = Integer.MIN_VALUE;
	}

	@Override
	public boolean isSlow() {
		return false;
	}

}
