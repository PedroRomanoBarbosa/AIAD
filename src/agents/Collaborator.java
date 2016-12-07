package agents;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import data.CollaboratorData;
import data.Task;


public class Collaborator extends Agent{
	private static final long serialVersionUID = 1l;
	private String id;
	private HashMap<String, Float> skills; // skillId -> value(probabilistic)
	private Task currentTask; // The current task this collaborator is doing
	private boolean ocuppied; // Whether this agent is occupied or not
	private HashMap<CollaboratorData,AID> collaboratorData; //skillId -> value(probabilistic)
	
	public HashMap getSkills() {
		return this.collaboratorData;
	}
	
	public Task getCurrentTask() {
		return this.currentTask;
	}
	
	public void setTask(Task task) {
		this.currentTask = task;
	}
	
	public void addSkill(String skillId, Float performance) {
		this.skills.put(skillId, performance);
	}

	@Override
	protected void setup() {
		skills = new HashMap<String, Float>();
		ocuppied = false;
		
		// TO CREATE COLLABORATORS FROM GUI
		Object[] args = getArguments();
		String s;
        if (args != null) {
            for (int i = 0; i < args.length-1; i+=2) {
                s = (String) args[i];
                skills.put((String)args[i], Float.parseFloat((String) args[i+1]));
            }
        }
        
        //Create Behaviours
        createFIPARequestBehaviour();
        
        registerService();
	}
	
	/**
	 * Creates a FIPA request protocol behaviour.
	 */
	private void createFIPARequestBehaviour() {
		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
		addBehaviour(new AchieveREResponder(this, template) {
			
			@Override
			protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
				System.out.println("Collaborator " + getLocalName() + ": REQUEST received from " + request.getSender().getName()); 
				System.out.println("Action is " + request.getContent());
				if(!ocuppied) {
					ocuppied = true;
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					return agree;
				} else {
					throw new RefuseException("check-failed");
				}
			}
			
			@Override
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				doTask();
				System.out.println("Agent " + getLocalName() + ": Action successfully performed");
				ACLMessage inform = request.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				return inform;
			}
		} );
	}
	
	/**
	 * Method to simulate the collaborator performing a task.
	 */
	private void doTask() {
		Random rand = new Random();
		int n = rand.nextInt(5) + 1;
		doWait(n * 1000);
	}
	
	//TODO Remove this method
	public CollaboratorData getCollaboratorDataByAID(AID collaboratorAID) {
		CollaboratorData collaboratorData = null;
		for (Entry<CollaboratorData, AID> entry : this.collaboratorData.entrySet()) {
            if (entry.getValue().equals(collaboratorAID)) {
                collaboratorData = entry.getKey();
                break;
            }
        }
		return collaboratorData;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	private void registerService() {
		DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName(getLocalName() + " project collaborator");
  		sd.setType("collaborator");
  		for(Map.Entry<String, Float> entry : skills.entrySet()) {
  			sd.addProperties(new Property(entry.getKey(), entry.getValue()));
  		}
  		sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
  		dfd.addServices(sd);
  		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
}
