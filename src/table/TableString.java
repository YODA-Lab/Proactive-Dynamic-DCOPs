package table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author khoihd
 *
 */
public class TableString extends AbstractTable implements Serializable {

	/**
   * 
   */
  private static final long serialVersionUID = 7744720382630253811L;

  private List<String> decVarLabel = new ArrayList<>();
	
	private List<String> randVarLabel = new ArrayList<>();
	
	private List<RowString> rowList = new ArrayList<>();
	
	private boolean isRandTableString = false;

	public TableString(TableString anotherTableString) {
	  this.decVarLabel.addAll(anotherTableString.getDecVarLabel());	  
	  this.randVarLabel.addAll(anotherTableString.getRandVarLabel());
	  
		for (RowString row : anotherTableString.getRowList()) {
			this.rowList.add(new RowString(row));
		}
		
		isRandTableString = anotherTableString.isRandTable();
	}
	
	public double getUtilityGivenDecValueList(List<String> decValueList) {
		for (RowString row : rowList) {
			if (row.getValueList().equals(decValueList)) {
				return row.getUtility();
			}
		}
		
		return -Double.MAX_VALUE;
	}
	
	public double getUtilityGivenDecAndRandValueList(List<String> decValueList, List<String> randValueList) {
		for (RowString row : rowList) {
			if (row.getValueList().equals(decValueList) && row.getRandomList().equals(randValueList)) {
				return row.getUtility();
			}
		}
		
    return -Double.MAX_VALUE;
	}
	
	 /**
	 * @param newLabel
	 * @param isRandTableString
	 */
	public TableString(List<String> newLabel, boolean isRandTableString) {
	    this.decVarLabel.addAll(newLabel);
	    this.isRandTableString = isRandTableString;
	  }
	
	public TableString(List<String> decVarList, List<String> randVarList, boolean isRandTableString) {
		decVarLabel.addAll(decVarList);
		randVarLabel.addAll(randVarList);
		this.isRandTableString = isRandTableString;
	}
	
	public void addRow(RowString newRowString) {
		rowList.add(newRowString);
	}
	
	public double getUtilityFromTableGivenDecAndRand(List<String> decValueList, List<String> randValueList) {
	  for (RowString row : rowList) {
	    if (row.getValueList().equals(decValueList) && row.getRandomList().equals(randValueList)) {
	      return row.getUtility();
	    }
	  }
	  
//	  return Double.NEGATIVE_INFINITY;
	  return -Double.MAX_VALUE;
	}

	public int getRowCount() {
		return rowList.size();
	}
	
	public int getVariableCount() {
		return decVarLabel.size();
	}

	public List<RowString> getRowList() {
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
    if (!(obj instanceof TableString)) {
      return false;
    }
    TableString other = (TableString) obj;
    return Objects.equals(decVarLabel, other.decVarLabel) && Objects.equals(randVarLabel, other.randVarLabel)
        && Objects.equals(rowList, other.rowList);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("TableString: decVarLabel=");
    sb.append(decVarLabel);
    sb.append(", randVarLabel=");
    sb.append(randVarLabel);
    sb.append(", isRandTableString=");
    sb.append(isRandTableString + "\n");
    for (RowString row : rowList) {
      sb.append(row + "\n");
    }
    sb.append("]\n");
    
    return sb.toString();
  }

  public boolean isRandTable() {
    return !randVarLabel.isEmpty() || isRandTableString;
  }

  public void setRandTable(boolean isRandTableString) {
    this.isRandTableString = isRandTableString;
  }
}