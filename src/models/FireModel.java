package models;

import java.util.ArrayList;

import jade.core.AID;

import models.Interaction;

public class FireModel implements Model {
	private int interaction_id = 0;
	AID coordinatorAID;
	AID collaboratorAID;
	private double recency_factor = 0;
	private ArrayList<Interaction> database;
	private double startTime = 0;
	
	public FireModel() {
		database = new ArrayList<Interaction>();
	}
	
	public FireModel(AID coordinatorAID, AID collaboratorAID, int d){
		database = new ArrayList<Interaction>();
		this.recency_factor = (- d) / (Math.log(0.5)/Math.log(Math.E)); //TODO remove
		this.coordinatorAID = coordinatorAID;
		this.collaboratorAID = collaboratorAID;
		this.startTime = ( System.nanoTime() / (long)1000000000 );
	}
	
	private double getRecencyFactor() {
		return this.recency_factor;
	}
	
	private void addRating() {
		/*
		long endTime = (long) (System.nanoTime() - this.startTime);
		double rating_weight = Math.exp(endTime/getRecencyFactor());
		Interaction interaction = new Interaction(this.coordinatorAID, this.collaboratorAID, this.interaction_id);
		this.ratings_database.put(interaction, rating_weight);
		*/
	}
	
	/**
	 * Adds a new interaction to the database.
	 * @param coord The coordinator AID.
	 * @param col The collaborator AID.
	 * @param type The type of the task.
	 * @param rating The rating for the task.
	 */
	@Override
	public void addInteraction(AID coord, AID coll, String type, double rating) {
		Interaction interaction = new Interaction(coord,coll,type,rating,System.nanoTime());
		database.add(interaction);
	}
	
	@SuppressWarnings("rawtypes")
	public double calculateTrustworthiness(double... ParamsToCheck){
		/*
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
		*/
		return 0;
	}
	
	/**
	 * Get a given trust value for a certain collaborator with a given task.
	 * @param aid The collaborator AID.
	 * @param type The task type.
	 * @return Trust value.
	 */
	@Override
	public double getCollaboratorTrustByTask(AID aid, String type) {
		double trust = 0;
		ArrayList<Interaction> interactions = getInteractionsByCollaboratorById(aid, type);
		ArrayList<Long> times = new ArrayList<Long>();
		if(interactions.size() > 0) {
			long first = interactions.get(interactions.size() - 1).time;
			long totalSum = 0;
			if(interactions.size() > 1) {
				for (int i = 0; i < interactions.size(); i++) {
					long t = interactions.get(i).time - first;
					times.add(t);
					totalSum += t;
				}
				for (int i = 0; i < interactions.size(); i++) {
					trust += (times.get(i)/(totalSum*1.0f)) * interactions.get(i).rating;
				}
			} else {
				trust = interactions.get(0).rating;
			}
			// Only here to provide a security check
			if(trust > 1.0d) {
				trust = 1.0d;
			}
		} else {
			trust = 0;
		}
		return trust;
	}
	
	/**
	 * Gets all the interactions for a certain collaborator with a given task.
	 * @param aid The collaborator AID.
	 * @param type The task type.
	 */
	private ArrayList<Interaction> getInteractionsByCollaboratorById(AID aid, String type) {
		ArrayList<Interaction> results = new ArrayList<Interaction>();
		for (int i = 0; i < database.size(); i++) {
			Interaction interaction = database.get(i);
			if(interaction.collaboratorAID.equals(aid) && interaction.type.equals(type)) {
				results.add(interaction);
			}
		}
		return results;
	}

	/**
	 * Utility function to display the database entries.
	 */
	@Override
	public void print() {
		for (int i = 0; i < database.size(); i++) {
			Interaction interaction = database.get(i);
			System.out.println(interaction.interaction_id + 
					" - " + interaction.type + 
					" | " + interaction.coordinatorAID.getLocalName() +
					" | " + interaction.collaboratorAID.getLocalName() +
					" | " + interaction.rating);
		}
	}

}
