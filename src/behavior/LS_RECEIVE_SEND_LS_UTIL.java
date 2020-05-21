package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import utilities.*;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class LS_RECEIVE_SEND_LS_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 4766760189659187968L;

	AgentPDDCOP agent;
	@SuppressWarnings("unused")
  private int lastTimeStep;
	private int localTimeStep;
	
	public LS_RECEIVE_SEND_LS_UTIL(AgentPDDCOP agent, int lastTimeStep, int localTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
		this.localTimeStep = localTimeStep;
	}
	
	@Override
	public void action() {	  
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
		
    double localSearchQuality = utilFromChildren + 
        agent.utilityLSWithParentAndPseudoAndUnary() - agent.computeSwitchingCostAllTimeStep();
    
    agent.stopStimulatedTiming();

		if (!agent.isRoot()) {
			agent.sendObjectMessageWithTime(agent.getParentAID(), localSearchQuality, LS_UTIL, agent.getSimulatedTime());
		}
		else {
      agent.setLocalSearchQuality(localTimeStep,
          Math.max(localSearchQuality, agent.getLocalSearchQualityAt(localTimeStep - 1)));
      agent.setLocalSearchRuntime(localTimeStep, agent.getSimulatedTime());
		}
				
    agent.print("is done LS_RECEIVE_SEND_LS_UTIL at iteration: " + localTimeStep);
		
		if (localTimeStep == AgentPDDCOP.MAX_ITERATION && agent.isRoot()) {
		  Utilities.writeLocalSearchResult(agent);
		}
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
