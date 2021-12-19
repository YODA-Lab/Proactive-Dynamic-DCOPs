package behavior;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import agent.AgentPDDCOP;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import maxsum.MaxSumMessage;

import agent.DcopConstants.DcopAlgorithm;
import agent.DcopConstants.PDDcopAlgorithm;
import function.multivariate.PiecewiseMultivariateQuadFunction;

import static agent.DcopConstants.MessageType;
import static agent.DcopConstants.GRADIENT_SCALING_FACTOR;

/**
 * @author khoihd
 *
 */
public class MAXSUM_VARIABLE_TO_FUNCTION extends OneShotBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = -6435195074924409292L;
  
  private AgentPDDCOP agent;
  
  private final int currentTimeStep;
  
  private final int iteration;
  
  public MAXSUM_VARIABLE_TO_FUNCTION(AgentPDDCOP agent, int timeStep, int iteration) {
    super(agent);
    this.agent = agent;
    this.currentTimeStep = timeStep;
    this.iteration = iteration;
  }

  @Override
  public void action() {    
    /* PSEUDO-CODE
     * *****************
     * If the iteration == 0:
     *  Initialize the VAR_TO_FUNC message to 0s
     *  Initialize the newValueSet with currentValueSet
     *  Send VARIABLE_TO_FUNCTION messages to agents in functionOwnedByOther
     *  Store VARIABLE_TO_FUNCTION messages to agents in stored_VARIABLE_TO_FUNCTION
     * ElseIf the iteration != 0:
     *  Update the valueSet based on the firstDerivative of all FUNCTION_TO_VARIABLE messages
     *  
     *  Now agents create the VARIABLE_TO_FUNCTION message
     *  For each neighbor
     *    Retrieve FUNCTION_TO_VARIABLE messages from received_FUNCTION_TO_VARIABLE (except the neighbor)
     *    Retrieve FUNCTION_TO_VARIABLE messages from stored_FUNCTION_TO_VARIABLE (except the neighbor)
     *    Calculate the alpha for that function_to_send
     *    Create the VARIABLE_TO_FUNCTION messages, and update alpha values
     *    If (neighbor is in getFunctionOwnedByOther)
     *      Send the VARIABLE_TO_FUNCTION message
     *    Else if (neighbor is in getFunctionIOwn)
     *      Store the VARIABLE_TO_FUNCTION message
     *    End
     *  End
     * End
     * For each agent in functionIOwn
     *  Waiting for VARIABLE_TO_FUNCTION messages from getFunctionIOwn
     *  Store the VARIABLE_TO_FUNCTION messages to RECEIVED_VARIABLE_TO_FUNCTION map
     * End
     * 
     * TODO: clear the function to message map
     */
    
    // Initialize the message to 0 for all agent in agentKeepMyFunctionAIDSet
    // Set newValues same to old values at the first iteration
    if (iteration == 0) {
      agent.startSimulatedTiming();
      
      agent.computeExpectedFunctionCurrentTimeStep(currentTimeStep);
      
      // Initialize the message and set initial value set
      MaxSumMessage msgVAR_TO_FUNC = new MaxSumMessage(agent.getCurrentDiscreteValues(currentTimeStep));
      msgVAR_TO_FUNC.setNewValueSet(agent.getCurrentDiscreteValues(currentTimeStep));
      
      // Update VAR_TO_FUNC message with expected function
      if (agent.hasRandomFunction()) {
        updateMSmessageWithExpectedAndCostFunction(msgVAR_TO_FUNC, agent.getExpectedFunction(currentTimeStep));
      }

      // Update VAR_TO_FUNC message with switching cost function
      if (agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.FORWARD || agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.BACKWARD) {
        PiecewiseMultivariateQuadFunction swFunction = agent.computeSwitchingCostDiscountedFunction(currentTimeStep, agent.getPDDCOP_Algorithm(), agent.getSwitchingCost(), agent.SWITCHING_TYPE);
        updateMSmessageWithExpectedAndCostFunction(msgVAR_TO_FUNC, swFunction);
      }
      
      agent.stopSimulatedTiming();
      
      for (AID receiver : agent.getNeighborFunctionOwnedByOther()) {
        agent.sendObjectMessageWithTime(receiver, msgVAR_TO_FUNC, MessageType.VAR_TO_FUNC, agent.getSimulatedTime());
      }
      for (AID store_agent : agent.getNeighborFunctionOwnedByMe()) {
        agent.getStored_VARIABLE_TO_FUNCTION().put(store_agent, msgVAR_TO_FUNC);
      }
      
      // Store self initial values to agent itself
//      if (agent.hasRandomFunction() || agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.FORWARD || agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.BACKWARD) {
//        agent.getStored_VARIABLE_TO_FUNCTION().put(agent.getAID(), msgVAR_TO_FUNC);
//      }
    } // end the if (iteration == 0)
    else {
     /*
      * For each neighbor
      *   Retrieve FUNCTION_TO_VARIABLE messages from received_FUNCTION_TO_VARIABLE (except the neighbor)
      *   Retrieve FUNCTION_TO_VARIABLE messages from stored_FUNCTION_TO_VARIABLE (except the neighbor)
      *   Calculate the alpha for that function_to_send
      *   Create the VARIABLE_TO_FUNCTION messages, and update alpha values
      *   If (neighbor is in getFunctionOwnedByOther)
      *     Send the VARIABLE_TO_FUNCTION message
      *   Else if (neighbor is in getFunctionIOwn)
      *     Store the VARIABLE_TO_FUNCTION message
      *   End
      * End
      */
      agent.startSimulatedTiming();
      
      MaxSumMessage msgVAR_TO_FUNC = new MaxSumMessage(agent.getCurrentDiscreteValues(currentTimeStep));
      
      if (agent.getDcop_algorithm() == DcopAlgorithm.HYBRID_MAXSUM) {
        modifyMSValuesUsingGradient(); 
      }
      
      agent.stopSimulatedTiming();
      
      // For each neighbor, process func-to-var message except for this neighbor
      for (AID neighbor : agent.getNeighborAIDSet()) {
        // process the function that is OWNED BY OTHER AGENTS
        if (agent.getNeighborFunctionOwnedByOther().contains(neighbor)) {
          agent.startSimulatedTiming();
          
          // Add all messages from getReceived_FUNCTION_TO_VARIABLE except for this neighbor
          // The value set from those messages are identical 
          for (Entry<AID, MaxSumMessage> msgEntry : agent.getReceived_FUNCTION_TO_VARIABLE().entrySet()) {
            if (neighbor.equals(msgEntry.getKey())) {continue;}
            msgVAR_TO_FUNC = msgVAR_TO_FUNC.addMessage(msgEntry.getValue());
          }
          
          // add all messages from getStored_FUNCTION_TO_VARIABLE
          msgVAR_TO_FUNC = msgVAR_TO_FUNC.addAllMessages(agent.getStored_FUNCTION_TO_VARIABLE().values());
          
          // Update VAR_TO_FUNC message with expected function
          if (agent.hasRandomFunction()) {
            updateMSmessageWithExpectedAndCostFunction(msgVAR_TO_FUNC, agent.getExpectedFunction(currentTimeStep));
          }

          // Update VAR_TO_FUNC message with switching cost function
          if (agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.FORWARD || agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.BACKWARD) {
            PiecewiseMultivariateQuadFunction swFunction = agent.computeSwitchingCostDiscountedFunction(currentTimeStep, agent.getPDDCOP_Algorithm(), agent.getSwitchingCost(), agent.SWITCHING_TYPE);
            updateMSmessageWithExpectedAndCostFunction(msgVAR_TO_FUNC, swFunction);
          }
          
          msgVAR_TO_FUNC.updateAlphaAndValuesForGraph();
          
          msgVAR_TO_FUNC.setNewValueSet(agent.getCurrentDiscreteValues(currentTimeStep));

          agent.stopSimulatedTiming();

          agent.sendObjectMessageWithTime(neighbor, msgVAR_TO_FUNC, MessageType.VAR_TO_FUNC, agent.getSimulatedTime());
        } 
        // process the function that I owned
        else if (agent.getNeighborFunctionOwnedByMe().contains(neighbor)) {
          agent.startSimulatedTiming();
          
          // add all messages from getReceived_FUNCTION_TO_VARIABLE
          msgVAR_TO_FUNC = msgVAR_TO_FUNC.addAllMessages(agent.getReceived_FUNCTION_TO_VARIABLE().values());

          // Except for the neighbor, add messages from getStored_FUNCTION_TO_VARIABLE
          for (Entry<AID, MaxSumMessage> msgEntry : agent.getStored_FUNCTION_TO_VARIABLE().entrySet()) {
            if (neighbor.equals(msgEntry.getKey())) {continue;}
            msgVAR_TO_FUNC = msgVAR_TO_FUNC.addMessage(msgEntry.getValue());
          }
          
          // Update VAR_TO_FUNC message with expected function
          if (agent.hasRandomFunction()) {
            updateMSmessageWithExpectedAndCostFunction(msgVAR_TO_FUNC, agent.getExpectedFunction(currentTimeStep));
          }

          // Update VAR_TO_FUNC message with switching cost function
          if (agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.FORWARD || agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.BACKWARD) {
            PiecewiseMultivariateQuadFunction swFunction = agent.computeSwitchingCostDiscountedFunction(currentTimeStep, agent.getPDDCOP_Algorithm(), agent.getSwitchingCost(), agent.SWITCHING_TYPE);
            updateMSmessageWithExpectedAndCostFunction(msgVAR_TO_FUNC, swFunction);
          }
          
          msgVAR_TO_FUNC.updateAlphaAndValuesForGraph();
          msgVAR_TO_FUNC.setNewValueSet(agent.getCurrentDiscreteValues(currentTimeStep));
          
          agent.getStored_VARIABLE_TO_FUNCTION().put(neighbor, msgVAR_TO_FUNC);
          
          agent.stopSimulatedTiming();
        }
      }
    }
    
    waiting_store_VAR_TO_FUNC_message_with_time(MessageType.VAR_TO_FUNC);    
  }

  /**
   * THIS FUNCTION HAS BEEN REVIEWED <br>
   * 
   * This function takes into account expected and switching cost function <br>
   * 
   *  From all first derivative from FUNCTION_TO_VALUES messages
   *  Add those messages up and modify the values accordingly
   */
  private void modifyMSValuesUsingGradient() { 
    agent.print("Before moving MS values: " + agent.getCurrentDiscreteValues(currentTimeStep));
    
    double scalingFactor = GRADIENT_SCALING_FACTOR;
    
    Set<Double> newValueSet = new HashSet<>();
    
    for (double oldValue : agent.getCurrentDiscreteValues(currentTimeStep)) {
      double sumGradient = 0;
      for (AID neighbor : agent.getNeighborAIDSet()) {
        if (agent.getNeighborFunctionOwnedByOther().contains(neighbor)) {
          sumGradient = sumGradient + agent.getReceived_FUNCTION_TO_VARIABLE().get(neighbor).getFirstDerivativeMap().get(oldValue);
        }
        else if (agent.getNeighborFunctionOwnedByMe().contains(neighbor)) {
          sumGradient = sumGradient + agent.getStored_FUNCTION_TO_VARIABLE().get(neighbor).getFirstDerivativeMap().get(oldValue);
        }
      }
      
      // Take into account the gradient of expected function
      if (agent.hasRandomFunction()) {
        Map<String, Double> valueMap = new HashMap<>();
        valueMap.put(agent.getLocalName(), oldValue);
        
        PiecewiseMultivariateQuadFunction randomFunction = agent.getExpectedFunction(currentTimeStep);
        sumGradient += randomFunction.getTheFirstFunction().takeFirstPartialDerivative(agent.getLocalName(), agent.getLocalName(), "").evaluateToValueGivenValueMap(valueMap);
      }
      
      // Take into account the gradient of switching cost function
      if (agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.FORWARD || agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.BACKWARD) {
        Map<String, Double> valueMap = new HashMap<>();
        valueMap.put(agent.getLocalName(), oldValue);
        
        PiecewiseMultivariateQuadFunction swFunction = agent.computeSwitchingCostDiscountedFunction(currentTimeStep, agent.getPDDCOP_Algorithm(), agent.getSwitchingCost(), agent.SWITCHING_TYPE);
        if (swFunction != null) {
          sumGradient += swFunction.getTheFirstFunction().takeFirstPartialDerivative(agent.getLocalName(), agent.getLocalName(), "").evaluateToValueGivenValueMap(valueMap);
        }
      }
      
      // Taking into account switching cost and expected table here
      double newValue = oldValue + scalingFactor * sumGradient;
      
      // Add new value only if it's in the interval
      if (agent.getSelfInterval().contains(newValue)) {
        newValueSet.add(newValue);
      } else {
        newValueSet.add(oldValue);
      }
    }
    
    agent.setCurrentDiscreteValues(currentTimeStep, newValueSet);
           
    agent.print("After moving MS values: " + agent.getCurrentDiscreteValues(currentTimeStep));
  }

  private void waiting_store_VAR_TO_FUNC_message_with_time(MessageType msgType) {
    agent.startSimulatedTiming();
    
    int msgCode = msgType.ordinal();
        
    int msgCount = 0;    
    while (msgCount < agent.getNeighborFunctionOwnedByMe().size()) {
      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
      ACLMessage receivedMessage = myAgent.blockingReceive(template);

      MaxSumMessage maxsumMsg = null;
      try {
        maxsumMsg = (MaxSumMessage) receivedMessage.getContentObject();
        
        long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());
        if (timeFromReceiveMessage > agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime()) {
          agent.setSimulatedTime(timeFromReceiveMessage);
        } 
        else {
          agent.setSimulatedTime(agent.getSimulatedTime() + agent.getBean().getCurrentThreadUserTime() - agent.getCurrentStartTime());
        }
      } catch (UnreadableException e) {
        e.printStackTrace();
      }
      agent.getReceived_VARIABLE_TO_FUNCTION().put(receivedMessage.getSender(), maxsumMsg);
      msgCount++;
    }
    
    agent.stopSimulatedTiming();
  }
  
  public void updateMSmessageWithExpectedAndCostFunction(MaxSumMessage msgVAR_TO_FUNC, PiecewiseMultivariateQuadFunction function) {        
    if (function == null) {
      return ;
    }
    
    for (Entry<Double, Double> utilityEntry : msgVAR_TO_FUNC.getValueUtilityMap().entrySet()) {
      double value = utilityEntry.getKey();
      double utility = utilityEntry.getValue();

      Map<String, Double> valueMap = new HashMap<>();
      valueMap.put(agent.getLocalName(), value);
      
      double evaluation = function.getTheFirstFunction().evaluateToValueGivenValueMap(valueMap);
      utilityEntry.setValue(utility + evaluation);
    }
  }
}
