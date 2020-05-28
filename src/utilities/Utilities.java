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
import agent.AgentPDDCOP.DcopAlgorithm;

public class Utilities {	
	/**
	 * 
	 */
	private static String header = initializeHeader();
	
	private static String localSearchHeader = initializeLocalSearchHeader();
	
	private static String effectiveRewardHeaders = initializeEffectiveHeader();
		 
	public static void writeFinalResult(AgentPDDCOP agent) {
	  StandardOpenOption writeMode = agent.isFirstInstance() ? TRUNCATE_EXISTING : APPEND;

	  String result = getResult(agent);
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
	  
  private static String getResult(AgentPDDCOP agent) {
    DecimalFormat df = new DecimalFormat("##.##");
    
    StringBuffer sb = new StringBuffer();
    sb.append(agent.getInstanceID() + "\t");    
    if (agent.isRunningPDDCOPLocalSearch()) {
      // Get the runtime where the solution quality converges
      for (int i = -1; i < agent.getLocalSearchQualityMap().size() - 2; i++) {
        if (Double.compare(agent.getLocalSearchQualityMap().get(i), agent.getLocalSearchQualityMap().get(i + 1)) == 0) {
          sb.append(df.format(agent.getLocalSearchQualityMap().get(i)) + "\t");
          if (agent.getDcop_algorithm() == DcopAlgorithm.MGM) {
            long runtime = agent.getLocalSearchRuntimeMap().get(i);
            long totalWastedRuntime = agent.getMGMdifferenceRuntimeMap().values().stream().mapToLong(Long::longValue).sum();           
            sb.append((runtime - totalWastedRuntime)/1000000 + "\t");
            agent.print("getMGMdifferenceRuntimeMap=" + agent.getMGMdifferenceRuntimeMap());
          } 
          else {
            sb.append(agent.getLocalSearchRuntimeMap().get(i)/1000000 + "\t");
          }
          break;
        }
      }
    }
    else {
      sb.append(df.format(agent.getSolutionQuality()) + "\t");
      if (agent.getDcop_algorithm() == DcopAlgorithm.MGM) {
        long runtime = agent.getFinalRuntime();
        long totalWastedRuntime = agent.getMGMdifferenceRuntimeMap().values().stream().mapToLong(Long::longValue).sum();           
        sb.append((runtime - totalWastedRuntime)/1000000 + "\t");
        agent.print("getMGMdifferenceRuntimeMap=" + agent.getMGMdifferenceRuntimeMap());
      } 
      else {
        sb.append(agent.getFinalRuntime()/1000000 + "\t");
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
      sb.append(runtime/1000000 + "\t");
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
    StandardOpenOption writeMode = agent.isFirstInstance() ? TRUNCATE_EXISTING : APPEND;

    String result = getEffectiveResult(agent);
    if (agent.isFirstInstance()) {
      result = effectiveRewardHeaders + result;
    }
    writeToFile(result, AgentPDDCOP.OUTPUT_FOLDER + agent.getOutputFileName(), writeMode);    
  }
  
  private static String initializeEffectiveHeader() {
    StringBuffer sb = new StringBuffer();
    sb.append("Iteration" + "\t");
    sb.append("EffectiveQuality" + "\t");
    sb.append("EffectiveSwitchingCost" + "\t");
    sb.append("EffectiveSolvingTime" + "\t");
    sb.append("InstanceID" + "\t");
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
    
    for (int iteration = -1; iteration <= agent.getHorizon(); iteration++) {
      double quality = effectiveReward.getOrDefault(iteration, 0D);
      double switchCost = effectiveSwitcCost.getOrDefault(iteration, 0D);
      long runtime = effectiveSolvingTime.getOrDefault(iteration, 0L);
      
      sb.append(iteration + "\t");
      sb.append(df.format(quality) + "\t");
      sb.append(df.format(switchCost) + "\t");
      sb.append(runtime/1000000 + "\t");
      sb.append(agent.getInstanceID() + "\t");
      sb.append(agent.getPDDCOP_Algorithm() + "\t");
      sb.append(agent.getDcop_algorithm() + "\t");
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
}
