package behaviour;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.HashMap;

import agent.ND_DCOP;

/**
 * @author khoihd
 *
 */
public class INIT_RECEIVE_DPOP_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -2879055736536273274L;

	ND_DCOP agent;
	
	public INIT_RECEIVE_DPOP_VALUE(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		
		ArrayList<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborsWithTime(PROPAGATE_DPOP_VALUE);
		
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		for (ACLMessage receivedMessage:receivedMessageFromNeighborList) {
			String sender = receivedMessage.getSender().getLocalName();
			HashMap<Integer, String> neighborValuesAtEachTSMap = new HashMap<Integer, String>();
			try {
				neighborValuesAtEachTSMap = (HashMap<Integer, String>) receivedMessage.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}

			agent.getAgentView_DPOP_TSMap().put(sender, neighborValuesAtEachTSMap);
		}
		
		agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
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
	
	public ArrayList<ACLMessage> waitingForMessageFromNeighborsWithTime(int msgCode) {
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
		while (messageList.size() < agent.getNeighborAIDList().size()) {
			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			ACLMessage receivedMessage = myAgent.receive(template);
			if (receivedMessage != null) {
//				System.out.println("Agent " + getLocalName() + " receive message "
//						+ msgTypes[msgCode] + " from Agent " + receivedMessage.
//						getSender().getLocalName());
				long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
				if (timeFromReceiveMessage > agent.getSimulatedTime())
					agent.setSimulatedTime(timeFromReceiveMessage);
				messageList.add(receivedMessage);
			}
			else
				block();
		}
		agent.addupSimulatedTime(ND_DCOP.getDelayMessageTime());
		return messageList;
	}
}
