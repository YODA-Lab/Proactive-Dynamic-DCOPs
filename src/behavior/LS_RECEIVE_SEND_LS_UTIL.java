package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import utilities.*;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * @author khoihd
 *
 */
public class LS_RECEIVE_SEND_LS_UTIL extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 4766760189659187968L;

	AgentPDDCOP agent;
	
	private int lastTimeStep;
	
	public LS_RECEIVE_SEND_LS_UTIL(AgentPDDCOP agent, int lastTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
	}
	
	@Override
	public void action() {
	  agent.print("iteration " + agent.getLocalSearchIteration());
	  
    double utilFromChildren = 0;
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(LS_UTIL);
    
    agent.startSimulatedTiming();
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
		
    agent.setCurentLocalSearchQuality(utilFromChildren + 
        agent.utilityWithParentAndPseudoAndUnary(lastTimeStep) - agent.computeSwitchingCostAllTimeStep());
    
    agent.stopStimulatedTiming();

		if (!agent.isRoot()) {
			agent.sendObjectMessageWithTime(agent.getParentAID(), agent.getCurentLocalSearchQuality(), LS_UTIL, agent.getSimulatedTime());
		}
		else {
		  if (Double.compare(agent.getCurentLocalSearchQuality(), agent.getBestLocalSearchQuality()) > 0) {
		    agent.setBestLocalSearchQuality(agent.getCurentLocalSearchQuality());
		    agent.setBestLocalSearchRuntime(agent.getSimulatedTime());
		  }
		}
		
		agent.incrementLocalSearchIteration();
		
		if (agent.getLocalSearchIteration() < AgentPDDCOP.MAX_ITERATION) {
			agent.sendImprove(lastTimeStep);
		} else if (agent.isRoot()) {
      Utilities.writeResult(agent);
		}
	}

	@Override
	public boolean done() {
	  agent.print("is done LS_RECEIVE_SEND_LS_UTIL: " + agent.getLocalSearchIteration());
		return agent.getLocalSearchIteration() == AgentPDDCOP.MAX_ITERATION;
	}
	
  private List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getChildrenAIDSet().size()) {
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
    agent.setSimulatedTime(agent.getSimulatedTime() + AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
