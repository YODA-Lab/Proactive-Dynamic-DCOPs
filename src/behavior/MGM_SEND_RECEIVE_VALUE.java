package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agent.AgentPDDCOP;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MGM_SEND_RECEIVE_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {

  /**
   * 
   */
  private static final long serialVersionUID = -9079921323844342119L;
  
  private AgentPDDCOP agent;
  
  private int pd_dcop_time_step;
  
  public MGM_SEND_RECEIVE_VALUE(AgentPDDCOP agent, int pd_dcop_time_step) {
    super(agent);
    this.agent = agent;
    this.pd_dcop_time_step = pd_dcop_time_step;
  }
  
  @Override
  public void action() {
    for (AID neighborAgentAID : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithTime(neighborAgentAID, agent.getChosenValueAtEachTSMap().get(pd_dcop_time_step),
          MGM_VALUE, agent.getSimulatedTime());
    }
    
    List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborWithTime(MGM_VALUE);
        
    agent.startSimulatedTiming();
    
    for (ACLMessage receivedMessage : receivedMessageFromNeighborList) {
      String sender = receivedMessage.getSender().getLocalName();
      String valueFromThisNeighbor = null;
      try {
        valueFromThisNeighbor = (String) receivedMessage.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }

      agent.getAgentViewEachTimeStepMap().computeIfAbsent(sender, k-> new HashMap<>()).put(pd_dcop_time_step, valueFromThisNeighbor);
    }
    
    agent.stopStimulatedTiming();
  }
  
  private List<ACLMessage> waitingForMessageFromNeighborWithTime(int msgCode) {
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getNeighborStrSet().size()) {
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
//          if (agent.isPrinting()) {agent.print("Waiting in MGM VALUE");}
//          block();
//      }
    }
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
