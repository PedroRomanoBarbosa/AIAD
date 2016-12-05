package agents;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import data.Task;
import models.Model;

public class Coordinator extends Agent{
	private static final long serialVersionUID = 1L;
	
	// Project Meta Information
	private Model chosenModel; // The trust model chosen
	private List<AID> collaborators; // All the collaborators AIDs
	private Queue<Task> tasks; // A task Queue ordered by crescent number of precedences
	private List<Task> tasksList;
	private List<Integer> tasksCompleted; // List of Tasks Ids already done
	private boolean projectFinished; // Boolean flag indicating the project is over
	private double projectDuration; // The duration of the project when ended
	private Task selectedTask;
	private int potencialCollaboratorIndex;
	
	// Coordinator Behaviours
	private OneShotBehaviour createProjectBehaviour;
	private OneShotBehaviour endProjectBehaviour;
	private OneShotBehaviour assignTaksBehaviour, sendTaskBehaviour;

	@Override
	protected void setup() {
		collaborators = new ArrayList<AID>();
		tasks = new PriorityQueue<Task>();
		tasksCompleted = new ArrayList<Integer>();
		projectFinished = false;
		
		//Tests
		tasks.add(new Task());
		tasks.add(new Task());
		tasks.add(new Task());
		
		// Create Behaviours
		createProjectBehaviour();
		createAssignTaskBehaviour();
		createSendTaskBehaviour();
		
		// Add Behaviours
		addBehaviour(createProjectBehaviour);
	}
	
	public ArrayList<AID> getCollaboratorsAIDs(){
		return (ArrayList<AID>) this.collaborators;
	}
	
	public void setModel(Model model){
		this.chosenModel = model;
	}
	
	public boolean isProjectFinished(){
		return projectFinished;
	}
	
	public void addCollaborator(AID collaboratorAID){
		collaborators.add(collaboratorAID);
	}
	
	public void addTask(Task task){
		tasks.add(task);
	}
	
	public void createProjectBehaviour(){
		createProjectBehaviour = new OneShotBehaviour() {
			@Override
			public void action() {
				getAgents();
				addBehaviour(assignTaksBehaviour);
			}
		};
	}
	
	private void getAgents() {
		try {
			SearchConstraints c = new SearchConstraints();
	        c.setMaxResults(-1l);
			AMSAgentDescription[] agents = AMSService.search(this, new AMSAgentDescription(), c);
			System.out.println("All Collaborator agents:");
			for (AMSAgentDescription amsAgentDescription : agents) {
				if(amsAgentDescription.getName().getLocalName().equals("Lulu")){
					System.out.println(amsAgentDescription.getName());
					collaborators.add(amsAgentDescription.getName());
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	private void createAssignTaskBehaviour() {
		assignTaksBehaviour = new OneShotBehaviour() {
			
			@Override
			public void action() {
				//printState();
				boolean assign = true;
				
				// If the task queue still has tasks
				if(!tasks.isEmpty()) {
					Task task = tasks.poll();
					
					// Check if task precedences are all done or not
					for(int i = 0; (i < task.getPrecedenceNumber()) && (assign == true); i++) {
						int id = task.getPrecedence(i);
						
						// If there is at least one precedence to be done do not assign this task and end loop
						if(!tasksCompleted.contains(id)){
							assign = false;
							
							// Push this task to the end of the tasks queue
							tasks.add(task);
						}
					}
					
					// If there are no precedences to be done in this task try to assign it
					if(assign) {
						selectedTask = task;
						addBehaviour(sendTaskBehaviour);
						//tasksCompleted.add(task.getTaskId());
					}
				} else {
					// End project!
					System.out.println("Project finished");
					projectFinished = true;
				}
			}
		};
	}
	
	private void createSendTaskBehaviour() {
		sendTaskBehaviour = new OneShotBehaviour() {
			
			@Override
			public void action() {
				//TODO se estiverem todos ocupados esperar
				
				//TODO Verificar se o colaborador pode efetuar a tarefa
				
				//TODO Verificar qual o melhor(TRUST) para fazer a tarefa
				
				// Send FIPA complient request protocol message
				AID collaborator = collaborators.get(potencialCollaboratorIndex); //TODO change to best collaborator
				ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
				message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				message.addReceiver(collaborator);
				message.setContent("ASSIGN TASK " + selectedTask.getTaskId());
				
				//Initiate request //TODO Maybe this approach can cause problems...
				boolean assign = false;
				boolean block = false;
				//TODO check for valid and/or best collaborator to do task
				addBehaviour(new AchieveREInitiator(getAgent(), message) {

					@Override
					protected void handleInform(ACLMessage inform) {
						System.out.println("Agent "+ inform.getSender().getName() + " successfully performed the requested action");
					}
					
					@Override
					protected void handleAgree(ACLMessage agree) {
						System.out.println("Agent "+ agree.getSender().getName() + " agreed to perform the requested action");
						addBehaviour(assignTaksBehaviour);
					}

					@Override
					protected void handleRefuse(ACLMessage refuse) {
						System.out.println("Agent " + refuse.getSender().getName() + " is ocuppied");
						potencialCollaboratorIndex = 0; //TODO change this to the second best available
						addBehaviour(sendTaskBehaviour);
					}

					@Override
					protected void handleFailure(ACLMessage failure) {
						System.out.println("There was a failure!");
					}
					
				});
			}
		};
	}
	
	public void assignTask(Task t) {
		
	}
	
	public void printState() {
		System.out.println("#### STATE ####");
		List<Task> l = new ArrayList<Task>(tasks);
		System.out.println("Number of tasks: " + l.size());
		for (Task task : l) {
			System.out.println(task);
		}
		System.out.print("TASKS DONE: (");
		for (int i : tasksCompleted) {
			System.out.print(" " + i);
		}
		System.out.println(" )\n");
	}
	
}
