package table;

import static java.lang.Double.compare;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.io.Serializable;
import java.util.ArrayList;
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
public class Table implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2675509097502238364L;
	
	private List<String> decVarLabel = new ArrayList<>();
	
	private List<String> randVarLabel = new ArrayList<>();
	
	private List<Row> rowList = new ArrayList<>();
	
	private boolean isRandTable = false;

	public Table(List<String> newLabel) {
	  decVarLabel.addAll(newLabel);
	}
	
	public Table(Table anotherTable) {
	  this.decVarLabel.addAll(anotherTable.getDecVarLabel());	  
	  this.randVarLabel.addAll(anotherTable.getRandVarLabel());
	  
		for (Row row : anotherTable.getRowList()) {
			this.rowList.add(new Row(row));
		}
		
		isRandTable = anotherTable.isRandTable();
	}
	
	public double getUtilityGivenDecValueList(List<String> decValueList) {
		for (Row row : rowList) {
			if (row.getValueList().equals(decValueList)) {
				return row.getUtility();
			}
		}
		
		return -Double.MAX_VALUE;
	}
	
	public double getUtilityGivenDecAndRandValueList(List<String> decValueList, List<String> randValueList) {
		for (Row row : rowList) {
			if (row.getValueList().equals(decValueList) && row.getRandomList().equals(randValueList)) {
				return row.getUtility();
			}
		}
		
    return -Double.MAX_VALUE;
	}
	
	public int size() {
	  return rowList.size();
	}
	
	 /**
	 * @param newLabel
	 * @param isRandTable
	 */
	public Table(List<String> newLabel, boolean isRandTable) {
	    this.decVarLabel.addAll(newLabel);
	    this.isRandTable = isRandTable;
	  }
	
	public Table(List<String> decVarList, List<String> randVarList, boolean isRandTable) {
		decVarLabel.addAll(decVarList);
		randVarLabel.addAll(randVarList);
		this.isRandTable = isRandTable;
	}
	
	public void addRow(Row newRow) {
		rowList.add(newRow);
	}
	
	public double getUtilityFromTableGivenDecAndRand(List<String> decValueList, List<String> randValueList) {
	  for (Row row : rowList) {
	    if (row.getValueList().equals(decValueList) && row.getRandomList().equals(randValueList)) {
	      return row.getUtility();
	    }
	  }
	  
//	  return Double.NEGATIVE_INFINITY;
	  return -Double.MAX_VALUE;
	}
	
	 /**
   * Return empty HashSet if the table doesn't contain the agent
   * @param agent
   * @param isThrowException
   * @return
   */
  public Set<String> getValueSetOfGivenAgent(String agent, boolean isThrowException) {
    if (!decVarLabel.contains(agent)) {
      if (isThrowException)
        throw new FunctionException("The table label " + decVarLabel + " doesn't contain the agent: " + agent);
      else
        return new HashSet<String>();
    }
    int index = decVarLabel.indexOf(agent);
    
    Set<String> valueSet = new HashSet<>();
    for (Row row: rowList) {      
        valueSet.add(row.getValueAtPosition(index));
    }
    
    return valueSet;
  }

	public int getRowCount() {
		return rowList.size();
	}
	
	public int getVariableCount() {
		return decVarLabel.size();
	}

	public List<Row> getRowList() {
		return rowList;
	}

	public List<String> getDecVarLabel() {
		return decVarLabel;
	}

	public List<String> getRandVarLabel() {
		return randVarLabel;
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
    if (!(obj instanceof Table)) {
      return false;
    }
    Table other = (Table) obj;
    return Objects.equals(decVarLabel, other.decVarLabel) && Objects.equals(randVarLabel, other.randVarLabel)
        && Objects.equals(rowList, other.rowList);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Table: decVarLabel=");
    sb.append(decVarLabel);
    sb.append(", randVarLabel=");
    sb.append(randVarLabel);
    sb.append(", isRandTable=");
    sb.append(isRandTable + "\n");
    for (Row row : rowList) {
      sb.append(row + "\n");
    }
    sb.append("]\n");
    
    return sb.toString();
  }

  public boolean isRandTable() {
    return !randVarLabel.isEmpty() || isRandTable;
  }

  public void setRandTable(boolean isRandTable) {
    this.isRandTable = isRandTable;
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
  public double[] maxArgmaxHybrid(Map<String, String> valueMapOfOtherVariables, Set<String> midPointInHalfIntegerRanges) {  
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
        pointOutsideTable.add(Double.valueOf(valueMapOfOtherVariables.get(tableAgent)));
      } else {
        pointOutsideTable.add(0.0);
        missingIndex = i;
      }
    }
        
    List<Double> partialDistanceList = computePartialDistanceFromAllRow(pointOutsideTable, missingIndex);
    
    Set<String> stepSizeAgentValue = new HashSet<>(midPointInHalfIntegerRanges);

    // For each value, calculate the complete distance to every row, and get the utility of that row as 
