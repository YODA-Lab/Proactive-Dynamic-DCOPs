package behaviour;

import agent.ND_DCOP;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * @author khoihd
 *
 */
public class SEARCH_NEIGHBORS extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 6680449924898094747L;

	ND_DCOP agent;
	
	public SEARCH_NEIGHBORS(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		DFAgentDescription templateDF = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(agent.getIdStr());
		templateDF.addServices(serviceDescription);
		
		while (agent.getNeighborAIDList().size() < agent.getNeighborStrList().size()) {
			try {
				DFAgentDescription[] foundAgentList = DFService.search(myAgent, templateDF);
				agent.getNeighborAIDList().clear();
				for (int foundAgentIndex=0; foundAgentIndex<foundAgentList.length; foundAgentIndex++)
					agent.getNeighborAIDList().add(foundAgentList[foundAgentIndex].getName());
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
	}
}
