package behavior;

import jade.core.behaviours.OneShotBehaviour;
import table.Row;
import table.Table;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.DcopConstants.DynamicType;
import agent.DcopConstants.PDDcopAlgorithm;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class MGM_RAND_PICK_VALUE extends OneShotBehaviour {

  private static final long serialVersionUID = -6711542619242113965L;

  private AgentPDDCOP agent;
  
  private int pd_dcop_time_step;
  
  public MGM_RAND_PICK_VALUE(AgentPDDCOP agent, int pd_dcop_time_step) {
    super(agent);
    this.agent = agent;
    this.pd_dcop_time_step = pd_dcop_time_step;
  }
  
  @Override
  public void action() {
    agent.startSimulatedTiming();
    
    List<String> domain = agent.getSelfDomain();
    
    agent.getChosenValueAtEachTSMap().put(pd_dcop_time_step, domain.get(agent.getRandom().nextInt(domain.size())));
    
    agent.print("choose random value at time step " + pd_dcop_time_step + ": " + agent.getChosenValueAtEachTSMap().get(pd_dcop_time_step));
    
    // Compute expected table for the DCOP at this time step
    agent.getMgmTableList().clear();
    agent.getMgmTableList().addAll(computeDiscountedMGMAndSwitchingCostTables(agent.getDynamicType(), agent.getPDDCOP_Algorithm(), pd_dcop_time_step));
    
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
  private List<Table> computeDiscountedMGMAndSwitchingCostTables(DynamicType dynamicType, PDDcopAlgorithm algorithm, int timeStep) {
    List<Table> mgmTableList = new ArrayList<>();
    
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
            Table switchingCostToPreviousSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(timeStep - 1)); 
            mgmTableList.add(switchingCostToPreviousSolution);
          }
          // Also add switching cost table to the stationary solution found at horizon
          if (timeStep == agent.getHorizon() - 1) {
            Table switchingCostToLastSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(agent.getHorizon())); 
            mgmTableList.add(switchingCostToLastSolution);
          }
        }
        else if (algorithm == PDDcopAlgorithm.BACKWARD) {
          mgmTableList.addAll(agent.computeDiscountedDecisionTableList(agent.getRawDecisionTableList(), timeStep, 1D));
          mgmTableList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getRawRandomTableList(), timeStep, 1D));
          // If not at the horizon, add switching cost regarding the solution at timeStep + 1
          if (timeStep < agent.getHorizon()) {
            Table switchingCostToLaterSolution = switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(timeStep + 1)); 
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
      agent.getActualDpopTableAcrossTimeStep().computeIfAbsent(pd_dcop_time_step, k -> new ArrayList<>())
          .addAll(agent.computeActualDpopTableGivenRandomValues(pd_dcop_time_step));
      agent.getActualDpopTableAcrossTimeStep().get(pd_dcop_time_step).addAll(agent.getDpopDecisionTableList());
     
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
        mgmTableList.addAll(computeActualTableGivenRandomValues(pd_dcop_time_step));
        
        if (pd_dcop_time_step > 0) {
          mgmTableList.add(switchingCostGivenSolution(agent.getAgentID(), agent.getDecisionVariableDomainMap().get(agent.getAgentID()), agent.getChosenValueAtEachTimeStep(pd_dcop_time_step - 1)));
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
  private List<Table> computeActualTableGivenRandomValues(int timeStep) {
    List<Table> tableList = new ArrayList<>();
    
    // traverse each random table
    for (Table randTable : agent.getRawRandomTableList()) {
      List<String> decLabel = randTable.getDecVarLabel();
      // at current time step, create a new table 
      // add the tuple with corresponding random values

      Table newTable = new Table(decLabel, AgentPDDCOP.DECISION_TABLE);
      
      String simulatedRandomValues = agent.getPickedRandomAt(timeStep);

      for (Row row : randTable.getRowList()) {
        if (row.getRandomList().get(0).equals(simulatedRandomValues)) {
          newTable.addRow(new Row(row.getValueList(), row.getUtility()));
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
  
}