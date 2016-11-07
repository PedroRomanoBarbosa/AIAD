package agents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import jade.core.Agent;
import data.Task;
import models.Model;
import models.SinalphaModel;

public class Coordinator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private Model chosenModel; //The trust model chosen
	private List<Collaborator> collaborators; //All the collaborators
	private Queue<Task> tasks; //A task Queue ordered by crescent number of precedences
	private boolean endProject;
	private double projectDuration; //The duration of the project when ended
	
	public Coordinator(){
		this.collaborators = new ArrayList<Collaborator>();
		this.tasks = new PriorityQueue<Task>(); // se calhar outra queue....
	}
	
	public void setModel(Model model){
		this.chosenModel = model;
	}
	
	public boolean isProjectEnded(){
		return this.endProject;
	}
	
	public void addCollaborator(Collaborator collaborator){
		this.collaborators.add(collaborator);
	}
	
	public void addTask(Task task){
		this.tasks.add(task);
	}
	
	public void assignTaskToCollaborator(Task task, Collaborator collaborator){
		collaborator.setTask(task);
	}
}
