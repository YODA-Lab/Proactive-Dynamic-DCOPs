package behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import agent.AgentPDDCOP;
import jade.core.behaviours.OneShotBehaviour;
import table.AugmentedState;
import table.TableString;

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
		
		String solutionPrev = agent.getChosenValueAtEachTimeStep(currentLearningTimeStep - 1);
		String solutionCurrentState = agent.getChosenValueAtEachTimeStep(currentLearningTimeStep);
		String solutionNextState =  agent.getChosenValueAtEachTimeStep(currentLearningTimeStep + 1);
		
		double alpha = agent.getAlpha_r();
		double beta = agent.getBeta_r();
		
		Map<AugmentedState, Double> r_function = agent.getRFunction();
		
		for (String decisionValue : agent.getDecisionVariableDomainMap().get(agent.getAgentID())) {
			double immediateReward = computeUtilityGivenCurrentState(decisionValue, agent.getActualDpopTableAcrossTimeStep(currentLearningTimeStep));
			
			AugmentedState updateState = currentLearningTimeStep == 0 ? 
											AugmentedState.of(currrentRandomState, decisionValue) :
											AugmentedState.of(currrentRandomState, solutionPrev, decisionValue);
			
			AugmentedState argmaxNextState = AugmentedState.of(nextRandomState, solutionCurrentState, solutionNextState);
			AugmentedState argmaxCurrentState = currentLearningTimeStep == 0 ?
											AugmentedState.of(currrentRandomState, solutionCurrentState) :
											AugmentedState.of(currrentRandomState, solutionPrev, solutionCurrentState);
			
			// Update R value
			double updatedR = r_function.get(updateState) * (1 - beta) +
									beta * (immediateReward - agent.getAverageRewardR() +
											r_function.get(argmaxNextState));
			r_function.put(updateState, updatedR);
			
			// Update average reward
			double updatedReward = agent.getAverageRewardR() * (1 - alpha) + alpha * (immediateReward + r_function.get(argmaxNextState) - r_function.get(argmaxCurrentState));
			agent.setAverageRewardR(updatedReward);	
		}
	}
	
	// Only apply to the unary constraint with the random variable
	private double computeUtilityGivenCurrentState(String decisionValue, List<TableString> tableList) {		
		String solutionPrev = agent.getChosenValueAtEachTimeStep(currentLearningTimeStep - 1);
		
		double switchCost = solutionPrev == null ? 0D : agent.switchingCostFunction(solutionPrev, decisionValue); 
		
		List<String> valueList = new ArrayList<>();
		valueList.add(decisionValue);
		
		for (TableString table : agent.getActualDpopTableAcrossTimeStep().get(currentLearningTimeStep)) {
			// Unary constraint after removing random state
			if (table.getDecVarLabel().size() == 1) {
				return table.getUtilityGivenDecValueList(valueList) - switchCost;
			}
		}
		
		return 0;
	}
}