//    for (double value : agentValues) {
    for (String valueStr : stepSizeAgentValue) {      
      double value = Double.valueOf(valueStr);
      
      double weightedUtility = 0;
      double weightedSum = 0;
      for (int rowIndex = 0; rowIndex < rowList.size(); rowIndex++) {
        Row row = rowList.get(rowIndex);
        List<String> point = row.getValueList();
        
        double totalDistance = sqrt(pow(partialDistanceList.get(rowIndex), 2) + pow(value - Double.valueOf(point.get(missingIndex)), 2));
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
    
    for (Row row : rowList) {
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
  private double partialDistance(List<String> valueList, List<Double> pointOutsideTable, int missingIndex) {
    double distance = 0;
    for (int i = 0; i < decVarLabel.size(); i++) {
      if (i != missingIndex) {
        distance += pow(Double.valueOf(valueList.get(i))
                        - pointOutsideTable.get(i), 2);
      }
    }
    return sqrt(distance);
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
  
  public void addRowSet(Set<Row> rows) {
    rowList.addAll(rows);
  }
  
  /**
   * TODO: Review this function. Find where the function appears. The map is partial only
   * Given a Map<String, Set<Double>>, find the corresponding points and do the interpolation
   * Return a set of Row. Used to add to the table later in DPOP UTIL
   * @param valueMap
   * @return
   */
  public Set<Row> interpolateGivenValueSetMap(Map<String, Set<String>> valueMap, int stepSize) {
    Set<Row> interpolatedRows = new HashSet<>();
    
    // create all points to be interpolated
    List<Set<String>> valueSetList = new ArrayList<>();
    Map<String, Integer> agentPositionInTheValeSet = new HashMap<>();
    int position = 0;
    // The traversal order is the same as the variable ordering in the Cartesian product later
    for (Entry<String, Set<String>> entry : valueMap.entrySet()) {
      valueSetList.add(entry.getValue());
      agentPositionInTheValeSet.put(entry.getKey(), position);
      position++;
    }
    Set<List<String>> productInterpolatedValues = Sets.cartesianProduct(valueSetList);
        
    for (List<String> partialPoint : productInterpolatedValues) {
      for (Row row : rowList) {
        List<String> point = new ArrayList<>(row.getValueList());

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
  
  public int positionOfVariableInTheLabel(String agent) {
    return decVarLabel.indexOf(agent);
  }
  
  /**
   * THIS FUNCTION IS UNIT-TESTED
   * Do the interpolation with the inverse weights.
   * @param interpolatedPoint
   * @return the row if the point needed to be interpolated is already in the table
   */
  public Row inverseWeightedInterpolation(List<String> interpolatedPoint, int percentageOfTable) {
    List<Double> inverseWeights = new ArrayList<>();
    double interpolatedUtility = 0;
    for (int i = 0; i < this.rowList.size(); i+= percentageOfTable) {
      Row row = this.rowList.get(i);
      
      double eucliDistance = euclidDistance(row.getValueList(), interpolatedPoint);
      
      // Return null if the interpolatedPoint is already in the table
      if (compare(eucliDistance, 0) == 0) {
        return row;
      }
      
      double weight = 1.0 / eucliDistance;
      interpolatedUtility += weight * row.getUtility();
      inverseWeights.add(weight);
    }
            
    double sumOfWeights = inverseWeights.stream().mapToDouble(x -> x).sum();
    return new Row(interpolatedPoint, interpolatedUtility / sumOfWeights);
  }
  
  private double euclidDistance(List<String> pointA, List<String> pointB) {    
    double distance = 0;
    for (int index = 0; index < pointA.size(); index++) {
      double valueA = Double.valueOf(pointA.get(index));
      double valueB = Double.valueOf(pointB.get(index));
      
//      distance += Math.pow(pointA.get(index) - pointB.get(index), 2);
      distance += Math.pow(valueA - valueB, 2);
    }
    
    return Math.sqrt(distance);
  }
}
