package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.DcopConstants.MessageType;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utilities.Utilities;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class SEND_RECEIVE_FINAL_UTIL_CONTINUOUS extends OneShotBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = -2565102810370434662L;
  AgentPDDCOP agent;
  
  public SEND_RECEIVE_FINAL_UTIL_CONTINUOUS(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }
  
  @Override
  public void action() {    
    double utilFromChildren = 0;
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(MessageType.FINAL_UTIL_CONTINOUS);
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
    
    double localQuality = utilFromChildren + 
        agent.utilityLSWithParentAndPseudoAndUnaryContinuous() - agent.computeSwitchingCostAllTimeStepContinuous();

    if (!agent.isRoot()) {
      agent.sendObjectMessageWithTime(agent.getParentAID(), localQuality, MessageType.FINAL_UTIL_CONTINOUS, agent.getSimulatedTime());
    }
            
    if (agent.isRoot()) {
      agent.setSolutionQuality(localQuality);
      agent.setFinalRuntime(agent.getSimulatedTime());
      Utilities.writeFinalResult(agent);
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
