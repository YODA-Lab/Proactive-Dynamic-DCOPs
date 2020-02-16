//package table;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author khoihd
// *
// */
//public class ReuseTable implements Serializable {
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -2675509097502238364L;
//	static final int CONTAIN_RAND = 0;
//	static final int NOT_CONTAIN_RAND = 1;
//	int rowCount;
//	int variableCount;
//	int randomCount;
//	List<Row> table;
//	List<String> decVarLabel;
//	List<String> randVarLabel;
//	int randType;
//
//	public ReuseTable(ReuseTable anotherTable) {
//		this.rowCount = anotherTable.rowCount;
//		this.variableCount = anotherTable.variableCount;
//		this.table = new ArrayList<Row>();
////		this.decVarLabel = (List<String>) anotherTable.decVarLabel.clone();
//		this.decVarLabel = new ArrayList<String>();
//		for (String label:anotherTable.getDecVarLabel()) {
//			this.decVarLabel.add(label);
//		}
//		this.randType = anotherTable.randType;
//		//add row to new table
//		for (Row row:anotherTable.getTable()) {
//			this.table.add(new Row(row));
//		}
//	}
//	
//	boolean equalArray(List<?> list1, List<?> list2) {
//		if (list1 == null || list2 == null)
//			return false;
//		if (list1.size() != list2.size())
//			return false;
//		for (int index=0; index<list1.size(); index++) {
//			if (list1.get(index).equals(list2.get(index)) == false)
//				return false;				
//		}
//		return true;
//	}
//	
//	double getUtilityGivenDecValueList(List<String> decValueList) {
//		for (int index=0; index<table.size(); index++) {
//			if (equalArray(table.get(index).getValueList(), decValueList))
//				return table.get(index).getUtility();
//		}
//		return -999;
//	}
//	
//	double getUtilityGivenDecAndRandValueList(List<String> decValueList, List<String> randValueList) {
//		for (int index=0; index<table.size(); index++) {
//			if (equalArray(table.get(index).getValueList(), decValueList)
//			&& equalArray(table.get(index).getRandomList(), randValueList))
//				return table.get(index).getUtility();
//		}
//		return -999;
//	}
//	
//	@Override
//    public boolean equals(Object tableToCompare) {
//        // If the object is compared with itself then return true  
//        if (tableToCompare == this) {
//            return true;
//        }
// 
//        if (!(tableToCompare instanceof ReuseTable)) {
//            return false;
//        }
//         
//        // typecast o to Complex so that we can compare data members 
//        ReuseTable castedTypeTable = (ReuseTable) tableToCompare;
//         
//        // Compare the data members and return accordingly 
//        return  castedTypeTable.rowCount == this.rowCount	
//        	&&	castedTypeTable.variableCount == this.variableCount
//        	&&	castedTypeTable.table.equals(this.table)
//        	&&	castedTypeTable.decVarLabel == this.decVarLabel;
//	}
//	
//	public ReuseTable(List<String> newLabel) {
//		table = new ArrayList<Row>();
//		decVarLabel = new ArrayList<String>();
//		for (String variable:newLabel)
//			decVarLabel.add(variable);
//		variableCount = decVarLabel.size();
//		rowCount = 0;
//		//default is not contain rand, then detect
//		//this.randType = NOT_CONTAIN_RAND;
//	}
//	
//	public ReuseTable(List<String> newLabel, int randType) {
//		this(newLabel);
//		this.randType = randType;
//	}
//	
//	public ReuseTable(List<String> decVarList, List<String> randVarList) {
//		table = new ArrayList<Row>();
//		decVarLabel = decVarList;
//		randVarLabel = randVarList;
//		variableCount = decVarList.size();
//		randomCount = randVarList.size();
//		//default is not contain rand, then detect
//		this.randType = NOT_CONTAIN_RAND;
//	}
//	
//	public ReuseTable(List<String> decVarList, List<String> randVarList, int randType) {
//		this(decVarList, randVarList);
//		this.randType = randType;
//	}
//	
//	public void addRow(Row newRow) {
//		this.table.add(newRow);
//		this.rowCount++;
//	}
//	
//	//tao 1 array chua values cua 1 bien
//	//chay tung dong cua vector
//	//kiem tra gia tri cua bien, co trong listValues chua
//	//neu chua thi them vao
//	//tra ve danh sach tat ca gia tri cua 1 bien, khong bi duplicate
//	List<String> listValuesOfVariable(int index) {
//		List<String> listValues = new ArrayList<String>();
//		for (Row row: table) {			
//			if (!listValues.contains(row.getValueAtPosition(index))) {
//				listValues.add(row.getValueAtPosition(index));
//			}
//		}
//		return listValues;
//	}
//	
//	void printDecVar() {
//		System.out.println("Dec lable list: ");
//		for (String variable:decVarLabel)
//			System.out.print(variable + " ");
//		System.out.println();
//		
//		for (Row row:table) {
//			row.printDecVar();
//		}
//	}
//	
//	void printRandVar() {
//		System.out.println("Rand lable list: ");
//		for (String variable:randVarLabel)
//			System.out.print(variable + " ");
//		System.out.println();
//		
//		for (Row row:table) {
//			row.printRandVar();
//		}
//	}
//	
//	void printDecAndRandVar() {
//		System.out.println("Dec and Rand lable list: ");
//		for (String variable:decVarLabel)
//			System.out.print(variable + " ");
//		System.out.print("y ");
//		for (String variable:randVarLabel)
//			System.out.print(variable + " ");
//		
//		System.out.println();
//		for (Row row:table) {
//			row.printBoth();
//		}
//	}
//
//	public int getRowCount() {
//		return rowCount;
//	}
//	
//	public int getVariableCount() {
//		return variableCount;
//	}
//
//	public List<Row> getTable() {
//		return table;
//	}
//
//	public List<String> getDecVarLabel() {
//		return decVarLabel;
//	}
//
//	public List<String> getRandVarLabel() {
//		return randVarLabel;
//	}
//
//	public int getRandType() {
//		return randType;
//	}
//
//	public void setRandType(int randType) {
//		this.randType = randType;
//	}
//}
