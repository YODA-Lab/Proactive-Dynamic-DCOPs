package behavior;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;

import java.util.ArrayList;

import agent.AgentPDDCOP;
import agent.AgentPDDCOP.DcopAlgorithm;
import agent.AgentPDDCOP.DynamicType;
import table.Row;
import table.Table;

/*
 * This is UTIL phrase of DTREE
 * 1. If X is leaf THEN
 *      WE ASSUME EACH VALUE OF PARENT HAS AT LEAST ONE CORRESPONDING VALUES FROM CHILDREN 
 * 		FOR EACH value from domain(parent)
 * 			Calculate the minimum utility constraint (for each corresponding value of children)
 * 			, then store the minimum pair (parent, children)
 * 		Then combine all the parent_value, utility
 * 		Send this vector to the parent
 * 		STOP;
 * 
 * 2. ELSE (not a leaf)
 * 		Wait until receiving all messages from all the children
 * 		2.1 If X is a root THEN
 * 			FOR EACH value of X
 * 				sum the utility that received from all the children
 * 			pick the value with the minimum utility from all the children.
 * 			STOP;
 * 
 * 		2.2 X is not a root
 * 			FOR EACH value of X
 * 				sum the utility that received from all the children
 * 			So here, we have each pair of value of X, and corresponding utility for this subtree
 * 			FOR EACH value of parent X
 * 				Calculate the minimum utility BASED ON the SUM of (corresponding constraints, and
 * 															utility from this value of X constraints)
 * 				Store this pair of (parent_value, children_value, utility)
 * 			Combine all the value of (parent_value, utility) and send to the parent
 * 			STOP;  
 */
public class DPOP_UTIL extends OneShotBehaviour implements MESSAGE_TYPE {

	private static final long serialVersionUID = -2438558665331658059L;

	AgentPDDCOP agent;
	
	private int currentTimeStep;
	
	private List<Table> dpopTableList = new ArrayList<>();
		
	public DPOP_UTIL(AgentPDDCOP agent, int currentTimeStep) {
		super(agent);
		this.agent = agent;
		this.currentTimeStep = currentTimeStep;
	}
	
	@Override
	public void action() {	  
	  // Combine all local constraints and store in non-discounted forms
	  if (agent.isRunningAlgorithm(DcopAlgorithm.LS_SDPOP) && isFirstTimeUTIL()) {
      Table joinedDecisionTable = null;
      for (Table decisionLocalConstraints : agent.getDpopDecisionTableList()) {
        joinedDecisionTable = joinTable(joinedDecisionTable, decisionLocalConstraints);
      }
      agent.setStoredReuseTable(joinedDecisionTable);
	  } 
	  else if (agent.isRunningAlgorithm(DcopAlgorithm.LS_SDPOP) && !isFirstTimeUTIL()) {
	    // Do not need to join local decision constraints again
	    // Do nothing
	  }
	  else if (agent.isRunningAlgorithm(DcopAlgorithm.REACT)) {
	    dpopTableList = agent.getActualDpopTableAcrossTimeStep().get(currentTimeStep);
	  }
	  else {
	    // Compute decision, random and switching cost tables
	    // Add all of them to the dpopTableList
	    dpopTableList.addAll(computeDpopAndSwitchingCostTables(agent.getDynamicType(), agent.getAlgorithm(), currentTimeStep));
	  }
		
		if (agent.isLeaf()) {
			leafDoUtilProcess();
		} 
		else if (agent.isRoot()){
			try {
        rootDoUtilProcess();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
		}
		else {
			try {
        internalNodeDoUtilProcess();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
		}
	}		
	
  private void leafDoUtilProcess() {
		agent.startSimulatedTiming();
    
		Table combinedTable = null;
		// Leafs behave the same even when in all DPOP runs
    if (agent.isRunningAlgorithm(DcopAlgorithm.LS_SDPOP)) {
      if (!agent.isRecomputingDPOP_UTIL()) {
        return ;
      }
      
      combinedTable = agent.computeDiscountedDecisionTable(agent.getStoredReuseTable(), currentTimeStep, agent.getDiscountFactor());
      for (Table localRandomConstraints : agent.getDpopRandomTableList()) {
        Table expectedTable = agent.computeDiscountedExpectedTable(localRandomConstraints, currentTimeStep, agent.getDiscountFactor());
        combinedTable = joinTable(combinedTable, expectedTable);
      }
    
      // If not a rand table, no need to compute in later DPOP rounds
      if (!combinedTable.isRandTable()) {
        agent.setRecomputingDPOP_UTILToFalse();
      }
    }
    else {
      //joining all tables together
      for (Table pseudoParentTable : dpopTableList) {
        agent.print("dpopTableList = " + pseudoParentTable);
        combinedTable = joinTable(combinedTable, pseudoParentTable);
      }
    }
		agent.setAgentViewTable(combinedTable);
		
		Table projectedTable = projectOperator(combinedTable, agent.getLocalName());

		agent.stopStimulatedTiming();
		
		agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DPOP_UTIL, agent.getSimulatedTime());
		
		agent.print("leaf is done");
	}
	
	private void internalNodeDoUtilProcess() throws UnreadableException {	
    Table combinedTable = computeCombinedTableAtNonLeave();
    
    // It means agents are not recomputing tables
    if (combinedTable == null) {return ;}

		agent.setAgentViewTable(combinedTable);

		Table projectedTable = projectOperator(combinedTable, agent.getLocalName());
		
		agent.stopStimulatedTiming();

		agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, DPOP_UTIL, agent.getSimulatedTime());
		
    agent.print("Internal node is done");
	}
	
