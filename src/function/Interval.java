package function;

import static java.lang.Double.*;

import java.io.Serializable;
import java.util.*;

/**
 * This class is IMMUTABLE with all private fields and no setter function
 * @author khoihd
 */
public class Interval implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 803822839716215962L;
  private double lowerBound;
  private double upperBound;

  public Interval() {
  }

  /**
   * Copied constructor
   * 
   * @param object
   *          the object to be copied
   */
  public Interval(Interval object) {
    this();
    this.lowerBound = object.getLowerBound();
    this.upperBound = object.getUpperBound();
  }

  public Interval(double lowerBound, double upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  public double getMidValue() {
    return 0.5 * (lowerBound + upperBound);
  }
  
  public double getLowerBound() {
    return lowerBound;
  }

  public double getUpperBound() {
    return upperBound;
  }

  /**
   * This function is TESTED
   * @param numberOfIntervals
   *          the number of intervals, size of the returned list
   * @return a list of separated intervals in the ascending order
   */
  public List<Interval> separateIntoAListOfIncreasingIntervals(int numberOfIntervals) {
    List<Interval> intervalList = new ArrayList<>();
    double increment = (upperBound - lowerBound) / numberOfIntervals;
    double currentLB = lowerBound;
    for (int i = 0; i < numberOfIntervals; i++) {
      intervalList.add(new Interval(currentLB, currentLB + increment));
      currentLB += increment;
    }
    return intervalList;
  }
  
  public boolean isTotallyBigger(Interval another) {
    return compare(lowerBound, another.getLowerBound()) <= 0 &&
        compare(upperBound, another.getUpperBound()) >= 0;
  }
  
  /**
   * Get the intersection of two intervals
   * @param otherInterval
   * @return
   */
  public Interval intersectInterval(Interval otherInterval) {
    
    double LB1; // = interval1.getLowerBound();
    double UB1; // = interval1.getUpperBound();
    double UB2; // = otherInterval.getUpperBound();
    
    // Pre-condition: LB2 <= LB1
    if (compare(otherInterval.getLowerBound(), lowerBound) <= 0) {
      LB1 = lowerBound;
      UB1 = upperBound;
      UB2 = otherInterval.getUpperBound();  
    } else {
      LB1 = otherInterval.getLowerBound();
      UB1 = otherInterval.getUpperBound();
      UB2 = upperBound;
    }
    
    // Pre-condition: LB2 <= LB1

    // [LB2, UB2] < [LB1, UB1] 
    if (compare(UB2, LB1) < 0) {
      return null;
    }
    
    // LB2 <= LB1 <= UB2 <= UB1
    //        LB1 <= UB2 <= UB1
    if (compare(LB1, UB2) <= 0 && compare(UB2, UB1) <= 0) {
      return new Interval(LB1, UB2);
    }
    
    // LB2 <= LB1 <= UB1 <= UB2 
    //        LB1 <= UB1 <= UB2 
    if (compare(UB1, UB2) < 0) {
      return new Interval(LB1, UB1);
    }
    
    return null;
  }
  
  public boolean contains(double value) {
    return compare(lowerBound, value) <= 0 && compare(value, upperBound) <= 0;
  }


  @Override
  public String toString() {
    return "[" + lowerBound + ", " + upperBound + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;

    if (!(o instanceof Interval)) {
      return false;
    }
    Interval interval = (Interval) o;
    return compare(this.lowerBound, interval.getLowerBound()) == 0
        && compare(this.upperBound, interval.getUpperBound()) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(lowerBound, upperBound);
  }

  public int getIncrementRange() {
    return (int) (upperBound - lowerBound);
  }
  
  public Set<Double> getIntegerValues() {
    Set<Double> valueSet = new HashSet<>();
    for (double value = lowerBound; compare(value, upperBound) <=0; value++) {
      valueSet.add(value);
    }
    return valueSet;
  }
  
  public Set<String> getMidPointInIntegerRanges() {
    return getMidPoints(getIncrementRange());
  }
  
  public Set<String> getMidPointInHalfIntegerRanges() {
    return getMidPoints(getIncrementRange() * 2);
  }
  
  public Set<String> getMidPointInQuarterIntegerRanges() {
    return getMidPoints(getIncrementRange() * 4);
  }
  
  public Set<String> getMidPointInEightIntegerRanges() {
    return getMidPoints(getIncrementRange() * 8);
  }
  
  public Set<Double> getNonMidPoint(int numberOfValues) {
    Set<Double> valueSet = new HashSet<>();
    double increment = (upperBound - lowerBound) / (numberOfValues - 1); //1, 5.5, 10
        
    double currentLB = lowerBound;
    for (int i = 0; i < numberOfValues; i++) {
      valueSet.add(currentLB + i * increment);
    }
    
    return valueSet;
  }
  
  public Set<String> getMidPoints(int numberOfValues) {
    Set<String> valueSet = new HashSet<>();
    
    double increment = (upperBound - lowerBound) / numberOfValues;
    double currentLB = lowerBound;
    for (int i = 0; i < numberOfValues; i++) {
      double value = 0.5 * (currentLB * 2 + increment);
      valueSet.add(String.valueOf(value));
      currentLB += increment;
    }
    
    return valueSet;
  }
}
