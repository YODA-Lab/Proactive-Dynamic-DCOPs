package table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author khoihd
 *
 */
public class RowDouble extends AbstractRow implements Serializable {
	
	/**
   * 
   */
  private static final long serialVersionUID = 8837176380653705108L;
  private List<Double> valueList = new ArrayList<>();
	private List<Double> randomList  = new ArrayList<>();;

	private double utility;
	
	public RowDouble() {
	  
	}
	
	public RowDouble(RowDouble newRow) {
		this.valueList.addAll(newRow.getValueList());
		this.randomList.addAll(newRow.getRandomList());		
    this.utility = newRow.utility;
	}
	
	//input: X1, X2, X3,...,Xn
	//input utility
	public RowDouble(List<Double> input, Double utility) {
		this.valueList.addAll(input);
		this.utility = utility;
	}
	
	 
  public RowDouble(double[] input, double utility) {
    valueList.addAll(Arrays.stream(input).boxed().collect(Collectors.toList()));
    this.utility = utility;
  }
	
	public RowDouble(List<Double> decisionVariableList, List<Double> randVariableList, double utility) {
		this.valueList.addAll(decisionVariableList);
		this.randomList.addAll(randVariableList);
		this.utility = utility;
	}
	
	public RowDouble(List<Double> decisionAndRandomList, int noDecision, double utility) {
		for (int i = 0; i < noDecision; i++) {
			this.valueList.add(decisionAndRandomList.get(i));
		}
		
		for (int i = noDecision; i < decisionAndRandomList.size(); i++) {
			this.randomList.add(decisionAndRandomList.get(i));
		}

		this.utility = utility;
	}

  public double getValueAtPosition(int index) {
		if (index < 0 || index >= valueList.size()) {return Double.MAX_VALUE;}
	  
		return valueList.get(index);
	}
	
	public int getVariableCount() {
		return valueList.size();
	}

	public List<Double> getValueList() {
		return valueList;
	}

	public int getRandomCount() {
		return randomList.size();
	}

	public List<Double> getRandomList() {
		return randomList;
	}

	public double getUtility() {
		return utility;
	}
	
	public void setUtility(double utility) {
		this.utility = utility;
	}
	
	 public void addValueToTheEnd(double value) {
	    valueList.add(value);
	  }

  @Override
  public String toString() {
    return "RowDouble [valueList=" + valueList + ", randomList=" + randomList + ", utility=" + utility + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(randomList, utility, valueList);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof RowDouble))
      return false;
    RowDouble other = (RowDouble) obj;
    return Objects.equals(randomList, other.randomList)
        && Double.doubleToLongBits(utility) == Double.doubleToLongBits(other.utility)
        && Objects.equals(valueList, other.valueList);
  }
}
