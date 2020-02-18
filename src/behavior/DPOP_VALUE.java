package behavior;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agent.AgentPDDCOP;
import agent.AgentPDDCOP.DcopAlgorithm;
import table.Row;
/*	1. IF X is a root
 * 		Send the value of root to all the children
 *		PRINT OUT the value picked
 *		STOP
 * 
 *  2. ELSE (not a root)
 *  	Waiting from message from the parent
 *  	From the received parent_value, pick X_value from the store (parent_value, X_value)
 *  	//which is the corresponding X_value to parent_value with the minimum utility
 *  	2.1 IF (X is not a leaf)
 *  		Send the value to all the children
 *  	PRINT_OUT the picked value
 *  	STOP 
 */
public class DPOP_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = 4288241761322913640L;
	
	AgentPDDCOP agent;
	
	private int currentTimeStep;
	
	public DPOP_VALUE(AgentPDDCOP agent, int currentTimeStep) {
		super(agent);
		this.agent = agent;
		this.currentTimeStep = currentTimeStep;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {		
		if (agent.isRoot()) {
			agent.addValuesToSendInValuePhase(agent.getAgentID(), agent.getChosenValueAtEachTimeStep(currentTimeStep));
			for (AID childrenAgentAID:agent.getChildrenAIDList()) {
				agent.sendObjectMessageWithTime(childrenAgentAID, agent.getValuesToSendInVALUEPhase(),
								DPOP_VALUE, agent.getSimulatedTime());
			}
		}
		else {//leaf or internal nodes
			ACLMessage receivedMessage = waitingForValuesInItsAgentViewFromParent(DPOP_VALUE);
			agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
			
			HashMap<Integer, String> variableAgentViewIndexValueMap = new HashMap<Integer, String>();
			HashMap<String, String> valuesFromParent = new HashMap<String, String>();
			try {
				valuesFromParent = (HashMap<String, String>) receivedMessage.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}

			for (String agentKey:valuesFromParent.keySet()) {
				int positionInParentMessage = agent.getAgentViewTable().getDecVarLabel().indexOf(agentKey);
				if (positionInParentMessage == -1) //not in agentView
					continue;
				//if exist this agent in agent view, add to values to send
				agent.addValuesToSendInValuePhase(agentKey, valuesFromParent.get(agentKey));
				variableAgentViewIndexValueMap.put(positionInParentMessage						
											,valuesFromParent.get(agentKey));
			}
			int agentIndex = agent.getAgentViewTable().getDecVarLabel().indexOf(agent.getAgentID());

			Row chosenRow = new Row();
			double maxUtility = Integer.MIN_VALUE;
			for (Row agentViewRow:agent.getAgentViewTable().getRowList()) {
				boolean isMatch = true;	

				//check for each of index, get values and compared to the agentViewRow's values
				//if one of the values is not match, set flag to false and skip to the next row
				for (Integer variableIndex : variableAgentViewIndexValueMap.keySet()) {
					if (agentViewRow.getValueAtPosition(variableIndex).equals(variableAgentViewIndexValueMap.get(variableIndex)) == false) {
						isMatch = false;
						break;
					}
				}
				if (isMatch == false)
					continue;

				if (agentViewRow.getUtility() > maxUtility) {
					maxUtility = agentViewRow.getUtility();
					chosenRow = agentViewRow;
				}
			}
			
			String chosenValue = chosenRow.getValueAtPosition(agentIndex);
			
			agent.setChosenValueAtEachTimeStep(currentTimeStep, chosenValue);

			agent.addValuesToSendInValuePhase(agent.getAgentID(), chosenValue);
			
      if (agent.isRunningAlgorithm(DcopAlgorithm.C_DPOP)) {
        agent.setChosenValueAtEachTimeStep(currentTimeStep, chosenValue);
      }
			
			agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
			
			if (agent.isLeaf() == false) {
				List<String> agent_value = new ArrayList<String>();
				agent_value.add(agent.getAgentID());
				agent_value.add(chosenValue);
				if (agent.isRunningAlgorithm(DcopAlgorithm.C_DPOP)) {
					System.out.println("Chosen value is " + chosenValue);
				}
				agent_value.add(String.valueOf(agent.getCurrentLocalSearchSolutionQuality()));
				
				for (AID children:agent.getChildrenAIDList()) {
					agent.sendObjectMessageWithTime(children, agent.getValuesToSendInVALUEPhase(), DPOP_VALUE, agent.getSimulatedTime());
				}
			}
		}
	}
	
//	private List<ACLMessage> waitingForMessageFromPseudoParent(int msgCode) {
//		List<ACLMessage> messageList = new ArrayList<ACLMessage>();
//		//no of messages are no of pseudoParent + 1 (parent)
//		while (messageList.size() < agent.getPseudoParentAIDList().size() + 1) {
//			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
//			ACLMessage receivedMessage = myAgent.receive(template);
//			if (receivedMessage != null) {
//				messageList.add(receivedMessage);
//			}
//			else
//				block();
//		}
//		return messageList;
//	}
	
  private ACLMessage waitingForValuesInItsAgentViewFromParent(int msgCode) {
    ACLMessage receivedMessage = null;

    while (true) {
      agent.startSimulatedTiming();

      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      receivedMessage = myAgent.receive(template);

      agent.stopStimulatedTiming();
      if (receivedMessage != null) {
        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
        if (timeFromReceiveMessage > agent.getSimulatedTime()) {
          agent.setSimulatedTime(timeFromReceiveMessage);
        }
        
        break;
      } 
      else {
        block();
      }
    }

    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
    return receivedMessage;
  }
	
//	private void writeChosenValueToFile_Not_FW() {
//		String algName = null;
//		if (agent.isRunningAlgorithm(DcopAlgorithm.REACT))
//			algName = "react";
//		else if (agent.isRunningAlgorithm(DcopAlgorithm.HYBRID))
//			algName = "hybrid";
//		
//		String line = "";
//		String alg = String.valueOf(agent.getAlgorithm());
////		String scType = (agent.scType == AgentPDDCOP.CONSTANT) ? "constant" : "linear";
//		if (currentTimeStep == 0)
//			line = line + alg + "\t" + agent.getInputFileName() + "\t" + "sCost=" + agent.getSwitchingCost()
//			+ "\t" + "scType=" + AgentPDDCOP.SWITCHING_TYPE + "\n";
//			
//		line = line + "ts=" + currentTimeStep
//					+ "\t" + "x=" + agent.getChosenValueAtEachTimeStep(currentTimeStep)
//					+ "\t" + "y=" + agent.getPickedRandomAt(currentTimeStep);
//		
//		//write switching cost after y (react/hybrid) or x (forward)
//		String switchOrNot = null;
//		if (currentTimeStep == 0)
//			switchOrNot = AgentPDDCOP.switchNo;
//		else {
//			if (agent.getChosenValueAtEachTimeStep(currentTimeStep).equals
//					(agent.getChosenValueAtEachTimeStep(currentTimeStep-1)) == true) {
//				switchOrNot = AgentPDDCOP.switchNo;
//			}
//			else {
//				switchOrNot = AgentPDDCOP.switchYes;
//			}
//		}
//		
//		line = line + "\t" + "sw=" + switchOrNot + "\n";	
//	
//		String fileName = "id=" + agent.getInstanceID() + "/sw=" + agent.getSwitchingCost() + "/" + algName + "_" + agent.getAgentID() + ".txt";
//		byte data[] = line.getBytes();
//	    Path p = Paths.get(fileName);
//
//		try (OutputStream out = new BufferedOutputStream(
//			Files.newOutputStream(p, CREATE, APPEND))) {
//			out.write(data, 0, data.length);
//			out.flush();
//			out.close();
//		} catch (IOException x) {
//			System.err.println(x);
//		}
//	}
	
//	public void writeChosenValueToFileFW() {
////		String algName = "forward";
//		
//		String line = "";
////		String alg = AgentPDDCOP.algTypes[agent.algorithm];
////		String scType = (agent.scType == AgentPDDCOP.CONSTANT) ? "constant" : "linear";
//		if (currentTimeStep == 0)
//			line = line + agent.getAlgorithm() + "\t" + agent.getInputFileName() + "\t" + "sCost=" + agent.getSwitchingCost()
//			+ "\t" + "scType=" + AgentPDDCOP.SWITCHING_TYPE + "\n";
//				
//		//write switching cost after x (forward)
//		String switchOrNot = null;
//		if (currentTimeStep == 0) {
//			switchOrNot = AgentPDDCOP.switchNo;
//			line = line + "ts=" + currentTimeStep
//			 		+ "\t" + "x=" + agent.getChosenValueAtEachTimeStep(currentTimeStep)
//					+ "\t" + "sw=" + switchOrNot + "\n";
//		}
//		else if (currentTimeStep == agent.getHorizon()-1) {
//			//no switching cost for now, wait for
//			line = line + "ts=" + agent.getHorizon()
//			 		+ "\t" + "x=" + agent.getChosenValueAtEachTimeStep(agent.getHorizon());
//			agent.setLastLine(line);
//		}
//		else if (currentTimeStep == agent.getHorizon()) {
//			line = line + "ts=" + (agent.getHorizon()-1)
//	 					+ "\t" + "x=" + agent.getChosenValueAtEachTimeStep(agent.getHorizon()-1);
//			
//			//compare value at h-1 with h-2
//			if (agent.getChosenValueAtEachTimeStep(agent.getHorizon()-1).equals
//					(agent.getChosenValueAtEachTimeStep(agent.getHorizon()-2)) == true) {
//				switchOrNot = AgentPDDCOP.switchNo;
//			}
//			else {
//				switchOrNot = AgentPDDCOP.switchYes;
//			}
//			
//			line = line + "\t" + "sw=" + switchOrNot + "\n" + agent.getLastLine();
//			
//			if (agent.getChosenValueAtEachTimeStep(agent.getHorizon()).equals
//					(agent.getChosenValueAtEachTimeStep(agent.getHorizon()-1)) == true) {
//				switchOrNot = AgentPDDCOP.switchNo;
//			}
//			else {
//				switchOrNot = AgentPDDCOP.switchYes;
//			}
//			
//			line = line + "\t" + "sw=" + switchOrNot + "\n";
//		}
//		else {
//			line = line + "ts=" + currentTimeStep
//	 					+ "\t" + "x=" + agent.getChosenValueAtEachTimeStep(currentTimeStep);
//			if (agent.getChosenValueAtEachTimeStep(currentTimeStep).equals
//					(agent.getChosenValueAtEachTimeStep(currentTimeStep-1)) == true) {
//				switchOrNot = AgentPDDCOP.switchNo;
//			}
//			else {
//				switchOrNot = AgentPDDCOP.switchYes;
//			}
//			
//			line = line + "\t" + "sw=" + switchOrNot + "\n";
//		}
//				
//		//forward: at h-1, solve h, so not writing at all to wait for h (solve h-1, then write 2 at a time)
//		if (currentTimeStep != agent.getHorizon()-1) {
//			String fileName = "id=" + agent.getInstanceID() + "/sw=" + (int) agent.getSwitchingCost() + "/" + agent.getAlgorithm() + "_" + agent.getAgentID() + ".txt";
//			byte data[] = line.getBytes();
//		    Path p = Paths.get(fileName);
//	
//			try (OutputStream out = new BufferedOutputStream(
//				Files.newOutputStream(p, CREATE, APPEND))) {
//				out.write(data, 0, data.length);
//				out.flush();
//				out.close();
//			} catch (IOException x) {
//				System.err.println(x);
//			}
//		}
//	}
}
