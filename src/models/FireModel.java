package models;

import jade.core.AID;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import data.Task;
import agents.Coordinator;
import agents.Collaborator;
import models.Interaction;


public class FireModel extends Model {
	private int interaction_id = 0;
	AID coordinatorAID;
	AID collaboratorAID;
	private double recency_factor = 0;
	private double rating_weight = 0;
	
	private List<Interaction> ratings_database;
	private double startTime = 0;
	
	public FireModel(AID coordinatorAID, AID collaboratorAID, int d){
		this.ratings_database = new ArrayList<Interaction>();
		this.recency_factor = (- d) / (Math.log(0.5)/Math.log(Math.E));
		this.coordinatorAID = coordinatorAID;
		this.collaboratorAID = collaboratorAID;
		this.startTime = ( System.nanoTime() / (long)1000000000 );
	}
	
	private double getRecencyFactor(){
		return this.recency_factor;
	}
	
	private void addRating(){
		long endTime = (long) (System.nanoTime() - this.startTime);
		double rating_weight = Math.exp(endTime/getRecencyFactor());
		Interaction interaction = new Interaction(this.coordinatorAID, this.collaboratorAID, this.interaction_id);
		this.ratings_database.add(interaction);
	}	
}
