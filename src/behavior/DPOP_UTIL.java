package behavior;

import static agent.DcopConstants.DcopAlgorithm;
import static agent.DcopConstants.ADD_MORE_POINTS;
import static agent.DcopConstants.DONE_AT_INTERNAL_NODE;
import static agent.DcopConstants.DONE_AT_LEAF;
import static agent.DcopConstants.NOT_ADD_POINTS;
import static agent.DcopConstants.RANDOM_PREFIX;
import static agent.DcopConstants.GRADIENT_SCALING_FACTOR;
import static agent.DcopConstants.DECISION_TABLE;
import static java.lang.Double.compare;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import agent.AgentPDDCOP;
import agent.DcopConstants.PDDcopAlgorithm;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import agent.DcopConstants.DynamicType;
import agent.DcopConstants.MessageType;
import table.RowDouble;
import table.RowString;
import table.TableDouble;
import table.TableString;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 * @author khoihd REVIEWED <br>
 *         This is UTIL phrase of DTREE <br>
 *         1. If X is leaf THEN <br>
 *         WE ASSUME EACH VALUE OF PARENT HAS AT LEAST ONE CORRESPONDING VALUES
 *         FROM CHILDREN FOR EACH value from domain(parent) <br>
 *         Calculate the minimum utility constraint (for each corresponding
 *         value of children) , then store the minimum pair (parent, children)
 *         <br>
 *         Then combine all the parent_value, utility <br>
 *         Send this vector to the parent <br>
 *         STOP; <br>
 *         <br>
 *         2. ELSE (not a leaf) <br>
 *         Wait until receiving all messages from all the children <br>
 *         2.1 If X is a root THEN <br>
 *         FOR EACH value of X sum the utility that received from all the
 *         children pick the value with the minimum utility from all the
 *         children. <br>
 *         STOP; <br>
 *         <br>
 *         2.2 X is not a root <br>
 *         FOR EACH value of X, sum the utility that received from all the
 *         children <br>
 *         So here, we have each pair of value of X, and corresponding utility
 *         for this subtree <br>
 *         FOR EACH value of parent X <br>
 *         Calculate the minimum utility BASED ON the SUM of (corresponding
 *         constraints, and utility from this value of X constraints) <br>
 *         Store this pair of (parent_value, children_value, utility) <br>
 *         Combine all the value of (parent_value, utility) and send to the
 *         parent <br>
 *         STOP; <br>
 */
public class DPOP_UTIL extends OneShotBehaviour {

	private static final long serialVersionUID = -2438558665331658059L;

	AgentPDDCOP agent;

	private final int currentTimeStep;

	private List<TableString> dpopTableStringList = new ArrayList<>();
	
	private List<TableDouble> dpopTableDoubleList = new ArrayList<>();
	
	private Map<String, PiecewiseMultivariateQuadFunction> dpopFunctionMap = new HashMap<>();

	@SuppressWarnings("unused")
	private boolean isMaximize = true; // set to true by default

	public DPOP_UTIL(AgentPDDCOP agent, int currentTimeStep) {
		super(agent);
		this.agent = agent;
		this.currentTimeStep = currentTimeStep;
	}

	@Override
	public void action() {
	  if (agent.isDiscrete()) {
	    actionDiscrete();
	  }
	  else {
	    actionContinuous();
	  }
	}
	
	private void actionContinuous() {
	  // Note: regular and expected function are non-discounted (since solving each time step is done independently).
	  dpopFunctionMap.putAll(agent.getFunctionWithPParentMap());

	  // Compute expected function if any and add the expected function to the dpopFuncionList
	  if (agent.hasRandomFunction()) {
	    agent.computeExpectedFunctionCurrentTimeStep(currentTimeStep);
	    dpopFunctionMap.put(agent.getRandomVariable(), agent.getExpectedFunction(currentTimeStep));
	  }
	  
	  PiecewiseMultivariateQuadFunction swFunc = agent.computeSwitchingCostDiscountedFunction(currentTimeStep, agent.getPDDCOP_Algorithm(), agent.getSwitchingCost(), agent.SWITCHING_TYPE);
    if (swFunc != null) {
      dpopFunctionMap.put(agent.getLocalName(), swFunc);
    }
	  
	  // Convert function to table for DPOP
    if (agent.getDcop_algorithm() == DcopAlgorithm.DPOP) {
      // Doing this way will also add expected and switching cost table to the list
      List<TableDouble> tableListFromFunction = createDCOPTableFromFunction(dpopFunctionMap.values(), currentTimeStep);
      dpopTableDoubleList.addAll(tableListFromFunction);
    }
	  
    if (agent.getDcop_algorithm() == DcopAlgorithm.DPOP) {
      doUtil_TABLE();
    } else if (agent.getDcop_algorithm() == DcopAlgorithm.EC_DPOP || agent.getDcop_algorithm() == DcopAlgorithm.APPROX_DPOP) {
      doUtil_FUNC();
    } else if (agent.getDcop_algorithm() == DcopAlgorithm.AC_DPOP || agent.getDcop_algorithm() == DcopAlgorithm.CAC_DPOP) {
      doUtil_HYBRID();
    }
	}
	
  private void doUtil_TABLE() {
    if (agent.isLeaf())
      leaf_TABLE();
    else if (agent.isRoot())
      root_TABLE();
    else
      internalNode_TABLE();
  }
  
  private void doUtil_FUNC() {
    if (agent.isLeaf())
      leaf_FUNC();
    else if (agent.isRoot())
      root_FUNC();
    else
      internalNode_FUNC();
  }
  
  private void doUtil_HYBRID() {  
    if (agent.isLeaf())
      leaf_HYBRID();
    else if (agent.isRoot())
      root_HYBRID();
    else
      internalNode_HYBRID();
  }
  
