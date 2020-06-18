package table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author khoihd
 *
 */
public class Row implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7773374424812673056L;
	
	private List<String> valueList = new ArrayList<String>();
	private List<String> randomList  = new ArrayList<String>();;
	private double utility;
	
	public Row() {
	  
	}
	
	public Row(Row newRow) {
		this.valueList.addAll(newRow.getValueList());
		this.randomList.addAll(newRow.getRandomList());		
    this.utility = newRow.utility;
	}
	
	//input: X1, X2, X3,...,Xn
	//input utility
	public Row(List<String> input, double utility) {
		this.valueList.addAll(input);
		this.utility = utility;
	}
	
	public Row(List<String> decisionVariableList, List<String> randVariableList, double utility) {
		this.valueList.addAll(decisionVariableList);
		this.randomList.addAll(randVariableList);
		this.utility = utility;
	}
	
	public Row(List<String> decisionAndRandomList, int noDecision, double utility) {
		for (int i = 0; i < noDecision; i++) {
			this.valueList.add(decisionAndRandomList.get(i));
		}
		
		for (int i = noDecision; i < decisionAndRandomList.size(); i++) {
			this.randomList.add(decisionAndRandomList.get(i));
		}

		this.utility = utility;
	}
	
	public String getValueAtPosition(int index) {
		if (index < 0 || index >= valueList.size()) {return "";}
	  
		return valueList.get(index);
	}
	
	public int getVariableCount() {
		return valueList.size();
	}

	public List<String> getValueList() {
		return valueList;
	}

	public int getRandomCount() {
		return randomList.size();
	}

	public List<String> getRandomList() {
		return randomList;
	}

	public double getUtility() {
		return utility;
	}
	
	public void setUtility(double utility) {
		this.utility = utility;
	}

  @Override
  public String toString() {
    return "Row [valueList=" + valueList + ", randomList=" + randomList + ", utility=" + utility + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(randomList, utility, valueList);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Row)) {
      return false;
    }
    Row other = (Row) obj;
    return Objects.equals(randomList, other.randomList)
        && Double.doubleToLongBits(utility) == Double.doubleToLongBits(other.utility)
        && Objects.equals(valueList, other.valueList);
  }
}
