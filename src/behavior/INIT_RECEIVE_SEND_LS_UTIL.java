package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.DcopConstants.MessageType;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * REVIEWED 
 * @author khoihd
 *
 */
public class INIT_RECEIVE_SEND_LS_UTIL extends OneShotBehaviour {

  private static final long serialVersionUID = 6619734019693007342L;

  AgentPDDCOP agent;
  
  public INIT_RECEIVE_SEND_LS_UTIL(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }
  
  @Override
  public void action() {    
    double utilFromChildren = 0;
    
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(MessageType.INIT_LS_UTIL);
    agent.startSimulatedTiming();
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
        
    // Send the partial quality of the subtree to parent
    double localSearchQuality = utilFromChildren + 
        agent.utilityLSWithParentAndPseudoAndUnary() - agent.computeSwitchingCostAllTimeStep();
    
    agent.stopSimulatedTiming();

    if (!agent.isRoot()) {
      agent.sendObjectMessageWithTime(agent.getParentAID(), localSearchQuality, MessageType.INIT_LS_UTIL, agent.getSimulatedTime());
    }
    else {
      // First time
      agent.setLocalSearchQuality(-1, localSearchQuality);
      agent.setLocalSearchRuntime(-1, agent.getSimulatedTime());
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
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}