  /**
   * THIS FUNCTION HAS BEEN REVIEWED
   */
  public void leaf_TABLE() {
    agent.startSimulatedTiming();
    
    agent.print("LEAF is running");
    // get the first table
    TableDouble combinedTable = dpopTableDoubleList.get(0);
    // combinedTable.printDecVar();
    // joining other tables with table 0
    int currentTableListDPOPsize = dpopTableDoubleList.size();
    for (int index = 1; index < currentTableListDPOPsize; index++) {
      TableDouble pseudoParentTable = dpopTableDoubleList.get(index);
      combinedTable = joinTableDouble(combinedTable, pseudoParentTable);
    }

    agent.setAgentViewTableDouble(combinedTable);
    TableDouble projectedTable = projectOperatorDouble(combinedTable, agent.getLocalName());

    agent.stopSimulatedTiming();

    agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, MessageType.DPOP_UTIL, agent.getSimulatedTime());
  }
  
  /**
   * THIS FUNCTION HAS BEEN REVIEWED
   */
  public void leaf_FUNC() { 
    agent.startSimulatedTiming();
  
    agent.print("LEAF is running");
    
    PiecewiseMultivariateQuadFunction combinedFunction = new PiecewiseMultivariateQuadFunction();

    for (PiecewiseMultivariateQuadFunction func : dpopFunctionMap.values()) {
      combinedFunction = combinedFunction.addPiecewiseFunction(func);
    }

    combinedFunction.setOwner(agent.getLocalName());
    combinedFunction.setOtherAgent();

    agent.setAgentViewFunction(combinedFunction);

    PiecewiseMultivariateQuadFunction projectedFunction = null;

    if (agent.getDcop_algorithm() == DcopAlgorithm.APPROX_DPOP) {
      projectedFunction = combinedFunction.approxProject(agent.getNumberOfPoints(), agent.getLocalName(),
          agent.getNumberOfApproxAgents(), agent.isApprox());
    } 
    else if (agent.getDcop_algorithm() == DcopAlgorithm.EC_DPOP) {
      agent.debug("combinedFunction=" + combinedFunction.toString());
      projectedFunction = combinedFunction.analyticalProject();
    }

    agent.stopSimulatedTiming();
    
    try {
      agent.sendByteObjectMessageWithTime(agent.getParentAID(), projectedFunction, MessageType.DPOP_UTIL, agent.getSimulatedTime());
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    agent.print("LEAF is done");
  }
  
  /*
   * THIS FUNCTION HAS BEEN REVIEWED
   * 1. Move the values of parent and pseudo-parents 
   *    The values are the numberOfPoints
   * 2. After moving:
   *  - For each value combination, find the max from sum of utility function
   *  - Create UTIL message from the valueCombination and max value
   *  - Agent_view is not needed.
   */
  public void leaf_HYBRID() {
    agent.startSimulatedTiming();

    agent.print("LEAF is running");
    
    // Sum up all functions to create agentViewFunction
    PiecewiseMultivariateQuadFunction sumFunction = new PiecewiseMultivariateQuadFunction();
    
    for (PiecewiseMultivariateQuadFunction function : dpopFunctionMap.values()) {
      sumFunction = sumFunction.addPiecewiseFunction(function);
    }
    
    agent.setAgentViewFunction(sumFunction);
    
    /*
     * Move the values of parent and pseudo-parents
     */
    Set<List<Double>> productPPValues = movingPointsUsingTheGradient(null, DONE_AT_LEAF);
    
   /*
    * After moving, find the max utility from all functions with parent and pseudo-parents
    * For each value list
    *  Evaluate the sumFunction with the given value list to a unary function
    *  Find the max of that Unary function
    *  Add to the UTIL messages
    * End
    */
    List<String> label = agent.getParentAndPseudoStrList();
    
    TableDouble utilTable = new TableDouble(label);
    
    for (List<Double> valueList : productPPValues) {
      Map<String, Double> valueMapOfOtherVariables = new HashMap<>();

      for (int parentIndex = 0; parentIndex < agent.getParentAndPseudoStrList().size(); parentIndex++) {
        String pAgent = label.get(parentIndex);
        double pValue = valueList.get(parentIndex);

        valueMapOfOtherVariables.put(pAgent, pValue);
      }
      
      // Expected function and switching cost function is already integrated into the sumFunction
      PiecewiseMultivariateQuadFunction unaryFunction = sumFunction.evaluateToUnaryFunction(valueMapOfOtherVariables);
      
      double max = -Double.MAX_VALUE;
      
      for (Map<String, Interval> interval : sumFunction.getTheFirstIntervalSet()) {
        double maxArgmax[] = unaryFunction.getTheFirstFunction().getMaxAndArgMax(interval);
        
        if (compare(maxArgmax[0], max) > 0) {
          max = maxArgmax[0];
        }
      }
      
      utilTable.addRow(new RowDouble(valueList, max));
    }
    
    agent.stopSimulatedTiming();
    
    agent.print(" send utilTable size " + utilTable.size() + " to agent " + agent.getParentAID().getLocalName());
    agent.sendObjectMessageWithTime(agent.getParentAID(), utilTable, MessageType.DPOP_UTIL, agent.getSimulatedTime());
  }
  
  /**
   * THIS FUNCTION HAS BEEN REVIEWED
   */
  private void internalNode_TABLE() {
    // Start of processing 
    agent.startSimulatedTiming();
    
    agent.print("INTERNAL node is running");
    
    agent.print("STARTS joining tables from pParent table list");

    TableDouble joinedTable = dpopTableDoubleList.get(0);
    for (int i = 1; i < dpopTableDoubleList.size(); i++) {
      joinedTable = joinTableDouble(joinedTable, dpopTableDoubleList.get(i));
    }
    
    agent.print("is DONE joining tables from pParent table list");
    
    agent.stopSimulatedTiming();
    
    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL);

    // Start of processing 
    agent.startSimulatedTiming();
    
    // After combined, it becomes a unary function
    agent.print("STARTS joining tables from UTIL message");

    TableDouble combinedUtilAndConstraintTable = combineMessageTableDouble(receivedUTILmsgList);
    
    agent.print("is DONE joining tables from UTIL message");

    combinedUtilAndConstraintTable = joinTableDouble(combinedUtilAndConstraintTable, joinedTable);
    
    agent.print("finishes joining tables");

    agent.setAgentViewTableDouble(combinedUtilAndConstraintTable);
    
    agent.print("is projecting table");

    TableDouble projectedTable = projectOperatorDouble(combinedUtilAndConstraintTable, agent.getLocalName());
    
    agent.print("finishes projecting table");

    agent.stopSimulatedTiming();
    
    agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, MessageType.DPOP_UTIL, agent.getSimulatedTime());
  }
  
  private void internalNode_FUNC() {
    agent.print("INTERNAL node is running");
    
    agent.startSimulatedTiming();
    
    agent.print("INTERNAL node STARTS adding functions from dpopFunctionMap");
    
    PiecewiseMultivariateQuadFunction sumFunction = new PiecewiseMultivariateQuadFunction();
    for (PiecewiseMultivariateQuadFunction pseudoParentFunction : dpopFunctionMap.values()) {
      sumFunction = sumFunction.addPiecewiseFunction(pseudoParentFunction);
    }
    
    agent.print("INTERNAL node is DONE adding functions from dpopFunctionMap with sumFunction=" + sumFunction);
    
    sumFunction.setOtherAgent();
    
    agent.stopSimulatedTiming();
    
    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL);
    
    agent.startSimulatedTiming();
        
    agent.print("INTERNAL node has received all UTIL messages");
    
    // UnaryPiecewiseFunction
    // PiecewiseMultivariateQuadFunction combinedFunctionMessage =
    // combineMessageToFunction(receivedUTILmsgList);
    PiecewiseMultivariateQuadFunction combinedFunctionMessage = null;
    try {
      combinedFunctionMessage = combineByteMessageToFunction(receivedUTILmsgList);
    } catch (ClassNotFoundException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    
    combinedFunctionMessage.setOtherAgent();

    agent.print("INTERNAL node functions counts before joining rewards size=" + combinedFunctionMessage.size() + ": " + combinedFunctionMessage);
    
    combinedFunctionMessage = combinedFunctionMessage.addPiecewiseFunction(sumFunction);
    
    combinedFunctionMessage.setOtherAgent();

    agent.print("INTERNAL node number of combined function size=" + combinedFunctionMessage.getFunctionMap().size() + ": " + combinedFunctionMessage);

    combinedFunctionMessage.setOwner(agent.getLocalName());
    combinedFunctionMessage.setOtherAgent();

    agent.setAgentViewFunction(combinedFunctionMessage);

    PiecewiseMultivariateQuadFunction projectedFunction = null;

    agent.print("INTERNAL node number of combined function: " + combinedFunctionMessage.getFunctionMap().size());

    agent.print("INTERNAL node STARTS projecting functions");
    
    if (agent.getDcop_algorithm() == DcopAlgorithm.APPROX_DPOP) {
      projectedFunction = combinedFunctionMessage.approxProject(agent.getNumberOfPoints(), agent.getLocalName(),
          agent.getNumberOfApproxAgents(), agent.isApprox());
    } else if (agent.getDcop_algorithm() == DcopAlgorithm.EC_DPOP) {
      projectedFunction = combinedFunctionMessage.analyticalProject();
    }
    
    agent.print("INTERNAL node is DONE projecting functions");

    agent.print("INTERNAL node number of projected function: " + projectedFunction.getFunctionMap().size());

    agent.stopSimulatedTiming();

    try {
      agent.sendByteObjectMessageWithTime(agent.getParentAID(), projectedFunction, MessageType.DPOP_UTIL, agent.getSimulatedTime());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * THIS FUNCTION HAS BEEN REVIEWED
   * 
   * Join the UTIL tables from children and add up the utility functions to the table
   * This is the agent_view_table
   * 
   * For value combinations of pParents:
   *  Moving their values from the table using the derivative of the corresponding utility function
   *  Given the current combination, all the possbible values of agent {}, create the set of interpolated row
   *  Find the argmax and move that values
   * End
   * 
   * After moving, we need to find the corresponding utility for each value combination.
   * Interpolate the set of row, find the max
   * Send this message up to the 
   * 
   * Interpolate new points using the joined tables
   * Add the corresponding utility functions to the joinedTable
   * Then send this UTIL message to the parent
   */
  private void internalNode_HYBRID() {    
    agent.startSimulatedTiming();
    
    agent.print("INTERNAL node is running");
    
    // Sum up all functions to create agentViewFunction
    PiecewiseMultivariateQuadFunction sumFunction = new PiecewiseMultivariateQuadFunction();
    
    for (PiecewiseMultivariateQuadFunction func : dpopFunctionMap.values()) {
      sumFunction.addPiecewiseFunction(func);
    }
    
    agent.setAgentViewFunction(sumFunction);

    agent.stopSimulatedTiming();

    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL);
    
    agent.startSimulatedTiming();
    
    List<TableDouble> tableList = createTableList(receivedUTILmsgList);
    
    agent.print(" receives the UTIL tables:");
    for (TableDouble table : tableList) {
      System.out.println(table);
    }
    
    // Interpolate points and join all the tables
    agent.print(" starts interpolating and joining table");
    TableDouble joinedTable = interpolateAndJoinTable(tableList, NOT_ADD_POINTS);
    agent.print(" finishes interpolating and joining table size " + joinedTable.size());
    agent.print(" joined the table:");
    agent.print(joinedTable.toString());
    
    agent.print(" starts adding functions to the table");

    joinedTable = addTheUtilityFunctionsToTheJoinedTable(joinedTable);
    agent.print(" finishes adding functions to the table size " + joinedTable.size());

    agent.setAgentViewTableDouble(joinedTable);
    
    agent.print(" has agentViewTable label: " + agent.getAgentViewTableDouble().getDecVarLabel());

    agent.print(" starts moving points with joinedTable size: " + joinedTable.size());

    Set<List<Double>> productPPValues = movingPointsUsingTheGradient(joinedTable, DONE_AT_INTERNAL_NODE);
    agent.print(" finishes moving points " + productPPValues.size());
        
    agent.print(" starts create UTIL tables from values set");

    TableDouble utilTable = createUtilTableFromValueSet(joinedTable, productPPValues);
    agent.print(" finishes create UTIL tables from values set");

    agent.print(" send utilTable size " + utilTable.size() + " to agent " + agent.getParentAID().getLocalName());
    
    agent.stopSimulatedTiming();
    
    agent.sendObjectMessageWithTime(agent.getParentAID(), utilTable, MessageType.DPOP_UTIL, agent.getSimulatedTime());
  }
  
  public void root_FUNC() {
    agent.startSimulatedTiming();
    
    agent.print("ROOT node is running");
    
    agent.print("ROOT node STARTS adding functions from dpopFunctionMap");
    
    PiecewiseMultivariateQuadFunction sumFunction = new PiecewiseMultivariateQuadFunction();
    
    for (PiecewiseMultivariateQuadFunction pseudoParentFunction : dpopFunctionMap.values()) {
      sumFunction = sumFunction.addPiecewiseFunction(pseudoParentFunction);
    }
    
    agent.print("ROOT node IS DONE adding functions from dpopFunctionMap");
    
    agent.stopSimulatedTiming();

    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL);
    
    // Start of processing time
    agent.startSimulatedTiming();
    
    agent.print("ROOT node STARTS adding functions with functions from message");
    
    PiecewiseMultivariateQuadFunction combinedFunctionMessage = null;
    try {
      combinedFunctionMessage = combineByteMessageToFunction(receivedUTILmsgList);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    combinedFunctionMessage = combinedFunctionMessage.addPiecewiseFunction(sumFunction);

    combinedFunctionMessage.setOwner(agent.getLocalName());
    
    agent.print("ROOT node STARTS adding functions with functions from message");
    
    agent.print("ROOT has final function before computing max and argmax: " + combinedFunctionMessage);

    // choose the maximum
    double argmax = -Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;

    for (Entry<MultivariateQuadFunction, Set<Map<String, Interval>>> functionEntry : combinedFunctionMessage.getFunctionMap()
        .entrySet()) {
      MultivariateQuadFunction function = functionEntry.getKey();
      for (Map<String, Interval> intervalMap : functionEntry.getValue()) {
        double[] maxAndArgMax = function.getMaxAndArgMax(intervalMap);

        if (compare(maxAndArgMax[0], max) > 0) {
          max = maxAndArgMax[0];
          argmax = maxAndArgMax[1];
        }
      }
    }
    
    agent.setChosenDoubleValueAtEachTimeStep(currentTimeStep, argmax);

    agent.print("MAX VALUE IS " + max);
    agent.print("ARGMAX VALUE IS " + argmax);
    
    agent.stopSimulatedTiming();
  }
  
  /**
   * THIS FUNCTION HAS BEEN REVIEWED
   */
  public void root_TABLE() {
    // Start of processing 
    agent.startSimulatedTiming();
    
    agent.print("INTERNAL node is running");
    
    agent.print("STARTS joining tables from pParent table list");

    TableDouble joinedTable = dpopTableDoubleList.get(0);
    for (int i = 1; i < dpopTableDoubleList.size(); i++) {
      joinedTable = joinTableDouble(joinedTable, dpopTableDoubleList.get(i));
    }
    
    agent.print("is DONE joining tables from pParent table list");
    
    agent.stopSimulatedTiming();
    
    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL);

    // Start of processing time
    agent.startSimulatedTiming();
    
    // After combined, it becomes a unary function
    agent.print("STARTS joining tables from UTIL message");

    TableDouble combinedUtilAndConstraintTable = combineMessageTableDouble(receivedUTILmsgList);
    
    agent.print("is DONE joining tables from UTIL message");

    combinedUtilAndConstraintTable = joinTableDouble(combinedUtilAndConstraintTable, joinedTable);
    
    agent.print("finishes joining tables");

    agent.print("Root is finding max and argmax");

    // pick value with smallest utility
    // since agent 0 is always at the beginning of the row formatted:
    // agent0,agent1,..,agentN -> utility
    double maxUtility = Integer.MIN_VALUE;
    // System.err.println("Timestep " + agent.getCurrentTS() + " Combined
    // messages at root:");
    // combinedUtilAndConstraintTable.printDecVar();
    for (RowDouble row : combinedUtilAndConstraintTable.getRowList()) {
      if (row.getUtility() > maxUtility) {
        maxUtility = row.getUtility();
        agent.setChosenDoubleValueAtEachTimeStep(currentTimeStep, row.getValueAtPosition(0));
      }
    }

    agent.print("CHOSEN: " + agent.getChosenDoubleValueAtEachTimeStep(currentTimeStep));

    agent.print(agent.getPDDCOP_Algorithm() + " " + agent.getDcop_algorithm() + " utility " + maxUtility);
    
    agent.stopSimulatedTiming();
  }
  
  /**
   * THIS FUNCTION HAS BEEN REVIEWED
   */
  public void root_HYBRID() {
    agent.print("ROOT is running");
    
    List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL);

    // Start of processing time
    agent.startSimulatedTiming();
    
    List<TableDouble> tableList = createTableList(receivedUTILmsgList);
    
    // Interpolate points and join all the tables
    TableDouble joinedTable = interpolateAndJoinTable(tableList, ADD_MORE_POINTS);
    
    // Might need to add expected table and unary constraint table
    joinedTable = addTheUtilityFunctionsToTheJoinedTable(joinedTable);
        
    double maxUtility = -Double.MAX_VALUE;
    
    // Find the maxUtility and argmax from the joinedTable
    for (RowDouble row : joinedTable.getRowList()) {
      if (compare(row.getUtility(), maxUtility) > 0) {
        // only choose the row with value in the interval
        maxUtility = row.getUtility();
        agent.setChosenDoubleValueAtEachTimeStep(currentTimeStep, row.getValueList().get(0));
      }
    }

    agent.print("CHOSEN: " + agent.getChosenDoubleValueAtEachTimeStep(currentTimeStep));

    agent.print(agent.getDcop_algorithm() + " utility " + maxUtility);

    agent.stopSimulatedTiming();
  }
  
  /**
   * THIS FUNCTION HAS BEEN REVIEWED
   * 
   * Moving the values of parent and pseudo-parent
   * This function is called in both leaves and internal nodes.
   * There is a flag to differentiate between the two.
   * @param joinedTable this table is used in internal nodes. At leaf, it is null.
   * @param flag FUNCTION_ONLY or FUNCTION_AND_TABLE
   */
  private Set<List<Double>> movingPointsUsingTheGradient(TableDouble joinedTable, int flag) {
    Set<List<Double>> immutableProductPPValues;
    // Create a set of PP's value list
    // Create a list of valueSet with the same ordering of PP
    // Then do the Cartesian product to get the set of valueList (same ordering as PP)
    List<Set<Double>> valueSetList = new ArrayList<Set<Double>>();
    for (String pParent : agent.getParentAndPseudoStrList()) {
      if (flag == DONE_AT_LEAF) {
        valueSetList.add(agent.getCurrentDiscreteValues(currentTimeStep));
      } // The joined table might contain the PP or not
      else if (flag == DONE_AT_INTERNAL_NODE) {
        Set<Double> valueSetOfPParentToAdd = joinedTable.getValueSetOfGivenAgent(pParent, false);
        valueSetList.add(valueSetOfPParentToAdd);
      }
    }
    immutableProductPPValues = Sets.cartesianProduct(valueSetList);
    
    // Make the productPPValues to be mutable
    Set<List<Double>> mutableProductPPValues = new HashSet<>();
    for (List<Double> innerList : immutableProductPPValues) {
      List<Double> newList = new ArrayList<>(innerList);
      mutableProductPPValues.add(newList);
    }
    
    // Traverse the valueList
    int maxIteration = flag == DONE_AT_LEAF ? agent.getGradientIteration() : agent.getGradientIteration() / 2;
    
    for (int movingIteration = 0; movingIteration < maxIteration; movingIteration++) {
      agent.print(" is moving iteration " + movingIteration);
      
      for (List<Double> valueList : mutableProductPPValues) {
        agent.print(" is moving point " + valueList);
        
        // For each ppToMove (direction), take the derivative of the utility function
        for (int ppToMoveIndex = 0; ppToMoveIndex < valueList.size(); ppToMoveIndex++) {
          String ppAgentToMove = agent.getParentAndPseudoStrList().get(ppToMoveIndex);
          double ppValueToMove = valueList.get(ppToMoveIndex);

          PiecewiseMultivariateQuadFunction functionWithPP = dpopFunctionMap.get(ppAgentToMove);
          
//          PiecewiseMultivariateQuadFunction derivativePw = agent.getDcop_algorithm() == DcopAlgorithm.AC_DPOP
//              ? functionWithPP.takeFirstPartialDerivative(ppAgentToMove) // At leaf, use constraint with pParent
//              : agent.getAgentViewFunction().takeFirstPartialDerivative(ppAgentToMove); // At internal, use agentView

          PiecewiseMultivariateQuadFunction derivativePw = functionWithPP.takeFirstPartialDerivative(ppAgentToMove, agent.getLocalName(), ppAgentToMove);
//              : agent.getAgentViewFunction().takeFirstPartialDerivative(ppAgentToMove); // At internal, use agentView
          
          // Create a map of other agents' values
          Map<String, Double> valueMapOfOtherVariables = new HashMap<>();
          if (flag == DONE_AT_LEAF) {
            valueMapOfOtherVariables.put(ppAgentToMove, ppValueToMove);
          } else if (flag == DONE_AT_INTERNAL_NODE) {
            for (int ppIndex = 0; ppIndex < agent.getParentAndPseudoStrList().size(); ppIndex++) {
              String ppAgent = agent.getParentAndPseudoStrList().get(ppIndex);
              double ppValue = valueList.get(ppIndex);
              
              valueMapOfOtherVariables.put(ppAgent, ppValue);
            }
          }
          
          double argMax = -Double.MAX_VALUE;
          // Finding the arg_max of multivariate agent view function seems wrong
          if (flag == DONE_AT_LEAF) {
            PiecewiseMultivariateQuadFunction unaryFunction = functionWithPP.evaluateToUnaryFunction(valueMapOfOtherVariables);
            double max = -Double.MAX_VALUE;
            
            // Find the arg_max of THE agent after evaluating the binary constraint function to unary
            for (Map<String, Interval> interval : unaryFunction.getTheFirstIntervalSet()) {
              double maxArgmax[] = unaryFunction.getTheFirstFunction().getMaxAndArgMax(interval);
              
              if (compare(maxArgmax[0], max) > 0) {
                max = maxArgmax[0];
                argMax = maxArgmax[1];
              }
            }
            
            agent.print("Unary function: " + unaryFunction);
            agent.print("Max of the function: " + max);
            agent.print("Argmax of the function: " + argMax);
            
          } 
          else if (flag == DONE_AT_INTERNAL_NODE){            
            argMax = agent.getAgentViewTableDouble().maxArgmaxHybrid(valueMapOfOtherVariables, agent.getSelfInterval().getMidPointInHalfIntegerRanges())[1];
            
            if (agent.isPrinting()) {
              agent.print("Argmax of the table: " + argMax);
            }
          }
          
          Map<String, Double> valueMap = new HashMap<>();
          valueMap.put(agent.getLocalName(), argMax);
          valueMap.put(ppAgentToMove, ppValueToMove);

          double gradient = derivativePw.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap);
          
          double movedPpValue = ppValueToMove + GRADIENT_SCALING_FACTOR * gradient;
          
          agent.print("Agent to move:" + ppAgentToMove);
          agent.print("Unary function is: " + functionWithPP);
          agent.print("Derivative is: " + derivativePw);
          agent.print("ppValueToMove " + ppValueToMove);
          agent.print("Argmax value is: " + argMax);
          agent.print("Moved value is: " + movedPpValue);
        
          // only move if the new point is within the interval
          if (agent.getSelfInterval().contains(movedPpValue)) {         
            valueList.set(ppToMoveIndex, movedPpValue);
          }
        }
      }
    }
    
    if (agent.isClustering()) {
      return kmeanCluster(mutableProductPPValues, agent.getNumberOfPoints());
    } else {
      return mutableProductPPValues;
    }
  }
  
  private PiecewiseMultivariateQuadFunction combineByteMessageToFunction(List<ACLMessage> list)
      throws IOException, ClassNotFoundException {
    List<PiecewiseMultivariateQuadFunction> listFunction = new ArrayList<>();
    for (ACLMessage msg : list) {
      ByteArrayInputStream bais = new ByteArrayInputStream(msg.getByteSequenceContent());
      GZIPInputStream gzipIn = new GZIPInputStream(bais);
      ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
      PiecewiseMultivariateQuadFunction func = (PiecewiseMultivariateQuadFunction) objectIn.readObject();
      objectIn.close();
      listFunction.add(func);
    }

    int size = listFunction.size();
    PiecewiseMultivariateQuadFunction function = listFunction.get(0);

    for (int i = 1; i < size; i++) {
      function = function.addPiecewiseFunction(listFunction.get(i));
    }

    return function;
  }
  
	
  /**
   * THIS FUNCTION HAS BEEN REVIEWED
   */
  public List<TableDouble> createDCOPTableFromFunction(Collection<PiecewiseMultivariateQuadFunction> functionList, int timeStep) {
    List<TableDouble> tableListWithParents = new ArrayList<>();
    for (PiecewiseMultivariateQuadFunction pwFunction : functionList) {
      MultivariateQuadFunction func = pwFunction.getTheFirstFunction(); // there is only one function in piecewise at this time

      List<String> varListLabel = new ArrayList<>(func.getVariableSet());
      TableDouble tableFromFunc = new TableDouble(varListLabel);

      List<List<Double>> valueSetList = new ArrayList<>();
      for (int i = 0; i < varListLabel.size(); i++) {
        valueSetList.add(new ArrayList<>(agent.getCurrentDiscreteValues(timeStep)));
      }
      
      for (List<Double> values : Lists.cartesianProduct(valueSetList)) {
        Map<String, Double> valueMap = new HashMap<>();
        for (int i = 0; i < varListLabel.size(); i++) {
          valueMap.put(varListLabel.get(i), values.get(i));
        }
        
        RowDouble newRow = new RowDouble(new ArrayList<>(values), func.evaluateToValueGivenValueMap(valueMap));
        tableFromFunc.addRow(newRow);
      }
      tableListWithParents.add(tableFromFunc);
    }
    
    return tableListWithParents;
  }   
	
	private void actionDiscrete() {
    if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.BOUND_DPOP)) {
      dpopTableStringList.addAll(agent.getDpopDecisionTableList());
      dpopTableStringList.addAll(agent.getDpopBoundRandomTableList());
    } 
    else if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.LS_SDPOP) && isFirstTimeUTIL()) {
      TableString joinedDecisionTable = joinTableListString(agent.getDpopDecisionTableList());

      agent.setStoredReuseTable(joinedDecisionTable);
    } 
    else if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.LS_SDPOP) && !isFirstTimeUTIL()) {
    }
    // Add actual tables to compute actual quality
    // Add tables to DPOP table list for solving
    else if (agent.isDynamic(DynamicType.ONLINE) || agent.isDynamic(DynamicType.STATIONARY)) {
      // Add actual tables for REACT
      if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.REACT)) {
        agent.getActualDpopTableAcrossTimeStep().computeIfAbsent(currentTimeStep, k -> new ArrayList<>())
            .addAll(agent.computeActualDpopTableGivenRandomValues(currentTimeStep));
        agent.getActualDpopTableAcrossTimeStep().get(currentTimeStep).addAll(agent.getDpopDecisionTableList());
        
        dpopTableStringList.addAll(agent.getActualDpopTableAcrossTimeStep().get(currentTimeStep));
      }
      else if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.R_LEARNING)) {
        // DPOP_UTIL will be called twice
        // First time for learning: true -> false
        // Second time for applying: false -> true
        if (currentTimeStep == 0) {
          // Switching from learning to applying
          agent.switchApplyingRLearning();
        }
        
        // Learning R values
        if (!agent.isApplyingRLearning()) {
          agent.getActualDpopTableAcrossTimeStep().computeIfAbsent(currentTimeStep, k -> new ArrayList<>())
            .addAll(agent.computeActualDpopTableGivenRandomValues(currentTimeStep));
          agent.getActualDpopTableAcrossTimeStep().get(currentTimeStep).addAll(agent.getDpopDecisionTableList()); 
          
          dpopTableStringList.addAll(agent.getActualDpopTableAcrossTimeStep().get(currentTimeStep));
          // Add unary constraint table with the switching cost to the dpopTableList
          if (currentTimeStep > 0) {
            TableString switchingCostToPreviousSolution = switchingCostGivenSolution(agent.getAgentID(),
                agent.getDecisionVariableDomainMap().get(agent.getAgentID()),
                agent.getChosenValueAtEachTimeStep(currentTimeStep - 1));
            dpopTableStringList.add(switchingCostToPreviousSolution);
          }
        }
        // Applying R learning
        else {
          // Apply R learning with R values
          // The actual DPOP table has been added above
          dpopTableStringList.addAll(agent.computeRLearningDpopTableGivenRandomValues(currentTimeStep));
          dpopTableStringList.addAll(agent.getDpopDecisionTableList());
          // No need to add unary constraint since R-learning has taken into account the previous decision variable in its domain
        }
      }
      // Add discounted expected tables for FORWARD and HYBRID
      else {
        agent.getActualDpopTableAcrossTimeStep().computeIfAbsent(currentTimeStep, k -> new ArrayList<>())
          .addAll(agent.computeActualDpopTableGivenRandomValues(currentTimeStep));
        agent.getActualDpopTableAcrossTimeStep().get(currentTimeStep).addAll(agent.getDpopDecisionTableList());

        double df = agent.getDiscountFactor();
        dpopTableStringList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(),
            currentTimeStep, df));
        dpopTableStringList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(),
            currentTimeStep, df));

        if (currentTimeStep > 0) {
          TableString switchingCostToPreviousSolution = switchingCostGivenSolution(agent.getAgentID(),
              agent.getDecisionVariableDomainMap().get(agent.getAgentID()),
              agent.getChosenValueAtEachTimeStep(currentTimeStep - 1));
          dpopTableStringList.add(switchingCostToPreviousSolution);
        }
      }
    } else {
      // For all other algorithms
      // Compute decision, random and switching cost tables
      // Add all of them to the dpopTableList
      dpopTableStringList.addAll(computeDiscountedDpopAndSwitchingCostTables(agent.getDynamicType(),
          agent.getPDDCOP_Algorithm(), currentTimeStep));
    }

    if (agent.isLeaf()) {
      agent.print("Leaf is running");
      leafDoUtilProcess();
      agent.print("Leaf is done");
    } else if (agent.isRoot()) {
      agent.print("Root is running");
      try {
        rootDoUtilProcess();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
      agent.print("Root is done");
    } else {
      agent.print("Internal node is running");
      try {
        internalNodeDoUtilProcess();
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
      agent.print("Internal node is done");
    }
  
	}

	/**
	 * REVIEWED
	 */
	private void leafDoUtilProcess() {
		agent.startSimulatedTiming();

		TableString combinedTable = null;
		// Leafs behave the same even when in all DPOP runs
		if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.LS_SDPOP)) {
			if (!agent.isRecomputingDPOP_UTIL()) {
				return;
			}

			combinedTable = agent.computeDiscountedDecisionTable(agent.getStoredReuseTable(), currentTimeStep,
					agent.getDiscountFactor());

			combinedTable = joinTableString(combinedTable,
					joinTableListString(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(),
							currentTimeStep, agent.getDiscountFactor())));

			// If not a rand table, no need to compute in later DPOP rounds
			if (!combinedTable.isRandTable()) {
				agent.setRecomputingDPOP_UTILToFalse();
			}
		} else {
			combinedTable = joinTableString(combinedTable, joinTableListString(dpopTableStringList));
		}
		agent.setAgentViewTable(combinedTable);

		TableString projectedTable = projectOperatorString(combinedTable, agent.getLocalName());

		agent.stopSimulatedTiming();

		agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, MessageType.DPOP_UTIL, agent.getSimulatedTime());
	}

	/**
	 * REVIEWED
	 * 
	 * @throws UnreadableException
	 */
	private void internalNodeDoUtilProcess() throws UnreadableException {
		TableString combinedTable = computeCombinedTableAtNonLeave();

		// It means agents are not recomputing tables
		if (combinedTable == null) {
			return;
		}

		agent.setAgentViewTable(combinedTable);

		TableString projectedTable = projectOperatorString(combinedTable, agent.getLocalName());

		agent.stopSimulatedTiming();

		agent.sendObjectMessageWithTime(agent.getParentAID(), projectedTable, MessageType.DPOP_UTIL, agent.getSimulatedTime());
	}

	/**
	 * REVIEWED
	 * 
	 * @return
	 * @throws UnreadableException
	 */
	private TableString computeCombinedTableAtNonLeave() throws UnreadableException {
		TableString combinedTable = null;

		if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.LS_SDPOP) && isFirstTimeUTIL()) {
			combinedTable = agent.getStoredReuseTable();

			List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL);
			// Separate children into children with decision and children with random
			List<TableString> decisionUtil = new ArrayList<>();
			List<TableString> randomUtil = new ArrayList<>();
			// Separate decision UTIL and random UTIL
			for (ACLMessage msg : receivedUTILmsgList) {
				TableString utilTable = (TableString) msg.getContentObject();

				if (utilTable.isRandTable()) {
					randomUtil.add(utilTable);
				} else {
					decisionUtil.add(utilTable);
					agent.getReuseChildUTIL().add(msg.getSender().getLocalName());
				}
			}
			// Add with decision UTIL and store the non-discount form
