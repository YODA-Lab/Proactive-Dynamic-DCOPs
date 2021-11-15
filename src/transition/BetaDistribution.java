package transition;

import org.apache.commons.math3.random.RandomGenerator;

public class BetaDistribution extends org.apache.commons.math3.distribution.BetaDistribution {

  /**
   * 
   */
  private static final long serialVersionUID = 4202822584423103360L;

  public BetaDistribution(double alpha, double beta) {
    super(alpha, beta);
  }

  public BetaDistribution(RandomGenerator rg, double alpha, double beta) {
    super(rg, alpha, beta);
  }

  @Override
  public String toString() {
    return "BetaDistribution [alpha=" + getAlpha() + ", beta=" + getBeta() + "]";
  }
}
