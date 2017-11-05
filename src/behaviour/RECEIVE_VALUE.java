package behaviour;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;

import agent.ND_DCOP;

/**
 * @author khoihd
 *
 */
public class RECEIVE_VALUE extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 3951196053602788669L;

	ND_DCOP agent;
	
	public RECEIVE_VALUE(ND_DCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		// save oldSimulatedTime here, roll back if stop condition
		long oldSimulatedTime = agent.getSimulatedTime();
		
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
		
		while (messageList.size() < agent.getNeighborAIDList().size()) {
			if (agent.getLsIteration() == ND_DCOP.MAX_ITERATION) {
				agent.setSimulatedTime(oldSimulatedTime);
				return;
			}
			MessageTemplate template = MessageTemplate.MatchPerformative(LS_VALUE);
			ACLMessage receivedMessage = myAgent.receive(template);
			if (receivedMessage != null) {
//				System.out.println("Agent " + getLocalName() + " receive message "
//						+ msgTypes[LS_VALUE] + " from Agent " + receivedMessage.
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
		
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		for (ACLMessage msg:messageList) {
			ArrayList<String> valuesFromNeighbor = new ArrayList<String>();
			try {
				valuesFromNeighbor = (ArrayList<String>) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
							
//			System.err.println("Agent " + idStr + " receive values: " + valuesFromNeighbor + " from Agent "
//					+ msg.getSender().getLocalName());
			
			//update agent_view?
			if (valuesFromNeighbor != null) {
				for (int ts=0; ts<=agent.h; ts++) {
					String valueFromNeighbor = valuesFromNeighbor.get(ts);
					String sender = msg.getSender().getLocalName();
					if (valueFromNeighbor != null) {
						agent.getAgentView_DPOP_TSMap().get(sender).put(ts, valueFromNeighbor);
					}
				}
			}
		}
		
		agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		
		for (AID neighbor:agent.getNeighborAIDList()) 
			agent.sendObjectMessageWithTime(neighbor, "", LS_ITERATION_DONE, agent.getSimulatedTime());		
	}

	@Override
	public boolean done() {
		return agent.getLsIteration() == ND_DCOP.MAX_ITERATION;
	}		
}
