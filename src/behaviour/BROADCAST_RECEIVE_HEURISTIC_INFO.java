package behaviour;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;

import agent.ND_DCOP;

/**
 * @author khoihd
 *
 */
//MAX-DEGREE HEURISTIC
public class BROADCAST_RECEIVE_HEURISTIC_INFO extends OneShotBehaviour implements MESSAGE_TYPE{
	private static final long serialVersionUID = 7277049523059465622L;

	ND_DCOP agent;
	
	public BROADCAST_RECEIVE_HEURISTIC_INFO(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@Override
	public void action() {		
		Integer maxDegreeHeuristic = agent.getNeighborAIDList().size();
		
		//broadcast to neighbors
		for (AID neighbor:agent.getNeighborAIDList()) {
			agent.sendObjectMessage(neighbor, maxDegreeHeuristic, INFO);
		}
		
		//receive messages from neighbors
		ArrayList<ACLMessage> messageList = waitingForMessageFromNeighbors(INFO);
		for (ACLMessage message:messageList) {
			Integer infoFromMessage = null;
			AID sender = message.getSender();
			try {
				infoFromMessage = (Integer) message.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			agent.getConstraintInfoMap().put(sender, infoFromMessage);
		}
	}
	
	public ArrayList<ACLMessage> waitingForMessageFromNeighbors(int msgCode) {
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
		while (messageList.size() < agent.getNeighborAIDList().size()) {
			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			ACLMessage receivedMessage = myAgent.receive(template);
			if (receivedMessage != null) {
				messageList.add(receivedMessage);
			}
			else
				block();
		}
		return messageList;
	}
}
