package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.AgentPDDCOP.DcopAlgorithm;
import agent.AgentPDDCOP.DynamicType;
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
    double utilFromChildren = 0;
    List<ACLMessage> receiveMessages = waitingForMessageFromChildrenWithTime(INIT_LS_UTIL);
    
    for (ACLMessage msg : receiveMessages) {
      try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }
		
    agent.setCurentLocalSearchQuality(utilFromChildren + 
        agent.utilityWithParentAndPseudoAndUnary(lastTimeStep) - agent.calculcatingSwitchingCost());

		if (!agent.isRoot()) {
			agent.sendObjectMessageWithTime(agent.getParentAID(), agent.getCurentLocalSearchQuality(), LS_UTIL, agent.getSimulatedTime());
		}
		else {
		  if (Double.compare(agent.getCurentLocalSearchQuality(), agent.getBestLocalSearchQuality()) > 0) {
		    agent.setBestLocalSearchQuality(agent.getCurentLocalSearchQuality());
		    //TODO: set runtime
		  }
		  
		  if (agent.getLsIteration() == AgentPDDCOP.MAX_ITERATION) {
		    //TODO: write solution quality and runtimes to file
		  }
//			Utilities.writeUtil_Time_LS(agent);
		}
		
		agent.incrementLocalSearchIteration();
		
		if (agent.getLsIteration() < AgentPDDCOP.MAX_ITERATION) {
//			agent.archivedSendImprove(lastTimeStep);
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
