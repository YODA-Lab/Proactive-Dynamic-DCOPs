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
	
	private boolean isRandTable = false;

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
	  
	  return Double.NEGATIVE_INFINITY;
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
}
