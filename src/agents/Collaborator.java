package agents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jade.core.AID;
import jade.core.Agent;
import data.CollaboratorData;
import data.Task;


public class Collaborator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private Coordinator projectCoordinator; //The project coordinator
	private Task currentTask; //The current task this collaborator is doing
	
	public Collaborator(){
		this.projectCoordinator = new Coordinator();
	}
	
	public Task getCurrentTask(){
		return this.currentTask;
	}
	
	public void setTask(Task task){
		this.currentTask = task;
	}

}
