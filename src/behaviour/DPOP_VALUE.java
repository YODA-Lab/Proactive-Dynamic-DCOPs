//package behaviour;
//
//import static java.nio.file.StandardOpenOption.APPEND;
//import static java.nio.file.StandardOpenOption.CREATE;
//import jade.core.AID;
//import jade.core.behaviours.OneShotBehaviour;
//import jade.lang.acl.ACLMessage;
//import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.UnreadableException;
//
//import java.io.BufferedOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import agent.ND_DCOP;
//import table.Row;
///*	1. IF X is a root
// * 		Send the value of root to all the children
// *		PRINT OUT the value picked
// *		STOP
// * 
// *  2. ELSE (not a root)
// *  	Waiting from message from the parent
// *  	From the received parent_value, pick X_value from the store (parent_value, X_value)
// *  	//which is the corresponding X_value to parent_value with the minimum utility
// *  	2.1 IF (X is not a leaf)
// *  		Send the value to all the children
// *  	PRINT_OUT the picked value
// *  	STOP 
// */
//public class DPOP_VALUE extends OneShotBehaviour implements MESSAGE_TYPE {
//
//	private static final long serialVersionUID = 4288241761322913640L;
//	
//	ND_DCOP agent;
//	
//	public DPOP_VALUE(ND_DCOP agent) {
//		super(agent);
//		this.agent = agent;
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void action() {
//		agent.setValuesToSendInVALUEPhase(new HashMap<String, String>());
//		if (agent.isRoot()) {
//			agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
//			if (agent.algorithm == ND_DCOP.C_DPOP) {
//				System.out.println(agent.getIdStr() + " choose value " + agent.getChosenValue());
//			}
//			
//			agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
//			agent.addValuesToSendInValuePhase(agent.getIdStr(), agent.getChosenValue());
//			for (AID childrenAgentAID:agent.getChildrenAIDList()) {
//				agent.sendObjectMessageWithTime(childrenAgentAID, agent.getValuesToSendInVALUEPhase(),
//								DPOP_VALUE, agent.getSimulatedTime());
//			}
//		}
//		else {//leaf or internal nodes
//			ACLMessage receivedMessage = waitingForValuesInItsAgentViewFromParent(DPOP_VALUE);
//			agent.setCurrentStartTime(agent.getBean().getCurrentThreadUserTime());
//			
//			HashMap<Integer, String> variableAgentViewIndexValueMap = new HashMap<Integer, String>();
//			HashMap<String, String> valuesFromParent = new HashMap<String, String>();
//			try {
//				valuesFromParent = (HashMap<String, String>) receivedMessage.getContentObject();
//			} catch (UnreadableException e) {
//				e.printStackTrace();
//			}
//
//			for (String agentKey:valuesFromParent.keySet()) {
//				int positionInParentMessage = agent.getAgentViewTable().getDecVarLabel().indexOf(agentKey);
//				if (positionInParentMessage == -1) //not in agentView
//					continue;
//				//if exist this agent in agent view, add to values to send
//				agent.addValuesToSendInValuePhase(agentKey, valuesFromParent.get(agentKey));
//				variableAgentViewIndexValueMap.put(positionInParentMessage						
//											,valuesFromParent.get(agentKey));
//			}
//			int agentIndex = agent.getAgentViewTable().getDecVarLabel().indexOf(agent.getIdStr());
//
//			Row chosenRow = new Row();
//			double maxUtility = Integer.MIN_VALUE;
//			for (Row agentViewRow:agent.getAgentViewTable().getTable()) {
//				boolean isMatch = true;	
//
//				//check for each of index, get values and compared to the agentViewRow's values
//				//if one of the values is not match, set flag to false and skip to the next row
//				for (Integer variableIndex:variableAgentViewIndexValueMap.keySet()) {
//					if (agentViewRow.getValueAtPosition(variableIndex).equals(variableAgentViewIndexValueMap.get(variableIndex)) == false) {
//						isMatch = false;
//						break;
//					}
//				}
//				if (isMatch == false)
//					continue;
//
//				if (agentViewRow.getUtility() > maxUtility) {
//					maxUtility = agentViewRow.getUtility();
//					chosenRow = agentViewRow;
//				}
//			}
//			
//			agent.setChosenValue(chosenRow.getValueAtPosition(agentIndex));
//
//			//add its chosen value to the map to send to its children
//			agent.addValuesToSendInValuePhase(agent.getIdStr(), agent.getChosenValue());			
//			
//			if (agent.algorithm == ND_DCOP.C_DPOP) {
//				System.out.println("Chosen value is " + agent.getChosenValue());
//			}
//			//correct
//			else if (agent.algorithm == ND_DCOP.LS_SDPOP || agent.algorithm == ND_DCOP.SDPOP) {
//				agent.getValueAtEachTSMap().put(agent.getCurrentTS(), agent.getChosenValue());
//			}
//			else if (agent.algorithm == ND_DCOP.HYBRID) {
//				agent.getValueAtEachTSMap().put(agent.getCurrentTS(), agent.getChosenValue());
//			}
//			else if (agent.algorithm == ND_DCOP.REACT) {
//				agent.getValueAtEachTSMap().put(agent.getCurrentTS(), agent.getChosenValue());
//			}
//			//correct
//			else if (agent.algorithm == ND_DCOP.FORWARD) {
//				//solution at current time step
//				if (agent.getCurrentTS() < agent.h-1)
//					agent.getValueAtEachTSMap().put(agent.getCurrentTS(), agent.getChosenValue());
//				//solution at h because we solve h before h-1
//				else if (agent.getCurrentTS() == agent.h-1)
//					agent.getValueAtEachTSMap().put(agent.h, agent.getChosenValue());
//				//solution at h-1
//				else if (agent.getCurrentTS() == agent.h)
//					agent.getValueAtEachTSMap().put(agent.h-1, agent.getChosenValue());
//
//			}
//			else if (agent.algorithm == ND_DCOP.BACKWARD) {
//				agent.getValueAtEachTSMap().put(agent.h - agent.getCurrentTS(), agent.getChosenValue());
//			}
//			
//			agent.addupSimulatedTime(agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
//			
//			if (agent.isLeaf() == false) {
//				ArrayList<String> agent_value = new ArrayList<String>();
//				agent_value.add(agent.getIdStr());
//				agent_value.add(agent.getChosenValue());
//				if (agent.algorithm == ND_DCOP.C_DPOP) {
//					System.out.println("Chosen value is " + agent.getChosenValue());
//				}
//				agent_value.add(String.valueOf(agent.getCurrentGlobalUtility()));
//				
//				for (AID children:agent.getChildrenAIDList()) {
//					agent.sendObjectMessageWithTime(children, agent.getValuesToSendInVALUEPhase()
//													, DPOP_VALUE, agent.getSimulatedTime());
//				}
//			}
//		}
//		
//		
//		agent.incrementCurrentTS();
//	}
//	
//	public ArrayList<ACLMessage> waitingForMessageFromPseudoParent(int msgCode) {
//		ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
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
//	
//	public ACLMessage waitingForValuesInItsAgentViewFromParent(int msgCode) {
//		ACLMessage receivedMessage = null;
//		while (true) {
//			
//		MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
//		receivedMessage = myAgent.receive(template);
//		if (receivedMessage != null) {
//			long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
//			if (timeFromReceiveMessage > agent.getSimulatedTime())
//				agent.setSimulatedTime(timeFromReceiveMessage);
//			break;
//		}
//		else
//			block();
//		}
//		
//		agent.addupSimulatedTime(ND_DCOP.getDelayMessageTime());
//		return receivedMessage;
//	}
//	
//	public void writeChosenValueToFile_Not_FW() {
//		String algName = null;
//		if (agent.algorithm == ND_DCOP.REACT)
//			algName = "react";
//		else if (agent.algorithm == ND_DCOP.HYBRID)
//			algName = "hybrid";
//		
//		String line = "";
//		String alg = ND_DCOP.algTypes[agent.algorithm];
//		String scType = (agent.scType == ND_DCOP.CONSTANT) ? "constant" : "linear";
//		if (agent.getCurrentTS() == 0)
//			line = line + alg + "\t" + agent.inputFileName + "\t" + "sCost=" + agent.switchingCost
//			+ "\t" + "scType=" + scType + "\n";
//			
//		line = line + "ts=" + agent.getCurrentTS()
//					+ "\t" + "x=" + agent.getValueAtEachTSMap().get(agent.getCurrentTS())
//					+ "\t" + "y=" + agent.getPickedRandomAt(agent.getCurrentTS());
//		
//		//write switching cost after y (react/hybrid) or x (forward)
//		String switchOrNot = null;
//		if (agent.getCurrentTS() == 0)
//			switchOrNot = ND_DCOP.switchNo;
//		else {
//			if (agent.getValueAtEachTSMap().get(agent.getCurrentTS()).equals
//					(agent.getValueAtEachTSMap().get(agent.getCurrentTS()-1)) == true) {
//				switchOrNot = ND_DCOP.switchNo;
//			}
//			else {
//				switchOrNot = ND_DCOP.switchYes;
//			}
//		}
//		
//		line = line + "\t" + "sw=" + switchOrNot + "\n";	
//	
//		String fileName = "id=" + agent.instanceD + "/sw=" + (int) agent.switchingCost + "/" + algName + "_" + agent.getIdStr() + ".txt";
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
//	
//	public void writeChosenValueToFileFW() {
//		String algName = "forward";
//		
//		String line = "";
//		String alg = ND_DCOP.algTypes[agent.algorithm];
//		String scType = (agent.scType == ND_DCOP.CONSTANT) ? "constant" : "linear";
//		if (agent.getCurrentTS() == 0)
//			line = line + alg + "\t" + agent.inputFileName + "\t" + "sCost=" + agent.switchingCost
//			+ "\t" + "scType=" + scType + "\n";
//				
//		//write switching cost after x (forward)
//		String switchOrNot = null;
//		if (agent.getCurrentTS() == 0) {
//			switchOrNot = ND_DCOP.switchNo;
//			line = line + "ts=" + agent.getCurrentTS()
//			 		+ "\t" + "x=" + agent.getValueAtEachTSMap().get(agent.getCurrentTS())
//					+ "\t" + "sw=" + switchOrNot + "\n";
//		}
//		else if (agent.getCurrentTS() == agent.h-1) {
//			//no switching cost for now, wait for
//			line = line + "ts=" + agent.h
//			 		+ "\t" + "x=" + agent.getValueAtEachTSMap().get(agent.h);
//			agent.setLastLine(line);
//		}
//		else if (agent.getCurrentTS() == agent.h) {
//			line = line + "ts=" + (agent.h-1)
//	 					+ "\t" + "x=" + agent.getValueAtEachTSMap().get(agent.h-1);
//			
//			//compare value at h-1 with h-2
//			if (agent.getValueAtEachTSMap().get(agent.h-1).equals
//					(agent.getValueAtEachTSMap().get(agent.h-2)) == true) {
//				switchOrNot = ND_DCOP.switchNo;
//			}
//			else {
//				switchOrNot = ND_DCOP.switchYes;
//			}
//			
//			line = line + "\t" + "sw=" + switchOrNot + "\n" + agent.getLastLine();
//			
//			if (agent.getValueAtEachTSMap().get(agent.h).equals
//					(agent.getValueAtEachTSMap().get(agent.h-1)) == true) {
//				switchOrNot = ND_DCOP.switchNo;
//			}
//			else {
//				switchOrNot = ND_DCOP.switchYes;
//			}
//			
//			line = line + "\t" + "sw=" + switchOrNot + "\n";
//		}
//		else {
//			line = line + "ts=" + agent.getCurrentTS()
//	 					+ "\t" + "x=" + agent.getValueAtEachTSMap().get(agent.getCurrentTS());
//			if (agent.getValueAtEachTSMap().get(agent.getCurrentTS()).equals
//					(agent.getValueAtEachTSMap().get(agent.getCurrentTS()-1)) == true) {
//				switchOrNot = ND_DCOP.switchNo;
//			}
//			else {
//				switchOrNot = ND_DCOP.switchYes;
//			}
//			
//			line = line + "\t" + "sw=" + switchOrNot + "\n";
//		}
//				
//		//forward: at h-1, solve h, so not writing at all to wait for h (solve h-1, then write 2 at a time)
//		if (agent.getCurrentTS() != agent.h-1) {
//			String fileName = "id=" + agent.instanceD + "/sw=" + (int) agent.switchingCost + "/" + algName + "_" + agent.getIdStr() + ".txt";
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
//}
