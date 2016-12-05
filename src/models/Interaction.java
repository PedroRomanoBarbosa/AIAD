package models;

import jade.core.AID;
import data.CollaboratorData;
import data.Task;
import agents.Collaborator;
import agents.Coordinator;

public class Interaction {
	private int interaction_id;
	AID coordinatorAID;
	AID collaboratorAID;
	Task task;
	private int rating_value = 0; // -1 - has no skills to achieve the task; 1 - has skills to achieve it
	
	public Interaction(AID coordinatorAID, AID collaboratorAID, int interaction_id){
		this.coordinatorAID = coordinatorAID;
		this.collaboratorAID = collaboratorAID;
		this.interaction_id = interaction_id;
		CollaboratorData collaboratorData = Coordinator.getCollaboratorDataByAID(this.collaboratorAID);
		if (collaboratorData.hasSkills(this.task))
			this.rating_value = 1;
		else
			this.rating_value = -1;
			
	}
	
	public int getRatingValue(){
		return this.rating_value;
	}
	
}
