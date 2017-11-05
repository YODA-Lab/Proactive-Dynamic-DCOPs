package behaviour;

import agent.ND_DCOP;
import jade.core.behaviours.OneShotBehaviour;

/**
 * @author khoihd
 *
 */
public class AGENT_TERMINATE extends OneShotBehaviour {

	private static final long serialVersionUID = -5079656778610995797L;

	ND_DCOP agent;
	
	public AGENT_TERMINATE(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		agent.doDelete();
	}
	
}
