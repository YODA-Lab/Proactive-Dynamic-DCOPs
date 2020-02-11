package transition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author khoihd
 *
 */
public class TransitionFunction {
	List<String> domain = new ArrayList<>();
	
	List<List<Double>> transitionMatrix = new ArrayList<>();
	
	public TransitionFunction(List<String> domain, List<List<Double>> transitionFunction) {
		this.domain = domain;
		this.transitionMatrix = transitionFunction;
	}
	
	public List<Double> getTransitionOf(String from) {
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

	public List<String> getDomain() {
		return domain;
	}

	public void setDomain(List<String> domain) {
		this.domain = domain;
	}

	public List<List<Double>> getTransitionMatrix() {
		return transitionMatrix;
	}

	public void setTransitionMatrix(List<List<Double>> transitionMatrix) {
		this.transitionMatrix = transitionMatrix;
	}
	
	public int getSize() {
		return transitionMatrix.size(); 
	}

}
