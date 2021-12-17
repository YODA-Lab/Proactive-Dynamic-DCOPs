package table;

import static java.lang.Double.compare;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Sets;

import zexception.FunctionException;

/**
 * @author khoihd
 *
 */
public class TableDouble extends AbstractTable implements Serializable {

	/**
   * 
   */
  private static final long serialVersionUID = 7744720382630253811L;

  private List<String> decVarLabel = new ArrayList<>();
	
	private List<String> randVarLabel = new ArrayList<>();
	
	private List<RowDouble> rowList = new ArrayList<>();
	
	private boolean isRandTableDouble = false;

	public TableDouble(TableDouble anotherTableDouble) {
	  this.decVarLabel.addAll(anotherTableDouble.getDecVarLabel());	  
	  this.randVarLabel.addAll(anotherTableDouble.getRandVarLabel());
	  
		for (RowDouble row : anotherTableDouble.getRowList()) {
			this.rowList.add(new RowDouble(row));
		}
		
		isRandTableDouble = anotherTableDouble.isRandTable();
	}
	
	 public TableDouble(List<String> newLabel) {
	   decVarLabel.addAll(newLabel);
	  }
	
	public double getUtilityGivenDecValueList(List<String> decValueList) {
		for (RowDouble row : rowList) {
			if (row.getValueList().equals(decValueList)) {
				return row.getUtility();
			}
		}
		
		return -Double.MAX_VALUE;
	}
	
	public double getUtilityGivenDecAndRandValueList(List<String> decValueList, List<String> randValueList) {
		for (RowDouble row : rowList) {
			if (row.getValueList().equals(decValueList) && row.getRandomList().equals(randValueList)) {
				return row.getUtility();
			}
		}
		
    return -Double.MAX_VALUE;
	}
	
	 /**
	 * @param newLabel
	 * @param isRandTableDouble
	 */
	public TableDouble(List<String> newLabel, boolean isRandTableDouble) {
	    this.decVarLabel.addAll(newLabel);
	    this.isRandTableDouble = isRandTableDouble;
	  }
	
	public TableDouble(List<String> decVarList, List<String> randVarList, boolean isRandTableDouble) {
		decVarLabel.addAll(decVarList);
		randVarLabel.addAll(randVarList);
		this.isRandTableDouble = isRandTableDouble;
	}
	
	public void addRow(RowDouble newRowDouble) {
		rowList.add(newRowDouble);
	}
	
	public double getUtilityFromTableDoubleGivenDecAndRand(List<String> decValueList, List<String> randValueList) {
	  for (RowDouble row : rowList) {
	    if (row.getValueList().equals(decValueList) && row.getRandomList().equals(randValueList)) {
	      return row.getUtility();
	    }
	  }
	  
//	  return Double.NEGATIVE_INFINITY;
	  return -Double.MAX_VALUE;
	}

	public int size() {
		return rowList.size();
	}
	
	public int getVariableCount() {
		return decVarLabel.size();
	}

	public List<RowDouble> getRowList() {
		return rowList;
	}

	public List<String> getDecVarLabel() {
		return decVarLabel;
	}

	public List<String> getRandVarLabel() {
		return randVarLabel;
	}
	
  public void addRows(Collection<RowDouble> rows) {
    rowList.addAll(rows);
  }

