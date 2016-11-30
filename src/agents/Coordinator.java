package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
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
	
	// Coordinator Behaviours
	private OneShotBehaviour createProjectBehaviour;
	private OneShotBehaviour endProjectBehaviour;
	private SimpleBehaviour assignTaks;
	
	public Coordinator() {

		collaborators = new ArrayList<AID>();
		tasks = new PriorityQueue<Task>();
		tasksCompleted = new ArrayList<Integer>();
		projectFinished = false;
		
		//Tests
		tasks.add(new Task(1));
		tasks.add(new Task());
		
		// Create Behaviours
		createProjectBehaviour();
		createAssignTaskBehaviour();
		
		// Add Behaviours
		addBehaviour(createProjectBehaviour);
		addBehaviour(assignTaks);
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
				
			}
		};
	}
	
	private void createAssignTaskBehaviour() {
		assignTaks = new SimpleBehaviour() {

			@Override
			public boolean done() {
				return projectFinished;
			}
			
			@Override
			public void action() {
				printState();
				// If the task queue still has tasks
				if(!tasks.isEmpty()) {
					boolean assign = true;
					Task t = tasks.poll();
					
					// Check if task precedences are all done or not
					for(int i = 0; i < t.getPrecedenceNumber(); i++) {
						Integer id = t.getPrecedence(i);
						
						// If there is at least one precedence to be done do not assign this task
						if(!tasksCompleted.contains(id)){
							assign = false;
							i = t.getPrecedenceNumber();
							
							// Push this task to the end of the tasks queue
							tasks.add(t);
						}
					}
					
					// If there are no precedences to be done in this task try to assign it
					if(assign) {
						assignTask(t);
						tasksCompleted.add(t.getTaskId());
					}
				} else {
					// End project!
					System.out.println("Project finished");
					projectFinished = true;
				}
			}
		};
	}
	
	public void assignTask(Task t) {
		//TODO se estiverem todos ocupados esperar
		
		//TODO Verificar se o colaborador pode efetuar a tarefa
		
		//TODO Verificar qual o melhor(TRUST) para fazer a tarefa
	}
	
	public void printState() {
		System.out.println("### STATE ###");
		List<Task> l = new ArrayList<Task>(tasks);
		for (Task task : l) {
			System.out.println(task);
		}
	}
	
}
