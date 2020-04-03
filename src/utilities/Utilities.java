package utilities;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;

import agent.AgentPDDCOP;

public class Utilities {	
	/**
	 * 
	 */
	public static String header = initializeHeader();
	
	public static String localSearchHeader = initializeLSHeader();
		 
	public static void writeResult(AgentPDDCOP agent) {
	  java.nio.file.StandardOpenOption writeMode = APPEND;
	  
	  String result = getResult(agent, agent.isRunningLocalSearch());

	  if (isFirstInstance(agent)) { 
	    writeMode = TRUNCATE_EXISTING;
	    writeToFile(header, agent.getOutputFileName(), writeMode);
	  }
	 	writeToFile(result, agent.getOutputFileName(), writeMode);
	}
	    	
	private static String initializeLSHeader() {
    // TODO Auto-generated method stub
    return null;
  }

  private static String getResult(AgentPDDCOP agent, boolean isLocalSearch) {
    DecimalFormat df = new DecimalFormat("##.##");
    
    StringBuffer sb = new StringBuffer();
    sb.append(agent.getInstanceID() + "\t");
    if (!isLocalSearch) {
      sb.append(df.format(agent.getSolutionQuality()) + "\t");
      sb.append(agent.getSimulatedTime()/1000000 + "\t");
    }
    else {
      sb.append(df.format(agent.getBestLocalSearchQuality()) + "\t");
      sb.append(agent.getBestLocalSearchRuntime()/1000000+ "\t");      
    }
    sb.append(agent.getActualSolutionQuality());
    sb.append(agent.getAlgorithm() + "\t");
    sb.append(agent.getDynamicType() + "\t");
    sb.append(agent.getAgentCount() + "\t");
    sb.append(agent.getSwitchingCost() + "\t");
    sb.append(df.format(agent.getDiscountFactor()) + "\n");

    return sb.toString();
  }
  
  private static String getLocalSearchResult(AgentPDDCOP agent) {
    DecimalFormat df = new DecimalFormat("##.##");
    
    StringBuffer sb = new StringBuffer();
    sb.append(agent.getLocalSearchIteration() + "\t");
    sb.append(df.format(agent.getSolutionQuality()) + "\t");
    sb.append(agent.getSimulatedTime() + "\t");
    sb.append(agent.getInstanceID() + "\t");
    sb.append(agent.getDynamicType() + "\t");
    sb.append(agent.getAlgorithm() + "\t");
    sb.append(agent.getAgentCount() + "\t");
    sb.append(agent.getSwitchingCost() + "\t");
    sb.append(df.format(agent.getDiscountFactor()) + "\n");

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
	
	public static String initializeHeader() {	  	  
	  StringBuffer sb = new StringBuffer();
    sb.append("InstanceID" + "\t");
    sb.append("Utility" + "\t");
    sb.append("Time (ms)" + "\t");
    sb.append("Actual Utility" + "\t");
    sb.append("Algorithm" + "\t");
    sb.append("Dynamic" + "\t");
    sb.append("Agents" + "\t");
    sb.append("Switching cost" + "\t");
    sb.append("Discount" + "\n");
    return sb.toString();
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
    sb.append("Discount" + "\n");
    return sb.toString();
  }

  public static void writeLocalSearchResult(AgentPDDCOP agent) {
    // TODO Auto-generated method stub
    
  }
}
