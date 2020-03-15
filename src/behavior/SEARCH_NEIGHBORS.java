package behavior;

import agent.AgentPDDCOP;
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

	AgentPDDCOP agent;
	
	public SEARCH_NEIGHBORS(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
		DFAgentDescription templateDF = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(agent.getAgentID());
		templateDF.addServices(serviceDescription);
		
		while (agent.getNeighborAIDSet().size() < agent.getNeighborStrSet().size()) {
			try {
				DFAgentDescription[] foundAgentList = DFService.search(myAgent, templateDF);
				agent.getNeighborAIDSet().clear();
				for (int foundAgentIndex=0; foundAgentIndex<foundAgentList.length; foundAgentIndex++)
					agent.getNeighborAIDSet().add(foundAgentList[foundAgentIndex].getName());
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}		
	}
}
