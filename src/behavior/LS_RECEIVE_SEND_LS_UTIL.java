package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.DcopConstants.MessageType;

import static agent.DcopConstants.MAX_ITERATION;
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
public class LS_RECEIVE_SEND_LS_UTIL extends OneShotBehaviour {

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
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(MessageType.LS_UTIL);
    
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
    
    agent.stopSimulatedTiming();

		if (!agent.isRoot()) {
			agent.sendObjectMessageWithTime(agent.getParentAID(), localSearchQuality, MessageType.LS_UTIL, agent.getSimulatedTime());
		}
		else {
      agent.setLocalSearchQuality(localTimeStep,
          Math.max(localSearchQuality, agent.getLocalSearchQualityAt(localTimeStep - 1)));
      agent.setLocalSearchRuntime(localTimeStep, agent.getSimulatedTime());
		}
				
    agent.println("is done LS_RECEIVE_SEND_LS_UTIL at iteration: " + localTimeStep);
		
		if (localTimeStep == MAX_ITERATION && agent.isRoot()) {
		  Utilities.writeLocalSearchResult(agent);
		}
	}
	
  private List<ACLMessage> waitingForMessageFromChildrenWithTime(MessageType msgType) {
    int msgCode = msgType.ordinal();
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getChildrenAIDSet().size()) {
      agent.startSimulatedTiming();
      
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.blockingReceive(template);
        
      agent.stopSimulatedTiming();
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
    agent.setSimulatedTime(agent.getSimulatedTime() + AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
