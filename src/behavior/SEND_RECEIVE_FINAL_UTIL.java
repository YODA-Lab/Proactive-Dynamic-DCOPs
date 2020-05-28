package behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.AgentPDDCOP;
import agent.AgentPDDCOP.DynamicType;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import utilities.Utilities;

/**
 * @author khoihd
 *
 */
public class SEND_RECEIVE_FINAL_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

  private static final long serialVersionUID = 6619734019693007342L;

  AgentPDDCOP agent;
  
  public SEND_RECEIVE_FINAL_UTIL(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public void action() {    
    double pddcop_quality_from_children = 0D;
    Map<Integer, Double> actual_quality_from_children = new HashMap<>();
    Map<Integer, Double> actual_switching_cost_from_children = new HashMap<>();
    agent.print("Chosen value across time steps: " + agent.getChosenValueAtEachTSMap());
    
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(FINAL_UTIL);
    agent.startSimulatedTiming();
    
    for (ACLMessage msg : receiveMessages) {
      try {
        pddcop_quality_from_children += (Double) ((List) msg.getContentObject()).get(0);
        if (agent.isDynamic(DynamicType.ONLINE)) {
          Map<Integer, Double> quality_children = (Map<Integer, Double>) ((List) msg.getContentObject()).get(1);
          Map<Integer, Double> switching_cost_children = (Map<Integer, Double>) ((List) msg.getContentObject()).get(2);
          for (int i = -1; i <= agent.getHorizon(); i++) {
            actual_quality_from_children.merge(i, quality_children.getOrDefault(i, 0D), Double::sum);
            actual_switching_cost_from_children.merge(i, switching_cost_children.getOrDefault(i, 0D), Double::sum);
          }
        }
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
            
    // Send the partial quality of the subtree to parent
    double pddcop_solution_quality = pddcop_quality_from_children + agent.utilityLSWithParentAndPseudoAndUnary()
        - agent.computeSwitchingCostAllTimeStep();

    List messageForParent = new ArrayList();
   
    messageForParent.add(pddcop_solution_quality);
    
    Map<Integer, Double> actual_solution_quality = new HashMap<>();
    Map<Integer, Double> actual_switching_cost = new HashMap<>();

    if (agent.isDynamic(DynamicType.ONLINE)) {
      actual_solution_quality.putAll(agent.computeActualQualityWithoutTime());
      actual_switching_cost.putAll(agent.computeActualSwitchingCost());
      
      for (int i = -1; i <= agent.getHorizon(); i++) {
        actual_solution_quality.merge(i,
            actual_solution_quality.getOrDefault(i, 0D) + actual_quality_from_children.getOrDefault(i, 0D), Double::sum);
        actual_switching_cost.merge(i,
            actual_switching_cost.getOrDefault(i, 0D) + actual_switching_cost_from_children.getOrDefault(i, 0D), Double::sum);
      }
  
      messageForParent.add(actual_solution_quality);
      messageForParent.add(actual_switching_cost);
    }
    
    agent.stopStimulatedTiming();

    
    if (!agent.isRoot()) {
      agent.sendObjectMessageWithTime(agent.getParentAID(), messageForParent, FINAL_UTIL, agent.getSimulatedTime());
    }
    else {
      if (agent.isDynamic(DynamicType.ONLINE)) {
        for (int ts = 0; ts <= agent.getHorizon(); ts++) {
          double quality = actual_solution_quality.get(ts);
          double switchingCost = actual_switching_cost.get(ts);
          long solvingTime = ts == 0 ? agent.getDpopSolvingTime(ts) : agent.getDpopSolvingTime(ts) - agent.getDpopSolvingTime(ts - 1);
          
          agent.getEffectiveQualityMap().put(ts, quality);
          agent.getEffectiveSwitchingCostMap().put(ts, switchingCost);
          agent.getEffectiveSolvingTimeMap().put(ts, solvingTime);
        }
        
        agent.getEffectiveQualityMap().put(-1, actual_solution_quality.getOrDefault(-1, 0D));
        Utilities.writeEffectiveReward(agent);
      }
      
      // Write final results 
      agent.setSolutionQuality(pddcop_solution_quality);
      agent.setFinalRuntime(agent.getSimulatedTime());
      Utilities.writeFinalResult(agent);
    }
  }
  
  private List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getChildrenAIDSet().size()) {
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
//          block();
//      }
    }
    
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
