package function.multivariate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import function.Interval;
import utilities.Utilities;
import zexception.FunctionException;

import com.google.common.collect.Sets;
import static java.lang.Math.*;
import static java.lang.Double.*;
import static agent.DcopConstants.*;

/**
 * 
 * @author khoihd
 */
public final class MultivariateQuadFunction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2751671064195985634L;
	
	private String owner = "";
	private String otherVariable = "";
	private Table<String, String, Double> coefficients = HashBasedTable.create();
	private Map<String, Interval> critFuncIntervalMap = new HashMap<>();

	/**
	 * Constructor with no parameter <br>
	 * Initialize the constant coefficient to 0.0
	 */
	public MultivariateQuadFunction() {
		coefficients.put("", "", 0.0);
	}
	
	public static MultivariateQuadFunction switchingCostFunction(String selfAgent, double value, SwitchingType type) {	  
	  MultivariateQuadFunction swFunc = new MultivariateQuadFunction();
	  
	  // (x - value)^2 = x^2 - 2 * value + value^2
	  if (type == SwitchingType.QUADRATIC) {
	    swFunc.addOrUpdate(selfAgent, selfAgent, 1D);
	    swFunc.addOrUpdate(selfAgent, "", -2 * value);
	    swFunc.addOrUpdate("", "",  value * value);
	  }
	  
	  return swFunc;
	}

	// function -281v_2^2 199v_2 -22v_0^2 252v_0 288v_2v_0 358;
	/**
	 * This is the constructor built for BinaryQuadFunction. This is used to read
	 * the input files.
	 * 
	 * @param coeff
	 * @param selfAgent
	 * @param otherAgent
	 * @param globalInterval
	 */
	public MultivariateQuadFunction(String[] coeff, String selfAgent, String otherAgent) {
		coefficients.put(selfAgent, selfAgent, Double.valueOf(coeff[0]));
		coefficients.put(selfAgent, "", Double.valueOf(coeff[1]));
		coefficients.put(otherAgent, otherAgent, Double.valueOf(coeff[2]));
		coefficients.put(otherAgent, "", Double.valueOf(coeff[3]));
		coefficients.put(selfAgent, otherAgent, Double.valueOf(coeff[4]));
		coefficients.put("", "", Double.valueOf(coeff[5]));

		owner = selfAgent;
		otherVariable = otherAgent;
	}

	/**
	 * This is a unary quadratic function
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param owner
	 */
	public MultivariateQuadFunction(double a, double b, double c, final String owner, final Interval critFuncInterval) {
		coefficients.put(owner, owner, a);
		coefficients.put(owner, "", b);
		coefficients.put("", "", c);
		this.owner = owner;
		critFuncIntervalMap.put(owner, critFuncInterval);
	}

	public MultivariateQuadFunction(final String owner, final String other,
			final Table<String, String, Double> coefficients, Map<String, Interval> intervalMap) {
		this();
		this.owner = owner;
		this.otherVariable = other;
		this.coefficients.putAll(coefficients);
		this.critFuncIntervalMap.putAll(intervalMap);
	}

	/**
	 * Copy constructor <br>
	 * This function is already TESTED
	 * 
	 * @param object is the MultivariateQuadFunction to be copied
	 */
	public MultivariateQuadFunction(final MultivariateQuadFunction object) {
		this(object.getOwner(), object.getOtherVariable(), object.getCoefficients(), object.getCritFuncIntervalMap());
	}

	/**
	 * ADD operator <br>
	 * This function is being TESTED Precondition: two functions share the same
	 * ranges of common variables This testing is done by addNewInterval() function
	 * 
	 * @param tobeAddedFunction another MultivariateQuadFunction to add
	 * @return a new function which is a sum of this function and tobeAddedFunction
	 */
	public MultivariateQuadFunction add(final MultivariateQuadFunction tobeAddedFunction) {
		MultivariateQuadFunction result = new MultivariateQuadFunction(this);

		// Update quadratic coefficients
		for (Cell<String, String, Double> cellEntry : tobeAddedFunction.getCoefficients().cellSet()) {
			result.addOrUpdate(cellEntry.getRowKey(), cellEntry.getColumnKey(), cellEntry.getValue());
		}

		return result;
	}

	/**
	 * If contains(key1, key2), then add the 'value' <br>
	 * Otherwise, put(key1, key2)
	 * 
	 * @param key1
	 * @param key2
	 * @param value
	 */
	public void addOrUpdate(final String key1, final String key2, final double value) {
		if (this.contains(key1, key2)) {
			this.put(key1, key2, this.get(key1, key2) + value);
		} else if (this.contains(key2, key1)) {
			this.put(key2, key1, this.get(key2, key1) + value);
		} else {
			this.put(key1, key2, value);
		}
	}

	/**
	 * Add a new interval to this function <br>
	 * If already contained this variable, check for same interval <br>
	 * If not containing this variable, add <variable, interval> to list <br>
	 * This function is already TESTED
	 * 
	 * @param variable
	 * @param interval
	 * @param isCheckingSameInterval
	 */
