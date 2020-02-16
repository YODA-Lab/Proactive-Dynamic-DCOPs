package behavior;

import agent.AgentPDDCOP;
import jade.core.behaviours.OneShotBehaviour;

/**
 * @author khoihd
 *
 */
public class SEND_IMPROVE extends OneShotBehaviour {
	private static final long serialVersionUID = 6159093695904595420L;

	private AgentPDDCOP agent;
	private int lastTimeStep;
	
	public SEND_IMPROVE(AgentPDDCOP agent, int lastTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
	}
	
	@Override
	public void action() {
//		agent.archivedSendImprove(lastTimeStep);
		agent.sendImprove(lastTimeStep);
	}
}
