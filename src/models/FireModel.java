package models;

import jade.core.AID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import data.Task;
import agents.Coordinator;
import agents.Collaborator;
import models.Interaction;

public class FireModel implements Model {
	private int interaction_id = 0;
	AID coordinatorAID;
	AID collaboratorAID;
	private double recency_factor = 0;
	private HashMap<Interaction, Double> ratings_database; // double --> rating_weight
	
	//private List<Interaction> ratings_database;
	private double startTime = 0;
	
	public FireModel(AID coordinatorAID, AID collaboratorAID, int d){
		this.ratings_database = new HashMap<Interaction,Double>();
		this.recency_factor = (- d) / (Math.log(0.5)/Math.log(Math.E));
		this.coordinatorAID = coordinatorAID;
		this.collaboratorAID = collaboratorAID;
		this.startTime = ( System.nanoTime() / (long)1000000000 );
	}
	
	private double getRecencyFactor(){
		return this.recency_factor;
	}
	
	private HashMap<Interaction, Double> getRatingsDatabase(){
		return (HashMap<Interaction, Double>) this.ratings_database;
	}
	
	private void addRating(){
		long endTime = (long) (System.nanoTime() - this.startTime);
		double rating_weight = Math.exp(endTime/getRecencyFactor());
		Interaction interaction = new Interaction(this.coordinatorAID, this.collaboratorAID, this.interaction_id);
		this.ratings_database.put(interaction, rating_weight);
	}
	
	@SuppressWarnings("rawtypes")
	public double calculateTrustworthiness(double... ParamsToCheck){
		if (ParamsToCheck.length != 0) {
	         throw new IllegalArgumentException();
	    }
		double trustworthiness = 0;
		HashMap<Interaction, Double> database = getRatingsDatabase();
		Iterator it = database.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			trustworthiness += ((Interaction) pair.getKey()).getRatingValue() * ((Double) pair.getValue());
			it.remove();
		}
		return trustworthiness;
	}

}
