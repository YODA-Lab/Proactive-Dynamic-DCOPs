package behavior;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import agent.AgentPDDCOP;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * REVIEWED
 * 
 * @author khoihd
 *
 */
public class PSEUDOTREE_GENERATION extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 4730436360893574779L;

	private static final boolean WAITING_FOR_MSG = true;

	private Map<AID, Double> neighborHeuristicMap = new HashMap<>();

	AgentPDDCOP agent;

	public PSEUDOTREE_GENERATION(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}

	public AID returnAndRemoveNeighborCurrentBestInfo() {
		double maxInfo = -Double.MAX_VALUE;
		AID agentWithBestInfo = null;

		for (Entry<AID, Double> entry : neighborHeuristicMap.entrySet()) {
			if (Double.compare(entry.getValue(), maxInfo) > 0) {
				maxInfo = entry.getValue();
				agentWithBestInfo = entry.getKey();
			}
		}

		neighborHeuristicMap.remove(agentWithBestInfo);
		return agentWithBestInfo;
	}

	@Override
	public void action() {
	   agent.print("Start building pseudo-tree");
	  
		for (AID neighbor : agent.getNeighborAIDSet()) {
			neighborHeuristicMap.put(neighbor, agent.getAgentHeuristicStringMap().get(neighbor.getLocalName()));
		}

		if (agent.isRoot()) {
			agent.setNotVisited(false);

			// remove best children and add to childrenList
			AID childrenWithBestInfo = returnAndRemoveNeighborCurrentBestInfo();
			agent.getChildrenAIDSet().add(childrenWithBestInfo);

			agent.sendStringMessage(childrenWithBestInfo, "CHILD", PSEUDOTREE);
		}

		while (WAITING_FOR_MSG) {
			MessageTemplate template = MessageTemplate.MatchPerformative(PSEUDOTREE);
			ACLMessage receivedMessage = myAgent.blockingReceive(template);

			AID sender = receivedMessage.getSender();

			// first time the agent is visited
			if (receivedMessage.getContent().equals("CHILD") && agent.isNotVisited()) {
				agent.setNotVisited(false);
				// add all neighbors to open_neighbors, except sender;
				// v2: remove sender from infoMap
				neighborHeuristicMap.remove(sender);
				// set parent
				agent.setParentAID(sender);
			} // end of first IF
			else if (receivedMessage.getContent().equals("CHILD") && neighborHeuristicMap.containsKey(sender)) {
				// remove sender from open_neighbors and add to pseudo_children
				neighborHeuristicMap.remove(sender);
				agent.getPseudoChildrenAIDSet().add(sender);

				agent.sendStringMessage(sender, "PSEUDO", PSEUDOTREE);

				continue;
			} // end of second IF
			else if (receivedMessage.getContent().equals("PSEUDO")) {

				// remove sender from children_agent, and add to pseudo_parent
				agent.getChildrenAIDSet().remove(sender);
				agent.getPseudoParentAIDList().add(sender);
			}

			// Forward the CHILD message to the next open neighbor
			// Check if it has open neighbors
			if (neighborHeuristicMap.size() > 0) {
				// choose a random agent from openNeighborAIDList, and delete from
				// openNeighborAIDList
				// v2: choose a best children
				AID childrenWithBestInfo = returnAndRemoveNeighborCurrentBestInfo();
				agent.getChildrenAIDSet().add(childrenWithBestInfo);

				agent.sendStringMessage(childrenWithBestInfo, "CHILD", PSEUDOTREE);

			} else {
				if (agent.isRoot() == false) {
					agent.sendStringMessage(agent.getParentAID(), "FINISH", PSEUDOTREE);
				}
//					printTree(isRoot);

				// assign leaf
				if (agent.getChildrenAIDSet().size() == 0)
					agent.setLeaf(true);

				break;
			}
		}

		// confirm process
		// if root, send message to all the children
		// set pseudotree_process = true
		if (agent.isRoot()) {
			for (AID childrenAID : agent.getChildrenAIDSet()) {
				agent.sendStringMessage(childrenAID, "TREE_FINISH", PSEUDOTREE);

			}
		}
		// waiting for message from the parent
		// send message to all the children
		else {
			while (WAITING_FOR_MSG) {
				MessageTemplate template = MessageTemplate.MatchPerformative(PSEUDOTREE);
				ACLMessage receivedMessage = myAgent.blockingReceive(template);

				if (receivedMessage.getContent().equals("TREE_FINISH")) {
					for (AID childrenAgentAID : agent.getChildrenAIDSet()) {
						agent.sendStringMessage(childrenAgentAID, "TREE_FINISH", PSEUDOTREE);
					}
					break;
				}	
			}
		}

		for (AID pseudo_parent : agent.getPseudoParentAIDList()) {
			agent.getParentAndPseudoStrList().add(pseudo_parent.getLocalName());
		}

		if (agent.isRoot() == false) {
			agent.getParentAndPseudoStrList().add(agent.getParentAID().getLocalName());
		}
		
		agent.print("Done building pseudo-tree");

		if (agent.isDiscrete()) {
  		// Add tables for DPOP here (those contain only parent and pseudo-parents)
  		agent.getDpopDecisionTableList()
  				.addAll(agent.getTableWithoutChildrenAndPseudochilren(agent.getRawDecisionTableList()));
  		agent.getDpopRandomTableList()
  				.addAll(agent.getTableWithoutChildrenAndPseudochilren(agent.getRawRandomTableList()));
		}
		else if (agent.isContinuous()) {	    
	    for (Entry<String, PiecewiseMultivariateQuadFunction> entry : agent.getNeighborFunctionMap().entrySet()) {
	      String neighbor = entry.getKey();
	      PiecewiseMultivariateQuadFunction constraint = entry.getValue();
	      
	      if (agent.getParentAndPseudoStrList().contains(neighbor)) {
	        agent.setFunctionWithPParentMap(neighbor, constraint);
	      }
	    }
	    
	    
		}
	}
}
