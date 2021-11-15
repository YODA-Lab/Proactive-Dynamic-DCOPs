package behavior;

import agent.AgentPDDCOP;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class SEARCH_NEIGHBORS extends OneShotBehaviour implements MESSAGE_TYPE {

	/**
   * 
   */
  private static final long serialVersionUID = -4671159077938034634L;
  AgentPDDCOP agent;
	
	public SEARCH_NEIGHBORS(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {
	  agent.print("Start looking for neighbors:" + agent.getNeighborStrSet());
	  
		DFAgentDescription templateDF = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(agent.getAgentID());
		templateDF.addServices(serviceDescription);
		
		while (agent.getNeighborAIDSet().size() < agent.getNeighborStrSet().size()) {
			try {
				DFAgentDescription[] foundAgentList = DFService.search(myAgent, templateDF);
				agent.getNeighborAIDSet().clear();
				for (int foundAgentIndex=0; foundAgentIndex<foundAgentList.length; foundAgentIndex++) {
				  agent.getNeighborAIDSet().add(foundAgentList[foundAgentIndex].getName());
				}
			} catch (FIPAException e) {
				e.printStackTrace();
			}
		}
		
		agent.print("Done looking for neighbors");
	}
}
