package behavior;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.AgentPDDCOP;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class LS_RECEIVE_VALUE extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 3951196053602788669L;

	AgentPDDCOP agent;
	
	private int lastTimeStep;
	
	public LS_RECEIVE_VALUE(AgentPDDCOP agent, int lastTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
    if (agent.getLocalSearchIteration() == AgentPDDCOP.MAX_ITERATION) {
      return;
    }
		
		List<ACLMessage> messageList = waitingForMessageFromNeighborWithTime(LS_VALUE);
		
		agent.startSimulatedTiming();
						
		for (ACLMessage msg : messageList) {
			Map<Integer, String> valuesFromNeighbor = new HashMap<>();
			try {
				valuesFromNeighbor = (Map<Integer, String>) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			// Update agent view only value from neighbor is not null
			if (valuesFromNeighbor != null && !valuesFromNeighbor.isEmpty()) {
				for (int ts = 0; ts <= lastTimeStep; ts++) {
					String valueFromNeighbor = valuesFromNeighbor.get(ts);
					String sender = msg.getSender().getLocalName();
					
					if (valueFromNeighbor != null) {
						agent.getAgentViewEachTimeStepMap().get(sender).put(ts, valueFromNeighbor);
					}
				}
			}
		}
		
		agent.stopStimulatedTiming();
	}
	
  private List<ACLMessage> waitingForMessageFromNeighborWithTime(int msgCode) {
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
      else {
          block();
      }
    }
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }

	@Override
	public boolean done() {
	  agent.print("is done RECEIVE_VALUE: " + agent.getLocalSearchIteration());
		return agent.getLocalSearchIteration() == AgentPDDCOP.MAX_ITERATION;
	}		
}
