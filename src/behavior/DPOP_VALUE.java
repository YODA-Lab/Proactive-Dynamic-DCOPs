package behavior;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import agent.AgentPDDCOP;
import agent.DcopConstants.DcopAlgorithm;
import agent.DcopConstants.MessageType;
import agent.DcopConstants.PDDcopAlgorithm;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import table.RowString;

/**
 * @author khoihd REVIEWED <br>
 *         1. IF X is a root Send the value of root to all the children PRINT
 *         OUT the value picked STOP
 * 
 *         2. ELSE (not a root) Waiting from message from the parent From the
 *         received parent_value, pick X_value from the store (parent_value,
 *         X_value) //which is the corresponding X_value to parent_value with
 *         the minimum utility 2.1 IF (X is not a leaf) Send the value to all
 *         the children PRINT_OUT the picked value STOP
 */
public class DPOP_VALUE extends OneShotBehaviour {
	/**
   * 
   */
  private static final long serialVersionUID = -1272666821422854692L;

  AgentPDDCOP agent;

	private int currentTimeStep;

	public DPOP_VALUE(AgentPDDCOP agent, int currentTimeStep) {
		super(agent);
		this.agent = agent;
		this.currentTimeStep = currentTimeStep;
	}

	@Override
	public void action() {
	  if (agent.isDiscrete()) {
	    action_discrete();
	  }
	  else {
	    action_continuous();
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

  private void action_continuous() {
    agent.getDoubleValuesToSendInVALUEPhase().clear();
    
    // Root do the same thing for all algorithms
    if (agent.isRoot()) {
      rootSendChosenValueWithTime(currentTimeStep);
      return ;
    }
    
    nonRootChooseAndSendValue(currentTimeStep);
  }
  
  /**
   * Send value to the children. The value is already chosen in the UTIL phase. <br> 
   * The simulated processing time is ignore here because of lightweight operations.
   */
  private void rootSendChosenValueWithTime(int timeStep) {    
    agent.print("Choose value " + getValue(timeStep));

    agent.addDoubleValuesToSendInVALUEPhase(agent.getLocalName(), getValue(timeStep));
    
    for (AID childrenAgentAID : agent.getChildrenAIDSet()) {
      agent.sendObjectMessageWithTime(childrenAgentAID, agent.getDoubleValuesToSendInVALUEPhase(), MessageType.DPOP_VALUE, agent.getSimulatedTime());
    }
  }
  
  private double getValue(int timeStep) {
    return agent.getChosenDoubleValueAtEachTimeStep(timeStep);
  }
  
  /**
   * Non-root agent chooses and sends value to its children
   */
  private void nonRootChooseAndSendValue(int timeStep) {
    Map<String, Double> valuesFromParent = waitingForValuesFromParentWithTime(MessageType.DPOP_VALUE);
    
    agent.startSimulatedTiming();
    
    // Only choose value if running DPOP-like algorithm
    if (agent.getDcop_algorithm() == DcopAlgorithm.DPOP) {
      agent.setChosenDoubleValueAtEachTimeStep(timeStep, chooseValue_TABLE(valuesFromParent));
    } 
    else if (agent.getDcop_algorithm() == DcopAlgorithm.EC_DPOP) {
      agent.setChosenDoubleValueAtEachTimeStep(timeStep, chooseValue_FUNCTION(valuesFromParent));
    } 
    else if (agent.getDcop_algorithm() == DcopAlgorithm.AC_DPOP || agent.getDcop_algorithm() == DcopAlgorithm.CAC_DPOP) {
      agent.setChosenDoubleValueAtEachTimeStep(timeStep, chooseValue_HYBRID(valuesFromParent));
    }    
    
    agent.addDoubleValuesToSendInVALUEPhase(agent.getLocalName(), getValue(timeStep));
    agent.getDoubleValuesToSendInVALUEPhase().putAll(valuesFromParent);
        
    agent.stopSimulatedTiming();
    
    if (agent.isLeaf() == false) {      
      for (AID children : agent.getChildrenAIDSet()) {
        agent.sendObjectMessageWithTime(children, agent.getDoubleValuesToSendInVALUEPhase(), MessageType.DPOP_VALUE, agent.getSimulatedTime());
      }
    } 
  }
  
  /**
   * This function has been REVIEWED
   * Choose value from the agent view table
   * The agent view table surely has the values from the parent
   * @param valuesFromParent
   * @return
   */
  private double chooseValue_TABLE(Map<String, Double> valuesFromParent) {
    return agent.getAgentViewTableDouble().getArgmaxGivenVariableAndValueMap(agent.getLocalName(), valuesFromParent);
  }

  private double chooseValue_HYBRID(Map<String, Double> valuesFromParent) {
    if (agent.isLeaf()) {
      return leafValue_HYBRID(valuesFromParent);
    }
    else {
      return nonLeafValue_HYBRID(valuesFromParent);
    }
  }
  
  /**
   * This is called by a non-leaf agent in the HYBRID DPOP
   * IN VALUE phase, the agent will find max and arg_max with finer granularity
   * @param valuesFromParent
   * @return
   */
  private double nonLeafValue_HYBRID(Map<String, Double> valuesFromParent) {
    return agent.getAgentViewTableDouble().maxArgmaxHybrid(valuesFromParent, agent.getSelfInterval().getMidPointInHalfIntegerRanges() )[1];
  }
  
  /**
   * Call the chooseValue_FUNCTION(valuesFromParent)
   * @param valuesFromParent
   * @return
   */
  private double leafValue_HYBRID(Map<String, Double> valuesFromParent) {
    return chooseValue_FUNCTION(valuesFromParent);
  }
  
  /**
   * This function has been REVIEWED
   * @param valuesFromParent
   * @return
   */
  private double chooseValue_FUNCTION(Map<String, Double> valuesFromParent) {
    Map<String, Double> valueMapOfOtherVariables = new HashMap<>();
    
    PiecewiseMultivariateQuadFunction agentViewFunction = agent.getAgentViewFunction();
    
    for (String agent : agent.getParentAndPseudoStrList()) {
      double value = valuesFromParent.get(agent);
      valueMapOfOtherVariables.put(agent, value);
    }
    
    agentViewFunction = agentViewFunction.evaluateToUnaryFunction(valueMapOfOtherVariables);
    
    double currentChosenValue = -Double.MAX_VALUE;
    double currentMax = -Double.MAX_VALUE;
    
    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> entry : agentViewFunction.getFunctionMap().entrySet()) {
      MultivariateQuadFunction function = entry.getKey();
      Set<Map<String, Interval>> intervalSet = entry.getValue();
      
      for (Map<String, Interval> interval : intervalSet) {
        double[] maxArgmax = function.getMaxAndArgMax(interval);
              
        if (Double.compare(maxArgmax[0], currentMax) > 0) {
          currentMax = maxArgmax[0];
          currentChosenValue = maxArgmax[1];
        }
      }
    }
    
    return currentChosenValue;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Double> waitingForValuesFromParentWithTime(MessageType msgType) {
    agent.startSimulatedTiming();    
    
    int msgCode = msgType.ordinal();
    
    ACLMessage receivedMessage = null;
    Map<String, Double> valuesFromParent = new HashMap<String, Double>();

    while (true) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      receivedMessage = myAgent.blockingReceive(template);

      long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
        if (timeFromReceiveMessage > agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime()) {
          agent.setSimulatedTime(timeFromReceiveMessage);
        } else {
          agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
        }
        
        break;
    }
        
    try {
      valuesFromParent = (Map<String, Double>) receivedMessage.getContentObject();
    } catch (UnreadableException e) {
      e.printStackTrace();
    }
    
    return valuesFromParent;
  }

  @SuppressWarnings("unchecked")
  private void action_discrete() {
    // VALUE might be called multiple times
    agent.getValuesToSendInVALUEPhase().clear();

    if (agent.isRoot()) {
      if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.C_DCOP)) {
        agent.addValuesToSendInValuePhase(agent.getAgentID(), agent.getCDPOP_value());
      } else {
        agent.addValuesToSendInValuePhase(agent.getAgentID(), agent.getChosenValueAtEachTimeStep(currentTimeStep));
      }

      for (AID childrenAgentAID : agent.getChildrenAIDSet()) {
        agent.sendObjectMessageWithTime(childrenAgentAID, agent.getValuesToSendInVALUEPhase(), MessageType.DPOP_VALUE,
            agent.getSimulatedTime());
      }
    } else {
      // leaf or internal nodes
      ACLMessage receivedMessage = waitingForMessageFromParent(MessageType.DPOP_VALUE);
      agent.startSimulatedTiming();

      HashMap<Integer, String> variableAgentViewIndexValueMap = new HashMap<Integer, String>();
      HashMap<String, String> valuesFromParent = new HashMap<String, String>();
      try {
        valuesFromParent = (HashMap<String, String>) receivedMessage.getContentObject();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }

      for (Entry<String, String> valuesEntry : valuesFromParent.entrySet()) {
        String agentKey = valuesEntry.getKey();
        String agentValue = valuesEntry.getValue();

        int positionInAgentView = agent.getAgentViewTable().getDecVarLabel().indexOf(agentKey);

        // not in agentView
        if (positionInAgentView == -1) {
          continue;
        }
        // if exist this agent in agent view, add to values to send
        agent.addValuesToSendInValuePhase(agentKey, agentValue);

        variableAgentViewIndexValueMap.put(positionInAgentView, agentValue);
      }

      int selfAgentIndex = agent.getAgentViewTable().getDecVarLabel().indexOf(agent.getAgentID());

      RowString chosenRow = new RowString();
      double maxUtility = -Double.MAX_VALUE;

      for (RowString agentViewRow : agent.getAgentViewTable().getRowList()) {
        boolean isMatch = true;

        // check for each of index, get values and compared to the agentViewRow's values
        // if one of the values is not match, set flag to false and skip to the next row
        for (Entry<Integer, String> valuePositionEntry : variableAgentViewIndexValueMap.entrySet()) {
          int position = valuePositionEntry.getKey();
          String value = valuePositionEntry.getValue();

          // this row does not match the values
          if (!agentViewRow.getValueAtPosition(position).equals(value)) {
            isMatch = false;
            break;
          }
        }

        // Only compare if this row matches the value
        if (isMatch && Double.compare(agentViewRow.getUtility(), maxUtility) > 0) {
          maxUtility = agentViewRow.getUtility();
          chosenRow = agentViewRow;
        }
      }

      String chosenValue = chosenRow.getValueAtPosition(selfAgentIndex);

      agent.storeDpopSolution(chosenValue, currentTimeStep);
      // Set random solution for REACT algorithm
      if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.REACT) && currentTimeStep == 0) {
        int randomIndex = agent.getRandomGenerator().nextInt(agent.getSelfDomain().size());
        agent.getChosenValueAtEachTSMap().put(-1, agent.getSelfDomain().get(randomIndex));
      }

      agent.addValuesToSendInValuePhase(agent.getAgentID(), chosenValue);

      agent.stopSimulatedTiming();

      if (!agent.isLeaf()) {
        for (AID children : agent.getChildrenAIDSet()) {
          agent.sendObjectMessageWithTime(children, agent.getValuesToSendInVALUEPhase(), MessageType.DPOP_VALUE,
              agent.getSimulatedTime());
        }
      }
    }

    agent.print("Chosen value across time steps: " + agent.getChosenValueAtEachTSMap().values());
  }

  private ACLMessage waitingForMessageFromParent(MessageType msgType) {
    int msgCode = msgType.ordinal();
    ACLMessage receivedMessage = null;

		while (true) {
			agent.startSimulatedTiming();

			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			receivedMessage = myAgent.blockingReceive(template);

			agent.stopSimulatedTiming();
			long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
			if (timeFromReceiveMessage > agent.getSimulatedTime()) {
				agent.setSimulatedTime(timeFromReceiveMessage);
			}

			break;
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
