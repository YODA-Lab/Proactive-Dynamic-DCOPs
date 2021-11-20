package behavior;

import static agent.DcopConstants.DSA_VALUE;
import static agent.DcopConstants.NOT_TO_OPTIMIZE_INTERVAL;
import static agent.DcopConstants.DSA_PROBABILITY;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import agent.AgentPDDCOP;
import agent.DcopConstants.PDDcopAlgorithm;
import agent.DcopConstants.SwitchingType;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;
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
    functionMap.putAll(agent.getFunctionWithPParentMap());
    // Compute expected function if any and add the expected function to the dpopFuncionList
    if (agent.hasRandomFunction()) {
      computeExpectedFunctionCurrentTimeStep(currentTimeStep);
      functionMap.put(agent.getRandomVariable(), agent.getExpectedFunction(currentTimeStep));
    }
    
    // Add switching cost function / constraint to the appropriate map or list
    if (agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.FORWARD) {
      addSwitchingCostFunction(currentTimeStep - 1);
    }
    else if (agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.BACKWARD) {
      addSwitchingCostFunction(currentTimeStep + 1);
    }
    // Do not add switching cost function to LS when running EC, AC or CAC-DPOP
    else if (agent.getPDDCOP_Algorithm() == PDDcopAlgorithm.LS_SDPOP) {
      agent.print();
    }
    
    // Initialize value for this time step if this is the first iteration
    if (localSearchIteration == 0) {
      // Randomize initial values here
      Interval agentDomain = agent.getSelfInterval();
      agent.setChosenDoubleValueAtEachTimeStep(currentTimeStep, agentDomain.randomDouble());
    }
    
    // send the current value to neighbors
    for (AID neighborAID : agent.getNeighborAIDSet()) {
      agent.sendObjectMessageWithIteration(neighborAID, agent.getChosenValueAtEachTimeStep(currentTimeStep),
          DSA_VALUE, localSearchIteration, agent.getSimulatedTime());
    }

    PiecewiseMultivariateQuadFunction combinedFunction = new PiecewiseMultivariateQuadFunction();

    Map<String, Double> neighborValueMap = waitingForMessageFromNeighborWithTime(DSA_VALUE, localSearchIteration);
    
    for (PiecewiseMultivariateQuadFunction function : functionMap.values()) {
      combinedFunction = combinedFunction.addPiecewiseFunction(function);
    }
        
    double chosenValue = combinedFunction.getArgmax(this.getAgent().getLocalName(), neighborValueMap);
    
    // Found new value
    // Choose which DSA version?
    if (Double.compare(chosenValue, getCurrentValue()) != 0) {
      if (Double.compare(new Random().nextDouble(), DSA_PROBABILITY) <= 0) {
        setCurrentValue(chosenValue);
        System.out.println("Iteration " + localSearchIteration + " Agent " + agent.getLocalName() + " changes to a better value " + chosenValue);
      } else {
        System.out.println("Iteration " + localSearchIteration + " Agent " + agent.getLocalName() + " could change to a better value " + chosenValue
            + ", but it decides to remain the value " + getCurrentValue());
      }
    } 
    // Can't find better value
    else {
      System.out.println("Iteration " + localSearchIteration + " Agent " + agent.getLocalName() + " doesn't find a better value and remains " + getCurrentValue());
    }    
  }
  
  private Double getCurrentValue() {
    return Double.valueOf(agent.getChosenValueAtEachTimeStep(currentTimeStep));
  }
  
  private void setCurrentValue(double value) {
    agent.setChosenValueAtEachTimeStep(currentTimeStep, String.valueOf(value));
  }
  
  //TODO: Review the simulated runtime
  private Map<String, Double> waitingForMessageFromNeighborWithTime(int msgCode, int iteration) {
    // Start of waiting time for the message
    agent.startSimulatedTiming();    
    
    Map<String, Double> valueMap = new HashMap<>();
    
    while (valueMap.size() < agent.getNeighborAIDSet().size()) {
      
      MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchPerformative(msgCode), MessageTemplate.MatchConversationId(String.valueOf(iteration)));      
      ACLMessage receivedMessage = myAgent.blockingReceive(template);
        
        try {
          String sender = receivedMessage.getSender().getLocalName();
          Double content = (Double) receivedMessage.getContentObject();
          
          if (!valueMap.containsKey(sender)) {
            valueMap.put(sender, content);
          }
          
          System.out.println("Iteration " + localSearchIteration + " Agent " + agent.getLocalName() + " receives " + receivedMessage.getContentObject() + " from "
              + receivedMessage.getSender().getLocalName());

        } catch (UnreadableException e) {
          e.printStackTrace();
        }        
//      } else
//        block();
    }
    
    return valueMap;
  }
  
  private void addSwitchingCostFunction(int timeStep) {
    if (timeStep < 0 || timeStep > agent.getHorizon()) {
      return ;
    }
    
    String prevValue = agent.getChosenValueAtEachTimeStep(timeStep);
    PiecewiseMultivariateQuadFunction swFunc = switchingCostFunctionGivenSolution(agent.getLocalName(), prevValue, AgentPDDCOP.SWITCHING_TYPE);
    functionMap.put(agent.getLocalName(), swFunc);
  }
  
  private PiecewiseMultivariateQuadFunction switchingCostFunctionGivenSolution(String agentName, String value, SwitchingType type) {
    PiecewiseMultivariateQuadFunction swFunction = new PiecewiseMultivariateQuadFunction();
    Map<String, Interval> intervalMap = new HashMap<>();
    intervalMap.put(agent.getLocalName(), agent.getSelfInterval());
    
    MultivariateQuadFunction func = MultivariateQuadFunction.switchingCostFunction(agentName, value, type);
    swFunction.addToFunctionMapWithInterval(func, intervalMap, NOT_TO_OPTIMIZE_INTERVAL);
    
    return swFunction;
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
