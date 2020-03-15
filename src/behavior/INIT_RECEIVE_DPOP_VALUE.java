package behavior;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agent.AgentPDDCOP;

/**
 * @author khoihd
 *
 */
public class INIT_RECEIVE_DPOP_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -2879055736536273274L;

	AgentPDDCOP agent;
	
	public INIT_RECEIVE_DPOP_VALUE(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborsWithTime(PROPAGATE_DPOP_VALUE);
		
//		long currentStartTime = agent.getBean().getCurrentThreadUserTime();
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
		
		agent.stopStimulatedTiming();
//		agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - currentStartTime);
	}
	
	private List<ACLMessage> waitingForMessageFromNeighborsWithTime(int msgCode) {
	  List<ACLMessage> messageList = new ArrayList<ACLMessage>();
		while (messageList.size() < agent.getNeighborAIDSet().size()) {
			agent.startSimulatedTiming();
		  
		  MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			ACLMessage receivedMessage = myAgent.receive(template);
			
			agent.stopStimulatedTiming();
			if (receivedMessage != null) {
				long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
				
				if (timeFromReceiveMessage > agent.getSimulatedTime()) {
					agent.setSimulatedTime(timeFromReceiveMessage);
				}
				messageList.add(receivedMessage);
			}
			else
				block();
		}
		
		agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
		return messageList;
	}
}
