package transition;

import java.util.ArrayList;

/**
 * @author khoihd
 *
 */
public class TransitionFunction {
	ArrayList<String> domain;
	ArrayList<ArrayList<Double>> transitionMatrix;
	public TransitionFunction(ArrayList<String> domain, ArrayList<ArrayList<Double>> transitionFunction) {
		this.domain = domain;
		this.transitionMatrix = transitionFunction;
	}
	
	public ArrayList<Double> getTransitionOf(String from) {
		int fromIndex = domain.indexOf(from);
		return transitionMatrix.get(fromIndex);
	}
	
	public double getProbByValue(String from, String to) {
		int fromIndex = domain.indexOf(from);
		int toIndex = domain.indexOf(to);
		
		if (fromIndex == -1 || toIndex == -1)
			System.err.println("Wrong trans value!!!!!!!! Recheck your code");
		return transitionMatrix.get(fromIndex).get(toIndex);
	}
	
	public double getProbByIndex(int fromIndex, int toIndex) {
		if (fromIndex >= domain.size() || toIndex >= domain.size()) {
			System.err.println("Wrong trans index!!!!!!!! Recheck your code");
		}
		return transitionMatrix.get(fromIndex).get(toIndex);
	}

	public ArrayList<String> getDomain() {
		return domain;
	}

	public void setDomain(ArrayList<String> domain) {
		this.domain = domain;
	}

	public ArrayList<ArrayList<Double>> getTransitionMatrix() {
		return transitionMatrix;
	}

	public void setTransitionMatrix(ArrayList<ArrayList<Double>> transitionMatrix) {
		this.transitionMatrix = transitionMatrix;
	}
	
	public int getSize() {
		return transitionMatrix.size(); 
	}

}
