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
  
  public GD_RAND_PICK_VALUE(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }
  
  @Override
  public void action() {
    agent.startSimulatedTiming();
    
    for (int ts = 0; ts <= agent.getHorizon(); ts++) {
      double lower = agent.getDecisionVariableIntervalMap().get(agent.getAgentID()).getLowerBound();
      double upper = agent.getDecisionVariableIntervalMap().get(agent.getAgentID()).getUpperBound();

      double randomSample = lower + Math.random() * (upper - lower);
      agent.getChosenValueAtEachTSMap().put(ts, String.valueOf(randomSample));
    }

    agent.print("Chosen random values=" + agent.getChosenDoubleValueAtEachTSMap());

    
    agent.stopSimulatedTiming();
  }
}