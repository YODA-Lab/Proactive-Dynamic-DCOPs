package behavior;

import jade.core.behaviours.OneShotBehaviour;

import java.util.List;

import agent.AgentPDDCOP;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class LS_RAND_PICK_VALUE extends OneShotBehaviour {

	private static final long serialVersionUID = -6711542619242113965L;

	AgentPDDCOP agent;
	
	public LS_RAND_PICK_VALUE(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
	  agent.startSimulatedTiming();
		
		List<String> domain = agent.getDecisionVariableDomainMap().get(agent.getAgentID());
		
		for (int ts = 0; ts <= agent.getHorizon(); ts++) {
			if (agent.isDiscrete()) {
		     agent.getChosenValueAtEachTSMap().put(ts, domain.get(agent.getRandom().nextInt(domain.size())));
			}
			else if (agent.isContinuous()) {
			  double lower = agent.getDecisionVariableIntervalMap().get(agent.getAgentID()).getLowerBound();
			  double upper = agent.getDecisionVariableIntervalMap().get(agent.getAgentID()).getUpperBound();
			  
			  double randomSample = lower + Math.random() * (upper - lower);
			  agent.getChosenValueAtEachTSMap().put(ts, String.valueOf(randomSample));
			} 
		}
		
		agent.print("Chosen random values=" + agent.getChosenValueAtEachTSMap());
		
		agent.stopSimulatedTiming();
	}
}