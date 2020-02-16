package behavior;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;;

public class RECEIVE_IMPROVE extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -5530908625966260157L;

	private AgentPDDCOP agent;
	private int lastTimeStep;
	
	public RECEIVE_IMPROVE(AgentPDDCOP agent, int lastTimeStep) {
		super(agent);
		this.agent = agent;
		this.lastTimeStep = lastTimeStep;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		// backup oldSimulatedTime
		long oldSimulatedTime = agent.getSimulatedTime();
		
		List<ACLMessage> messageList = waitingForMessageFromNeighborWithTime(LS_IMPROVE);
	
		agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
		
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
					//exist a neighbor that dominate my improve, so I set my best improve value to null
					if (Double.compare(improveUtilFromNeighbor.get(ts), 0) > 0 && 
					      Double.compare(improveUtilFromNeighbor.get(ts), agent.getBestImproveUtilityList().get(ts)) > 0) {
						agent.getBestImproveValueList().set(ts, null);
					}
				}
				//if not, my best improve value dominates all
			}
		}
		
		//if I cannot improve any at all, I set my value to null
    for (int index = 0; index <= lastTimeStep; index++) {
			if (Double.compare(agent.getBestImproveUtilityList().get(index), 0) <= 0) {
				agent.getBestImproveValueList().set(index, null);
			}
		}
		
		//update my values base on my best improve
    for (int index = 0; index <= lastTimeStep; index++) {
			String improvedValue = agent.getBestImproveValueList().get(index);
			if (improvedValue != null) {
				System.err.println(agent.getAgentID() + " " + improvedValue);
				agent.setValueAtTimeStep(index, improvedValue);
			}
		}
		
		agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		
		for (AID neighbor:agent.getNeighborAIDList()) { 
			agent.sendObjectMessageWithTime(neighbor, agent.getBestImproveValueList(), 
						LS_VALUE, agent.getSimulatedTime());	
		}
	}
	
  private List<ACLMessage> waitingForMessageFromNeighborWithTime(int msgCode) {
    List<ACLMessage> messageList = new ArrayList<ACLMessage>();

    while (messageList.size() < agent.getNeighborAIDList().size()) {
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

	@Override
	public boolean done() {
		return agent.getLsIteration() == AgentPDDCOP.MAX_ITERATION;
	}
}