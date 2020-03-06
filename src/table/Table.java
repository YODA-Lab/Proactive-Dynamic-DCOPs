package table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	public Table(Table anotherTable) {
	  this.decVarLabel.addAll(anotherTable.getDecVarLabel());	  
	  this.randVarLabel.addAll(anotherTable.getRandVarLabel());
	  
		for (Row row : anotherTable.getRowList()) {
			this.rowList.add(new Row(row));
		}
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
	
	public Table(List<String> newLabel) {
	  this.decVarLabel.addAll(newLabel);
	}
	
	public Table(List<String> decVarList, List<String> randVarList) {
		decVarLabel.addAll(decVarList);
		randVarLabel.addAll(randVarList);
	}
	
	public void addRow(Row newRow) {
		rowList.add(newRow);
	}
	

//	List<String> listValuesOfVariable(int index) {
//		List<String> listValues = new ArrayList<String>();
//		for (Row row: rowList) {			
//			if (listValues.contains(row.getValueAtPosition(index)) == false)
//				listValues.add(row.getValueAtPosition(index));
//		}
//		return listValues;
//	}
	
	public double getUtilityFromTableGivenDecAndRand(List<String> decValueList, List<String> randValueList) {
	  for (Row row : rowList) {
	    if (row.getValueList().equals(decValueList) && row.getRandomList().equals(randValueList)) {
	      return row.getUtility();
	    }
	  }
	  
	  return Double.MAX_VALUE;
	}
	
	public boolean isRandomTable() {
	  return !randVarLabel.isEmpty();
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
    return "Table [decVarLabel=" + decVarLabel + ", randVarLabel=" + randVarLabel + ", rowList=" + rowList + "]";
  }
}
