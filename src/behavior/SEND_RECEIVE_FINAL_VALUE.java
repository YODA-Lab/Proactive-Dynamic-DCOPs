package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.AgentPDDCOP;
import agent.AgentPDDCOP.DcopAlgorithm;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * @author khoihd
 *
 */
public class SEND_RECEIVE_FINAL_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {

  /**
   * 
   */
  private static final long serialVersionUID = -2079835904525024506L;
  
  private AgentPDDCOP agent;
  
  public SEND_RECEIVE_FINAL_VALUE(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void action() {    
    // Send out values to neighbors
    for (AID neighborAgentAID : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithTime(neighborAgentAID, agent.getChosenValueAtEachTSMap(),
          FINAL_VALUE, agent.getSimulatedTime());
    }
    
    List<ACLMessage> messageList = waitingForMessageFromNeighborsWithTime(FINAL_VALUE);
    
    agent.startSimulatedTiming();
            
    for (ACLMessage msg : messageList) {
      Map<Integer, String> valuesFromNeighbor = new HashMap<>();
      
      try {
        valuesFromNeighbor = (Map<Integer, String>) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
      
      int initTimeStep = agent.isRunningAlgorithm(DcopAlgorithm.REACT) ? -1 : 0;
      
      // Update agent view only value from neighbor is not null
      for (int ts = initTimeStep; ts <= agent.getHorizon(); ts++) {
        String valueFromNeighbor = valuesFromNeighbor.get(ts);
        String sender = msg.getSender().getLocalName();
        
        agent.getAgentViewEachTimeStepMap().computeIfAbsent(sender, k -> new HashMap<>()).put(ts, valueFromNeighbor);
      }
    }
    
    agent.stopStimulatedTiming();
  }

  private List<ACLMessage> waitingForMessageFromNeighborsWithTime(int msgCode) {
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();
    while (messageList.size() < agent.getNeighborAIDSet().size()) {
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
      else
        block();
    }
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
