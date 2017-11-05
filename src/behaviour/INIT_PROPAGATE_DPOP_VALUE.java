package behaviour;

import agent.ND_DCOP;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;

/**
 * @author khoihd
 *
 */
public class INIT_PROPAGATE_DPOP_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -9137969826179481705L;

	ND_DCOP agent;
	
	public INIT_PROPAGATE_DPOP_VALUE(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		//looking for each agents in neighbor AID List
		//send message
		//content of each message is list of its values			
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// update 100ms to simulated time
		agent.addupSimulatedTime(100);
		
		for (AID neighborAgentAID:agent.getNeighborAIDList()) {
			agent.sendObjectMessageWithTime(neighborAgentAID, agent.getValueAtEachTSMap(), 
					PROPAGATE_DPOP_VALUE, agent.getSimulatedTime());
		}
	}
}