	private Table computeCombinedTableAtNonLeave() throws UnreadableException {
	  Table combinedTable = null;
	  
    if (agent.isRunningAlgorithm(DcopAlgorithm.LS_SDPOP) && isFirstTimeUTIL()) {
      combinedTable = agent.getStoredReuseTable();
      
      List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
      // Separate children into children with decision and children with random 
      List<Table> decisionUtil = new ArrayList<>();
      List<Table> randomUtil = new ArrayList<>();
      for (ACLMessage msg : receivedUTILmsgList) {
        Table utilTable = (Table) msg.getContentObject();
        
        if (utilTable.isRandTable()) {
          randomUtil.add(utilTable);
        } 
        else {
          decisionUtil.add(utilTable);
          agent.getReuseChildUTIL().add(msg.getSender().getLocalName());
        }
      }
      
      for (Table decisionTable : decisionUtil) {
        combinedTable = joinTable(combinedTable, decisionTable);
      }
      agent.setStoredReuseTable(combinedTable);
      
      for (Table randomTable : randomUtil) {
        combinedTable = joinTable(combinedTable, randomTable);
      }
    }
    else if (agent.isRunningAlgorithm(DcopAlgorithm.LS_SDPOP) && !isFirstTimeUTIL()) {    
      if (!agent.isRecomputingDPOP_UTIL()) {return null;}
      
      List<ACLMessage> randomUtilMessages = waitingForMessageFromChildrenWithTime(DPOP_UTIL, agent.getChildrenAIDSet().size() - agent.getReuseChildUTIL().size());

      agent.startSimulatedTiming();
      
      combinedTable = agent.computeDiscountedDecisionTable(agent.getStoredReuseTable(), currentTimeStep, agent.getDiscountFactor());
      // Join with discounted random local constraints
      for (Table localRandomConstraints : agent.getDpopRandomTableList()) {
        Table expectedTable = agent.computeDiscountedExpectedTable(localRandomConstraints, currentTimeStep, agent.getDiscountFactor());
        combinedTable = joinTable(combinedTable, expectedTable);
      }
      // Join with UTIL which contains only random UTIL
      combinedTable = joinTable(combinedTable, combineMessage(randomUtilMessages));   }
    // Not LS_SDPOP
    else {
      List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(DPOP_UTIL);
      
      agent.startSimulatedTiming();

      combinedTable = combineMessage(receivedUTILmsgList);
      
      for (Table pseudoParentTable : dpopTableList) {
        combinedTable = joinTable(combinedTable, pseudoParentTable);
      }
    }
    
    return combinedTable;
	}
	
