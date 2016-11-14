package models;

import data.Task;
import agents.Collaborator;
import agents.Coordinator;

public class Interaction {
	private int interaction_id = 0;
	Coordinator coordinator;
	Collaborator collaborator;
	Task t;
	private int rating_value = 0; // -1 - has no skills to achieve the task; 1 - has skills to achieve it
	
	/*public void setCollaborator(Collaborator collaborator){
		this.collaborator = collaborator;
	}*/
	
}
