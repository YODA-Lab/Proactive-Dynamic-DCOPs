package behavior;

import jade.core.behaviours.OneShotBehaviour;

import java.util.List;

import agent.AgentPDDCOP;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class MGM_RAND_PICK_VALUE extends OneShotBehaviour {

  private static final long serialVersionUID = -6711542619242113965L;

  private AgentPDDCOP agent;
  
  private int timeStep;
  
  public MGM_RAND_PICK_VALUE(AgentPDDCOP agent, int timeStep) {
    super(agent);
    this.agent = agent;
    this.timeStep = timeStep;
  }
  
  @Override
  public void action() {
    agent.startSimulatedTiming();
    
    List<String> domain = agent.getDecisionVariableDomainMap().get(agent.getAgentID());
    
    agent.getChosenValueAtEachTSMap().put(timeStep, domain.get(agent.getRandom().nextInt(domain.size())));
    
    agent.print("choose random value at time step " + timeStep + ": " + agent.getChosenValueAtEachTSMap().get(timeStep));
    
    agent.stopStimulatedTiming();
  }
}