	private void rootDoUtilProcess() throws UnreadableException {	  
	  Table combinedTable = computeCombinedTableAtNonLeave();
		
		//pick value with smallest utility
		//since agent 0 is always at the beginning of the row formatted: agent0,agent1,..,agentN -> utility
		double maxUtility = -Double.MAX_VALUE;
		
		agent.print("Timestep " +  currentTimeStep + " combined messages at root:");
		
		agent.print("" + combinedTable);
		
		String chosenValue = "";
		
		for (Row row : combinedTable.getRowList()) {
			if (Double.compare(row.getUtility(), maxUtility) > 0) {
				maxUtility = row.getUtility();
				chosenValue = row.getValueAtPosition(0);
			}
		}
			
		System.out.println("Root agent " + agent.getAgentID() + "has chosen value " + chosenValue);
    System.out.println("Root agent " + agent.getAgentID() + "has chosen utility " + maxUtility);
    
    agent.storeDpopSolution(chosenValue, currentTimeStep);
    
    // Compute the difference between two time steps
    agent.setDpopSolvingTime(currentTimeStep, agent.getSimulatedTime());
    
    if (agent.isAlgorithmIn(new DcopAlgorithm[]{DcopAlgorithm.C_DPOP, DcopAlgorithm.FORWARD, DcopAlgorithm.BACKWARD})) { 
      agent.updateSolutionQuality(maxUtility);
    }  		
    		
    agent.stopStimulatedTiming();
	}
	
