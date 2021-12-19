package utilities;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import agent.AgentPDDCOP;
import agent.DcopConstants.DcopAlgorithm;
import function.Interval;
import function.multivariate.MultivariateQuadFunction;

public class Utilities {
	/**
	 * 
	 */
	private static String header = initializeHeader();

	private static String localSearchHeader = initializeLocalSearchHeader();

	private static String effectiveRewardHeaders = initializeEffectiveHeader();

	public static void writeFinalResult(AgentPDDCOP agent) {
		StandardOpenOption writeMode = agent.isFirstInstance() ? TRUNCATE_EXISTING : APPEND;

		String result = agent.isDiscrete() ? getResult(agent) : getResultContiuous(agent);
		if (agent.isFirstInstance()) {
			result = header + result;
		}
		writeToFile(result, agent.getOutputFileName(), writeMode);
	}

	public static String initializeHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("InstanceID" + "\t");
		sb.append("Utility" + "\t");
		sb.append("Time (ms)" + "\t");
		sb.append("PD_DCOP_Algorithm" + "\t");
		sb.append("DCOP_Algorithm" + "\t");
		sb.append("Dynamic" + "\t");
		sb.append("Agents" + "\t");
		sb.append("Switching cost" + "\t");
		sb.append("Discount" + "\t");
		sb.append("Horizon" + "\n");
		return sb.toString();
	}
	
	private static String getResultContiuous(AgentPDDCOP agent) {
	  DecimalFormat df = new DecimalFormat("##.##");

	  StringBuffer sb = new StringBuffer();
	  sb.append(agent.getInstanceID() + "\t");
    sb.append(df.format(agent.getSolutionQuality()) + "\t");
    sb.append(agent.getFinalRuntime() / 1000000 + "\t");
    sb.append(agent.getPDDCOP_Algorithm() + "\t");
    sb.append(agent.getDcop_algorithm() + "\t");
    sb.append(agent.getDynamicType() + "\t");
    sb.append(agent.getAgentCount() + "\t");
    sb.append(agent.getSwitchingCost() + "\t");
    sb.append(df.format(agent.getDiscountFactor()) + "\t");
    sb.append(agent.getHorizon() + "\n");
	  
	  return sb.toString();
	}

	private static String getResult(AgentPDDCOP agent) {
		DecimalFormat df = new DecimalFormat("##.##");

		StringBuffer sb = new StringBuffer();
		sb.append(agent.getInstanceID() + "\t");
		if (agent.isRunningPDDCOPLocalSearch()) {
			// Get the runtime where the solution quality converges
			for (int i = -1; i < agent.getLocalSearchQualityMap().size() - 2; i++) {
				if (Double.compare(agent.getLocalSearchQualityMap().get(i),
						agent.getLocalSearchQualityMap().get(i + 1)) == 0) {
					sb.append(df.format(agent.getLocalSearchQualityMap().get(i)) + "\t");
					if (agent.getDcop_algorithm() == DcopAlgorithm.MGM) {
						long runtime = agent.getLocalSearchRuntimeMap().get(i);
						long totalWastedRuntime = agent.getMGMdifferenceRuntimeMap().values().stream()
								.mapToLong(Long::longValue).sum();
						sb.append((runtime - totalWastedRuntime) / 1000000 + "\t");
						agent.println("getMGMdifferenceRuntimeMap=" + agent.getMGMdifferenceRuntimeMap());
					} else {
						sb.append(agent.getLocalSearchRuntimeMap().get(i) / 1000000 + "\t");
					}
					break;
				}
			}
		} else {
			sb.append(df.format(agent.getSolutionQuality()) + "\t");
			if (agent.getDcop_algorithm() == DcopAlgorithm.MGM) {
				long runtime = agent.getFinalRuntime();
				long totalWastedRuntime = agent.getMGMdifferenceRuntimeMap().values().stream()
						.mapToLong(Long::longValue).sum();
				sb.append((runtime - totalWastedRuntime) / 1000000 + "\t");
				agent.println("getMGMdifferenceRuntimeMap=" + agent.getMGMdifferenceRuntimeMap());
			} else {
				sb.append(agent.getFinalRuntime() / 1000000 + "\t");
			}
		}

		sb.append(agent.getPDDCOP_Algorithm() + "\t");
		sb.append(agent.getDcop_algorithm() + "\t");
		sb.append(agent.getDynamicType() + "\t");
		sb.append(agent.getAgentCount() + "\t");
		sb.append(agent.getSwitchingCost() + "\t");
		sb.append(df.format(agent.getDiscountFactor()) + "\t");
		sb.append(agent.getHorizon() + "\n");

		return sb.toString();
	}

	public static void writeLocalSearchResult(AgentPDDCOP agent) {
		StandardOpenOption writeMode = TRUNCATE_EXISTING;
		String result = getLocalSearchResult(agent);
		writeToFile(localSearchHeader + result, agent.getLocalSearchOutputFileName(), writeMode);
	}

	public static String initializeLocalSearchHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("Iteration" + "\t");
		sb.append("Utility" + "\t");
		sb.append("Time (ms)" + "\t");
		sb.append("InstanceID" + "\t");
		sb.append("PD_DCOP_Algorithm" + "\t");
		sb.append("DCOP_Algorithm" + "\t");
		sb.append("Dynamic" + "\t");
		sb.append("Agents" + "\t");
		sb.append("Switching cost" + "\t");
		sb.append("Discount" + "\t");
		sb.append("Horizon" + "\n");
		return sb.toString();
	}

	private static String getLocalSearchResult(AgentPDDCOP agent) {
		DecimalFormat df = new DecimalFormat("##.##");

		StringBuffer sb = new StringBuffer();

		Map<Integer, Double> localSearchQuality = agent.getLocalSearchQualityMap();
		Map<Integer, Long> localSearchRuntime = agent.getLocalSearchRuntimeMap();
		for (Entry<Integer, Double> entry : localSearchQuality.entrySet()) {
			int iteration = entry.getKey();
			double quality = entry.getValue();
			long runtime = localSearchRuntime.get(iteration);

			sb.append(iteration + "\t");
			sb.append(df.format(quality) + "\t");
			sb.append(runtime / 1000000 + "\t");
			sb.append(agent.getInstanceID() + "\t");
			sb.append(agent.getDynamicType() + "\t");
			sb.append(agent.getPDDCOP_Algorithm() + "\t");
			sb.append(agent.getDcop_algorithm() + "\t");
			sb.append(agent.getAgentCount() + "\t");
			sb.append(agent.getSwitchingCost() + "\t");
			sb.append(df.format(agent.getDiscountFactor()) + "\t");
			sb.append(agent.getHorizon() + "\n");
		}

		return sb.toString();
	}

	public static void writeEffectiveReward(AgentPDDCOP agent) {
		String result = effectiveRewardHeaders + getEffectiveResult(agent);
		writeToFile(result, agent.getOnlineOutputFileName(), TRUNCATE_EXISTING);
//    writeToFile(result, agent.getLocalSearchOutputFileName(), TRUNCATE_EXISTING);    
	}

	private static String initializeEffectiveHeader() {
		StringBuffer sb = new StringBuffer();
		sb.append("Horizon" + "\t");
		sb.append("Eff_Quality" + "\t");
		sb.append("Eff_SwitchingCost" + "\t");
		sb.append("Eff_SolvingTime" + "\t");
		sb.append("PD_DCOP_Algorithm" + "\t");
		sb.append("DCOP_Algorithm" + "\t");
		sb.append("Dynamic" + "\t");
		sb.append("Agents" + "\t");
		sb.append("SwitchingCost" + "\t");
		sb.append("Horizon" + "\n");
		return sb.toString();
	}

	private static String getEffectiveResult(AgentPDDCOP agent) {
		DecimalFormat df = new DecimalFormat("##.##");

		StringBuffer sb = new StringBuffer();

		Map<Integer, Double> effectiveReward = agent.getEffectiveQualityMap();
		Map<Integer, Double> effectiveSwitcCost = agent.getEffectiveSwitchingCostMap();
		Map<Integer, Long> effectiveSolvingTime = agent.getEffectiveSolvingTimeMap();

		for (int horizon = -1; horizon <= agent.getHorizon(); horizon++) {
			double quality = effectiveReward.getOrDefault(horizon, 0D);
			double switchCost = effectiveSwitcCost.getOrDefault(horizon, 0D);
			long solveTime = effectiveSolvingTime.getOrDefault(horizon, 0L);

			sb.append(horizon + "\t");
			sb.append(df.format(quality) + "\t");
			sb.append(df.format(switchCost) + "\t");
			sb.append(solveTime / 1000000 + "\t");
			sb.append(agent.getPDDCOP_Algorithm() + "\t");
			sb.append(agent.getDcop_algorithm() + "\t");
			sb.append(agent.getDynamicType() + "\t");
			sb.append(agent.getAgentCount() + "\t");
			sb.append(agent.getSwitchingCost() + "\t");
			sb.append(agent.getHorizon() + "\n");
		}

		return sb.toString();
	}

	public static void writeToFile(String line, String fileName, StandardOpenOption writeMode) {
		byte data[] = line.getBytes();

		Path p = Paths.get(fileName);

		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p, CREATE, writeMode))) {
			out.write(data, 0, data.length);
			out.flush();
			out.close();
		} catch (IOException x) {
			System.err.println(x);
		}
	}

	/**
	 * Code checked <br>
	 * 
	 * THIS FUNCTION HAS BEEN UNIT TESTED TOGETHER WITH analyticalProject() from Piecewise
	 * 
	 * @param sortedValues
	 * @return list of sorted merged intervals
	 */
	public static List<Interval> createSortedInterval(Set<Double> sortedValues) {
		List<Double> sortedValueList = new ArrayList<>(sortedValues);
		List<Interval> sortedInterval = new ArrayList<>();

		for (int i = 0; i < sortedValueList.size() - 1; i++) {
			sortedInterval.add(new Interval(sortedValueList.get(i), sortedValueList.get(i + 1)));
		}

		return sortedInterval;
	}

	/**
	 * @param func1 QuadraticUnaryFunction
	 * @param func2 QuadraticUnaryFunction
	 * @return a list of sorted intervals
	 */
	public static Set<Double> solveUnaryQuadForValues(MultivariateQuadFunction func1, MultivariateQuadFunction func2,
			boolean isGettingSmallerInterval) {

	  System.out.println("solveUnaryQuadForValues: f1=" + func1 + ", f2=" + func2);
	  
	  TreeSet<Double> valueIntervalSet = new TreeSet<>();

		func1.checkSameSelfAgent(func2);
		Interval intervalOfTheResult = null;

		if (!isGettingSmallerInterval) {
//	      func1.checkSameSelfInterval(func2);
			intervalOfTheResult = func1.getCritFuncIntervalMap().get(func1.getOwner());
		} else {
			intervalOfTheResult = func1.getCritFuncIntervalMap().get(func1.getOwner())
					.intersectInterval(func2.getCritFuncIntervalMap().get(func2.getOwner()));
			if (null == intervalOfTheResult)
				return valueIntervalSet;
		}

		MultivariateQuadFunction diffFunc1Func2 = new MultivariateQuadFunction(func1.getA() - func2.getA(),
				func1.getB() - func2.getB(), func1.getC() - func2.getC(), func1.getOwner(), intervalOfTheResult);

		valueIntervalSet.addAll(diffFunc1Func2.solveForRootsInsideInterval());

		return valueIntervalSet;
	}

}
