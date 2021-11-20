package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import static agent.DcopConstants.MAX_ITERATION;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class MGM_SEND_RECEIVE_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

  private static final long serialVersionUID = 4766760189659187968L;

  AgentPDDCOP agent;
  private int pd_dcop_time_step;
  private int mgm_time_step;
  
  public MGM_SEND_RECEIVE_UTIL(AgentPDDCOP agent, int pd_dcop_time_step, int mgm_time_step) {
    super(agent);
    this.agent = agent;
    this.pd_dcop_time_step = pd_dcop_time_step;
    this.mgm_time_step = mgm_time_step;
  }
  
  @Override
  public void action() {    
    double utilFromChildren = 0;
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(MGM_UTIL);
    
    agent.startSimulatedTiming();
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
    
    double localSearchQuality = utilFromChildren + agent.getLocalUtilitiesForUTIL(agent.getChosenValueAtEachTSMap().get(pd_dcop_time_step), pd_dcop_time_step);
    
    agent.stopSimulatedTiming();

    if (!agent.isRoot()) {
      agent.sendObjectMessageWithTime(agent.getParentAID(), localSearchQuality, MGM_UTIL, agent.getSimulatedTime());
    }
    else {
      if (mgm_time_step == 0) {
        agent.setMGMQuality(mgm_time_step, localSearchQuality);
      }
      else {
        agent.setMGMQuality(mgm_time_step, Math.max(localSearchQuality, agent.getMGMQualityMap().get(mgm_time_step - 1)));
      }
      agent.setMGMRuntime(mgm_time_step, agent.getSimulatedTime());
      agent.setOnlineSolvingTime(mgm_time_step, agent.getSimulatedTime());
    }
            
    // Last MGM time step
    if (mgm_time_step == MAX_ITERATION && agent.isRoot()) {
      // Compute the difference
      for (int i = 0; i < MAX_ITERATION - 1; i++) {
        if (Double.compare(agent.getMGMQualityMap().get(i), agent.getMGMQualityMap().get(i + 1)) == 0) {
          agent.getMGMdifferenceRuntimeMap().put(pd_dcop_time_step,
              agent.getMGMRuntimeMap().get(MAX_ITERATION) - agent.getMGMRuntimeMap().get(i));
          break;
        }
      }
      
      agent.getMGMQualityMap().clear();
      agent.getMGMRuntimeMap().clear();
    }
  }
  
  private List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
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
