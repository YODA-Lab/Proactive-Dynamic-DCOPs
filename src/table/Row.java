package table;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author khoihd
 *
 */
public class Row implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7773374424812673056L;
	int variableCount;
	ArrayList<String> valueList;
	int randomCount;
	ArrayList<String> randomList;
	double utility;
	
	public Row() {
		valueList = new ArrayList<String>();
		randomList = new ArrayList<String>();
	}
	

	public Row(Row newRow) {
		this.variableCount = newRow.variableCount;
		this.utility = newRow.utility;
		//copy value list from newRow
		this.valueList = new ArrayList<String>();
		for (String value:newRow.getValueList()) {
			this.valueList.add(new String(value));
		}
	}
	
	//input: X1, X2, X3,...,Xn
	//input utility
	public Row(ArrayList<String> input, double utility) {
		this.valueList = input;
		this.variableCount = input.size();
		this.utility = utility;
	}
	
	public Row(ArrayList<String> decisionVariableList, ArrayList<String> randVariableList, double utility) {
		this.valueList = decisionVariableList;
		this.randomList = randVariableList;
		this.variableCount = decisionVariableList.size();
		this.randomCount = randVariableList.size();
		this.utility = utility;
	}
	
	public Row(ArrayList<String> decisionAndRandomList, int noDecision, double utility) {
		for (int i = 0; i < noDecision; i++) {
			this.valueList.add(decisionAndRandomList.get(i));
		}
		
		for (int i = noDecision; i<decisionAndRandomList.size(); i++) {
			this.randomList.add(decisionAndRandomList.get(i));
		}
		
		this.variableCount = valueList.size();
		this.randomCount = randomList.size();
		this.utility = utility;
	}
	
	public String getValueAtPosition(int index) {
		if (index >= variableCount) {
			System.err.println("Index out of bounds: " + index);
			System.err.println("Size:" +  variableCount);
		}
		return valueList.get(index);
	}

	public void printDecVar() {
		for (String value:valueList)
			System.out.print(value + " ");
		System.out.println("utility " + utility);
	}
	
	public void printRandVar() {
		for (String value:randomList)
			System.out.print(value + " ");
		System.out.println("utility " + utility);
	}
	
	public void printBoth() {
		for (String value:valueList)
			System.out.print(value + " ");
		System.out.print("y ");
		for (String value:randomList)
			System.out.print(value + " ");
		System.out.println("utility " + utility);
	}
	
	public boolean equalDecisionVar(Object rowToCompare) {
		// If the object is compared with itself then return true  
        if (rowToCompare == this) {
            return true;
        }
 
        if (!(rowToCompare instanceof Row)) {
            return false;
        }
        
        // typecast o to Complex so that we can compare data members 
        Row castedTypeRow = (Row) rowToCompare;
        
        if (castedTypeRow.getVariableCount() != this.variableCount)
        	System.err.println("Different number of variable count: " + this.variableCount + " " + 
        							castedTypeRow.getRandomCount());
         
        // Compare the data members and return accordingly 
        return 	castedTypeRow.randomCount == this.randomCount
        	&&	castedTypeRow.randomList.equals(this.randomList);
	}
	
	@Override
    public boolean equals(Object rowToCompare) {
        // If the object is compared with itself then return true  
        if (rowToCompare == this) {
            return true;
        }
 
        if (!(rowToCompare instanceof Row)) {
            return false;
        }
         
        // typecast o to Complex so that we can compare data members 
        Row castedTypeRow = (Row) rowToCompare;
         
        // Compare the data members and return accordingly 
        return 	castedTypeRow.variableCount == this.variableCount
        	&&	castedTypeRow.valueList.equals(this.valueList)
        	&&	castedTypeRow.utility == this.utility;
	}
	
	public int getVariableCount() {
		return variableCount;
	}

	public ArrayList<String> getValueList() {
		return valueList;
	}

	public int getRandomCount() {
		return randomCount;
	}

	public ArrayList<String> getRandomList() {
		return randomList;
	}

	public double getUtility() {
		return utility;
	}
	
	public void setUtility(double utility) {
		this.utility = utility;
	}
	
}
