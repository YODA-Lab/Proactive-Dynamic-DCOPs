package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
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
		agent.startSimulatedTiming();
		
		for (ACLMessage msg : receiveMessages) {
		  try {
        utilFromChildren += (Double) msg.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
		}
				
		// Send the partial quality of the subtree to parent
		agent.setCurentLocalSearchQuality(utilFromChildren + 
				agent.utilityWithParentAndPseudoAndUnary(lastTimeStep) - agent.calculcatingSwitchingCost());
		
		agent.stopStimulatedTiming();

		if (!agent.isRoot()) {
			agent.sendObjectMessageWithTime(agent.getParentAID(), agent.getCurentLocalSearchQuality(), INIT_LS_UTIL, agent.getSimulatedTime());
		}
		else {
		  // First time
			agent.setBestLocalSearchQuality(agent.getCurentLocalSearchQuality());
			agent.setBestLocalSearchRuntime(agent.getSimulatedTime());
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
}
