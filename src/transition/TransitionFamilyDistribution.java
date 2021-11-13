package transition;

import org.apache.commons.math3.distribution.BetaDistribution;

public class TransitionFamilyDistribution {
  private final double alpha;
  
  public static TransitionFamilyDistribution of() {
    return new TransitionFamilyDistribution(0);
  }
  
  public static TransitionFamilyDistribution of(double alpha) {
    return new TransitionFamilyDistribution(alpha);
  }
  
  private TransitionFamilyDistribution(double alpha) {
    this.alpha = alpha;
  }

  public double getAlpha() {
    return alpha;
  }
  
  public double computeBetaAndSample(double mean) {
    BetaDistribution betaDist = computeBetaDistribution(mean);
    return betaDist.sample();
  }
  
  public BetaDistribution computeBetaDistribution(double mean) {
    double beta = computeBeta(mean);
    
    return new BetaDistribution(alpha, beta);
  }
  
  // TODO: Compute mean
  public double computeBeta(double mean) {
      return 0;
  }
}
