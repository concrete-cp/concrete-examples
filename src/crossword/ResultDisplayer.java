package crossword;

import java.io.IOException;
import java.util.Map;

import cspfj.ResultHandler;
import cspfj.problem.Variable;

public class ResultDisplayer extends ResultHandler {

	private final int x;

	private final int y;

	private final Variable[][] variables;

	public ResultDisplayer(int x, int y, Variable[][] variables) {
		super(true);
		this.x = x;
		this.y = y;
		this.variables = variables;
	}

	@Override
	public boolean solution(Map<Variable, Integer> solution, int nbSatisfied,
			boolean force) throws IOException {
		
		System.out.println(nbSatisfied + " constraints statisfied :");
		
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				System.out.print((char) (solution.get(variables[i][j])
						.intValue() + 65));
				System.out.print(" ");
			}
			System.out.println();
		}
		System.out.println();
		return super.solution(solution, nbSatisfied, force);

	}

}
