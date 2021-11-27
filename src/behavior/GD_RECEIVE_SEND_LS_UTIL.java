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
public class GD_RECEIVE_SEND_LS_UTIL extends OneShotBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = -7892863846164578817L;
  AgentPDDCOP agent;

  private int iteration;
  
  
  public GD_RECEIVE_SEND_LS_UTIL(AgentPDDCOP agent, int iteration) {
    super(agent);
    this.agent = agent;
    this.iteration = iteration;
  }
  
  @Override
  public void action() {    
    double utilFromChildren = 0;
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(MessageType.GD_UTL);
    
    agent.startSimulatedTiming();
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
    
    double localSearchQuality = utilFromChildren + 
        agent.utilityLSWithParentAndPseudoAndUnaryContinuous() - agent.computeSwitchingCostAllTimeStepContinuous();
    
    agent.stopSimulatedTiming();

    if (!agent.isRoot()) {
      agent.sendObjectMessageWithTime(agent.getParentAID(), localSearchQuality, MessageType.GD_UTL, agent.getSimulatedTime());
    }
    else {
      agent.setLocalSearchQuality(iteration, Math.max(localSearchQuality, agent.getLocalSearchQualityAt(iteration - 1)));
      agent.setLocalSearchRuntime(iteration, agent.getSimulatedTime());
    }
        
    agent.print("is done LS_RECEIVE_SEND_LS_UTIL at iteration: " + iteration);
    
    if (iteration == MAX_ITERATION && agent.isRoot()) {
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
    }
    agent.setSimulatedTime(agent.getSimulatedTime() + AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
