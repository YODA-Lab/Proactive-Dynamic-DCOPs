package behavior;

import agent.AgentPDDCOP;
import jade.core.behaviours.OneShotBehaviour;

/**
 * @author khoihd
 *
 */
public class SEND_IMPROVE extends OneShotBehaviour {
	private static final long serialVersionUID = 6159093695904595420L;

	AgentPDDCOP agent;
	
	public SEND_IMPROVE(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		agent.sendImprove();
	}
}
