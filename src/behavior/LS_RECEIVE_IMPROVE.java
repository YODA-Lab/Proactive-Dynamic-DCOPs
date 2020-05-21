package behavior;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.AgentPDDCOP;;

/**
 * REVIEWED 
 * @author khoihd
 *
 */
public class LS_RECEIVE_IMPROVE extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -5530908625966260157L;

	private AgentPDDCOP agent;
	private int lastTimeStep;
	private int localTimeStep;
	
	public LS_RECEIVE_IMPROVE(AgentPDDCOP agent, int lastTimeStep, int localTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
		this.localTimeStep = localTimeStep;
	}
	
	@SuppressWarnings("unchecked")
  @Override
	public void action() {
		List<ACLMessage> messageList = waitingForMessageFromNeighborWithTime(LS_IMPROVE);
		
		agent.startSimulatedTiming();
		
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		for (ACLMessage msg : messageList) {
			Map<Integer, Double> improveUtilFromNeighbor = new HashMap<>();
			try {
				improveUtilFromNeighbor = (Map<Integer, Double>) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			if (improveUtilFromNeighbor.size() == 0) {
				continue;
			}
			else {
				for (int ts = 0; ts <= lastTimeStep; ts++) {
				  // Set my best improve value list to null if one of the neighbors has better improved utility
					if (Double.compare(agent.getBestImproveUtilityMap().getOrDefault(ts, 0D), 0) <= 0 || 
					      Double.compare(improveUtilFromNeighbor.get(ts), agent.getBestImproveUtilityMap().get(ts)) > 0) {
						agent.getBestImproveValueMap().put(ts, null);
					}
				}
			}
		}
		
		// Set local assignment based on the best improve value list
    for (int index = 0; index <= lastTimeStep; index++) {
			String improvedValue = agent.getBestImproveValueMap().get(index);
			if (improvedValue != null) {
				agent.print("has improvedValue at timestep=" + index + " is: " + improvedValue);
				agent.setValueAtTimeStep(index, improvedValue);
			}
		}
		
    agent.stopStimulatedTiming();
		
    agent.print("is done RECEIVE_IMPROVE at iteration = " + localTimeStep);
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
          block();
      }
    }
    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return messageList;
  }
}