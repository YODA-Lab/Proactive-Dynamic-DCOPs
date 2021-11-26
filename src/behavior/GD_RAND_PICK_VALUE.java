package behavior;

import jade.core.behaviours.OneShotBehaviour;
import agent.AgentPDDCOP;

/**
 * REVIEWED
 * @author khoihd
 *
 */
public class GD_RAND_PICK_VALUE extends OneShotBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = 684241664835207245L;
  AgentPDDCOP agent;
  
  private final int timeStep;
  
  public GD_RAND_PICK_VALUE(AgentPDDCOP agent, int timeStep) {
    super(agent);
    this.agent = agent;
    this.timeStep = timeStep;
  }
  
  @Override
  public void action() {
    agent.startSimulatedTiming();
    
    double lower = agent.getSelfInterval().getLowerBound();
    double upper = agent.getSelfInterval().getUpperBound();

    double randomSample = lower + agent.getRandomGenerator().nextDouble() * (upper - lower);
    agent.getChosenDoubleValueAtEachTSMap().put(timeStep, randomSample);

    agent.print("Chosen random values=" + agent.getChosenDoubleValueAtEachTSMap());

    agent.stopSimulatedTiming();
  }
}