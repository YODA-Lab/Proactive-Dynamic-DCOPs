package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.AgentPDDCOP.DcopAlgorithm;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import table.Row;
import table.Table;
import utilities.Utilities;

/**
 * @author khoihd
 *
 */
public class ACTUAL_RECEIVE_SEND_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

  private static final long serialVersionUID = 6619734019693007342L;

  AgentPDDCOP agent;
  
  public ACTUAL_RECEIVE_SEND_UTIL(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }
  
  @Override
  public void action() {    
    // Retrieve the actual DPOP tables here
    if (!agent.isRunningAlgorithm(DcopAlgorithm.REACT)) {
      for (int i = 0; i < agent.getHorizon(); i++) {
        addReactTable(i);
        agent.getActualDpopTableAcrossTimeStep().get(i).addAll(agent.getDpopDecisionTableList());
      }
    }
    
    double utilFromChildren = 0;
    
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(ACTUAL_LS_UTIL);
    agent.startSimulatedTiming();
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
            
    // Send the partial quality of the subtree to parent
    double utilitySubtree = utilFromChildren + agent.computeActualUtilityWithParentAndPseudoParent() - agent.computeSwitchingCostAllTimeStep();
    
    agent.stopStimulatedTiming();

    if (!agent.isRoot()) {
      agent.sendObjectMessageWithTime(agent.getParentAID(), utilitySubtree, ACTUAL_LS_UTIL, agent.getSimulatedTime());
    }
    else {
      agent.setActualSolutionQuality(utilitySubtree);
      Utilities.writeResult(agent);
    }
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
  
  /**
   * @param timeStep
   */
  private void addReactTable(int timeStep) {
    // traverse to each random table
    for (Table randTable : agent.getDpopRandomTableList()) {
      List<String> decLabel = randTable.getDecVarLabel();
      // at current time step, create a new table, simulate the random,
      // and add the corresponding random values

      Table newTable = new Table(decLabel, AgentPDDCOP.DECISION_TABLE);
      
      String simulatedRandomValues = agent.getPickedRandomAt(timeStep);

      for (Row row : randTable.getRowList()) {
        if (row.getRandomList().get(0).equals(simulatedRandomValues)) {
          newTable.addRow(new Row(row.getValueList(), row.getUtility()));
        }
      }
      
      agent.getActualDpopTableAcrossTimeStep().computeIfAbsent(timeStep, k -> new ArrayList<>()).add(newTable);
    }
  }
}
