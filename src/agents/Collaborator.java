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
	private HashMap<CollaboratorData,AID> collaboratorData; //skillId -> value(probabilistic)
	private Task currentTask; //The current task this collaborator is doing
	
	public Collaborator(){
		this.collaboratorData = new HashMap<CollaboratorData, AID>();
		this.projectCoordinator = new Coordinator();
	}
	
	public HashMap getSkills(){
		return this.collaboratorData;
	}
	
	public Task getCurrentTask(){
		return this.currentTask;
	}
	
	public void setTask(Task task){
		this.currentTask = task;
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
