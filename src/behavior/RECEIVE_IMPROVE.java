package behavior;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;

import agent.AgentPDDCOP;

/**
 * @author khoihd
 *
 */
public class RECEIVE_IMPROVE extends Behaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -5530908625966260157L;

	AgentPDDCOP agent;
	
	public RECEIVE_IMPROVE(AgentPDDCOP agent) {
		super(agent);
		this.agent = agent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		// backup oldSimulatedTime
		long oldSimulatedTime = agent.getSimulatedTime();
		
		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
		
		while (messageList.size() < agent.getNeighborStrList().size()) {
			if (agent.getLsIteration() == AgentPDDCOP.MAX_ITERATION) {
				agent.setSimulatedTime(oldSimulatedTime);
				return;
			}
			MessageTemplate template = MessageTemplate.MatchPerformative(LS_IMPROVE);
			ACLMessage receivedMessage = myAgent.receive(template);
			if (receivedMessage != null) {
//				System.out.println("Agent " + getLocalName() + " receive message "
//						+ msgTypes[LS_IMPROVE] + " from Agent " + receivedMessage.
//						getSender().getLocalName());
				
				long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
				if (timeFromReceiveMessage > agent.getSimulatedTime())
					agent.setSimulatedTime(timeFromReceiveMessage);
				
				messageList.add(receivedMessage);
			}
			else
				block();
		}
		
		agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
		
		agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
		
		//ArrayList<Double> currentBestImproveUtilList = (ArrayList<Double>) bestImproveUtilityList.clone();
		for (ACLMessage msg:messageList) {
//			ArrayList<Double> improveUtilFromNeighbor = new ArrayList<Double>();
			Double improveUtilFromNeighbor = null;
			try {
//				improveUtilFromNeighbor = (ArrayList<Double>) msg.getContentObject();
				improveUtilFromNeighbor = (Double) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
//			if (improveUtilFromNeighbor.size() == 0)
			if (improveUtilFromNeighbor == null)
				continue;
			else {
//				for (int ts=0; ts<=agent.h; ts++) {
//					//exist a neighbor that dominate my improve, so I set my best improve value to null
//					if (improveUtilFromNeighbor.get(ts) > 0 && 
//					improveUtilFromNeighbor.get(ts) > agent.getBestImproveUtilityList().get(ts)) {
//						agent.getBestImproveValueList().set(ts, null);
//					}
//				}
				if (improveUtilFromNeighbor > agent.getBestImproveUtility())
					agent.setBestImproveUtility(null);
				//if not, my best improve value dominates all
			}
		}
		
		//if I cannot improve any at all, I set my value to null
//		for (int index=0; index<=agent.h; index++) {
			if (agent.getBestImproveUtility() <= 0)
				agent.setBestImproveValueListJESP(null);
//		}
		
		//update my values base on my best improve
		for (int index=0; index<=agent.getHorizon(); index++) {
			String improvedValue = agent.getBestImproveValueList().get(index);
			if (improvedValue != null) {
				System.err.println(agent.getAgentID() + " " + improvedValue);
				agent.getChosenValueAtEachTSMap().put(index, improvedValue);
			}
		}
		
		agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
		
		for (AID neighbor:agent.getNeighborAIDList()) 
			agent.sendObjectMessageWithTime(neighbor, agent.getBestImproveValueList(), 
						LS_VALUE, agent.getSimulatedTime());			
	}

	@Override
	public boolean done() {
		return agent.getLsIteration() == AgentPDDCOP.MAX_ITERATION;
	}
}
