package behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import agent.AgentPDDCOP;
import jade.core.behaviours.OneShotBehaviour;
import table.AugmentedState;
import table.Table;

public class R_LEARNING_UPDATE extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	AgentPDDCOP agent;

	private int currentLearningTimeStep;
	
	public R_LEARNING_UPDATE(AgentPDDCOP agent, int currentLearningTimeStep) {
		super(agent);
		this.agent = agent;
		this.currentLearningTimeStep = currentLearningTimeStep;
	}
	
	@Override
	public void action() {
		String currrentRandomState = agent.getPickedRandomAt(currentLearningTimeStep);
		String nextRandomState = agent.getPickedRandomAt(currentLearningTimeStep + 1);
		
		String solutionNextState =  agent.getChosenValueAtEachTimeStep(currentLearningTimeStep);
		String solutionCurrentState = agent.getChosenValueAtEachTimeStep(currentLearningTimeStep + 1);
		
		double alpha = agent.getAlpha_r();
		double beta = agent.getBeta_r();
		
		Map<AugmentedState, Double> r_function = agent.getRFunction();
		
		//TODO: Take into account the previous solution to consider the switching cost
		for (String decisionValue : agent.getDecisionVariableDomainMap().get(agent.getAgentID())) {
			double immediateReward = computeUtilityGivenCurrentState(decisionValue, agent.getActualDpopTableAcrossTimeStep(currentLearningTimeStep));
			
			AugmentedState updateState = AugmentedState.of(currrentRandomState, decisionValue);
			
			AugmentedState argmaxNextState = AugmentedState.of(nextRandomState, solutionNextState);
			AugmentedState argmaxCurrentState = AugmentedState.of(currrentRandomState, solutionCurrentState);
			
			// Update R value
			double updatedR = r_function.get(updateState) * (1 - beta) + beta * (immediateReward - agent.getAverageRewardR() + r_function.get(argmaxNextState));
			r_function.put(updateState, updatedR);
			
			// Update average reward
			double updatedReward = agent.getAverageRewardR() * (1 - alpha) + alpha * (immediateReward + r_function.get(argmaxNextState) - r_function.get(argmaxCurrentState));
			agent.setAverageRewardR(updatedReward);	
		}
	}
	
	private double computeUtilityGivenCurrentState(String decisionValue, List<Table> tableList) {		
		List<String> valueList = new ArrayList<>();
		valueList.add(decisionValue);
		
		for (Table table : agent.getActualDpopTableAcrossTimeStep().get(currentLearningTimeStep)) {
			// Unary constraint after removing random state
			if (table.getDecVarLabel().size() == 1) {
				return table.getUtilityGivenDecValueList(valueList);
			}
		}
		
		return 0;
	}
}