  @Override
  public int hashCode() {
    return Objects.hash(decVarLabel, randVarLabel, rowList);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TableDouble)) {
      return false;
    }
    TableDouble other = (TableDouble) obj;
    return Objects.equals(decVarLabel, other.decVarLabel) && Objects.equals(randVarLabel, other.randVarLabel)
        && Objects.equals(rowList, other.rowList);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("TableDouble: decVarLabel=");
    sb.append(decVarLabel);
    sb.append(", randVarLabel=");
    sb.append(randVarLabel);
    sb.append(", isRandTableDouble=");
    sb.append(isRandTableDouble + "\n");
    for (RowDouble row : rowList) {
      sb.append(row + "\n");
    }
    sb.append("]\n");
    
    return sb.toString();
  }

  public boolean isRandTable() {
    return !randVarLabel.isEmpty() || isRandTableDouble;
  }

  public void setRandTableDouble(boolean isRandTableDouble) {
    this.isRandTableDouble = isRandTableDouble;
  }
  
  /**
   * Return empty HashSet if the table doesn't contain the agent
   * @param agent
   * @param isThrowException
   * @return
   */
  public Set<Double> getValueSetOfGivenAgent(String agent, boolean isThrowException) {
    if (!decVarLabel.contains(agent)) {
      if (isThrowException)
        throw new FunctionException("The table label " + decVarLabel + " doesn't contain the agent: " + agent);
      else
        return new HashSet<Double>();
    }
    int index = decVarLabel.indexOf(agent);
    
    Set<Double> valueSet = new HashSet<>();
    for (RowDouble row: rowList) {      
        valueSet.add(row.getValueAtPosition(index));
    }
    return valueSet;
  }
  
  public boolean containsAgent(String agent) {
    return decVarLabel.contains(agent);
  }
  
  public int indexOf(String agent) {
    return decVarLabel.indexOf(agent);
  }
  
  public void extendToTheEndOfLabel(String agent) {
    decVarLabel.add(agent);
  }
  
  /**
   * Find the argmax of this agent given the agentViewTable and the valueMapOfOtherVariables
   * 1. Create the missing-one-dimension row from the valueMapOfOtherVariables
   * 2. Calculate the missing-one-dimension distance to all other points (and save it to the list)
   * 3. For each value in agentValues, calculate the distance to that dimension, and then compute the whole 
   * 4. Return the argmax
   * @param agentViewTable
   * @param valueMapOfOtherVariables
   * @param 
   * @return
   */
  public double[] maxArgmaxHybrid(Map<String, Double> valueMapOfOtherVariables, Set<Double> midPointInHalfIntegerRanges) {
    double[] maxArgmax = new double[2];
    maxArgmax[0] = -Double.MAX_VALUE;
    maxArgmax[1] = -Double.MAX_VALUE;
    
    // From the valueMapOfOtherVariables, create the list of missing-one-dimension distance 
    // For each value in agentValues, calculate the distance to the correct dimension of each row/point
    // Store the current row with maximum utility
    // Then return the max and argmax
        
    List<Double> pointOutsideTable = new ArrayList<>();
    int missingIndex = -1;
    for (int i = 0; i < decVarLabel.size(); i++) {
      String tableAgent = decVarLabel.get(i);
      if (valueMapOfOtherVariables.containsKey(tableAgent)) {
        pointOutsideTable.add(valueMapOfOtherVariables.get(tableAgent));
      } else {
        pointOutsideTable.add(0.0);
        missingIndex = i;
      }
    }
        
    List<Double> partialDistanceList = computePartialDistanceFromAllRow(pointOutsideTable, missingIndex);
    
    Set<Double> stepSizeAgentValue = new HashSet<>(midPointInHalfIntegerRanges);

    // For each value, calculate the complete distance to every row, and get the utility of that row as 
//    for (double value : agentValues) {
    for (double value : stepSizeAgentValue) {      
      double weightedUtility = 0;
      double weightedSum = 0;
      for (int rowIndex = 0; rowIndex < rowList.size(); rowIndex++) {
        RowDouble row = rowList.get(rowIndex);
        List<Double> point = row.getValueList();
        
        double totalDistance = sqrt(pow(partialDistanceList.get(rowIndex), 2) + pow(value - point.get(missingIndex), 2));
        weightedUtility += 1/totalDistance * row.getUtility();
        weightedSum += 1/totalDistance;
      }
      
      double interpolatedUtility = weightedUtility / weightedSum;
      if (compare(interpolatedUtility, maxArgmax[0]) > 0) {
        maxArgmax[0] = interpolatedUtility;
        maxArgmax[1] = value;
      }
    }
    
    return maxArgmax;
  
  }
  
  /**
   * Compute the partial distance given the partial point and missingIndex
   * @param pointOutsideTable
   * @param missingIndex
   * @return
   */
  private List<Double> computePartialDistanceFromAllRow(List<Double> pointOutsideTable, int missingIndex) {
    List<Double> partialDistance = new ArrayList<>();
    
    for (RowDouble row : rowList) {
      partialDistance.add(partialDistance(row.getValueList(), pointOutsideTable, missingIndex));
    }
    
    return partialDistance;
  }
  
  /**
   * Compute the partial distance
   * @param valueList
   * @param pointOutsideTable
   * @param missingIndex
   * @return
   */
  private double partialDistance(List<Double> valueList, List<Double> pointOutsideTable, int missingIndex) {
    double distance = 0;
    for (int i = 0; i < decVarLabel.size(); i++) {
      if (i != missingIndex) {
        distance += pow(valueList.get(i) - pointOutsideTable.get(i), 2);
      }
    }
    return sqrt(distance);
  }
  
  /**
   * TODO: Review this function. Find where the function appears. The map is partial only
   * Given a Map<String, Set<Double>>, find the corresponding points and do the interpolation
   * Return a set of Row. Used to add to the table later in DPOP UTIL
   * @param valueMap
   * @return
   */
  public Set<RowDouble> interpolateGivenValueSetMap(Map<String, Set<Double>> valueMap, int stepSize) {
    Set<RowDouble> interpolatedRows = new HashSet<>();
    
    // create all points to be interpolated
    List<Set<Double>> valueSetList = new ArrayList<Set<Double>>();
    Map<String, Integer> agentPositionInTheValeSet = new HashMap<>();
    int position = 0;
    // The traversal order is the same as the variable ordering in the Cartesian product later
    for (Entry<String, Set<Double>> entry : valueMap.entrySet()) {
      valueSetList.add(entry.getValue());
      agentPositionInTheValeSet.put(entry.getKey(), position);
      position++;
    }
    Set<List<Double>> productInterpolatedValues = Sets.cartesianProduct(valueSetList);
        
    for (List<Double> partialPoint : productInterpolatedValues) {
      for (RowDouble row : rowList) {
        List<Double> point = new ArrayList<>(row.getValueList());

        // replace the point at the position of the agent in the label with the value at the given position
        for (Entry<String, Integer> entry : agentPositionInTheValeSet.entrySet()) {
          if (decVarLabel.contains(entry.getKey())) {
            point.set(positionOfVariableInTheLabel(entry.getKey()), partialPoint.get(entry.getValue()));
          }
        }
        
        interpolatedRows.add(inverseWeightedInterpolation(point, stepSize));
      }
    }
    
    return interpolatedRows;
  }
  
  /**
   * THIS FUNCTION IS UNIT-TESTED <br>
   * Do the interpolation with the inverse weights.
   * @param interpolatedPoint
   * @return the row if the point needed to be interpolated is already in the table
   */
  public RowDouble inverseWeightedInterpolation(List<Double> interpolatedPoint, int percentageOfTable) {
    List<Double> inverseWeights = new ArrayList<>();
    double interpolatedUtility = 0;
    for (int i = 0; i < this.rowList.size(); i+= percentageOfTable) {
      RowDouble row = this.rowList.get(i);
      
      double eucliDistance = euclidDistance(row.getValueList(), interpolatedPoint);
            
      // Return the same row if the interpolatedPoint is already in the table
      if (compare(eucliDistance, 0) == 0) {
        return row;
      }
      
      double weight = 1.0 / eucliDistance;
      interpolatedUtility += weight * row.getUtility();
      inverseWeights.add(weight);
    }
            
    double sumOfWeights = inverseWeights.stream().mapToDouble(x -> x).sum();
    return new RowDouble(interpolatedPoint, interpolatedUtility / sumOfWeights);
  }
  
  private double euclidDistance(List<Double> pointA, List<Double> pointB) {    
    double distance = 0;
    for (int index = 0; index < pointA.size(); index++) {
      distance += Math.pow(pointA.get(index) - pointB.get(index), 2);
    }
    
    return Math.sqrt(distance);
  }
  
  public int positionOfVariableInTheLabel(String agent) {
    return decVarLabel.indexOf(agent);
  }
  
  /*
   * THIS FUNCTION IS REVIEWED
   * Traverse each row of the table
   *  Check if the row contain the valueMap
   *  If yes, maintain the max utility and argmax
   * End
   * 
   * If the tables doesn't contain the valueMapOfOtherVariables, it return -Double.MAX_VALUE
   */
  public double getArgmaxGivenVariableAndValueMap(String variableToGetArgmax, Map<String, Double> valueMapOfOtherVariables) {
    Map<Integer, Double> varIndexValueMap = new HashMap<>();
    for (Entry<String, Double> entry : valueMapOfOtherVariables.entrySet()) {
      int position = positionOfVariableInTheLabel(entry.getKey());
      
      if (position != -1) {
        varIndexValueMap.put(position, entry.getValue());
      }
    }
    
    double max = -Double.MAX_VALUE;
    double argmax = -Double.MAX_VALUE;
    for (RowDouble row : rowList) {
      if (!checkIfListContainValueGivenPosition(row.getValueList(), varIndexValueMap)) {
        continue;
      }
      
      if (compare(row.getUtility(), max) > 0) {
        max = row.getUtility();
        argmax = row.getValueList().get(positionOfVariableInTheLabel(variableToGetArgmax));
      }
    }
    
    return argmax;
  }
  
  /**
   * Return false if the list doesn't contains all values with the corresponding position from the map
   * @param list
   * @param map
   * @return
   */
  private boolean checkIfListContainValueGivenPosition(List<Double> list, Map<Integer, Double> map) { 
    for (Entry<Integer, Double> entry : map.entrySet()) {
      if (compare(list.get(entry.getKey()), entry.getValue()) != 0)
        return false;
    }
    return true;
  }
}