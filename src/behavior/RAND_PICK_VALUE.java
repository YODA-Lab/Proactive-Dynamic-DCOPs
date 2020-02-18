package behavior;

import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import agent.AgentPDDCOP;

/**
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
//		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
	  agent.startSimulatedTiming();
		
		List<String> domain = agent.getDecisionVariableDomainMap().get(agent.getAgentID());
		int domainSize = domain.size();
		Random rdn = new Random();
		for (int ts=0; ts<=agent.getHorizon(); ts++) {
			agent.getChosenValueAtEachTSMap().put(ts,domain.get(rdn.nextInt(domainSize)));
		}
		
//		agent.setSimulatedTime(agent.getSimulatedTime()
//							+ agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		
		agent.stopStimulatedTiming();
	}
}
