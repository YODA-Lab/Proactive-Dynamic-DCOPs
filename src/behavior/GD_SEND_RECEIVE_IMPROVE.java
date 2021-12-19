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

public class GD_SEND_RECEIVE_IMPROVE extends OneShotBehaviour {
  
  /**
   * 
   */
  private static final long serialVersionUID = 5822387491101918479L;

  private AgentPDDCOP agent;
  
  private int iteration;
  
  public GD_SEND_RECEIVE_IMPROVE(AgentPDDCOP agent, int iteration) {
    super(agent);
    this.agent = agent;
    this.iteration = iteration;
  }
  

  @SuppressWarnings("unchecked")
  @Override
  public void action() {
    agent.println("Iteration" + iteration + " GD_SEND_RECEIVE_IMPROVE");
    
    agent.startSimulatedTiming();
    
    agent.computeMaximumGainEveryTimeStep();

    agent.stopSimulatedTiming();
    
    for (AID neighbor : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithTime(neighbor, agent.getLocalSearchMaximumGain(), MessageType.GD_IMPROVE, agent.getSimulatedTime());
    }

    List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborWithTime(MessageType.GD_IMPROVE);
    
    agent.startSimulatedTiming();
    
    Map<String, Map<Integer, Double>> gainFromNeighborMap = new HashMap<>();
    for (ACLMessage receivedMessage : receivedMessageFromNeighborList) {
      String sender = receivedMessage.getSender().getLocalName();
      Map<Integer, Double> gainsAccrossTimeStep = new HashMap<>();
      try {
        gainsAccrossTimeStep = (Map<Integer, Double>) receivedMessage.getContentObject();
        gainFromNeighborMap.put(sender, gainsAccrossTimeStep);
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
    
    Map<Integer, Double> candidateNewValues = new HashMap<>(agent.getLocalSearchArgmax());
    
    for (int timeStep = 0; timeStep <= agent.getHorizon(); timeStep++) {      

      for (String neigbor : agent.getNeighborStrSet()) {
        double neighborGain = gainFromNeighborMap.get(neigbor).get(timeStep);
        double agentGain = agent.getLocalSearchMaximumGain().get(timeStep);
        
        if (Double.compare(neighborGain, agentGain) > 0) {
          candidateNewValues.put(timeStep, null);
          break;
        }
      }
    }
    
    agent.stopSimulatedTiming();   
  }
  
  private List<ACLMessage> waitingForMessageFromNeighborWithTime(MessageType msgType) {
    int msgCode = msgType.ordinal();
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getNeighborAIDSet().size()) {
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
