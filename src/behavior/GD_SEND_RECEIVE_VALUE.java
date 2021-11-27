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

public class GD_SEND_RECEIVE_VALUE extends OneShotBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = 6911027581252852162L;
  
  private AgentPDDCOP agent;
  
  private int iteration;
  
  public GD_SEND_RECEIVE_VALUE(AgentPDDCOP agent, int iteration) {
    super(agent);
    this.agent = agent;
    this.iteration = iteration;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void action() {
    
    agent.print("Iteration" + iteration + " GD_SEND_RECEIVE_VALUE");
    
    for (AID neighborAgentAID : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithTime(neighborAgentAID, agent.getChosenDoubleValueAtEachTSMap(),
          MessageType.GD_VALUE, agent.getSimulatedTime());
    }
    
    List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborWithTime(MessageType.GD_VALUE);
        
    agent.startSimulatedTiming();
    
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
    
    agent.stopSimulatedTiming();
  }
  
  private List<ACLMessage> waitingForMessageFromNeighborWithTime(MessageType msgType) {
    int msgCode = msgType.ordinal();
    List<ACLMessage> messageList = new ArrayList<>();

    while (messageList.size() < agent.getNeighborStrSet().size()) {
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
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
