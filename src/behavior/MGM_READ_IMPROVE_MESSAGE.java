package behavior;

import java.util.ArrayList;
import java.util.List;

import agent.AgentPDDCOP;
import agent.DcopConstants.MessageType;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MGM_READ_IMPROVE_MESSAGE extends CyclicBehaviour {

  /**
   * 
   */
  private static final long serialVersionUID = -8117289126233923082L;
  
  @SuppressWarnings("unused")
  private AgentPDDCOP agent;
  
  private List<ACLMessage> messageList = new ArrayList<ACLMessage>();
    
  public MGM_READ_IMPROVE_MESSAGE(AgentPDDCOP agent) {
    super(agent);
    this.agent = agent;
  }

  @Override
  public void action() {
    MessageTemplate template = MessageTemplate.MatchPerformative(MessageType.MGM_VALUE.ordinal());
    ACLMessage receivedMessage = myAgent.receive(template);
    
    if (receivedMessage != null) {
      messageList.add(receivedMessage);
    } 
    else {
      block();
    } 
  }
}
