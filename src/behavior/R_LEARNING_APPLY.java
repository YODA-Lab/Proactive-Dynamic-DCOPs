package behavior;

import agent.AgentPDDCOP;
import jade.core.behaviours.OneShotBehaviour;
import table.AugmentedState;

public class R_LEARNING_APPLY extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	AgentPDDCOP agent;

	private int currentTimeStep;
	
	public R_LEARNING_APPLY(AgentPDDCOP agent, int currentTimeStep) {
		super(agent);
		this.agent = agent;
		this.currentTimeStep = currentTimeStep;
	}

	@Override
	public void action() {
		AugmentedState solution = null;
		
		String currentState = agent.getPickedRandomAt(currentTimeStep);
		
		double maxUtil = -Double.MAX_VALUE;
		for (String decisionValue : agent.getSelfDomain()) {
			if (currentTimeStep == 0) {
				AugmentedState temp = AugmentedState.of(currentState, decisionValue);
				if (Double.compare(agent.getRFunction().get(temp), maxUtil) > 0) {
					maxUtil = agent.getRFunction().get(temp);
					solution = temp;
				}
			}
			else {
				String prev = agent.getChosenValueAtEachTimeStep(currentTimeStep - 1);
				AugmentedState temp = AugmentedState.of(currentState, prev, decisionValue);
				if (Double.compare(agent.getRFunction().get(temp), maxUtil) > 0) {
					maxUtil = agent.getRFunction().get(temp);
					solution = temp;
				}
			}
		}
		
		agent.storeDpopSolution(solution.getCurrent(), currentTimeStep);
	}
}
