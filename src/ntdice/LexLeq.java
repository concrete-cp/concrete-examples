package ntdice;

import java.util.Arrays;

import cspfj.constraint.AbstractConstraint;
import cspfj.problem.Variable;

public class LexLeq extends AbstractConstraint {

	private final Variable[] v0, v1;

	private final int q;

	private int alpha;

	private final boolean[] changed;

	public LexLeq(final Variable[] v0, final Variable[] v1) {
		super(merge(v0, v1, new Variable[v0.length + v1.length]), false);
		if (v0.length != v1.length) {
			throw new IllegalArgumentException();
		}
		this.v0 = v0;
		this.v1 = v1;
		this.q = v0.length;
		alpha = 0;
		changed = new boolean[2 * q];
	}

	private static <T> T[] merge(T[] v1, T[] v2, T[] into) {
		System.arraycopy(v1, 0, into, 0, v1.length);
		System.arraycopy(v2, 0, into, v1.length, v2.length);
		return into;
	}

	private boolean revise(final Variable v0, final Variable v1,
			final boolean reduceMin, final int level) {
		boolean changed = false;
		if (reduceMin) {
			if (v0.isAssigned()) {
				return false;
			}
			final int[] domain = v0.getDomain();
			final int max = v1.getDomain()[v1.getLast()];
			for (int index = v0.getLast(); index >= 0; index = v0
					.getPrev(index)) {
				if (domain[index] <= max) {
					break;
				}
				v0.remove(index, level);
				changed = true;
			}
		} else {
			if (v1.isAssigned()) {
				return false;
			}
			final int[] domain = v1.getDomain();
			final int min = v0.getDomain()[v0.getFirst()];
			for (int index = v1.getFirst(); index >= 0; index = v1
					.getNext(index)) {
				if (domain[index] >= min) {
					break;
				}
				v1.remove(index, level);
				changed = true;

			}
		}
		return changed;
	}

	
	public boolean[] revise(final int level) {
		Arrays.fill(changed, false);
		while (alpha < q) {
			if (revise(v0[alpha], v1[alpha], true, level)) {
				changed[alpha] = true;
				if (v0[alpha].getDomainSize() == 0) {
					return new boolean[0];
				}
			}
			if (revise(v0[alpha], v1[alpha], false, level)) {
				changed[q + alpha] = true;
				if (v1[alpha].getDomainSize() == 0) {
					return new boolean[0];
				}
			}
			if (v0[alpha].getDomainSize() != 1
					|| !sameDomain(v0[alpha], v1[alpha])) {
				break;
			}
			alpha++;
		}

		return changed;
	}

	private static boolean sameDomain(Variable v0, Variable v1) {
		if (v0.getDomainSize() != v1.getDomainSize()) {
			return false;
		}
		int index0 = v0.getFirst();
		int index1 = v1.getFirst();
		int[] d0 = v0.getDomain();
		int[] d1 = v1.getDomain();
		while (index0 >= 0) {
			if (d0[index0] != d1[index1]) {
				return false;
			}
			index0 = v0.getNext(index0);
			index1 = v1.getNext(index1);
		}
		return true;
	}

	@Override
	public boolean check() {
		for (int i = 0; i < v0.length; i++) {
			if (getValue(i) < getValue(i + v0.length)) {
				return true;
			}
		}
		return getValue(v0.length - 1) == getValue(getArity() - 1);
	}

	@Override
	public void restore(int level) {
		alpha = 0;
	}

	@Override
	public boolean isSlow() {
		return true;
	}

}
