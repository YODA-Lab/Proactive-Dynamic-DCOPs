package behavior;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;;

/**
 * REVIEWED 
 * @author khoihd
 *
 */
public class LS_RECEIVE_IMPROVE extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -5530908625966260157L;

	private AgentPDDCOP agent;
	private int lastTimeStep;
	
	public LS_RECEIVE_IMPROVE(AgentPDDCOP agent, int lastTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
	}
	
	@SuppressWarnings("unchecked")
  @Override
	public void action() {
    if (agent.getLocalSearchIteration() == AgentPDDCOP.MAX_ITERATION) {
      return;
    }
		
		List<ACLMessage> messageList = waitingForMessageFromNeighborWithTime(LS_IMPROVE);
		
		agent.startSimulatedTiming();
		
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		for (ACLMessage msg : messageList) {
			List<Double> improveUtilFromNeighbor = new ArrayList<Double>();
			try {
				improveUtilFromNeighbor = (ArrayList<Double>) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			if (improveUtilFromNeighbor.size() == 0) {
				continue;
			}
			else {
				for (int ts=0; ts <= lastTimeStep; ts++) {
				  // Set my best improve value list to null if one of the neighbors has better improved utility
					if (Double.compare(improveUtilFromNeighbor.get(ts), 0) > 0 && 
					      Double.compare(improveUtilFromNeighbor.get(ts), agent.getBestImproveUtilityList().get(ts)) > 0) {
						agent.getBestImproveValueList().set(ts, null);
					}
				}
			}
		}
		
		// Set value of the time step to null if self improve is negative
    for (int index = 0; index <= lastTimeStep; index++) {
			if (Double.compare(agent.getBestImproveUtilityList().get(index), 0) <= 0) {
				agent.getBestImproveValueList().set(index, null);
			}
		}
		
		// Set local assignment based on the best improve value list
    for (int index = 0; index <= lastTimeStep; index++) {
			String improvedValue = agent.getBestImproveValueList().get(index);
			if (improvedValue != null) {
				agent.print("improvedValue at timestep=" + index + " is: " + improvedValue);
				agent.setValueAtTimeStep(index, improvedValue);
			}
		}
		
    agent.stopStimulatedTiming();
    
		for (AID neighbor:agent.getNeighborAIDSet()) { 
			agent.sendObjectMessageWithTime(neighbor, agent.getBestImproveValueList(), 
						LS_VALUE, agent.getSimulatedTime());	
		}
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

	@Override
	public boolean done() {
	  agent.print("is done RECEIVE_IMPROVE: " + agent.getLocalSearchIteration());
		return agent.getLocalSearchIteration() == AgentPDDCOP.MAX_ITERATION;
	}
}