//  public void addNewInterval(String variable, Interval interval, boolean isCheckingSameInterval) {
//    if (isCheckingSameInterval) {
//      if (intervals.containsKey(variable)) {
//        if (!intervals.get(variable).equals(interval)) {
//          throw new FunctionException("Different INTERVAL when adding QUAD MULTIVARIATE function");
//        }
//      } else {
//        intervals.put(variable, new Interval(interval));
//      }      
//    } else {
//      intervals.put(variable, new Interval(interval));
//    }
//  }

//  public void replaceOrUpdateInterval(Map<String, Interval> tobeAddedInterval) {
//    for (Entry<String, Interval> entry : tobeAddedInterval.entrySet()) {
//      intervals.put(entry.getKey(), entry.getValue());
//    }
//  }
	
	public <T> MultivariateQuadFunction evaluate(final String variable, T value) {
	  return evaluate(variable, (Double) value);
	}

	/**
	 * EVALUATE operator <br>
	 * This function is already TESTED
	 * 
	 * @param variable
	 * @param value
	 * @return
	 */
	public MultivariateQuadFunction evaluate(final String variable, final double value) {
		if (!getVariableSet().contains(variable)) {
			throw new FunctionException("The function doesn't contain the variable that needed to be evaluated "
					+ variable + " " + critFuncIntervalMap);
		}

		MultivariateQuadFunction evaluatedFunc = new MultivariateQuadFunction(this);

		// Adding a_ii * xi^2 and a_i * xi to d (x_i = value)
		if (null != evaluatedFunc.getCoefficients().get(variable, variable)) {
			evaluatedFunc.addOrUpdate("", "",
					evaluatedFunc.getCoefficients().get(variable, variable) * Math.pow(value, 2)
							+ evaluatedFunc.getCoefficients().get(variable, "") * value);
		} else {
			evaluatedFunc.addOrUpdate("", "", evaluatedFunc.getCoefficients().get(variable, "") * value);

		}

		// Find aij * (xi, xj)
		// Update a_ij * xi * xj => (a_ij * value) xj
		for (Map.Entry<String, Double> rowEntry : evaluatedFunc.getCoefficients().row(variable).entrySet()) {
			String variable_j = rowEntry.getKey();
			if (!variable_j.equals("")) {
				double a_ij = rowEntry.getValue();
				evaluatedFunc.addOrUpdate(variable_j, "", a_ij * value);
			}
		}

		// Find aji * (xj, xi)
		// Update a_ji * xi * xj => (a_ji * value) xj
		for (Map.Entry<String, Double> columnEntry : evaluatedFunc.getCoefficients().column(variable).entrySet()) {
			String variable_j = columnEntry.getKey();
			if (!variable_j.equals("")) {
				double a_ji = columnEntry.getValue();
				evaluatedFunc.addOrUpdate(variable_j, "", a_ji * value);
			}
		}

		// Delete all columns and rows that contain the variable
		evaluatedFunc.getCoefficients().row(variable).clear();
		evaluatedFunc.getCoefficients().column(variable).clear();

		return evaluatedFunc;
	}

	/**
	 * Create an ordered set, where each element is a list of ordered intervals.
	 * <br>
	 * The ordering of interval in the list match the ordering of <variable,
	 * interval> from this.getIntervals() This function is already TESTED
	 * 
	 * @param numberOfIntervals is the number of smaller intervals to be divided
	 * @param numberOfAgents    is the limit of number of agents to choose to
	 *                          project
	 * @return
	 */
	public Set<List<Interval>> cartesianProductIntervalExcludingOwner(Map<String, Interval> intervals,
			int numberOfIntervals, int numberOfAgents, boolean isApprox) {
		// List of {intervalSetVar1,...,intervalSetVarN}
		List<Set<Interval>> intervalsSetList = new ArrayList<Set<Interval>>();
		int agentCount = 0;
		for (Map.Entry<String, Interval> entry : intervals.entrySet()) {
			String entryVariable = entry.getKey();
			if (entryVariable.equals(owner))
				continue;

			agentCount++;

			// stop choosing agents to divide the intervals
			if (isApprox && agentCount > numberOfAgents)
				break;

			Interval entryInterval = entry.getValue();

			// Use LinkedHashSet to preserve the ordering of inserted intervals
			Set<Interval> intervalsSet = new LinkedHashSet<Interval>(
					entryInterval.separateIntoAListOfIncreasingIntervals(numberOfIntervals));
			intervalsSetList.add(intervalsSet);
		}

		// The ordering of intervals is preserved in the ordering of variables from
		// intervals
		Set<List<Interval>> productIntervals = Sets.cartesianProduct(intervalsSetList);
		return productIntervals;
	}

	/**
	 * PROJECT operator This function is UNIT-TESTED 1. Divide each interval into to
	 * smaller k intervals accordingly <br>
	 * 2. In <itv1, itv2,..., itvn> in k^(#variables) intervals, get the midpoints
	 * <br>
	 * 3. Evaluate the original function recursively into a unary function where the
	 * selfvariable is the only variable <br>
	 * 4. Get v_i = argmax of the unary function<br>
	 * 5. Evaluate the original function with this argmax value (Now the arity is
	 * decreased by 1)
	 * 
	 * @param numberOfIntervals each agent divides its interval into this number of
	 *                          smaller intervals
	 * @param selfvariable      is the function's owner that needs to be projected
	 *                          out
	 * @param numberOfAgents    numberOfAgents to choose to divide the interval
	 * @return a piecewise function of MultivariateQuadratic functions
	 */
	public PiecewiseMultivariateQuadFunction approxProject(Map<String, Interval> intervalMap, int numberOfIntervals,
			String agentID, int numberOfAgents, boolean isApprox) {
		PiecewiseMultivariateQuadFunction mpwFunc = new PiecewiseMultivariateQuadFunction();
		Set<List<Interval>> productIntervals = cartesianProductIntervalExcludingOwner(intervalMap, numberOfIntervals,
				numberOfAgents, isApprox);

		// for each list of intervals, we have a function
		// the result of this process is a piecewise multivariate function
		for (List<Interval> prodItvList : productIntervals) {
			// to match variable from original this.intervals with the interval from
			// prodItvList
			int varIndex = -1;

			// for each variable, gets the values, evaluate the function
			MultivariateQuadFunction midPointedFunction = new MultivariateQuadFunction(this);

			Map<String, Interval> intervalsOfNewFunction = new HashMap<>();
			for (Map.Entry<String, Interval> entry : intervalMap.entrySet()) {
				String entryVariable = entry.getKey();
				if (entryVariable.equals(agentID)) {
					continue;
				}
				varIndex++;

				if (varIndex == prodItvList.size()) {
					break;
				}

				Interval interval = prodItvList.get(varIndex);
				intervalsOfNewFunction.put(entryVariable, interval);
			}

			// Now adding the rest
			Set<String> intervalVar = intervalsOfNewFunction.keySet();
			Set<String> functionVar = new HashSet<String>(intervalMap.keySet());
			functionVar.removeAll(intervalVar);
			functionVar.remove(agentID);
			for (String varToAdd : functionVar) {
				intervalsOfNewFunction.put(varToAdd, intervalMap.get(varToAdd));
			}

			Map<String, Interval> intervalMapUsedForEvaluation = new HashMap<>();
			intervalMapUsedForEvaluation.putAll(intervalsOfNewFunction);

			MultivariateQuadFunction unaryEvaluatedFunction = midPointedFunction
					.evaluateWithIntervalMap(intervalsOfNewFunction);

			double argmax = unaryEvaluatedFunction.getMaxAndArgMax(intervalMap)[1];

			unaryEvaluatedFunction = midPointedFunction.evaluate(owner, argmax);

			mpwFunc.addToFunctionMapWithInterval(unaryEvaluatedFunction, intervalsOfNewFunction,
					NOT_TO_OPTIMIZE_INTERVAL);
		}

		return mpwFunc;
	}

	public PiecewiseMultivariateQuadFunction analyticalProject(Map<String, Interval> intervalMap) {
		// After project, the function become PiecewiseQuadFunction
		// Also change the owner to the otherAgent
		PiecewiseMultivariateQuadFunction pwFunction = new PiecewiseMultivariateQuadFunction();
		TreeSet<Double> sortedPoint = new TreeSet<>();
		List<Interval> itvSortedList = new ArrayList<>();

		// All of them should have the same interval
		List<MultivariateQuadFunction> candidateFuncList = new ArrayList<>();
		MultivariateQuadFunction critF = getUnaryFunctionAtCriticalPoint(intervalMap);
		Interval intervalOfCritFunction = null;

		if (null != critF) {
			candidateFuncList.add(critF);
			intervalOfCritFunction = critF.getCritFuncIntervalMap().get(critF.getOwner());

			sortedPoint.add(intervalOfCritFunction.getLowerBound());
			sortedPoint.add(intervalOfCritFunction.getUpperBound());
		}
		candidateFuncList.add(evaluateBinaryFunctionX1(intervalMap.get(owner).getLowerBound(), intervalMap));
		candidateFuncList.add(evaluateBinaryFunctionX1(intervalMap.get(owner).getUpperBound(), intervalMap));

		// To compare those quadratic unary functions, we need to solve f1 = x2, and
		// then get the range
		for (int first = 0; first < candidateFuncList.size() - 1; first++) {
			for (int second = first + 1; second < candidateFuncList.size(); second++) {
				sortedPoint.addAll(Utilities.solveUnaryQuadForValues(candidateFuncList.get(first),
						candidateFuncList.get(second), true));
			}
		}

		double LB = intervalMap.get(getOtherVariable()).getLowerBound();
		double UB = intervalMap.get(getOtherVariable()).getUpperBound();

		sortedPoint.add(LB);
		sortedPoint.add(UB);

		itvSortedList = Utilities.createSortedInterval(sortedPoint.subSet(LB, true, UB, true));

		// To compare with the critical function later
		candidateFuncList.remove(critF);

		for (Interval interval : itvSortedList) {
			double midPoint = interval.getMidValue();
			List<Double> evalList = candidateFuncList.stream().map(x -> x.evaluateUnary(midPoint))
					.collect(Collectors.toList());

			double maxEval = Collections.max(evalList);
			int maxIndex = evalList.indexOf(maxEval);
			MultivariateQuadFunction functionToAdd = new MultivariateQuadFunction(candidateFuncList.get(maxIndex));

			// Now, compare with Critical Function
			if (null != critF && intervalOfCritFunction.isTotallyBigger(interval)) {
				// compare with critF
				double critVal = critF.evaluateUnary(midPoint);
				if (compare(critVal, maxEval) > 0) {
					functionToAdd = new MultivariateQuadFunction(critF);
				}
			}

			Map<String, Interval> domain = new HashMap<>();
			domain.put(getOtherVariable(), interval);

			pwFunction.addToFunctionMapWithInterval(functionToAdd, domain, TO_OPTIMIZE_INTERVAL);
		}

		return pwFunction;
	}

	/*
	 * TODO: rewrite this function to make it take derivative for multivariate
	 * function Take the first derivative of quadratic function
	 * 
	 * @param derivativeAgent
	 * 
	 * @return
	 */
	public MultivariateQuadFunction takeFirstPartialDerivative(String derivativeAgent) {
		MultivariateQuadFunction firstPartialDerivative = new MultivariateQuadFunction();

		// constant => 0
		firstPartialDerivative.put("", "", 0.0);

		// axy => ay
		// ax"" => a""
		// ax^2 => 2ax
		for (Entry<String, Double> rowEntry : this.getCoefficients().row(derivativeAgent).entrySet()) {
			if (rowEntry.getKey().equals(derivativeAgent)) {
				firstPartialDerivative.addOrUpdate(derivativeAgent, "", 2 * rowEntry.getValue());
			} else {
				firstPartialDerivative.addOrUpdate(rowEntry.getKey(), "", rowEntry.getValue());
			}
		}

		// ayx => ay
		for (Entry<String, Double> rowEntry : this.getCoefficients().column(derivativeAgent).entrySet()) {
			if (rowEntry.getKey().equals(derivativeAgent)) {
				continue;
			}
			firstPartialDerivative.addOrUpdate(rowEntry.getKey(), "", rowEntry.getValue());
		}

		firstPartialDerivative.setOwner(derivativeAgent);

		firstPartialDerivative.setcritFuncIntervalMap(this.getCritFuncIntervalMap());

		return firstPartialDerivative;
	}

	/**
	 * Return getCoefficients().get(agent1, agent2);
	 * 
	 * @param agent1
	 * @param agent2
	 * @return
	 */
	private Double get(String agent1, String agent2) {
		return getCoefficients().get(agent1, agent2);
	}

	/**
	 * Return getCoefficients().contains(agent1, agent2);
	 * 
	 * @param agent1
	 * @param agent2
	 * @return
	 */
	private boolean contains(String agent1, String agent2) {
		return getCoefficients().contains(agent1, agent2);
	}

	/**
	 * getCoefficients().put(agent1, agent2, value);
	 * 
	 * @param agent1
	 * @param agent2
	 * @param value
	 */
	private void put(String agent1, String agent2, Double value) {
		getCoefficients().put(agent1, agent2, value);
	}

	/**
	 * getCoefficients().remove(agent1, agent2);
	 * 
	 * @param agent1
	 * @param agent2
	 */
	@SuppressWarnings("unused")
	private void remove(String agent1, String agent2) {
		getCoefficients().remove(agent1, agent2);
	}

	/**
	 * Apply for binary function only This function is correct
	 * 
	 * @param x1
	 * @return
	 */
	public MultivariateQuadFunction evaluateBinaryFunctionX1(double x1, Map<String, Interval> intervalMap) {
//    String otherAgent = getOtherAgent();
		String otherAgent = otherVariable;

		double a1 = coefficients.get(owner, owner);
		double b1 = coefficients.get(owner, "");
		double a2 = coefficients.get(otherAgent, otherAgent);
		double b2 = coefficients.get(otherAgent, "");
		double a3 = coefficients.contains(owner, otherAgent) ? coefficients.get(owner, otherAgent)
				: coefficients.get(otherAgent, owner);
		double b3 = coefficients.get("", "");

		return new MultivariateQuadFunction(a2, b2 + a3 * x1, a1 * Math.pow(x1, 2) + b1 * x1 + b3, otherAgent,
				intervalMap.get(otherAgent));
	}

	/**
	 * This function is correct
	 * 
	 * @return UnaryFunction from taking the derivative and set to 0
	 */
	public MultivariateQuadFunction getUnaryFunctionAtCriticalPoint(Map<String, Interval> intervalMap) {
//    String otherAgent = getOtherAgent();
		String otherAgent = otherVariable;

		double a1 = coefficients.get(owner, owner);
		double b1 = coefficients.get(owner, "");
		double a2 = coefficients.get(otherAgent, otherAgent);
		double b2 = coefficients.get(otherAgent, "");
		double a3 = coefficients.contains(owner, otherAgent) ? coefficients.get(owner, otherAgent)
				: coefficients.get(otherAgent, owner);
		double b3 = coefficients.get("", "");

		double newA1 = -Math.pow(a3, 2) / (4 * a1) + a2;
		double newB1 = -b1 * a3 / (2 * a1) + b2;
		double newC1 = -Math.pow(b1, 2) / (4 * a1) + b3;

		double LB = intervalMap.get(owner).getLowerBound();
		double UB = intervalMap.get(owner).getUpperBound();

		// Solve for LB <= df/dx = 0 <= UB
		// Then we get the interval for the other agent, such that root of df/fx = 0
		// belongs to [LB, UB] of owner
		// Intersect the two intervals, if not valid, return null
		// If valid, return the intersected interval
		double bound1 = (LB * 2 * a1 + b1) / (-a3);
		double bound2 = (UB * 2 * a1 + b1) / (-a3);

		Interval boundCritPointInInterval = compare(bound1, bound2) <= 0 ? new Interval(bound1, bound2)
				: new Interval(bound2, bound1);
		Interval resultInterval = intervalMap.get(otherAgent).intersectInterval(boundCritPointInInterval);

		// the two interval can't intersect
		if (null == resultInterval)
			return null;

		return new MultivariateQuadFunction(newA1, newB1, newC1, otherAgent, resultInterval);
	}

	/**
	 * Sequentially evaluate the function with the midpoint from the interval until
	 * the function becomes unary This function is already TESTED
	 * 
	 * @param intervalMap is the map {@code <}variable, interval>
	 * @return
	 */
	public MultivariateQuadFunction evaluateWithIntervalMap(Map<String, Interval> intervalMap) {
		// sequentially evaluate the function
		MultivariateQuadFunction func = new MultivariateQuadFunction(this);
		for (Map.Entry<String, Interval> entry : intervalMap.entrySet()) {
			String variable = entry.getKey();
			double value = entry.getValue().getMidValue();
			func = func.evaluate(variable, value);
		}

		if (func.getNumberOfVariable() != 1) {
			throw new FunctionException("The number of variables should be ONE after being evaluated by a map: "
					+ func.getCritFuncIntervalMap());
		}

		return func;
	}

	public <T> MultivariateQuadFunction evaluateToFunctionGivenValueMap(Map<String, T> valueMap) {
		MultivariateQuadFunction func = new MultivariateQuadFunction(this);
		for (Map.Entry<String, T> entry : valueMap.entrySet()) {
			func = func.evaluate(entry.getKey(), entry.getValue());
		}

		return func;
	}

	public double evaluateToValueGivenValueMap(Map<String, Double> valueMap) {
		MultivariateQuadFunction func = new MultivariateQuadFunction(this);
		int count = 1;
		for (Map.Entry<String, Double> entry : valueMap.entrySet()) {
			if (count < valueMap.size()) {
				func = func.evaluate(entry.getKey(), entry.getValue());
			} else {
				return func.evaluateUnaryFunction(entry.getKey(), entry.getValue());
			}
			count++;
		}
		return Double.MAX_VALUE;
	}
	
	public double evaluateUnaryFunction(String variable, String value) {
	  return evaluateUnaryFunction(variable, Double.valueOf(value));
	}

	/**
	 * Evaluate this unary function. The number of variables is checked if 1 This
	 * function is already TESTED
	 * 
	 * @param variable
	 * @param value
	 * @return
	 */
	public double evaluateUnaryFunction(String variable, Double value) {
		if (getNumberOfVariable() != 1)
			throw new FunctionException(
					"The number of variable should be ONE in order to be evaluated as a unaryFunction: "
							+ getNumberOfVariable());

		if (null != coefficients.get(variable, variable)) {
			return coefficients.get(variable, variable) * Math.pow(value, 2) + coefficients.get(variable, "") * value
					+ coefficients.get("", "");
		} else {
			return coefficients.get(variable, "") * value + coefficients.get("", "");
		}
	}

	/**
	 * This function is already TESTED owner is the only variable left
	 * 
	 * @return max = [0], argmax = [1]
	 */
	public double[] getMaxAndArgMax(Map<String, Interval> intervalMap) {
		double[] result = new double[2];

		if (getNumberOfVariable() != 1) {
			throw new FunctionException(
					"The number of variable should be ONE in order to be evaluated as a unaryFunction: "
							+ critFuncIntervalMap);
		}

		if (!getVariableSet().contains(owner)) {
			throw new FunctionException("Owner, which is the only variable left, is not contained in variable list: "
					+ getVariableSet() + " and the owner " + owner);
		}

		double LB = intervalMap.get(owner).getLowerBound();
		double UB = intervalMap.get(owner).getUpperBound();
		// -b / 2a
		double midPoint = -getB() / (2 * getA());

		double lowerEvaluated = evaluateUnaryFunction(owner, LB);
		double upperEvaluated = evaluateUnaryFunction(owner, UB);
		double midEvaluated = compare(LB, midPoint) <= 0 && compare(midPoint, UB) <= 0
				? evaluateUnaryFunction(owner, midPoint)
				: -Double.MAX_VALUE;

		double max = Math.max(Math.max(lowerEvaluated, upperEvaluated), midEvaluated);
		result[0] = max;

		if (compare(max, lowerEvaluated) == 0)
			result[1] = LB;
		else if (compare(max, upperEvaluated) == 0)
			result[1] = UB;
		else if (compare(max, midEvaluated) == 0)
			result[1] = midPoint;

		return result;
	}

	/**
	 * Assume this is a binary function
	 * 
	 * @return
	 */
