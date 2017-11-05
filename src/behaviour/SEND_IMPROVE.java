package behaviour;

import agent.ND_DCOP;
import jade.core.behaviours.OneShotBehaviour;

/**
 * @author khoihd
 *
 */
public class SEND_IMPROVE extends OneShotBehaviour {
	private static final long serialVersionUID = 6159093695904595420L;

	ND_DCOP agent;
	
	public SEND_IMPROVE(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		agent.sendImprove();
	}
}
