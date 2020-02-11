package behavior;

import utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.AgentPDDCOP.DcopAlgorithm;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * @author khoihd
 *
 */
public class INIT_RECEIVE_SEND_LS_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 6619734019693007342L;

	AgentPDDCOP agent;
	
	private int lastTimeStep;
	
	public INIT_RECEIVE_SEND_LS_UTIL(AgentPDDCOP agent, int lastTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
	}
	
	@Override
	public void action() {		
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
			agent.sendObjectMessageWithTime(agent.getParentAID(), agent.getUtilityAndCost(), INIT_LS_UTIL, agent.getSimulatedTime());
		}
		else {
//			agent.setOldLSRunningTime(System.currentTimeMillis() - agent.getStartTime());

			agent.setOldLSUtility(agent.getUtilityAndCost());
//			if (agent.algorithm == ND_DCOP.LS_SDPOP)
//				Utilities.writeUtil_Time_BeforeLS(agent);
//			else if (agent.algorithm == ND_DCOP.LS_RAND)
			if (agent.getAlgorithm() == DcopAlgorithm.JESP)
				Utilities.writeUtil_Time_BeforeLS_Rand(agent);
			
			System.out.println("SIMULATED TIME: " + agent.getSimulatedTime()/1000000 + "ms");
			
//			if (agent.algorithm == ND_DCOP.LS_SDPOP)
//				System.err.println("Utility of Local-search DPOP at iteration 0: " + agent.getUtilityAndCost());
//			else if (agent.algorithm == ND_DCOP.LS_RAND)
			if (agent.getAlgorithm() == DcopAlgorithm.JESP)
				System.err.println("Utility of Local-search RAND at iteration 0: " + agent.getUtilityAndCost());
		}
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
