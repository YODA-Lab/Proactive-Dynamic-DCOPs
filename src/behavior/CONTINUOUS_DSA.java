package behavior;

import static agent.DcopConstants.DSA_PROBABILITY;
import static agent.DcopConstants.MessageType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import agent.AgentPDDCOP;
import function.multivariate.PiecewiseMultivariateQuadFunction;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CONTINUOUS_DSA extends OneShotBehaviour {
  /**
   * 
   */
  private static final long serialVersionUID = 5573433680877118333L;

  private AgentPDDCOP agent;
  
  private final int currentTimeStep;
  
  private final int localSearchIteration;
  
  private Map<String, PiecewiseMultivariateQuadFunction> functionMap = new HashMap<>();

  public CONTINUOUS_DSA(AgentPDDCOP agent, int timeStep, int iteration) {
    super(agent);
    this.agent = agent;
    this.currentTimeStep = timeStep;
    this.localSearchIteration = iteration;
  }

  @Override
  public void action() {
    agent.startSimulatedTiming();
    
    for (String neighbor : agent.getNeighborStrSet()) {
      functionMap.put(neighbor, agent.getNeighborFunctionMap().get(neighbor));
    }
    
    // Compute expected function if any and add the expected function to the dpopFuncionList
    if (agent.hasRandomFunction()) {
      computeExpectedFunctionCurrentTimeStep(currentTimeStep);
      functionMap.put(agent.getRandomVariable(), agent.getExpectedFunction(currentTimeStep));
    }
    
    // Add switching cost function to the function list
    PiecewiseMultivariateQuadFunction swFunc = agent.computeSwitchingCostDiscountedFunction(currentTimeStep, agent.getPDDCOP_Algorithm(), agent.SWITCHING_TYPE);
    if (swFunc != null) {
      functionMap.put(agent.getLocalName(), swFunc);
    }
    
    // Initialize value for this time step if this is the first iteration
    if (localSearchIteration == 0) {
      // Randomize initial values here
      double randomValue = agent.getSelfInterval().randomDouble(agent.getRandomGenerator());
      agent.setChosenDoubleValueAtEachTimeStep(currentTimeStep, randomValue);
    }
    
    agent.stopSimulatedTiming();
    
    // send the current value to neighbors
    for (AID neighborAID : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithIteration(neighborAID, getCurrentValue(),
          MessageType.DSA_VALUE, localSearchIteration, agent.getSimulatedTime());
    }

    PiecewiseMultivariateQuadFunction combinedFunction = new PiecewiseMultivariateQuadFunction();

    Map<String, Double> neighborValueMap = waitingForMessageFromNeighborWithTime(MessageType.DSA_VALUE, localSearchIteration);
    
    agent.startSimulatedTiming();
    
    for (PiecewiseMultivariateQuadFunction function : functionMap.values()) {
      combinedFunction = combinedFunction.addPiecewiseFunction(function);
    }
        
    agent.debug(agent.getNeighborStrSet().toString());
    agent.debug(combinedFunction.toString());
    agent.debug(neighborValueMap.toString());
    double chosenValue = combinedFunction.getArgmax(agent.getLocalName(), neighborValueMap);
    
    // Found new value
    // Choose which DSA version?
    if (Double.compare(chosenValue, getCurrentValue()) != 0) {
      if (Double.compare(new Random().nextDouble(), DSA_PROBABILITY) <= 0) {
        setCurrentValue(chosenValue);
        agent.print("Iteration " + localSearchIteration + " changes to a better value " + chosenValue);
      } else {
        agent.print("Iteration " + localSearchIteration + " could change to a better value " + chosenValue
            + ", but it decides to remain the value " + getCurrentValue());
      }
    } 
    // Can't find better value
    else {
      agent.print("Iteration " + localSearchIteration + " doesn't find a better value and remains " + getCurrentValue());
    }
    
    agent.stopSimulatedTiming();
  }
  
  private Double getCurrentValue() {
    return agent.getChosenDoubleValueAtEachTimeStep(currentTimeStep);
  }
  
  private void setCurrentValue(double value) {
    agent.setChosenDoubleValueAtEachTimeStep(currentTimeStep, value);
  }
  
  private Map<String, Double> waitingForMessageFromNeighborWithTime(MessageType msgType, int iteration) {    
    int msgCode = msgType.ordinal();
    
    Map<String, Double> valueMap = new HashMap<>();
    
    while (valueMap.size() < agent.getNeighborAIDSet().size()) {
      MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(msgCode), MessageTemplate.MatchConversationId(String.valueOf(iteration)));      
      ACLMessage receivedMessage = myAgent.blockingReceive(template);
        try {
          String sender = receivedMessage.getSender().getLocalName();
          Double content = (Double) receivedMessage.getContentObject();
          int msgIteration = Integer.valueOf(receivedMessage.getConversationId());
          
          // Matching iteration
          if (msgIteration != iteration) {
            continue;
          }
          
          if (!valueMap.containsKey(sender)) {
            valueMap.put(sender, content);
          }
          
          long timeFromReceiveMessage = Long.parseLong(receivedMessage.getLanguage());

          if (timeFromReceiveMessage > agent.getSimulatedTime()) {
            agent.setSimulatedTime(timeFromReceiveMessage);
          }
          
          agent.print("Iteration " + localSearchIteration + " receives " + receivedMessage.getContentObject() + " from "
              + receivedMessage.getSender().getLocalName());

        } catch (UnreadableException e) {
          e.printStackTrace();
        }        
    }
    
    return valueMap;
  }
  
  /**
   * Compute the expected function (if any) at current time step
   * 
   * THIS FUNCTION HAS TO BE CALLED AFTER THE PSEUDOTREE_GENERATION behavior has been executed
   */
  private void computeExpectedFunctionCurrentTimeStep(int timeStep) {
    String randomVariable = agent.getRandomVariable(); 
    if (randomVariable != null) {
      PiecewiseMultivariateQuadFunction randomFunction = agent.getNeighborFunctionMap().get(randomVariable);
      
      Map<String, Double> randomValueMap = new HashMap<>();
      double distributionMean = agent.getMeanAtEveryTimeStep().get(timeStep);
      randomValueMap.put(randomVariable, distributionMean);

      agent.getExpectedFunctionMap().put(timeStep, randomFunction.evaluateToUnaryFunction(randomValueMap));
    }
  }   
}