	/**
	 * This function is called only when the algorithm is not LS_SDPOP due to reusing tables
	 * @param dynamicType
	 * @param algorithm
	 * @param timeStep
	 * @return
	 */
	private List<Table> computeDpopAndSwitchingCostTables(DynamicType dynamicType, DcopAlgorithm algorithm, int timeStep) {
	  List<Table> tableList = new ArrayList<>();
    if (dynamicType == DynamicType.INFINITE_HORIZON) {
      // When currentTimeStep == agent.getHorizon(), solve for the last time step
      if (currentTimeStep == agent.getHorizon()) {
        dpopTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(), agent.getHorizon(), 1D));
        dpopTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(), agent.getHorizon(), 1D));      
      }
      else {
        // Compute the switching cost constraint
        // Collapse DCOPs from t = 0 to t = horizon -  1
        if (algorithm == DcopAlgorithm.C_DPOP) {
          dpopTableList.addAll(agent.computeCollapsedDecisionTableList(agent.getDpopDecisionTableList(), agent.getHorizon() - 1, 1D));
          dpopTableList.addAll(agent.computeCollapsedDecisionTableList(agent.getDpopRandomTableList(), agent.getHorizon() - 1, 1D));
          dpopTableList.add(agent.computeCollapsedSwitchingCostTable(agent.getSelfDomain(), agent.getHorizon() - 1));
        }
        else if (algorithm == DcopAlgorithm.FORWARD) {
          dpopTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(), currentTimeStep, 1D));
          dpopTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(), currentTimeStep, 1D));
          if (currentTimeStep > 0) {
            Table switchingCostToPreviousSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(currentTimeStep - 1)); 
            dpopTableList.add(switchingCostToPreviousSolution);
          }
          // Add switching cost table to the stationary solution found at horizon
          if (currentTimeStep == agent.getHorizon() - 1) {
            Table switchingCostToLastSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(agent.getHorizon())); 
            dpopTableList.add(switchingCostToLastSolution);
          }
        }
        else if (algorithm == DcopAlgorithm.BACKWARD) {
          dpopTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(), currentTimeStep, 1D));
          dpopTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(), currentTimeStep, 1D));
          // If not at the horizon, add switching cost regarding the solution at timeStep + 1
          if (currentTimeStep < agent.getHorizon()) {
            Table switchingCostToLaterSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(currentTimeStep + 1)); 
            dpopTableList.add(switchingCostToLaterSolution);
          }
        }
      }
    }
    else if (dynamicType == DynamicType.FINITE_HORIZON) {
      double df = agent.getDiscountFactor();
      
      if (algorithm == DcopAlgorithm.C_DPOP) {
          dpopTableList.addAll(agent.computeCollapsedDecisionTableList(agent.getDpopDecisionTableList(), agent.getHorizon(), df));
          dpopTableList.addAll(agent.computeCollapsedRandomTableList(agent.getDpopRandomTableList(), agent.getHorizon(), df));
          dpopTableList.add(agent.computeCollapsedSwitchingCostTable(agent.getSelfDomain(), agent.getHorizon()));
      }
      else {
        dpopTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(), currentTimeStep, df));
        dpopTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(), currentTimeStep, df));
        if (algorithm == DcopAlgorithm.FORWARD) {
          if (currentTimeStep > 0) {
            dpopTableList.add(switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(currentTimeStep - 1))); 
          }
        }
        else if (algorithm == DcopAlgorithm.BACKWARD) {
          if (currentTimeStep < agent.getHorizon()) {
            dpopTableList.add(switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(currentTimeStep + 1))); 
          }
        }
      }
    }
    //TODO: Hybrid can be used as both INFINITE and FINITE
    else if (algorithm == DcopAlgorithm.HYBRID) {
      double df = agent.getDiscountFactor();
      dpopTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(), currentTimeStep, df));
      dpopTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(), currentTimeStep, df));

      if (currentTimeStep > 0) {
        Table switchingCostToPreviousSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(currentTimeStep - 1)); 
        dpopTableList.add(switchingCostToPreviousSolution);
      }
    }
    else if (algorithm == DcopAlgorithm.REACT) {
      if (currentTimeStep > 0) {
        Table switchingCostToPreviousSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(currentTimeStep - 1)); 
        dpopTableList.add(switchingCostToPreviousSolution);
      }
    }
    
    return tableList;
	}
	
	private List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode) {
	  return waitingForMessageFromChildrenWithTime(msgCode, agent.getChildrenAIDSet().size());
	}
	
	 private List<ACLMessage> waitingForMessageFromChildrenWithTime(int msgCode, int childCount) {
	    List<ACLMessage> messageList = new ArrayList<ACLMessage>();
	    
	    while (messageList.size() < childCount) {
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
	      else
	        block();
	    }
	    
	    agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
	    return messageList;
	  }

	private Table joinTable(Table table1, Table table2) {
	  if (table1 == null) {return table2;}
	  if (table2 == null) {return table1;}
	  
	  //get commonVariables
		List<String> commonVariables = getCommonVariables(table1.getDecVarLabel(), table2.getDecVarLabel());
		
		//create indexList1, indexList2
		//xet tung variable commonVariables
		//add index of that variable to the indexList
		List<Integer> indexContainedInCommonList1 = new ArrayList<Integer>();
		List<Integer> indexContainedInCommonList2 = new ArrayList<Integer>();
		for (String variable:commonVariables) {
			indexContainedInCommonList1.add(table1.getDecVarLabel().indexOf(variable));
			indexContainedInCommonList2.add(table2.getDecVarLabel().indexOf(variable));
		}
		//create returnTable
		//join label
    List<String> joinedLabelTable1FirstThenTable2 = getJoinLabel(table1.getDecVarLabel(), table2.getDecVarLabel(),
        indexContainedInCommonList2);
		
		Table joinedTable = new Table(joinedLabelTable1FirstThenTable2, table1.isRandTable() || table2.isRandTable());
		for (Row row1 : table1.getRowList()) {
			for (Row row2 : table2.getRowList()) {
				Row joinedRow = getJoinRow(row1, row2, indexContainedInCommonList1, indexContainedInCommonList2);
				if (joinedRow != null) {
					joinedTable.addRow(joinedRow);
				}
			}
		}
		
		return joinedTable;
	}
	
	private List<String> getCommonVariables(List<String> variableList1, List<String> variableList2) {
		List<String> commonVariableList = new ArrayList<String>();
		
		for (String variable1:variableList1)
			for (String variable2:variableList2) {
				//check if equal agents, and if list contains agent or not
				//if (variable1.equals(variable2) && isContainVariable(commonVariableList, variable1) == false)
				if (variable1.equals(variable2) && !commonVariableList.contains(variable1)) {	
					commonVariableList.add(variable1);
					break;
				}
			}
		
		return commonVariableList;
	}
	
	//for variable1 from label1, add to joinedLabel
	//for variable2 from label2
	//	if index not in indexContainedInCommonList2
	//	then add to joinedLabel
	public List<String> getJoinLabel(List<String> label1, List<String> label2,
											List<Integer> indexContainedInCommonList2) {
												
	  List<String> joinedLabel = new ArrayList<String>();// (label1);
		for (String variable1:label1) {
			joinedLabel.add(variable1);
		}
		
		//add variable with index not in indexContainedInCommonList2
		for (int i=0; i<label2.size(); i++) {
			if (!indexContainedInCommonList2.contains(i))
				joinedLabel.add(label2.get(i));
		}
	
		return joinedLabel;
	}
	
	public Row getJoinRow(Row row1, Row row2, List<Integer> indexList1, 
			  List<Integer> indexList2) {
		//check if same size
		if (indexList1.size() != indexList2.size()) {
			System.err.println("Different size from indexList: " + indexList1.size() +
										 " " + indexList2.size());
			return null;
		}
		
		int listSize = indexList1.size();
		//check if same values
		for (int i=0; i<listSize; i++) {
			if (row1.getValueList().get(indexList1.get(i)).equals
			(row2.getValueList().get(indexList2.get(i))) == false) {
				return null;
			}
		}
		
		//join two row
		List<String> joinedValues = new ArrayList<String>(row1.getValueList());//(row1.getValueList());
		
		for (int i=0; i<row2.getValueList().size(); i++) {
			if (!indexList2.contains(i)) {
				joinedValues.add(row2.getValueList().get(i));
			}
		}
		
		Row joinedRow = new Row(joinedValues, row1.getUtility() + row2.getUtility());
		return joinedRow;				
	}
	
	//create new TabelDPOP
	//create new Label: eliminate variableToProject
	//create new Table with -1 dimension
	//create checkedList mark already picked tuples
	//for each tuple1 from the table
	//	if index(tuple1) already in picked tuple => continue
	//	for each tuple2:tuple1->end from the table
	//		compare to the minimum , and update
	//	add to new Table
	public Table projectOperator(Table table, String variableToProject) {
		int indexEliminated = getIndexOfContainedVariable(table.getDecVarLabel(), variableToProject);
		
		if (indexEliminated == -1) {
			return null;
		}
		
		//create arrayIndex
		List<Integer> arrayIndex = new ArrayList<Integer>();
		for (int i=0; i<table.getDecVarLabel().size(); i++) {
			if (i != indexEliminated)
				arrayIndex.add(i);
		}
		
		//create checkedList
		List<Integer> checkedList = new ArrayList<Integer>();
		
		//create projectedLabel
		List<String> projectedLabel = createTupleFromList(table.getDecVarLabel(), arrayIndex);
		
		//create projectedTable
		Table projectTable = new Table(projectedLabel, table.isRandTable());
		for (int i=0; i<table.getRowCount(); i++) {
			if (checkedList.contains(i) == true)
				continue;
			checkedList.add(i);
			Row row1 = table.getRowList().get(i);
			List<String> tuple1 = createTupleFromRow(row1, arrayIndex);
			double maxUtility = row1.getUtility();
			List<String> maxTuple = tuple1;
			
			for (int j=i+1; j<table.getRowCount(); j++) {
				Row row2 = table.getRowList().get(j);
				List<String> tuple2 = createTupleFromRow(row2, arrayIndex);
				double row2Utility = row2.getUtility();
				if (isSameTuple(tuple1, tuple2) == true) {
					checkedList.add(j);
					if (row2Utility > maxUtility) {
						maxUtility = row2Utility;
						maxTuple = tuple2;
					}
				}
				
			}
			
			projectTable.addRow(new Row(maxTuple, maxUtility));
		}
		
		return projectTable;
	}
	
	int getIndexOfContainedVariable(List<String> list, String input) {
		return list.indexOf(input);
	}
	
	//create tuples from Row and arrayIndex
	public List<String> createTupleFromList(List<String> list, List<Integer> arrayIndex) {
		if (arrayIndex.size() >= list.size()) {
//			System.err.println("Cannot create tuple with size: " + arrayIndex + " from Row size: " +
//									list.size());
			return null;
		}
		List<String> newTuple = new ArrayList<String>();
		for (Integer index:arrayIndex) {
			newTuple.add(list.get(index));
		}
		return newTuple;
	}
	
	//create tuples from Row and arrayIndex
	public List<String> createTupleFromRow(Row row, List<Integer> arrayIndex) {
		if (arrayIndex.size() >= row.getVariableCount()) {
//			System.err.println("Cannot create tuple with size: " + arrayIndex + " from Row size: " +
//									row.variableCount);
			return null;
		}
		List<String> newTuple = new ArrayList<String>();
		for (Integer index:arrayIndex) {
			newTuple.add(row.getValueAtPosition(index));
		}
		return newTuple;
	}
	
	//check if two tuples has the same values
	public boolean isSameTuple(List<String> tuple1, List<String> tuple2) {
		if (tuple1.size() != tuple2.size()) {
			System.err.println("Different size from two tuples: " + tuple1.size() + " and "
																	+ tuple2.size());
			return false;
		}
		int size = tuple1.size();
		for (int i=0; i<size; i++) {
			if (tuple1.get(i).equals(tuple2.get(i)) == false) {
				return false;
			}
		}
		return true;
	}
	
	//for each value of X
	//for each message received from the children
	//sum the utility that received from the children
	Table combineMessage(List<ACLMessage> list) {
		List<Table> listTable = new ArrayList<Table>();
		for (ACLMessage msg:list) {
			try {
				listTable.add((Table) msg.getContentObject());
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
		
		int size = listTable.size();
		Table table = listTable.get(0);

		for (int i=1; i<size; i++) {
			table = joinTable(table, listTable.get(i));
		}

		return table;
	}
	
	public void writeTimeToFile() {
		if (!agent.isRunningAlgorithm(DcopAlgorithm.REACT)) {return;}
		
		String line = "";
//		String alg = DCOP_INFO.algTypes[agent.algorithm];
		String alg = String.valueOf(agent.getAlgorithm());
		if (currentTimeStep == 0)
			line = line + alg + "\t" + agent.getInputFileName() + "\n";
		
		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		long runTime = System.currentTimeMillis() - agent.getCurrentUTILstartTime();
		
		line = line + "ts=" + currentTimeStep + "\t" + df.format(runTime) + " ms" + "\n";

		String fileName = "id=" + agent.getInstanceID() + "/sw=" + (int) agent.getSwitchingCost() + "/react_runtime.txt";
		byte data[] = line.getBytes();
	    Path p = Paths.get(fileName);

	    try (OutputStream out = new BufferedOutputStream(
	      Files.newOutputStream(p, CREATE, APPEND))) {
	      out.write(data, 0, data.length);
	    } catch (IOException x) {
	      System.err.println(x);
	    }
	}
 
// private List<Table> computeLongtermExpectedTableList(List<Table> randomTableList, double discountFactor) {
//   List<Table> tableList = new ArrayList<>();
//   for (Table table : randomTableList) {
//     tableList.add(computeLongtermExpectedTable(table, discountFactor));
//   }
//   
//   return tableList;
// }

 
// private double computeSwitchingCost(List<String> valueTuple, double discountFactor) {
//   double scost = 0;
//   
//   for (int index = 0; index < valueTuple.size() - 1; index++) {
//     scost += Math.pow(discountFactor, index) * agent.switchingCostFunction(valueTuple.get(index), valueTuple.get(index + 1));
//   }
//   
//   return scost;
// }
 
 /**
  * Compute unary switching cost table of given agent agent given valueList and differentValue
  * @param valueList
  * @param differentValue
  * @return
  */
 private Table switchingCostGivenSolution(String agentIdentifier, List<String> valueList, String differentValue) {
   List<String> label = new ArrayList<>();
   label.add(agentIdentifier);
   Table unarySwitchingCostTable = new Table(label, AgentPDDCOP.DECISION_TABLE);
   
   for (String value : valueList) {
     List<String> tableValueList = new ArrayList<>();
     tableValueList.add(value);
     Row row = new Row(tableValueList, -agent.switchingCostFunction(value, differentValue));
     
     unarySwitchingCostTable.addRow(row);
   }
   
   return unarySwitchingCostTable;
 }
  
  private boolean isFirstTimeUTIL() {
    return (agent.isDynamic(DynamicType.INFINITE_HORIZON) && currentTimeStep == agent.getHorizon()) 
        || (agent.isDynamic(DynamicType.FINITE_HORIZON) && currentTimeStep == 0);
  }
}
