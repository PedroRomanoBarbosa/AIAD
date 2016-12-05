package agents;

import java.util.HashMap;
import java.util.Random;

import jade.core.Agent;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import data.Task;


public class Collaborator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private HashMap<String,Integer> skills; // skillId -> value(probabilistic)
	private Task currentTask; // The current task this collaborator is doing
	private boolean ocuppied; // Whether this agent is occupied or not
	
	public HashMap<String, Integer> getSkills() {
		return this.skills;
	}
	
	public Task getCurrentTask() {
		return this.currentTask;
	}
	
	public void setTask(Task task) {
		this.currentTask = task;
	}
	
	public void addSkill(String skillId, Integer performance) {
		this.skills.put(skillId, performance);
	}

	@Override
	protected void setup() {
		skills = new HashMap<String, Integer>();
		ocuppied = false;
		createFIPARequestBehaviour();
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
	
}