//  public String getOtherAgent() {
//    List<String> tempVarList = new ArrayList<>(getVariableSet());
//    tempVarList.remove(owner);
//    return tempVarList.get(0); // assume binary function
//  }

	public double getA() {
		return coefficients.get(owner, owner);
	}

	public double getB() {
		return coefficients.get(owner, "");
	}

	public double getC() {
		return coefficients.get("", "");
	}

	public Set<Double> solveForRootsInsideInterval() {
		TreeSet<Double> rootsAndBounds = new TreeSet<>();

		double a = getA();
		double b = getB();
		double c = getC();

		double LB = critFuncIntervalMap.get(owner).getLowerBound();
		double UB = critFuncIntervalMap.get(owner).getUpperBound();

		rootsAndBounds.add(LB);
		rootsAndBounds.add(UB);

		// Linear
		if (compare(a, 0) == 0) {
			rootsAndBounds.add(-c / b);
			return rootsAndBounds.subSet(LB, false, UB, false);
		}

		// Quadratic
		double delta = pow(b, 2) - 4 * a * c;
		if (compare(delta, 0) == 0) {
			rootsAndBounds.add(-b / (2 * a));
		} else if (compare(delta, 0) > 0) {
			rootsAndBounds.add((-b - sqrt(delta)) / (2 * a));
			rootsAndBounds.add((-b + sqrt(delta)) / (2 * a));
		}

		return rootsAndBounds.subSet(LB, false, UB, false);
	}

	double evaluateUnary(double value) {
		double a = getA();
		double b = getB();
		double c = getC();

		return a * pow(value, 2) + b * value + c;
	}

	/**
	 * Return the number of variables in this function This function is already
	 * TESTED
	 * 
	 * @return the number of variables
	 */
	public int getNumberOfVariable() {
		return getVariableSet().size();
	}

	/**
	 * This function is already TESTED
	 * 
	 * @return the set of variables through intervals.keySet()
	 */
	public Set<String> getVariableSet() {
		Set<String> varSet = new HashSet<>();
		varSet.add(owner);
		if (otherVariable.contains("x")) {
		  varSet.add(otherVariable);
		}
//		varSet.addAll(coefficients.columnKeySet());
//		varSet.addAll(coefficients.rowKeySet());
//		varSet.remove("");

		return varSet;
	}

	/**
	 * @return the coefficients in Table{@code <}String, String, Double> form
	 */
	public Table<String, String, Double> getCoefficients() {
		return coefficients;
	}

	public String getOwner() {
		return owner;
	}

	public void setCoefficients(final Table<String, String, Double> coefficients) {
		this.coefficients = coefficients;
	}

