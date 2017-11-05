package behaviour;

import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.Random;

import agent.ND_DCOP;

/**
 * @author khoihd
 *
 */
public class RAND_PICK_VALUE extends OneShotBehaviour {

	private static final long serialVersionUID = -6711542619242113965L;

	ND_DCOP agent;
	
	public RAND_PICK_VALUE(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		ArrayList<String> domain = agent.getDecisionVariableDomainMap().get(agent.getIdStr());
		int domainSize = domain.size();
		Random rdn = new Random();
		for (int ts=0; ts<=agent.h; ts++) {
			agent.getValueAtEachTSMap().put(ts,domain.get(rdn.nextInt(domainSize)));
		}
		
		agent.setSimulatedTime(agent.getSimulatedTime()
							+ agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
	}
}
