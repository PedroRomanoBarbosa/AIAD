package agents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import data.Task;
import models.Model;
import parser.Parser;

public class Coordinator extends Agent{
	private static final long serialVersionUID = 1L;
	
	// Project Meta Information
	private Model chosenModel; // The trust model chosen
	private List<AID> collaborators; // All the collaborators AIDs
	private Queue<Task> tasks; // A task Queue ordered by crescent number of precedences
	private List<Integer> tasksCompleted; // List of Tasks Ids already done
	private boolean projectFinished; // Boolean flag indicating the project is over
	private double projectDuration; // The duration of the project when ended
	
	
	
	// Coordinator Behaviours
	private OneShotBehaviour createProjectBehaviour;
	private OneShotBehaviour endProjectBehaviour;
	private CyclicBehaviour assignTaks;
	
	public Coordinator() {

		collaborators = new ArrayList<AID>();
		tasks = new PriorityQueue<Task>();
		tasksCompleted = new ArrayList<Integer>();
		projectFinished = false;
		
		// Create Behaviours 
		createProjectBehaviour();
		
		// Add Behaviours
		addBehaviour(createProjectBehaviour);
	}
	
	public void setModel(Model model){
		this.chosenModel = model;
	}
	
	public boolean isProjectFinish(){
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
	
	/**
	 * Creates the behaviour responsible for assigning tasks to the collaborators
	 */
	public void createAssignTaskBehaviour() {
		assignTaks = new CyclicBehaviour() {
			@Override
			public void action() {
				
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
					}
				} else {
					
					// End project!
					System.out.println("Project finished");
				}
			}
		};
	}
	
	public void assignTask(Task t) {
		//TODO se estiverem todos ocupados esperar
		
		//TODO Verificar se o colaborador pode efetuar a tarefa
		
		//TODO Verificar qual o melhor(TRUST) para fazer a tarefa
	}
	
}
