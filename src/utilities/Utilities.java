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
	public static String headerLine = "InstanceID" + "\t" + "Alg" + "\t" + "Decision"
									+ "\t" + "Time" + "\t" + "Utility";
	
	//before local search iteration
	public static void writeUtil_Time_BeforeLS(AgentPDDCOP agent) {
//		String newFileName = "SDPOP" + "_d=" + agent.getAgentC
//									+ "_sw=" + (int) agent.getSwitchingCost()
//									+ "_h=" + agent.getHorizon() + ".txt";  
//		
		if (agent.getInstanceID() == 0) {
			headerLine += "\t" + "Switch";
			writeHeaderLineToFile(agent.getOutputFileName());
		}
		
		//startWriting file
		String alg = "SDPOP";
		
		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
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
			headerLine += "\t" + "Switch";
			writeHeaderLineToFile(newFileName);
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
			writeHeaderLineToFile(agent.getOutputFileName());
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
				writeHeaderLineToFile(agent.getOutputFileName());
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
	
	public static void writeHeaderLineToFile(String outputFile) {
		byte data[] = headerLine.getBytes();
	    Path p = Paths.get(outputFile);

	    try (OutputStream out = new BufferedOutputStream(
	      Files.newOutputStream(p, CREATE, APPEND))) {
	      out.write(data, 0, data.length);
	      out.flush();
	      out.close();
	    } catch (IOException x) {
	      System.err.println(x);
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
}
