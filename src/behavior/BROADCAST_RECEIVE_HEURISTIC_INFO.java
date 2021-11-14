//package behavior;
//
//import jade.core.AID;
//import jade.core.behaviours.OneShotBehaviour;
//import jade.lang.acl.ACLMessage;
//import jade.lang.acl.MessageTemplate;
//import jade.lang.acl.UnreadableException;
//
//import java.util.ArrayList;
//
//import agent.AgentPDDCOP;
//import static agent.DcopConstants.*;
//
////MAX-DEGREE HEURISTIC
//public class BROADCAST_RECEIVE_HEURISTIC_INFO extends OneShotBehaviour {
//
//  /**
//   * 
//   */
//  private static final long serialVersionUID = -1668465137225772273L;
//  private AgentPDDCOP agent;
//  
//  public BROADCAST_RECEIVE_HEURISTIC_INFO(AgentPDDCOP agent) {
//    super(agent);
//    this.agent = agent;
//  }
//  
//  @Override
//  public void action() {    
//    Integer maxDegreeHeuristic = agent.getNeighborAIDSet().size();
//    
//    //broadcast to neighbors
//    for (AID neighbor:agent.getNeighborAIDSet()) {
//      agent.sendObjectMessage(neighbor, maxDegreeHeuristic, PSEUDO_INFO);
//    }
//    
//    //receive messages from neighbors
//    ArrayList<ACLMessage> messageList = waitingForMessageFromNeighbors(PSEUDO_INFO);
//    for (ACLMessage message:messageList) {
//      Integer infoFromMessage = null;
//      AID sender = message.getSender();
//      try {
//        infoFromMessage = (Integer) message.getContentObject();
//      } catch (UnreadableException e) {
//        e.printStackTrace();
//      }
//      agent.getConstraintInfoMap().put(sender, infoFromMessage);
//    }
//  }
//  
//  public ArrayList<ACLMessage> waitingForMessageFromNeighbors(int msgCode) {
//    ArrayList<ACLMessage> messageList = new ArrayList<ACLMessage>();
//    while (messageList.size() < agent.getNeighborAIDSet().size()) {
//      MessageTemplate template = MessageTemplate.MatchPerformative(msgCode);
//      ACLMessage receivedMessage = myAgent.blockingReceive(template);
////      if (receivedMessage != null) {
//        messageList.add(receivedMessage);
////      }
////      else
////        block();
//    }
//    return messageList;
//  }
//}
