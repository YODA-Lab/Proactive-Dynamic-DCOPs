package behavior;

import jade.core.behaviours.OneShotBehaviour;
import table.RowString;
import table.TableString;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.DcopConstants.DynamicType;
import agent.DcopConstants.PDDcopAlgorithm;
import static agent.DcopConstants.DECISION_TABLE;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class MGM_RAND_PICK_VALUE extends OneShotBehaviour {

  private static final long serialVersionUID = -6711542619242113965L;

  private AgentPDDCOP agent;
  
  private int currentTimeStep;
  
  public MGM_RAND_PICK_VALUE(AgentPDDCOP agent, int currentTimeStep) {
    super(agent);
    this.agent = agent;
    this.currentTimeStep = currentTimeStep;
  }
  
  @Override
  public void action() {
    agent.startSimulatedTiming();
    
    List<String> domain = agent.getSelfDomain();
    
    agent.getChosenValueAtEachTSMap().put(currentTimeStep, domain.get(agent.getRandomGenerator().nextInt(domain.size())));
    
    agent.println("choose random value at time step " + currentTimeStep + ": " + agent.getChosenValueAtEachTSMap().get(currentTimeStep));
    
    // Compute expected table for the DCOP at this time step
    agent.getMgmTableList().clear();
    agent.getMgmTableList().addAll(computeDiscountedMGMAndSwitchingCostTables(agent.getDynamicType(), agent.getPDDCOP_Algorithm(), currentTimeStep));
    
    agent.stopSimulatedTiming();
  }
  
  /**
   * REVIEWED <br>
   * This function is called only when the algorithm is not LS_SDPOP due to reusing tables <br>
   * Also, not called with ONLINE algorithms
   * @param dynamicType
   * @param algorithm
   * @param timeStep
   * @return
   */
  private List<TableString> computeDiscountedMGMAndSwitchingCostTables(DynamicType dynamicType, PDDcopAlgorithm algorithm, int timeStep) {
    List<TableString> mgmTableList = new ArrayList<>();
    
    if (dynamicType == DynamicType.INFINITE_HORIZON) {
      // When currentTimeStep == agent.getHorizon(), solve for the last time step
      if (timeStep == agent.getHorizon()) {
        mgmTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getRawDecisionTableList(), agent.getHorizon(), 1D));
        mgmTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getRawRandomTableList(), agent.getHorizon(), 1D));      
      }
      else {
        // Compute the switching cost constraint
        // Collapse DCOPs from t = 0 to t = horizon - 1
        if (algorithm == PDDcopAlgorithm.C_DCOP) {
          mgmTableList.addAll(agent.computeCollapsedDecisionTableList(agent.getRawDecisionTableList(), agent.getHorizon() - 1, 1D));
          mgmTableList.addAll(agent.computeCollapsedRandomTableList(agent.getRawRandomTableList(), agent.getHorizon() - 1, 1D));
          mgmTableList.add(agent.computeCollapsedSwitchingCostTable(agent.getSelfDomain(), agent.getHorizon() - 1, 1D));
        }
        else if (algorithm == PDDcopAlgorithm.FORWARD) {
          mgmTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getRawDecisionTableList(), timeStep, 1D));
          mgmTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getRawRandomTableList(), timeStep, 1D));
          if (timeStep > 0) {
            TableString switchingCostToPreviousSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(timeStep - 1)); 
            mgmTableList.add(switchingCostToPreviousSolution);
          }
          // Also add switching cost table to the stationary solution found at horizon
          if (timeStep == agent.getHorizon() - 1) {
            TableString switchingCostToLastSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(agent.getHorizon())); 
            mgmTableList.add(switchingCostToLastSolution);
          }
        }
        else if (algorithm == PDDcopAlgorithm.BACKWARD) {
          mgmTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getRawDecisionTableList(), timeStep, 1D));
          mgmTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getRawRandomTableList(), timeStep, 1D));
          // If not at the horizon, add switching cost regarding the solution at timeStep + 1
          if (timeStep < agent.getHorizon()) {
            TableString switchingCostToLaterSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(timeStep + 1)); 
            mgmTableList.add(switchingCostToLaterSolution);
          }
        }
      }
    }
    else if (dynamicType == DynamicType.FINITE_HORIZON) {
      double df = agent.getDiscountFactor();
      
      if (algorithm == PDDcopAlgorithm.C_DCOP) {
        mgmTableList.addAll(agent.computeCollapsedDecisionTableList(agent.getRawDecisionTableList(), agent.getHorizon(), df));
        mgmTableList.addAll(agent.computeCollapsedRandomTableList(agent.getRawRandomTableList(), agent.getHorizon(), df));
        mgmTableList.add(agent.computeCollapsedSwitchingCostTable(agent.getSelfDomain(), agent.getHorizon(), df));
      }
      else {
        mgmTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getRawDecisionTableList(), timeStep, df));
        mgmTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getRawRandomTableList(), timeStep, df));
        if (algorithm == PDDcopAlgorithm.FORWARD) {
          if (timeStep > 0) {
            mgmTableList.add(switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(timeStep - 1))); 
          }
        }
        else if (algorithm == PDDcopAlgorithm.BACKWARD) {
          if (timeStep < agent.getHorizon()) {
            mgmTableList.add(switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(timeStep + 1))); 
          }
        }
      }
    }
    else if (dynamicType == DynamicType.ONLINE) {
      double df = agent.getDiscountFactor();
      
      // Add actual DPOP tables to compute actual quality
      agent.getActualDpopTableAcrossTimeStep().computeIfAbsent(currentTimeStep, k -> new ArrayList<>())
          .addAll(agent.computeActualDpopTableGivenRandomValues(currentTimeStep));
      agent.getActualDpopTableAcrossTimeStep().get(currentTimeStep).addAll(agent.getDpopDecisionTableList());
     
      // Compute discounted expected tables from raw table lists to run MGM
      if (algorithm == PDDcopAlgorithm.FORWARD || algorithm == PDDcopAlgorithm.HYBRID) {
        mgmTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getRawDecisionTableList(), timeStep, df));
        mgmTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getRawRandomTableList(), timeStep, df));
        if (timeStep > 0) {
          mgmTableList.add(switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(timeStep - 1))); 
        }
      }
      // Add all actual tables
      // Add switching cost tables
      else if (algorithm == PDDcopAlgorithm.REACT) {
        mgmTableList.addAll(agent.getRawDecisionTableList());        
        mgmTableList.addAll(computeActualTableGivenRandomValues(currentTimeStep));
        
        if (currentTimeStep > 0) {
          mgmTableList.add(switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(currentTimeStep - 1)));
        }
      }
    }
    
    return mgmTableList;
  }
  
  /**
   * REVIEWED <br>
   * From DPOP random table list, return new tables with the corresponding picked random variables
   * @param timeStep
   */
  private List<TableString> computeActualTableGivenRandomValues(int timeStep) {
    List<TableString> tableList = new ArrayList<>();
    
    // traverse each random table
    for (TableString randTable : agent.getRawRandomTableList()) {
      List<String> decLabel = randTable.getDecVarLabel();
      // at current time step, create a new table 
      // add the tuple with corresponding random values

      TableString newTable = new TableString(decLabel, DECISION_TABLE);
      
      String simulatedRandomValues = agent.getPickedRandomAt(timeStep);

      for (RowString row : randTable.getRowList()) {
        if (row.getRandomList().get(0).equals(simulatedRandomValues)) {
          newTable.addRow(new RowString(row.getValueList(), row.getUtility()));
        }
      }
      
      tableList.add(newTable);
    }
    
    return tableList;
  }
  
  /**
   * REVIEWED <br>
   * Compute unary switching cost table of given agent agent given valueList and differentValue
   * @param valueList
   * @param differentValue
   * @return
   */
  private TableString switchingCostGivenSolution(String agentIdentifier, List<String> valueList, String differentValue) {
    List<String> label = new ArrayList<>();
    label.add(agentIdentifier);
    TableString unarySwitchingCostTable = new TableString(label, DECISION_TABLE);
    
    for (String value : valueList) {
      List<String> tableValueList = new ArrayList<>();
      tableValueList.add(value);
      RowString row = new RowString(tableValueList, -agent.switchingCostFunction(value, differentValue));
      
      unarySwitchingCostTable.addRow(row);
    }
    
    return unarySwitchingCostTable;
  }
  
}