//  public void setIntervals(final Map<String, Interval> intervals) {
//    this.intervals.clear();
//    this.intervals.putAll(intervals);
//  }

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void checkSameSelfAgent(MultivariateQuadFunction func2) {
		if (!owner.equals(func2.getOwner()))
			throw new FunctionException("DIFFERENT owner when checkSameSelfAgent " + owner + " " + func2.getOwner());
	}

//  public void checkSameSelfInterval(MultivariateQuadFunction func2) {
//    if (!intervals.get(owner).equals(func2.getIntervals().get(func2.getOwner())))
//    throw new FunctionException(
//        "DIFFERENT interval when checkSameSelfInterval " + intervals.get(owner) + " " + func2.getIntervals().get(func2.getOwner()));
//  }

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Coefficients = ");
		sb.append(coefficients);
		sb.append(", Owner = ");
		sb.append(owner);
		sb.append(", critFuncIntervalMap = ");
		sb.append(critFuncIntervalMap + "]");
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (!(o instanceof MultivariateQuadFunction)) {
			return false;
		}
		MultivariateQuadFunction function = (MultivariateQuadFunction) o;

		return this.coefficients.equals(function.getCoefficients()) 
		    && this.owner.equals(function.getOwner())
				&& this.otherVariable.equals(function.getOtherVariable());
	}

	@Override
	public int hashCode() {
		return Objects.hash(coefficients, owner, otherVariable);
	}

	public Map<String, Interval> getCritFuncIntervalMap() {
		return critFuncIntervalMap;
	}

	public void setcritFuncIntervalMap(Map<String, Interval> critFuncIntervalMap) {
		this.critFuncIntervalMap = critFuncIntervalMap;
	}

	public String getOtherVariable() {
		return otherVariable;
	}

	public void setOtherVariable(String otherVariable) {
		this.otherVariable = otherVariable;
	}
	
	public boolean hasRandom() {
	  return otherVariable.contains(RANDOM_PREFIX);
	}
}
