package agents;

import java.util.HashMap;
import java.util.List;

import jade.core.Agent;
import data.Task;


public class Collaborator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private Coordinator projectCoordinator; //The project coordinator
	private HashMap<String,Integer> skills; //skillId -> value(probabilistic)
	private Task currentTask; //The current task this collaborator is doing
	
	public Collaborator(){
		this.skills = new HashMap<String, Integer>();
		this.projectCoordinator = new Coordinator();
	}
	
	public HashMap getSkills(){
		return this.skills;
	}
	
	public Task getCurrentTask(){
		return this.currentTask;
	}
	
	public void setTask(Task task){
		this.currentTask = task;
	}
	
	public void addSkill(String skillId, Integer performance){
		this.skills.put(skillId, performance);
	}
}
