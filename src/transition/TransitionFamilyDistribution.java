package transition;

import java.util.Objects;

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
  
  /**
   * Compute the new Beta distribution based on the current Alpha value, and the computed Beta value from the alpha and mean <br>
   * @param mean
   * @return
   */
  public BetaDistribution computeBetaDistribution(double mean) {
    double beta = computeBetaFromAlphaAndMean(alpha, mean);
    
    return new BetaDistribution(alpha, beta);
  }
  
  /**
   * mean = alpha / (alpha + beta) <br>
   * => beta = (alpha - alpha * mean) / mean 
   * @param alpha
   * @param mean
   * @return
   */
  public double computeBetaFromAlphaAndMean(double alpha, double mean) {
      return (alpha - alpha * mean) / mean;
  }

  @Override
  public int hashCode() {
    return Objects.hash(alpha);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof TransitionFamilyDistribution))
      return false;
    TransitionFamilyDistribution other = (TransitionFamilyDistribution) obj;
    return Double.doubleToLongBits(alpha) == Double.doubleToLongBits(other.alpha);
  }

  @Override
  public String toString() {
    return "TransitionFamilyDistribution [alpha=" + alpha + "]";
  }
}
