package behavior;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
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
public class LS_RECEIVE_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 3951196053602788669L;

	AgentPDDCOP agent;
	private int lastTimeStep;
	private int localTimeStep;
	
	public LS_RECEIVE_VALUE(AgentPDDCOP agent, int lastTimeStep, int localTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
		this.localTimeStep = localTimeStep;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {    
    for (AID neighbor:agent.getNeighborAIDSet()) { 
      agent.sendObjectMessageWithTime(neighbor, agent.getBestImproveValueMap(), 
            LS_VALUE, agent.getSimulatedTime());  
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
		
    agent.print("is done SEND_RECEIVE_VALUE at iteration: " + localTimeStep);
	}
	
  private List<ACLMessage> waitingForMessageFromNeighborWithTime(int msgCode) {
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getNeighborAIDSet().size()) {
      agent.startSimulatedTiming();
      
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.blockingReceive(template);
        
      agent.stopStimulatedTiming();
//      if (receivedMessage != null) {
        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
          
        if (timeFromReceiveMessage > agent.getSimulatedTime()) {
          agent.setSimulatedTime(timeFromReceiveMessage);
        }
        
        messageList.add(receivedMessage); 
//      }
//      else {
//          block();
//      }
    }
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
