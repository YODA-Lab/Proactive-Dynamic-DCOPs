package utilities;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

import agent.AgentPDDCOP;

public class Utilities {	
	/**
	 * 
	 */
	public static String header = initializeHeader();
	
  public static DecimalFormat df = new DecimalFormat("##.##");
	
	public static void writeResult(AgentPDDCOP agent) {
	  if (isFirstInstance(agent)) { 
	    writeToFile(header, agent.getOutputFileName());
	  }
	  String result = getResult(agent);
	 	writeToFile(result, agent.getOutputFileName());
	}
	    	
	private static String getResult(AgentPDDCOP agent) {
	  StringBuffer sb = new StringBuffer();
    sb.append(agent.getAgentID() + "\t");
    sb.append(df.format(agent.getSolutionQuality()) + "\t");
    sb.append(agent.getSimulatedTime() + "\t");
    sb.append(agent.getDynamicType() + "\t");
    sb.append(agent.getAlgorithm() + "\t");
    sb.append(agent.getAgentCount() + "\t");
    sb.append(agent.getSwitchingCost() + "\t");
    sb.append(df.format(agent.getDiscountFactor()) + "\n");

    return sb.toString();
  }

  //before local search iteration
	public static void writeUtil_Time_BeforeLS(AgentPDDCOP agent) {
//		String newFileName = "SDPOP" + "_d=" + agent.getAgentC
//									+ "_sw=" + (int) agent.getSwitchingCost()
//									+ "_h=" + agent.getHorizon() + ".txt";  
//		
		if (agent.getInstanceID() == 0) {
			header += "\t" + "Switch";
      writeToFile(header, agent.getOutputFileName());
		}
		
		//startWriting file
		String alg = "SDPOP";
		
		String line = null;
		
		line = "\n" + agent.getInstanceID() + "\t" + alg + "\t" + agent.getAgentCount() + "\t" + 
				agent.getSimulatedTime() + "\t" + df.format(agent.getCurentLocalSearchQuality()) + "\t" + "*";

		writeToFile(line, agent.getOutputFileName());
	}
	
	public static void writeUtil_Time_BeforeLS_Rand(AgentPDDCOP agent) {
		String newFileName = "FIRST_RAND" + "_d=" + agent.getAgentCount()
				+ "_sw=" + (int) agent.getSwitchingCost()
				+ "_h=" + agent.getHorizon() + ".txt";  

		if (agent.getInstanceID() == 0) {
			header += "\t" + "Switch";
      writeToFile(header, agent.getOutputFileName());
		}

		// startWriting file
		String alg = "LS_RAND";

		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		String line = null;

		line = "\n" + agent.getInstanceID() + "\t" + alg + "\t" + agent.getAgentCount() + "\t" +
				agent.getSimulatedTime() + "\t" + df.format(agent.getCurentLocalSearchQuality()) + "\t" + "*";

		writeToFile(line, newFileName);
	}
	
	public static void writeUtil_Time_FW_BW(AgentPDDCOP agent) {
		if (agent.getInstanceID() == 0) {
      writeToFile(header, agent.getOutputFileName());
		}
		
		agent.setStop(true);
		
		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		String line = null;

		line = "\n" + agent.getInstanceID() + "\t" + agent.getAlgorithm() + "\t" + agent.getAgentCount() + "\t" + 
				agent.getSimulatedTime() + "\t" + df.format(agent.getSolutionQuality());
					
		writeToFile(line, agent.getOutputFileName());
	}

	public static void writeUtil_Time_LS(AgentPDDCOP agent) {
		if (agent.getCurentLocalSearchQuality() == agent.getBestLocalSearchQuality() && agent.isStop() == false) {
			if (agent.getInstanceID() == 0) {
				writeToFile(header, agent.getOutputFileName());
			}
			
			int countIteration = agent.getLsIteration() + 1;
			//startWriting file
			agent.setStop(true);
//			String alg = ND_DCOP.algTypes[agent.algorithm];
			
			
			DecimalFormat df = new DecimalFormat("##.##");
			df.setRoundingMode(RoundingMode.DOWN);
			String line = "\n" + agent.getInstanceID() + "\t" + agent.getAlgorithm() + "\t" + agent.getAgentCount() + "\t" + 
					agent.getSimulatedTime() + "\t" + df.format(agent.getBestLocalSearchQuality()) + "\t" + (countIteration - 1);
			
			writeToFile(line, agent.getOutputFileName());
		}
	}
	
	public static void writeToFile(String line, String fileName) {
		byte data[] = line.getBytes();
	    Path p = Paths.get(fileName);

	    try (OutputStream out = new BufferedOutputStream(
	      Files.newOutputStream(p, CREATE, APPEND))) {
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
	  df.setRoundingMode(RoundingMode.DOWN);
	  
	  StringBuffer sb = new StringBuffer();
    sb.append("InstanceID" + "\t");
    sb.append("Utility" + "\t");
    sb.append("Time (ms)" + "\t");
    sb.append("Dynamic" + "\t");
    sb.append("Algorithm" + "\t");
    sb.append("Agents" + "\t");
    sb.append("Switching cost" + "\t");
    sb.append("Discount" + "\n");
    return sb.toString();
	}
}
