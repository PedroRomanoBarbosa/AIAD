package models;

import java.util.List;
import java.util.Queue;

import data.Task;
import agents.Coordinator;
import agents.Collaborator;


public class FireModel extends Model{
	private int interaction_id = 0;
	Coordinator coordinator;
	private double recency_factor = 0;
	private double rating_weight = 0;
	private List<Interaction> ratings_database;
	
	private void setRecencyFactor(int d){
		this.recency_factor = (- d) / (Math.log(0.5)/Math.log(Math.E));
	}

	
	
	
	
}
