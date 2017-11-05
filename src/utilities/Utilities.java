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

import agent.ND_DCOP;

public class Utilities {	
	/**
	 * 
	 */
	public static String headerLine = "InstanceID" + "\t" + "Alg" + "\t" + "Decision"
									+ "\t" + "Time" + "\t" + "Utility";
	
	//before local search iteration
	public static void writeUtil_Time_BeforeLS(ND_DCOP agent) {
		String newFileName = "SDPOP" + "_d=" + agent.noAgent
									+ "_sw=" + (int) agent.switchingCost
									+ "_h=" + agent.h + ".txt";  
		
		if (agent.instanceD == 0) {
			headerLine += "\t" + "Switch";
			writeHeaderLineToFile(newFileName);
		}
		
		//startWriting file
		String alg = "SDPOP";
		
		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		String line = null;
		
		line = "\n" + agent.instanceD + "\t" + alg + "\t" + agent.noAgent + "\t" + 
				agent.getOldLSRunningTime() + "\t" + df.format(agent.getUtilityAndCost()) + "\t" + "*";

		writeToFile(line, newFileName);
	}
	
	public static void writeUtil_Time_BeforeLS_Rand(ND_DCOP agent) {
		String newFileName = "FIRST_RAND" + "_d=" + agent.noAgent
				+ "_sw=" + (int) agent.switchingCost
				+ "_h=" + agent.h + ".txt";  

		if (agent.instanceD == 0) {
			headerLine += "\t" + "Switch";
			writeHeaderLineToFile(newFileName);
		}

		// startWriting file
		String alg = "LS_RAND";

		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		String line = null;

		line = "\n" + agent.instanceD + "\t" + alg + "\t" + agent.noAgent + "\t" +
				agent.getOldLSRunningTime() + "\t" + df.format(agent.getUtilityAndCost()) + "\t" + "*";

		writeToFile(line, newFileName);
	}
	
	public static void writeUtil_Time_FW_BW(ND_DCOP agent) {
		if (agent.instanceD == 0)
			writeHeaderLineToFile(agent.varDecisionFileName);
		
		agent.setStop(true);
		String alg = ND_DCOP.algTypes[agent.algorithm];
		double runningTime = agent.getEndTime() - agent.getStartTime();
		
		DecimalFormat df = new DecimalFormat("##.##");
		df.setRoundingMode(RoundingMode.DOWN);
		String line = null;

		line = "\n" + agent.instanceD + "\t" + alg + "\t" + agent.noAgent + "\t" + 
				runningTime + "\t" + df.format(agent.getTotalGlobalUtility());
					
		writeToFile(line, agent.varDecisionFileName);
	}

	public static void writeUtil_Time_LS(ND_DCOP agent) {
		if (agent.getUtilityAndCost() == agent.getOldLSUtility() && agent.isStop() == false) {
			if (agent.instanceD == 0) {
				writeHeaderLineToFile(agent.varDecisionFileName);
			}
			
			int countIteration = agent.getLsIteration() + 1;
			//startWriting file
			agent.setStop(true);
			String alg = ND_DCOP.algTypes[agent.algorithm];
			
			DecimalFormat df = new DecimalFormat("##.##");
			df.setRoundingMode(RoundingMode.DOWN);
			String line = null;

			line = "\n" + agent.instanceD + "\t" + alg + "\t" + agent.noAgent + "\t" + 
					agent.getOldLSRunningTime() + "\t" + df.format(agent.getOldLSUtility()) + "\t" + (countIteration-1);
			
			writeToFile(line, agent.varDecisionFileName);
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