//      for (Table decisionTable : decisionUtil) {
//        combinedTable = joinTable(combinedTable, decisionTable);
//      }
			combinedTable = joinTableString(combinedTable, joinTableListString(decisionUtil));

			agent.setStoredReuseTable(combinedTable);

			// Combine with random UTIL
//      for (Table randomTable : randomUtil) {
//        combinedTable = joinTable(combinedTable, randomTable);
//      }
			combinedTable = joinTableString(combinedTable, joinTableListString(randomUtil));

			// Combine with local random constraint
//      for (Table randomConstraint : agent.getDpopRandomTableList()) {
//        combinedTable = joinTable(combinedTable,
//            agent.computeDiscountedExpectedTable(randomConstraint, currentTimeStep, agent.getDiscountFactor()));
//      }
			combinedTable = joinTableString(combinedTable,
					joinTableListString(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(),
							currentTimeStep, agent.getDiscountFactor())));

			if (!combinedTable.isRandTable()) {
				agent.setRecomputingDPOP_UTILToFalse();
			}
		} else if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.LS_SDPOP) && !isFirstTimeUTIL()) {
			if (!agent.isRecomputingDPOP_UTIL()) {
				return null;
			}

			List<ACLMessage> randomUtilMessages = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL,
					agent.getChildrenAIDSet().size() - agent.getReuseChildUTIL().size());

			agent.startSimulatedTiming();

			combinedTable = agent.computeDiscountedDecisionTable(agent.getStoredReuseTable(), currentTimeStep,
					agent.getDiscountFactor());
			// Join with discounted random local constraints
			for (TableString localRandomConstraints : agent.getDpopRandomTableList()) {
				TableString expectedTable = agent.computeDiscountedExpectedTable(localRandomConstraints, currentTimeStep,
						agent.getDiscountFactor());
				combinedTable = joinTableString(combinedTable, expectedTable);
			}
			// Join with UTIL which contains only random UTIL
			combinedTable = joinTableString(combinedTable, combineMessageTableString(randomUtilMessages));
		}
		// Not LS_SDPOP
		else {
			List<ACLMessage> receivedUTILmsgList = waitingForMessageFromChildrenWithTime(MessageType.DPOP_UTIL);

			agent.startSimulatedTiming();

			combinedTable = combineMessageTableString(receivedUTILmsgList);

			for (TableString pseudoParentTable : dpopTableStringList) {
				combinedTable = joinTableString(combinedTable, pseudoParentTable);
			}
		}

		return combinedTable;
	}

	/**
	 * REVIEWED
	 * 
	 * @throws UnreadableException
	 */
	private void rootDoUtilProcess() throws UnreadableException {
		TableString combinedTable = computeCombinedTableAtNonLeave();

		// pick value with smallest utility
		// since agent 0 is always at the beginning of the row formatted: agent0,
		// agent1,.., agentN -> utility
		double maxUtility = -Double.MAX_VALUE;

		String chosenValue = "";

		agent.print("Root has combinedTable=" + combinedTable);

		for (RowString row : combinedTable.getRowList()) {
			if (Double.compare(row.getUtility(), maxUtility) > 0) {
				maxUtility = row.getUtility();
				chosenValue = row.getValueAtPosition(0);
			}
		}

		agent.print("Root has chosen value " + chosenValue);
		agent.print("Root has chosen utility " + maxUtility);

		agent.storeDpopSolution(chosenValue, currentTimeStep);
		if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.C_DCOP)) {
			agent.setCDPOP_value(chosenValue);
		}

		// Randomize DPOP solutions for the first time step before solving
		if (agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.REACT) && currentTimeStep == 0) {
			int randomIndex = agent.getRandomGenerator().nextInt(agent.getSelfDomain().size());
			agent.getChosenValueAtEachTSMap().put(-1, agent.getSelfDomain().get(randomIndex));
		}

		agent.stopSimulatedTiming();
		agent.setOnlineSolvingTime(currentTimeStep, agent.getSimulatedTime());
	}

	/**
	 * REVIEWED <br>
	 * This function is called only when the algorithm is not LS_SDPOP due to
	 * reusing tables <br>
	 * Also, not called with ONLINE algorithms
	 * 
	 * @param dynamicType
	 * @param algorithm
	 * @param timeStep
	 * @return
	 */
	private List<TableString> computeDiscountedDpopAndSwitchingCostTables(DynamicType dynamicType, PDDcopAlgorithm algorithm,
			int timeStep) {
		List<TableString> resultTableList = new ArrayList<>();

		if (dynamicType == DynamicType.INFINITE_HORIZON) {
			// When currentTimeStep == agent.getHorizon(), solve for the last time step
			if (currentTimeStep == agent.getHorizon()) {
				dpopTableStringList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(),
						agent.getHorizon(), 1D));
				dpopTableStringList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(),
						agent.getHorizon(), 1D));
			} else {
				// Compute the switching cost constraint
				// Collapse DCOPs from t = 0 to t = horizon - 1
				if (algorithm == PDDcopAlgorithm.C_DCOP) {
				  dpopTableStringList.addAll(agent.computeCollapsedDecisionTableList(agent.getDpopDecisionTableList(),
							agent.getHorizon() - 1, 1D));
				  dpopTableStringList.addAll(agent.computeCollapsedRandomTableList(agent.getDpopRandomTableList(),
							agent.getHorizon() - 1, 1D));
				  dpopTableStringList.add(agent.computeCollapsedSwitchingCostTable(agent.getSelfDomain(),
							agent.getHorizon() - 1, 1D));
				} else if (algorithm == PDDcopAlgorithm.FORWARD) {
				  dpopTableStringList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(),
							currentTimeStep, 1D));
				  dpopTableStringList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(),
							currentTimeStep, 1D));
					if (currentTimeStep > 0) {
						TableString switchingCostToPreviousSolution = switchingCostGivenSolution(agent.getAgentID(),
								agent.getDecisionVariableDomainMap().get(agent.getAgentID()),
								agent.getChosenValueAtEachTimeStep(currentTimeStep - 1));
						dpopTableStringList.add(switchingCostToPreviousSolution);
					}
					// Also add switching cost table to the stationary solution found at horizon
					if (currentTimeStep == agent.getHorizon() - 1) {
						TableString switchingCostToLastSolution = switchingCostGivenSolution(agent.getAgentID(),
								agent.getDecisionVariableDomainMap().get(agent.getAgentID()),
								agent.getChosenValueAtEachTimeStep(agent.getHorizon()));
						dpopTableStringList.add(switchingCostToLastSolution);
					}
				} else if (algorithm == PDDcopAlgorithm.BACKWARD) {
				  dpopTableStringList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(),
							currentTimeStep, 1D));
				  dpopTableStringList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(),
							currentTimeStep, 1D));
					// If not at the horizon, add switching cost regarding the solution at timeStep
					// + 1
					if (currentTimeStep < agent.getHorizon()) {
						TableString switchingCostToLaterSolution = switchingCostGivenSolution(agent.getAgentID(),
								agent.getDecisionVariableDomainMap().get(agent.getAgentID()),
								agent.getChosenValueAtEachTimeStep(currentTimeStep + 1));
						dpopTableStringList.add(switchingCostToLaterSolution);
					}
				}
			}
		} else if (dynamicType == DynamicType.FINITE_HORIZON) {
			double df = agent.getDiscountFactor();

			if (algorithm == PDDcopAlgorithm.C_DCOP) {
			  dpopTableStringList.addAll(agent.computeCollapsedDecisionTableList(agent.getDpopDecisionTableList(),
						agent.getHorizon(), df));
			  dpopTableStringList.addAll(
						agent.computeCollapsedRandomTableList(agent.getDpopRandomTableList(), agent.getHorizon(), df));
			  dpopTableStringList
						.add(agent.computeCollapsedSwitchingCostTable(agent.getSelfDomain(), agent.getHorizon(), df));
			} else {
			  dpopTableStringList.addAll(agent.computeDiscountedDecisionTableList(agent.getDpopDecisionTableList(),
						currentTimeStep, df));
			  dpopTableStringList.addAll(agent.computeDiscountedExpectedRandomTableList(agent.getDpopRandomTableList(),
						currentTimeStep, df));
				if (algorithm == PDDcopAlgorithm.FORWARD) {
					if (currentTimeStep > 0) {
					  dpopTableStringList.add(switchingCostGivenSolution(agent.getAgentID(),
								agent.getDecisionVariableDomainMap().get(agent.getAgentID()),
								agent.getChosenValueAtEachTimeStep(currentTimeStep - 1)));
					}
				} else if (algorithm == PDDcopAlgorithm.BACKWARD) {
					if (currentTimeStep < agent.getHorizon()) {
					  dpopTableStringList.add(switchingCostGivenSolution(agent.getAgentID(),
								agent.getDecisionVariableDomainMap().get(agent.getAgentID()),
								agent.getChosenValueAtEachTimeStep(currentTimeStep + 1)));
					}
				}
			}
		}

		return resultTableList;
	}

	private List<ACLMessage> waitingForMessageFromChildrenWithTime(MessageType msgType) {
		return waitingForMessageFromChildrenWithTime(msgType, agent.getChildrenAIDSet().size());
	}

	private List<ACLMessage> waitingForMessageFromChildrenWithTime(MessageType msgType, int childCount) {
	  int msgCode = msgType.ordinal();
	  
	  List<ACLMessage> messageList = new ArrayList<ACLMessage>();

		while (messageList.size() < childCount) {
			agent.startSimulatedTiming();

			MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
			ACLMessage receivedMessage = myAgent.blockingReceive(template);

			agent.stopSimulatedTiming();

			long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());

			if (timeFromReceiveMessage > agent.getSimulatedTime()) {
				agent.setSimulatedTime(timeFromReceiveMessage);
			}
			messageList.add(receivedMessage);
		}

		agent.addupSimulatedTime(AgentPDDCOP.getDelayMessageTime());
		return messageList;
	}

	private TableString joinTableString(TableString table1, TableString table2) {
		if (table1 == null) {
			return table2;
		}
		if (table2 == null) {
			return table1;
		}

		// get commonVariables
		List<String> commonVariables = getCommonVariables(table1.getDecVarLabel(), table2.getDecVarLabel());

		// create indexList1, indexList2
		// xet tung variable commonVariables
		// add index of that variable to the indexList
		List<Integer> indexContainedInCommonList1 = new ArrayList<Integer>();
		List<Integer> indexContainedInCommonList2 = new ArrayList<Integer>();
		for (String variable : commonVariables) {
			indexContainedInCommonList1.add(table1.getDecVarLabel().indexOf(variable));
			indexContainedInCommonList2.add(table2.getDecVarLabel().indexOf(variable));
		}
		// create returnTable
		// join label
		List<String> joinedLabelTable1FirstThenTable2 = getJoinLabel(table1.getDecVarLabel(), table2.getDecVarLabel(),
				indexContainedInCommonList2);

		TableString joinedTable = new TableString(joinedLabelTable1FirstThenTable2, table1.isRandTable() || table2.isRandTable());
		for (RowString row1 : table1.getRowList()) {
			for (RowString row2 : table2.getRowList()) {
				RowString joinedRow = getJoinRowString(row1, row2, indexContainedInCommonList1, indexContainedInCommonList2);
				if (joinedRow != null) {
					joinedTable.addRow(joinedRow);
				}
			}
		}

		return joinedTable;
	}
	
	 private TableDouble joinTableDouble(TableDouble table1, TableDouble table2) {
	    if (table1 == null) {
	      return table2;
	    }
	    if (table2 == null) {
	      return table1;
	    }

	    // get commonVariables
	    List<String> commonVariables = getCommonVariables(table1.getDecVarLabel(), table2.getDecVarLabel());

	    // create indexList1, indexList2
	    // xet tung variable commonVariables
	    // add index of that variable to the indexList
	    List<Integer> indexContainedInCommonList1 = new ArrayList<Integer>();
	    List<Integer> indexContainedInCommonList2 = new ArrayList<Integer>();
	    for (String variable : commonVariables) {
	      indexContainedInCommonList1.add(table1.getDecVarLabel().indexOf(variable));
	      indexContainedInCommonList2.add(table2.getDecVarLabel().indexOf(variable));
	    }
	    // create returnTable
	    // join label
	    List<String> joinedLabelTable1FirstThenTable2 = getJoinLabel(table1.getDecVarLabel(), table2.getDecVarLabel(),
	        indexContainedInCommonList2);

	    TableDouble joinedTable = new TableDouble(joinedLabelTable1FirstThenTable2, table1.isRandTable() || table2.isRandTable());
	    for (RowDouble row1 : table1.getRowList()) {
	      for (RowDouble row2 : table2.getRowList()) {
	        RowDouble joinedRow = getJoinRowDouble(row1, row2, indexContainedInCommonList1, indexContainedInCommonList2);
	        if (joinedRow != null) {
	          joinedTable.addRow(joinedRow);
	        }
	      }
	    }

	    return joinedTable;
	  }

	private <T> List<T> getCommonVariables(List<T> variableList1, List<T> variableList2) {
		List<T> commonVariableList = new ArrayList<>();

		for (T variable1 : variableList1)
			for (T variable2 : variableList2) {
				// check if equal agents, and if list contains agent or not
				// if (variable1.equals(variable2) && isContainVariable(commonVariableList,
				// variable1) == false)
				if (variable1.equals(variable2) && !commonVariableList.contains(variable1)) {
					commonVariableList.add(variable1);
					break;
				}
			}

		return commonVariableList;
	}

	// for variable1 from label1, add to joinedLabel
	// for variable2 from label2
	// if index not in indexContainedInCommonList2
	// then add to joinedLabel
	public <T> List<T> getJoinLabel(List<T> label1, List<T> label2,
			List<Integer> indexContainedInCommonList2) {

		List<T> joinedLabel = new ArrayList<>();// (label1);
		for (T variable1 : label1) {
			joinedLabel.add(variable1);
		}

		// add variable with index not in indexContainedInCommonList2
		for (int i = 0; i < label2.size(); i++) {
			if (!indexContainedInCommonList2.contains(i))
				joinedLabel.add(label2.get(i));
		}

		return joinedLabel;
	}
	
	 public RowDouble getJoinRowDouble(RowDouble row1, RowDouble row2, List<Integer> indexList1, List<Integer> indexList2) {
	    // check if same size
	    if (indexList1.size() != indexList2.size()) {
	      System.err.println("Different size from indexList: " + indexList1.size() + " " + indexList2.size());
	      return null;
	    }

	    int listSize = indexList1.size();
	    // check if same values
	    for (int i = 0; i < listSize; i++) {
	      if (row1.getValueList().get(indexList1.get(i))
	          .equals(row2.getValueList().get(indexList2.get(i))) == false) {
	        return null;
	      }
	    }

	    // join two row
	    List<Double> joinedValues = new ArrayList<>(row1.getValueList());// (row1.getValueList());

	    for (int i = 0; i < row2.getValueList().size(); i++) {
	      if (!indexList2.contains(i)) {
	        joinedValues.add(row2.getValueList().get(i));
	      }
	    }

	    RowDouble joinedRow = new RowDouble(joinedValues, row1.getUtility() + row2.getUtility());
	    return joinedRow;
	  }

	public RowString getJoinRowString(RowString row1, RowString row2, List<Integer> indexList1, List<Integer> indexList2) {
		// check if same size
		if (indexList1.size() != indexList2.size()) {
			System.err.println("Different size from indexList: " + indexList1.size() + " " + indexList2.size());
			return null;
		}

		int listSize = indexList1.size();
		// check if same values
		for (int i = 0; i < listSize; i++) {
			if (row1.getValueList().get(indexList1.get(i))
					.equals(row2.getValueList().get(indexList2.get(i))) == false) {
				return null;
			}
		}

		// join two row
		List<String> joinedValues = new ArrayList<String>(row1.getValueList());// (row1.getValueList());

		for (int i = 0; i < row2.getValueList().size(); i++) {
			if (!indexList2.contains(i)) {
				joinedValues.add(row2.getValueList().get(i));
			}
		}

		RowString joinedRow = new RowString(joinedValues, row1.getUtility() + row2.getUtility());
		return joinedRow;
	}
	
	 // create new TabelDPOP
  // create new Label: eliminate variableToProject
  // create new Table with -1 dimension
  // create checkedList mark already picked tuples
  // for each tuple1 from the table
  // if index(tuple1) already in picked tuple => continue
  // for each tuple2:tuple1->end from the table
  // compare to the minimum , and update
  // add to new Table
  public TableDouble projectOperatorDouble(TableDouble table, String variableToProject) {
    int indexEliminated = getIndexOfContainedVariable(table.getDecVarLabel(), variableToProject);

    if (indexEliminated == -1) {
      return null;
    }

    // create arrayIndex
    List<Integer> arrayIndex = new ArrayList<Integer>();
    for (int i = 0; i < table.getDecVarLabel().size(); i++) {
      if (i != indexEliminated)
        arrayIndex.add(i);
    }

    // create checkedList
    List<Integer> checkedList = new ArrayList<Integer>();

    // create projectedLabel
    List<String> projectedLabel = createTupleFromList(table.getDecVarLabel(), arrayIndex);

    // create projectedTable
    TableDouble projectTable = new TableDouble(projectedLabel, table.isRandTable());
    for (int i = 0; i < table.size(); i++) {
      if (checkedList.contains(i) == true)
        continue;
      checkedList.add(i);
      RowDouble row1 = table.getRowList().get(i);
      List<Double> tuple1 = createTupleFromRowDouble(row1, arrayIndex);
      double maxUtility = row1.getUtility();
      List<Double> maxTuple = tuple1;

      for (int j = i + 1; j < table.size(); j++) {
        RowDouble row2 = table.getRowList().get(j);
        List<Double> tuple2 = createTupleFromRowDouble(row2, arrayIndex);
        double row2Utility = row2.getUtility();
        if (isSameTupleDouble(tuple1, tuple2) == true) {
          checkedList.add(j);
          if (row2Utility > maxUtility) {
            maxUtility = row2Utility;
            maxTuple = tuple2;
          }
        }

      }

      projectTable.addRow(new RowDouble(maxTuple, maxUtility));
    }

    return projectTable;
  }

	// create new TabelDPOP
	// create new Label: eliminate variableToProject
	// create new Table with -1 dimension
	// create checkedList mark already picked tuples
	// for each tuple1 from the table
	// if index(tuple1) already in picked tuple => continue
	// for each tuple2:tuple1->end from the table
	// compare to the minimum , and update
	// add to new Table
	public TableString projectOperatorString(TableString table, String variableToProject) {
		int indexEliminated = getIndexOfContainedVariable(table.getDecVarLabel(), variableToProject);

		if (indexEliminated == -1) {
			return null;
		}

		// create arrayIndex
		List<Integer> arrayIndex = new ArrayList<Integer>();
		for (int i = 0; i < table.getDecVarLabel().size(); i++) {
			if (i != indexEliminated)
				arrayIndex.add(i);
		}

		// create checkedList
		List<Integer> checkedList = new ArrayList<Integer>();

		// create projectedLabel
		List<String> projectedLabel = createTupleFromList(table.getDecVarLabel(), arrayIndex);

		// create projectedTable
		TableString projectTable = new TableString(projectedLabel, table.isRandTable());
		for (int i = 0; i < table.getRowCount(); i++) {
			if (checkedList.contains(i) == true)
				continue;
			checkedList.add(i);
			RowString row1 = table.getRowList().get(i);
			List<String> tuple1 = createTupleFromRowString(row1, arrayIndex);
			double maxUtility = row1.getUtility();
			List<String> maxTuple = tuple1;

			for (int j = i + 1; j < table.getRowCount(); j++) {
				RowString row2 = table.getRowList().get(j);
				List<String> tuple2 = createTupleFromRowString(row2, arrayIndex);
				double row2Utility = row2.getUtility();
				if (isSameTupleString(tuple1, tuple2) == true) {
					checkedList.add(j);
					if (row2Utility > maxUtility) {
						maxUtility = row2Utility;
						maxTuple = tuple2;
					}
				}

			}

			projectTable.addRow(new RowString(maxTuple, maxUtility));
		}

		return projectTable;
	}

	int getIndexOfContainedVariable(List<String> list, String input) {
		return list.indexOf(input);
	}

	// create tuples from Row and arrayIndex
	public List<String> createTupleFromList(List<String> list, List<Integer> arrayIndex) {
		if (arrayIndex.size() >= list.size()) {
//			System.err.println("Cannot create tuple with size: " + arrayIndex + " from Row size: " +
//									list.size());
			return null;
		}
		List<String> newTuple = new ArrayList<String>();
		for (Integer index : arrayIndex) {
			newTuple.add(list.get(index));
		}
		return newTuple;
	}
	
	 // create tuples from Row and arrayIndex
  public List<Double> createTupleFromRowDouble(RowDouble row, List<Integer> arrayIndex) {
    if (arrayIndex.size() >= row.getVariableCount()) {
      return null;
    }
    List<Double> newTuple = new ArrayList<>();
    for (Integer index : arrayIndex) {
      newTuple.add(row.getValueAtPosition(index));
    }
    return newTuple;
  }

	// create tuples from Row and arrayIndex
	public List<String> createTupleFromRowString(RowString row, List<Integer> arrayIndex) {
		if (arrayIndex.size() >= row.getVariableCount()) {
			return null;
		}
		List<String> newTuple = new ArrayList<String>();
		for (Integer index : arrayIndex) {
			newTuple.add(row.getValueAtPosition(index));
		}
		return newTuple;
	}

	// check if two tuples has the same values
	public boolean isSameTupleString(List<String> tuple1, List<String> tuple2) {
		if (tuple1.size() != tuple2.size()) {
			System.err.println("Different size from two tuples: " + tuple1.size() + " and " + tuple2.size());
			return false;
		}
		int size = tuple1.size();
		for (int i = 0; i < size; i++) {
			if (tuple1.get(i).equals(tuple2.get(i)) == false) {
				return false;
			}
		}
		return true;
	}
	
	 // check if two tuples has the same values
  public boolean isSameTupleDouble(List<Double> tuple1, List<Double> tuple2) {
    if (tuple1.size() != tuple2.size()) {
      System.err.println("Different size from two tuples: " + tuple1.size() + " and " + tuple2.size());
      return false;
    }
    int size = tuple1.size();
    for (int i = 0; i < size; i++) {
      if (compare(tuple1.get(i), tuple2.get(i)) != 0) {
        return false;
      }
    }
    return true;
  }

	// for each value of X
	// for each message received from the children
	// sum the utility that received from the children
	TableString combineMessageTableString(List<ACLMessage> list) {
		if (list.isEmpty()) {
			return null;
		}

		List<TableString> listTable = new ArrayList<TableString>();
		for (ACLMessage msg : list) {
			try {
				listTable.add((TableString) msg.getContentObject());
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}

		int size = listTable.size();
		TableString table = listTable.get(0);

		for (int i = 1; i < size; i++) {
			table = joinTableString(table, listTable.get(i));
		}

		return table;
	}
	
	 // for each value of X
  // for each message received from the children
  // sum the utility that received from the children
  TableDouble combineMessageTableDouble(List<ACLMessage> list) {
    if (list.isEmpty()) {
      return null;
    }

    List<TableDouble> listTable = new ArrayList<>();
    for (ACLMessage msg : list) {
      try {
        listTable.add((TableDouble) msg.getContentObject());
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    }

    int size = listTable.size();
    TableDouble table = listTable.get(0);

    for (int i = 1; i < size; i++) {
      table = joinTableDouble(table, listTable.get(i));
    }

    return table;
  }

	public void writeTimeToFile() {
		if (!agent.isRunningPddcopAlgorithm(PDDcopAlgorithm.REACT)) {
			return;
		}

		String line = "";
//		String alg = DCOP_INFO.algTypes[agent.algorithm];
		String alg = String.valueOf(agent.getPDDCOP_Algorithm());
		if (currentTimeStep == 0)
			line = line + alg + "\t" + agent.getInputFileName() + "\n";

		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		long runTime = System.currentTimeMillis() - agent.getCurrentUTILstartTime();

		line = line + "ts=" + currentTimeStep + "\t" + df.format(runTime) + " ms" + "\n";

		String fileName = "id=" + agent.getInstanceID() + "/sw=" + (int) agent.getSwitchingCost()
				+ "/react_runtime.txt";
		byte data[] = line.getBytes();
		Path p = Paths.get(fileName);

		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p, CREATE, APPEND))) {
			out.write(data, 0, data.length);
		} catch (IOException x) {
			System.err.println(x);
		}
	}

	/**
	 * REVIEWED <br>
	 * Compute unary switching cost table of given agent agent given valueList and
	 * differentValue
	 * 
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

	/**
	 * REVIEWED <br>
	 * INFINITE and FINITE only
	 * 
	 * @return
	 */
	private boolean isFirstTimeUTIL() {
		return (agent.isDynamic(DynamicType.INFINITE_HORIZON) && currentTimeStep == agent.getHorizon())
				|| (agent.isDynamic(DynamicType.FINITE_HORIZON) && currentTimeStep == 0);
	}

	/**
	 * Improving speed and reducing memory when joining tables
	 * 
	 * @param tableList
	 * @return
	 */
	public TableString joinTableListString(List<TableString> tableList) {
		int size = tableList.size();

		if (tableList.isEmpty()) {
			return null;
		} else if (size == 1) {
			return tableList.get(0);
		} else if (size == 2) {
			return joinTableString(tableList.get(0), tableList.get(1));
		} else {
			return joinTableString(joinTableListString(tableList.subList(0, size / 2)),
					joinTableListString(tableList.subList(size / 2, size)));
		}
	}
	
	 /**
   * Improving speed and reducing memory when joining tables
   * 
   * @param tableList
   * @return
   */
  public TableDouble joinTableListDouble(List<TableDouble> tableList) {
    int size = tableList.size();

    if (tableList.isEmpty()) {
      return null;
    } else if (size == 1) {
      return tableList.get(0);
    } else if (size == 2) {
      return joinTableDouble(tableList.get(0), tableList.get(1));
    } else {
      return joinTableDouble(joinTableListDouble(tableList.subList(0, size / 2)),
          joinTableListDouble(tableList.subList(size / 2, size)));
    }
  }
	
  private List<TableDouble> createTableList(List<ACLMessage> receivedUTILmsgList) {
    List<TableDouble> tableList = new ArrayList<>();
    for (ACLMessage msg : receivedUTILmsgList) {
      try {
        tableList.add((TableDouble) msg.getContentObject());
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
    } 
    
    return tableList;
  }
  
  /**
   * This function has been REVIEWED
   * Interpolate points that are not common among tables <br>
   * Fill in the table with interpolated points <br>
   * @param tableList
   * @return the joined table after interpolation
   */
  private TableDouble interpolateAndJoinTable(List<TableDouble> tableList, boolean isAddingPoints) {
    // Find the common variables
    Set<String> commonVariables = new HashSet<>();
    
    for (int i = 0; i < tableList.size() - 1; i++) {
      for (int j = 1; j < tableList.size(); j++) {
        Set<String> pairwiseCommonVar = new HashSet<String>(tableList.get(i).getDecVarLabel());
        pairwiseCommonVar.retainAll(tableList.get(j).getDecVarLabel());
        commonVariables.addAll(pairwiseCommonVar);
      }
    }
            
    /*
     * For each table, find all value combination of the common variables from other tables
     */
    Map<TableDouble, Set<RowDouble>> interpolatedRowSetOfEachTable = new HashMap<>();
    Map<String, Set<Double>> valueFromAllTableMap = new HashMap<>();
    
    /*
     * Traverse every table => create the map <Agent, Set<Double>>
     */
    agent.print("start creatings value map from All table");
    for (TableDouble utilTable : tableList) { 
      for (String commonAgent : commonVariables) {
        Set<Double> valueSetOtherTableGivenAgent = utilTable.getValueSetOfGivenAgent(commonAgent, false);
        
        if (isAddingPoints == ADD_MORE_POINTS) {
          valueSetOtherTableGivenAgent.addAll(agent.getCurrentDiscreteValues(currentTimeStep));
        }
        
        if (valueFromAllTableMap.containsKey(commonAgent)) {
          valueFromAllTableMap.get(commonAgent).addAll(valueSetOtherTableGivenAgent);
        } else {
          valueFromAllTableMap.put(commonAgent, new HashSet<>(valueSetOtherTableGivenAgent));
        }
      }
    }
    agent.print("Agent finishes creatings value map from all table");
    
    /*
     * For each table => do the interpolation and add them to the list
     */
    agent.print(" start interpolating tables");
    for (TableDouble utilTable : tableList) {
      interpolatedRowSetOfEachTable.put(utilTable, utilTable.interpolateGivenValueSetMap(valueFromAllTableMap, 1));
    }
    agent.print("finishes interpolating tables");

    
    // Add the interpolated row to the corresponding table
    for (Entry<TableDouble, Set<RowDouble>> entry : interpolatedRowSetOfEachTable.entrySet()) {
      entry.getKey().addRows(entry.getValue());
    }
    
    agent.print("start joining tables");
    // Now joining all the tables
    TableDouble joinedTable = null;
    for (TableDouble table : interpolatedRowSetOfEachTable.keySet()) {
      joinedTable = joinTableDouble(joinedTable, table);
    }
    agent.print("finishes joining tables");
    
    return joinedTable;
  
  }
  
  /*
   * THIS FUNCTION IS REVIEWED
   * For each pp
   *  If joinedTable contains pp
   *    Evaluate the row by the function and update the row
   *  Else if joinedTable doesn't contain pp
   *    Get a list of pp values (interval.discretize())
   *    Create new table with the label.add(pParent)
   *    For each ppValue
   *      For each row 
   *        Get the <agent, value> and <pp, ppValue>, evaluate the function
   *        Create the new row with extending the valueList
   *        Add up to the utility value
   *      End
   *    End
   *  Endif
   * End
   *
   * @param joinedTable
   * @return
   */
  private TableDouble addTheUtilityFunctionsToTheJoinedTable(TableDouble joinedTable) {  
    for (Entry<String, PiecewiseMultivariateQuadFunction> entry : agent.getFunctionWithPParentMap().entrySet()) {
      String pParent = entry.getKey();
      PiecewiseMultivariateQuadFunction pFunction = entry.getValue();
      
      if (joinedTable.containsAgent(pParent)) {
        for (RowDouble row : joinedTable.getRowList()) {          
          Map<String, Double> valueMap = new HashMap<>();
          valueMap.put(pParent, row.getValueAtPosition(joinedTable.indexOf(pParent)));
          valueMap.put(agent.getLocalName(), row.getValueAtPosition(joinedTable.indexOf(agent.getLocalName())));
          row.setUtility(row.getUtility() + pFunction.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap));
        }
      }
      // Unary switching cost constraint or expected table
      else if (pParent.equals(agent.getLocalName()) || pParent.contains(RANDOM_PREFIX)) {
        for (RowDouble row : joinedTable.getRowList()) {          
          Map<String, Double> valueMap = new HashMap<>();
          valueMap.put(agent.getLocalName(), row.getValueAtPosition(joinedTable.indexOf(agent.getLocalName())));
          row.setUtility(row.getUtility() + pFunction.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap));
        }
      }
      // Doesn't contain the pParent
      // Then extend a new column to the end of the label and each row
      else {
        TableDouble newTable = new TableDouble(joinedTable.getDecVarLabel());
        newTable.extendToTheEndOfLabel(pParent);
        
        // Add values for the new label of pParent
        Set<Double> pValueList = agent.getCurrentDiscreteValues(currentTimeStep);
        
        for (Double pValue : pValueList) {
          for (RowDouble row : joinedTable.getRowList()) {
            Map<String, Double> valueMap = new HashMap<>();
            valueMap.put(pParent, pValue);
            valueMap.put(agent.getLocalName(), row.getValueAtPosition(joinedTable.indexOf(agent.getLocalName())));
            double newUtility = row.getUtility() + pFunction.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap); 
            
            RowDouble newRow = new RowDouble(row.getValueList(), newUtility);
            newRow.addValueToTheEnd(pValue);
            newTable.addRow(newRow);
          }
        }
        joinedTable = newTable;
      }
    }
    
    return joinedTable;
  
  }
  
  /**
   * This function has been REVIEWED
   * 
   * Create the utilTable from agentViewTable (which contains this agent) and productPPValues of pParents
   * @param agentViewTable
   * @param productPPValues
   * @return
   */
  private TableDouble createUtilTableFromValueSet(TableDouble agentViewTable, Set<List<Double>> productPPValues) {
    //Now calculate the new UTIL table to send to parent
    List<String> label = agent.getParentAndPseudoStrList();
    TableDouble utilTable = new TableDouble(label);

    // Interpolate the table to get values for each of the valueList
    for (List<Double> valueList : productPPValues) {     
      Map<String, Double> valueMap = new HashMap<>();
      for (int i = 0; i < valueList.size(); i++) {
        valueMap.put(agent.getParentAndPseudoStrList().get(i), valueList.get(i));
      }
      
      double maxUtil = agentViewTable.maxArgmaxHybrid(valueMap, agent.getSelfInterval().getMidPointInHalfIntegerRanges())[0];
      
      RowDouble newRow = new RowDouble(valueList, maxUtil);

      // Add the utility of all functions to the row
      utilTable.addRow(newRow);
    }
    return utilTable;
  }
  
  private Set<List<Double>> kmeanCluster(Set<List<Double>> dataset, int numClusters) {
    Set<List<Double>> centroids = new HashSet<>();
    SimpleKMeans kmean = new SimpleKMeans();
    
    ArrayList<Attribute> attritbuteList = new ArrayList<Attribute>();
    int numberAttributes = dataset.iterator().next().size();
    for (int i = 1; i <= numberAttributes; i++) {
      attritbuteList.add(new Attribute(String.valueOf(i)));
    }

    Instances instances = new Instances("cluster", attritbuteList, dataset.size());
    for (List<Double> rawData : dataset) {
      Instance pointInstance = new SparseInstance(1.0, rawData.stream().mapToDouble(Double::valueOf).toArray());
      instances.add(pointInstance);
    }
    
    try {
      kmean.setNumClusters(agent.getNumberOfPoints());
      kmean.buildClusterer(instances);
      
      Instances centroidsInstance = kmean.getClusterCentroids();
      
      for (Instance pointArray : centroidsInstance) {
        
        List<Double> centroidList = new ArrayList<>();
        for (double value : pointArray.toDoubleArray()) {
          centroidList.add(value);
        }
        centroids.add(centroidList);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return centroids;
  }
}
