package agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Queue;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.Property;
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
	private String currentTask; // The current task this collaborator is doing
	private boolean ocuppied; // Whether this agent is occupied or not
	private HashMap<CollaboratorData,AID> collaboratorData; //skillId -> value(probabilistic)
	private Queue<AID> requested; // Queue with the coordinators that requested this collaborators service
	
	public Collaborator(){
		skills = new HashMap<String, Float>();
	}
	
	public HashMap<String, Float> getSkills() {
		return this.skills;
	}
	
	public void addSkill(String skillId, Float performance) {
		this.skills.put(skillId, performance);
	}
	
	public void setSkills(HashMap<String, Float> skills) {
		this.skills = skills;
	}

	@Override
	protected void setup() {
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
			protected ACLMessage handleRequest(ACLMessage request) {
				System.out.println("Collaborator " + getLocalName() + ": REQUEST received from " + request.getSender().getName()); 
				System.out.println("Action is " + request.getContent());
				String[] args = request.getContent().split(" ");
				if(args[0].equals("REQUEST") && !ocuppied) {
					ocuppied = true;
					currentTask = args[1];
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					return agree;
				} else {
					ACLMessage refuse = request.createReply();
					refuse.setPerformative(ACLMessage.REFUSE);
					requested.add(request.getSender());
					return refuse;
				}
			}
			
			@Override
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				doTask();
				System.out.println("Agent " + getLocalName() + ": Action successfully performed");
				ACLMessage inform = request.createReply();
				inform.setContent("DONE " + currentTask);
				inform.setPerformative(ACLMessage.INFORM);
				ocuppied = false;
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
		doWait(5000);
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
  		sd.setName("Projects Collaborator");
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
