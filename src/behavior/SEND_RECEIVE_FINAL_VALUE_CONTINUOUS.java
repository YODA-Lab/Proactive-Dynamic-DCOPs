package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.AgentPDDCOP;
import agent.DcopConstants.MessageType;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class SEND_RECEIVE_FINAL_VALUE_CONTINUOUS extends OneShotBehaviour {
  
  /**
   * 
   */
  private static final long serialVersionUID = 4656018519739199323L;

  private AgentPDDCOP agent;
  
  public SEND_RECEIVE_FINAL_VALUE_CONTINUOUS(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void action() {
    
    agent.println("SEND_RECEIVE_FINAL_VALUE_CONTINUOUS");
    
    for (AID neighborAgentAID : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithTime(neighborAgentAID, agent.getChosenDoubleValueAtEachTSMap(),
          MessageType.FINAL_VALUE_CONTINUOUS, agent.getSimulatedTime());
    }
    
    List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborWithTime(MessageType.FINAL_VALUE_CONTINUOUS);
    
    for (ACLMessage receivedMessage : receivedMessageFromNeighborList) {
      String sender = receivedMessage.getSender().getLocalName();
      Map<Integer, Double> valuesFromThisNeighbor = null;
      try {
        valuesFromThisNeighbor = (Map<Integer, Double>) receivedMessage.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }

      agent.getAgentViewDoubleEachTimeStepMap().computeIfAbsent(sender, k-> new HashMap<>()).putAll(valuesFromThisNeighbor);
    }
  }
  
  private List<ACLMessage> waitingForMessageFromNeighborWithTime(MessageType msgType) {
    int msgCode = msgType.ordinal();
    List<ACLMessage> messageList = new ArrayList<>();

    while (messageList.size() < agent.getNeighborStrSet().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.blockingReceive(template);  
      messageList.add(receivedMessage); 
    }
    
    return messageList;
  }
}
