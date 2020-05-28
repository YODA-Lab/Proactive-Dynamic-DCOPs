package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.AgentPDDCOP;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MGM_SEND_RECEIVE_IMPROVE extends OneShotBehaviour implements MESSAGE_TYPE {

  /**
   * 
   */
  private static final long serialVersionUID = -6167782659055141673L;
  
  private AgentPDDCOP agent;
  
  private int timeStep;
  
  public MGM_SEND_RECEIVE_IMPROVE(AgentPDDCOP agent, int timeStep) {
    super(agent);
    this.agent = agent;
    this.timeStep = timeStep;
  }
  

  @Override
  public void action() {
    sendMGMImprove();    
  }

  /**
   * @param lastTimeStep
   */
  public void sendMGMImprove() {
    agent.startSimulatedTiming();
        
    double localUtility = agent.computeMGMLocalUtility(agent.getChosenValueAtEachTSMap().get(timeStep), timeStep);
    
    double maxGain = Double.NEGATIVE_INFINITY;
    String chosenMGMValue = null;
    
    for (String value : agent.getSelfDomain()) {
      double gain = agent.computeMGMLocalUtility(value, timeStep) -  localUtility;
      if (Double.compare(gain, maxGain) > 0) {
        maxGain = gain;
        chosenMGMValue = value;
      }
    }

    agent.stopStimulatedTiming();

    for (AID neighbor : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithTime(neighbor, maxGain, MESSAGE_TYPE.MGM_IMPROVE, agent.getSimulatedTime());
    }

    List<ACLMessage> receivedMessageFromNeighborList = waitingForMessageFromNeighborWithTime(MGM_IMPROVE);
    
    agent.startSimulatedTiming();
    
    Map<String, Double> gainFromNeighborMap = new HashMap<>();
    for (ACLMessage receivedMessage : receivedMessageFromNeighborList) {
      String sender = receivedMessage.getSender().getLocalName();
      Double gainFromNeighbor = 0D;
      try {
        gainFromNeighbor = (Double) receivedMessage.getContentObject();
        gainFromNeighborMap.put(sender, gainFromNeighbor);
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }

    // Change self's value if self region has the maximum gain among neighbors
    double maxGainFromNeighbor = gainFromNeighborMap.values().stream().mapToDouble(Double::doubleValue).max().getAsDouble();
    if (Double.compare(maxGainFromNeighbor, maxGain) < 0) {
      agent.setChosenValueAtEachTimeStep(timeStep, chosenMGMValue);
    }
    
    agent.stopStimulatedTiming();
  }
  
  private List<ACLMessage> waitingForMessageFromNeighborWithTime(int msgCode) {
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
      else {
        if (agent.isPrinting()) {agent.print("Waiting in MGM IMPROVE");}
          block();
      }
    }
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
