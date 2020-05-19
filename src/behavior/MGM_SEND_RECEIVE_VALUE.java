package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MGM_SEND_RECEIVE_VALUE extends Behaviour implements MESSAGE_TYPE {

  /**
   * 
   */
  private static final long serialVersionUID = -9079921323844342119L;
  
  private AgentPDDCOP agent;
  
  private int timeStep;
  
  private static final long sleepTime = 100; // in milliseconds

  public MGM_SEND_RECEIVE_VALUE(AgentPDDCOP agent, int timeStep) {
    super(agent);
    this.agent = agent;
    this.timeStep = timeStep;
  }
  
  @Override
  public void action() {
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    agent.addupSimulatedTime(sleepTime);
    
    for (AID neighborAgentAID : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithTime(neighborAgentAID, agent.getChosenValueAtEachTSMap().get(timeStep),
          PROPAGATE_DPOP_VALUE, agent.getSimulatedTime());
    }
    
    List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromChildrenWithTime(MGM_VALUE);
        
    agent.startSimulatedTiming();
    
    for (ACLMessage receivedMessage : receivedMessageFromNeighborList) {
      String sender = receivedMessage.getSender().getLocalName();
      String valueFromThisNeighbor = null;
      try {
        valueFromThisNeighbor = (String) receivedMessage.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }

      agent.getAgentViewEachTimeStepMap().get(sender).put(timeStep, valueFromThisNeighbor);
    }
    
    agent.stopStimulatedTiming();
  }

  @Override
  public boolean done() {
    // TODO Auto-generated method stub
    return false;
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
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
