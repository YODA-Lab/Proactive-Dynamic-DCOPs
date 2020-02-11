package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.AgentPDDCOP.DcopAlgorithm;
import utilities.*;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * @author khoihd
 *
 */
public class LS_RECEIVE_SEND_LS_UTIL extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 4766760189659187968L;

	AgentPDDCOP agent;
	
	private int lastTimeStep;
	
	public LS_RECEIVE_SEND_LS_UTIL(AgentPDDCOP agent, int lastTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
	}
	
	@Override
	public void action() {
//		// no need to back up simulated time, since stop condition occurs in done condition.
//		int msgCount = 0;
//		while (msgCount < agent.getNeighborAIDList().size()) {
//			MessageTemplate template = MessageTemplate.MatchPerformative(LS_ITERATION_DONE);
//			ACLMessage receivedMessage = myAgent.receive(template);
//			if (receivedMessage != null) {
//				msgCount++;
//				
//				long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
//				if (timeFromReceiveMessage > agent.getSimulatedTime())
//					agent.setSimulatedTime(timeFromReceiveMessage);
//			}
//			else
//				block();	
//		}
//		agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
		
    double utilFromChildren = 0;
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(INIT_LS_UTIL);
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
		
    agent.setUtilityAndCost(utilFromChildren + 
        agent.utilityWithParentAndPseudoAndUnary(lastTimeStep) - agent.calculcatingSwitchingCost());

		if (!agent.isRoot()) {
			agent.sendObjectMessageWithTime(agent.getParentAID(), 
					String.valueOf(agent.getUtilityAndCost()), LS_UTIL, agent.getSimulatedTime());
		}
		else {
//			agent.setEndTime(System.currentTimeMillis());
			Utilities.writeUtil_Time_LS(agent);
//			agent.setOldLSRunningTime(agent.getEndTime() - agent.getStartTime());
			agent.setOldLSUtility(agent.getUtilityAndCost());
			
			System.out.println("SIMULATED TIME: " + agent.getSimulatedTime()/1000000 + "ms");
			int countIteration = agent.getLsIteration() + 1;
//			if (agent.algorithm == ND_DCOP.LS_SDPOP) {
//				System.err.println("Utility of Local-search DPOP at iteration " + countIteration + ": " + agent.getUtilityAndCost());
//			}
//			else if (agent.algorithm == ND_DCOP.LS_RAND)
			if (agent.getAlgorithm() == DcopAlgorithm.JESP)
				System.err.println("Utility of Local-search RAND at iteration " + countIteration + ": " + agent.getUtilityAndCost());
		}
		
		agent.incrementLocalSearchIteration();
		
		if (agent.getLsIteration() < AgentPDDCOP.MAX_ITERATION) {
			agent.sendImprove(lastTimeStep);
		}
	}

	@Override
	public boolean done() {
		return agent.getLsIteration() == AgentPDDCOP.MAX_ITERATION;
	}
	
  private List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getChildrenAIDList().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.receive(template);
        
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
    agent.setSimulatedTime(agent.getSimulatedTime() + AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}
