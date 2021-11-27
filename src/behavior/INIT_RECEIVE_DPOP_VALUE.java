package behavior;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agent.AgentPDDCOP;
import agent.DcopConstants.MessageType;

/**
 * REVIEWED
 * 
 * @author khoihd
 *
 */
public class INIT_RECEIVE_DPOP_VALUE extends OneShotBehaviour {

	private static final long serialVersionUID = -2879055736536273274L;

	AgentPDDCOP agent;
	
	public INIT_RECEIVE_DPOP_VALUE(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborsWithTime(MessageType.PROPAGATE_DPOP_VALUE);
		
		agent.startSimulatedTiming();
		
		for (ACLMessage receivedMessage : receivedMessageFromNeighborList) {
			String sender = receivedMessage.getSender().getLocalName();
			HashMap<Integer, String> neighborValuesAtEachTSMap = new HashMap<Integer, String>();
			try {
				neighborValuesAtEachTSMap = (HashMap<Integer, String>) receivedMessage.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}

			agent.getAgentViewEachTimeStepMap().put(sender, neighborValuesAtEachTSMap);
		}
		
		agent.stopSimulatedTiming();
	}
	
	private List<ACLMessage> waitingForMessageFromNeighborsWithTime(MessageType msgType) {
	  int msgCode = msgType.ordinal();
	  List<ACLMessage> messageList = new ArrayList<ACLMessage>();
		while (messageList.size() < agent.getNeighborAIDSet().size()) {
			agent.startSimulatedTiming();
		  
		  MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			ACLMessage receivedMessage = myAgent.blockingReceive(template);
			
			agent.stopSimulatedTiming();
//			if (receivedMessage != null) {
				long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
				
				if (timeFromReceiveMessage > agent.getSimulatedTime()) {
					agent.setSimulatedTime(timeFromReceiveMessage);
				}
				messageList.add(receivedMessage);
//			}
//			else
//				block();
		}
		
		agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
		return messageList;
	}
}
