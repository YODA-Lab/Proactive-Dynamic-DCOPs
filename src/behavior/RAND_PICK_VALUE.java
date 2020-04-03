package behavior;

import jade.core.behaviours.OneShotBehaviour;

import java.util.List;

import agent.AgentPDDCOP;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class RAND_PICK_VALUE extends OneShotBehaviour {

	private static final long serialVersionUID = -6711542619242113965L;

	AgentPDDCOP agent;
	
	public RAND_PICK_VALUE(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
	  agent.startSimulatedTiming();
		
		List<String> domain = agent.getDecisionVariableDomainMap().get(agent.getAgentID());
		
		for (int ts = 0; ts <= agent.getHorizon(); ts++) {
			agent.getChosenValueAtEachTSMap().put(ts, domain.get(agent.getRandom().nextInt(domain.size())));
		}
		
		agent.print("choose random values=" + agent.getChosenValueAtEachTSMap());
		
		agent.stopStimulatedTiming();
	}
}
