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
import java.util.Map;
import java.util.Map.Entry;

import agent.AgentPDDCOP;

public class Utilities {	
	/**
	 * 
	 */
	private static String header = initializeHeader();
	
	private static String localSearchHeader = initializeLocalSearchHeader();
	
	private static String effectiveRewardHeaders = initializeEffectiveHeader();
		 
	public static void writeFinalResult(AgentPDDCOP agent) {
	  StandardOpenOption writeMode = isFirstInstance(agent) ? TRUNCATE_EXISTING : APPEND;

	  String result = getResult(agent);
	  writeToFile(header + result, agent.getOutputFileName(), writeMode);
	}

  public static String initializeHeader() {
    StringBuffer sb = new StringBuffer();
    sb.append("InstanceID" + "\t");
    sb.append("Utility" + "\t");
    sb.append("Time (ms)" + "\t");
    sb.append("Algorithm" + "\t");
    sb.append("Dynamic" + "\t");
    sb.append("Agents" + "\t");
    sb.append("Switching cost" + "\t");
    sb.append("Discount" + "\t");
    sb.append("Horizon" + "\n");
    return sb.toString();
  }
	  
  private static String getResult(AgentPDDCOP agent) {
    DecimalFormat df = new DecimalFormat("##.##");
    
    StringBuffer sb = new StringBuffer();
    sb.append(agent.getInstanceID() + "\t");
    sb.append(df.format(agent.getSolutionQuality()) + "\t");
    sb.append(agent.getSimulatedTime()/1000000 + "\t");
    sb.append(agent.getAlgorithm() + "\t");
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
    sb.append("Algorithm" + "\t");
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
      double runtime = localSearchRuntime.get(iteration);
      
      sb.append(iteration + "\t");
      sb.append(df.format(quality) + "\t");
      sb.append(runtime/1000000 + "\t");
      sb.append(agent.getInstanceID() + "\t");
      sb.append(agent.getDynamicType() + "\t");
      sb.append(agent.getAlgorithm() + "\t");
      sb.append(agent.getAgentCount() + "\t");
      sb.append(agent.getSwitchingCost() + "\t");
      sb.append(df.format(agent.getDiscountFactor()) + "\t");
      sb.append(agent.getHorizon() + "\n");
    }

    return sb.toString();
  }
  
  public static void writeEffectiveReward(AgentPDDCOP agent) {
    StandardOpenOption writeMode = isFirstInstance(agent) ? TRUNCATE_EXISTING : APPEND;

    String result = getEffectiveResult(agent);
    writeToFile(effectiveRewardHeaders + result, agent.getOutputFileName(), writeMode);    
  }
  
  private static String initializeEffectiveHeader() {
    StringBuffer sb = new StringBuffer();
    sb.append("Iteration" + "\t");
    sb.append("EffectiveQuality" + "\t");
    sb.append("EffectiveSwitchingCost" + "\t");
    sb.append("EffectiveSolvingTime" + "\t");
    sb.append("InstanceID" + "\t");
    sb.append("Algorithm" + "\t");
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
    
    for (int iteration = -1; iteration <= agent.getHorizon(); iteration++) {
      double quality = effectiveReward.getOrDefault(iteration, 0D);
      double switchCost = effectiveSwitcCost.getOrDefault(iteration, 0D);
      long runtime = effectiveSolvingTime.getOrDefault(iteration, 0L);
      
      sb.append(iteration + "\t");
      sb.append(df.format(quality) + "\t");
      sb.append(df.format(switchCost) + "\t");
      sb.append(runtime/1000000 + "\t");
      sb.append(agent.getInstanceID() + "\t");
      sb.append(agent.getAlgorithm() + "\t");
      sb.append(agent.getDynamicType() + "\t");
      sb.append(agent.getAgentCount() + "\t");
      sb.append(agent.getHorizon() + "\n");
      sb.append(agent.getSwitchingCost() + "\t");
    }

    return sb.toString();
  }

  public static void writeToFile(String line, String fileName, StandardOpenOption writeMode) {
		byte data[] = line.getBytes();
	    Path p = Paths.get(fileName);

	    try (OutputStream out = new BufferedOutputStream(
	      Files.newOutputStream(p, CREATE, writeMode))) {
	      out.write(data, 0, data.length);
	      out.flush();
	      out.close();
	    } catch (IOException x) {
	      System.err.println(x);
	    }
	}
	
	public static boolean isFirstInstance(AgentPDDCOP agent) {
	  return agent.getInstanceID() == 0;
	}
}
