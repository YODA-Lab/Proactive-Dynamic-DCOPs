package behavior;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;

/**
 * @author khoihd
 *
 */
public class RECEIVE_VALUE extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 3951196053602788669L;

	AgentPDDCOP agent;
	
	public RECEIVE_VALUE(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
    if (agent.getLocalSearchIteration() == AgentPDDCOP.MAX_ITERATION) {
      return;
    }
		
		List<ACLMessage> messageList = waitingForMessageFromNeighborWithTime(LS_VALUE);
		
		agent.startSimulatedTiming();
						
		for (ACLMessage msg:messageList) {
			List<String> valuesFromNeighbor = new ArrayList<String>();
			try {
				valuesFromNeighbor = (ArrayList<String>) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			//update agent_view?
			if (valuesFromNeighbor != null) {
				for (int ts=0; ts <= agent.getHorizon(); ts++) {
					String valueFromNeighbor = valuesFromNeighbor.get(ts);
					String sender = msg.getSender().getLocalName();
					if (valueFromNeighbor != null) {
						agent.getAgentViewEachTimeStepMap().get(sender).put(ts, valueFromNeighbor);
					}
				}
			}
		}
		
//		agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		agent.stopStimulatedTiming();
		
		for (AID neighbor : agent.getNeighborAIDSet()) {
			agent.sendObjectMessageWithTime(neighbor, "", LS_ITERATION_DONE, agent.getSimulatedTime());
		}
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
