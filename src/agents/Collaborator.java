package agents;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import data.CollaboratorData;
import data.Task;


public class Collaborator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, Float> skills; // skillId -> value(probabilistic)
	private Task currentTask; // The current task this collaborator is doing
	private boolean ocuppied; // Whether this agent is occupied or not
	private Coordinator projectCoordinator; //The project coordinator
	private HashMap<CollaboratorData,AID> collaboratorData; //skillId -> value(probabilistic)
	
	
	
	public HashMap getSkills(){
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
		createFIPARequestBehaviour();
		
		
		// TO CREATE COLLABORATORS FROM GUI
		Object[] args = getArguments();
		String s;	
        if (args != null) {
            for (int i = 0; i<args.length; i++) {
                s = (String) args[i];
                //System.out.println("p" + i + ": " + s);
                
                String[] elements = s.split("\\s{1,}");
                //System.out.println(Arrays.toString(elements));
                
                for (int i1 = 0; i1 < elements.length-1; i1+=2) {
                	skills.put( elements[i1], Float.parseFloat((String) elements[i1+1]));
                }
                System.out.println(skills);
            }
        }
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
	
	public CollaboratorData getCollaboratorDataByAID(AID collaboratorAID){
		CollaboratorData collaboratorData = null;
		for (Entry<CollaboratorData, AID> entry : this.collaboratorData.entrySet()) {
            if (entry.getValue().equals(collaboratorAID)) {
                collaboratorData = entry.getKey();
                break;
            }
        }
		return collaboratorData;
	}


}
