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
	private List<Task> tasksCompleted; // List of Tasks already done
	private boolean projectFinished; // Boolean flag indicating the project is over
	private double projectDuration; // The duration of the project when ended
	
	
	
	// Coordinator Behaviours
	private OneShotBehaviour createProjectBehaviour;
	private OneShotBehaviour endProjectBehaviour;
	
	public Coordinator(){

		collaborators = new ArrayList<AID>();
		tasks = new PriorityQueue<Task>(); // se calhar outra queue....
		tasksCompleted = new ArrayList<Task>();
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
	
	
	public void orderTasks(){
		
	}
	
}
