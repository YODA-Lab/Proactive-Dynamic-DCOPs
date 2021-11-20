package maxsum;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.lang.Double.*;

public class MaxSumMessage implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -7715529313359092720L;
  
  private Map<Double, Double> valueUtilityMap = new HashMap<>();
  private Set<Double> newValueSet = new HashSet<>();
  private Map<Double, Double> firstDerivativeMap = new HashMap<>();

  public MaxSumMessage() {
  }
  
  public MaxSumMessage(MaxSumMessage object) {
    this(object.getValueUtilityMap(), object.getNewValueSet(), object.getFirstDerivativeMap());
  }
  
  public MaxSumMessage(Map<Double, Double> valueUtilityMap) {
    this.valueUtilityMap.putAll(valueUtilityMap);
  }
  
  public MaxSumMessage(Map<Double, Double> valueUtilityMap, Set<Double> valueSet) {
    this.valueUtilityMap.putAll(valueUtilityMap);
    this.newValueSet.addAll(valueSet);
  }
  
  public MaxSumMessage(Map<Double, Double> valueUtilityMap, Set<Double> valueSet, Map<Double, Double> firstDerivativeMap) {
    this.valueUtilityMap.putAll(valueUtilityMap);
    this.newValueSet.addAll(valueSet);
    this.firstDerivativeMap.putAll(firstDerivativeMap);
  }
    
  public MaxSumMessage addMessage(MaxSumMessage message) {
    MaxSumMessage resultMsg = new MaxSumMessage(this);
    for (Entry<Double, Double> entry : resultMsg.getValueUtilityMap().entrySet()) {
      double key = entry.getKey();
      double value = entry.getValue();
      entry.setValue(message.getValueUtilityMap().get(key) + value);
    }
    return resultMsg;
  }
  
  public MaxSumMessage addAllMessages(Collection<MaxSumMessage> msgSet) {
    MaxSumMessage resultMsg = new MaxSumMessage(this);
    for (MaxSumMessage msg : msgSet) {
      resultMsg = resultMsg.addMessage(msg);
    }
    return resultMsg;
  }
  
  /**
   * Initialize the MaxSumMessage to <value, 0.0>
   * @param MSvalueSet is the set containing the value
   */
  public <T> MaxSumMessage(Set<T> MSvalueSet) {
    for (T value : MSvalueSet) {
      this.valueUtilityMap.put((Double) value, 0.0);
    }
  }
  
  /**
   * Update alpha in MaxSum on graph
   */
  public void updateAlphaAndValues() {
    int size = valueUtilityMap.size();
    double alpha = -valueUtilityMap.values().stream().mapToDouble(value -> value.doubleValue()).sum() / size;
    for (Map.Entry<Double, Double> entry : valueUtilityMap.entrySet()) {
      entry.setValue(entry.getValue() + alpha);
    }
  }
  
  public double getBestValue() {
    double bestValue = -Double.MAX_VALUE;
    double maxUtility = -Double.MAX_VALUE;
    
    for (Entry<Double, Double> entry : valueUtilityMap.entrySet()) {
      if (compare(entry.getValue(), maxUtility) > 0) {
        maxUtility = entry.getValue();
        bestValue = entry.getKey();
      }
    }
    
    return bestValue;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[valueUtilityMap=" );
    sb.append(valueUtilityMap);
    sb.append(", newValueSet=");
    sb.append(newValueSet);
    sb.append(", firstDerivativeMap=");
    sb.append(firstDerivativeMap);
    sb.append("]\n");
    return sb.toString();
  }

  public void putValueUtilityToMap(double value, double utility) {
    this.valueUtilityMap.put(value, utility);
  }
  
  public Map<Double, Double> getValueUtilityMap() {
    return valueUtilityMap;
  }

  public void setValueUtilityMap(Map<Double, Double> valueUtilityMap) {
    this.valueUtilityMap = valueUtilityMap;
  }

  public Set<Double> getNewValueSet() {
    return newValueSet;
  }

  public void setNewValueSet(Set<Double> newValueSet) {
    this.newValueSet = newValueSet;
  }

  public Map<Double, Double> getFirstDerivativeMap() {
    return firstDerivativeMap;
  }

  public void setFirstDerivativeMap(Map<Double, Double> firstDerivativeMap) {
    this.firstDerivativeMap = firstDerivativeMap;
  }
}
