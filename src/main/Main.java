package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Main {

  public static void main(String[] args) {    
    // parameters for running experiments
    String inputFileName = args[1]; // "rep_0_d10.dzn";     
    int agentCount = Integer.parseInt(inputFileName.replaceAll("rep_","").replaceAll(".dzn","").split("_d")[1]);
    
    Runtime rt = Runtime.instance();
    rt.setCloseVM(true);
    Profile p = new ProfileImpl();
    p.setParameter(Profile.MAIN_HOST, "localhost");
    p.setParameter(Profile.GUI, "false");
    ContainerController cc = rt.createMainContainer(p);
    for (int i = 1; i <= agentCount; i++) {
      AgentController ac;
      try {
        ac = cc.createNewAgent(String.valueOf(i), "agent.AgentPDDCOP", args);
        ac.start();
      } catch (StaleProxyException e) {
        e.printStackTrace();
      }
    }
  }
}