package behaviour;


import agent.ND_DCOP;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * @author khoihd
 *
 */
public class PSEUDOTREE_GENERATION extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 4730436360893574779L;

	ND_DCOP agent;
	
	public PSEUDOTREE_GENERATION(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	public AID returnAndRemoveNeighborCurrentBestInfo() {
		double maxInfo = Integer.MIN_VALUE;
		AID agentWithBestInfo = null;
		
		for (AID innerAgent:agent.getConstraintInfoMap().keySet()) {
			if (agent.getConstraintInfoMap().get(innerAgent) > maxInfo) {
				maxInfo = agent.getConstraintInfoMap().get(innerAgent);
				agentWithBestInfo = innerAgent;
			}
		}
		agent.getConstraintInfoMap().remove(agentWithBestInfo);
		return agentWithBestInfo;
	}
	
	@Override
	public void action() {
		if (agent.isRoot()) {
			agent.setNotVisited(false);
		
			//remove best children and add to childrenList
			AID childrenWithBestInfo = returnAndRemoveNeighborCurrentBestInfo();
			agent.getChildrenAIDList().add(childrenWithBestInfo);
			
			//send an CHILD message to randomChosenNeighborAID
			ACLMessage childMessage = new ACLMessage(PSEUDOTREE);
			childMessage.setContent("CHILD");
			childMessage.addReceiver(childrenWithBestInfo);
			agent.send(childMessage);
//			System.out.println("Agent " + getLocalName() + " send message "
//							+ childMessage.getContent() + " to Agent " + childrenWithBestInfo.getLocalName());
		}
		
		while (ND_DCOP.WAITING_FOR_MSG) {
			MessageTemplate template = MessageTemplate.MatchPerformative(PSEUDOTREE);
			ACLMessage receivedMessage = myAgent.receive(template);
			if (receivedMessage != null) {
				AID sender = receivedMessage.getSender();
				
				//first time the agent is visited
				if (receivedMessage.getContent().equals("CHILD") && agent.isNotVisited()) {						
					agent.setNotVisited(false);
					//add all neighbors to open_neighbors, except sender;
					//v2: remove sender from infoMap
					agent.getConstraintInfoMap().remove(sender);						
					//set parent
					agent.setParentAID(sender);
//					agent.getParentAndPseudoStrList().add(sender.getLocalName());

				}//end of first IF
				else if (receivedMessage.getContent().equals("CHILD") && agent.getConstraintInfoMap().containsKey(sender)) {
					//remove sender from open_neighbors and add to pseudo_children
					agent.getConstraintInfoMap().remove(sender);
					agent.getPseudoChildrenAIDList().add(sender);
					
					//send PSEUDO message to sender;
					ACLMessage pseudoMsg = new ACLMessage(PSEUDOTREE);
					pseudoMsg.setContent("PSEUDO");
					pseudoMsg.addReceiver(sender);
					agent.send(pseudoMsg);
					
//					System.out.println("Agent " + getLocalName() + " send message "
//							+ pseudoMsg.getContent() + " to Agent " + sender.getLocalName());
					
					continue;
				}//end of second IF
				else if (receivedMessage.getContent().equals("PSEUDO")) {
					
					//remove sender from children_agent, and add to pseudo_parent
					agent.getChildrenAIDList().remove(sender);
					agent.getPseudoParentAIDList().add(sender);
//					agent.getParentAndPseudoStrList().add(sender.getLocalName());
				}
				
				//Forward the CHILD message to the next open neighbor
				//Check if it has open neighbors
				if (agent.getConstraintInfoMap().size() > 0) {
					//choose a random agent from openNeighborAIDList, and delete from openNeighborAIDList
					//v2: choose a best children
					AID childrenWithBestInfo = returnAndRemoveNeighborCurrentBestInfo();
					agent.getChildrenAIDList().add(childrenWithBestInfo);
					
					//send the message to y0
					ACLMessage childMsg = new ACLMessage(PSEUDOTREE);
					childMsg.setContent("CHILD");
					childMsg.addReceiver(childrenWithBestInfo);
					agent.send(childMsg);
					
//					System.out.println("Agent " + getLocalName() + " send message "
//							+ childMsg.getContent() + " to Agent " + childrenWithBestInfo.getLocalName());
				}
				else {
					if (agent.isRoot() == false) {
						ACLMessage finishMsg = new ACLMessage(PSEUDOTREE);
						finishMsg.setContent("FINISH");
						finishMsg.addReceiver(agent.getParentAID());
						
//						System.out.println("Agent " + getLocalName() + " send message "
//								+ finishMsg.getContent() + " to agent " + parentAID.getLocalName());
						agent.send(finishMsg);
					}
//					printTree(isRoot);
					
					//assign leaf
					if (agent.getChildrenAIDList().size() == 0)
						agent.setLeaf(true);
					
					break;
				}
			}
			else {
				block();
			}
		}
		
		//confirm process
		//if root, send message to all the children
		//set pseudotree_process = true
		if (agent.isRoot()) {
			for (AID childrenAID:agent.getChildrenAIDList()) {
				ACLMessage treeFinishMsg = new ACLMessage(PSEUDOTREE);
				treeFinishMsg.setContent("TREE_FINISH");
				treeFinishMsg.addReceiver(childrenAID);
				agent.send(treeFinishMsg);
//				System.out.println("Agent " + getLocalName() + " send message "
//								+ treeFinishMsg.getContent() + " to Agent " + childrenAID.getLocalName());
			}
//			isPseudotreeProcess = FINISHED;
		}
		//waiting for message from the parent
		//send message to all the children
		else {
			while (ND_DCOP.WAITING_FOR_MSG) {
				MessageTemplate template = MessageTemplate.MatchPerformative(PSEUDOTREE);
				ACLMessage receivedMessage = myAgent.receive(template);
				if (receivedMessage != null) {
					if (receivedMessage.getContent().equals("TREE_FINISH")) {							
						for (AID childrenAgentAID:agent.getChildrenAIDList()) {
							ACLMessage treeFinishMsg = new ACLMessage(PSEUDOTREE);
							treeFinishMsg.setContent("TREE_FINISH");
							treeFinishMsg.addReceiver(childrenAgentAID);
							agent.send(treeFinishMsg);
//							System.out.println("Agent " + getLocalName() + " send message "
//											+ treeFinishMsg.getContent() + " to Agent " + childrenAgentAID.getLocalName());
						}
						break;
					}
				}
				else
					block();		
			}
		}
	
		for (AID pseudo_parent:agent.getPseudoParentAIDList())
			agent.getParentAndPseudoStrList().add(pseudo_parent.getLocalName());
		if (agent.isRoot() == false)
			agent.getParentAndPseudoStrList().add(agent.getParentAID().getLocalName());
	}
}	
