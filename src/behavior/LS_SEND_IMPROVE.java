package behavior;

import agent.AgentPDDCOP;
import jade.core.behaviours.OneShotBehaviour;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class LS_SEND_IMPROVE extends OneShotBehaviour {
	private static final long serialVersionUID = 6159093695904595420L;

	private AgentPDDCOP agent;
	private int lastTimeStep;
	private int localTimeStep;
	
	public LS_SEND_IMPROVE(AgentPDDCOP agent, int lastTimeStep, int localTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
		this.localTimeStep = localTimeStep;
	}
	
	@Override
	public void action() {
		agent.sendImprove(lastTimeStep);
    agent.print("is done SEND_IMPROVE at iteration: " + localTimeStep);
	}
}
