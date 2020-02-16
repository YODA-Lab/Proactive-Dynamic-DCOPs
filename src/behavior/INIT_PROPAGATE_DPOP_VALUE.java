package behavior;

import agent.AgentPDDCOP;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

/**
 * @author khoihd
 *
 */
public class INIT_PROPAGATE_DPOP_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -9137969826179481705L;
	
	private static final long sleepTime = 100; // in milliseconds

	AgentPDDCOP agent;
	
	public INIT_PROPAGATE_DPOP_VALUE(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		agent.addupSimulatedTime(sleepTime);
		
		for (AID neighborAgentAID : agent.getNeighborAIDList()) {
			agent.sendObjectMessageWithTime(neighborAgentAID, agent.getChosenValueAtEachTSMap(),
					PROPAGATE_DPOP_VALUE, agent.getSimulatedTime());
		}
	}
}
