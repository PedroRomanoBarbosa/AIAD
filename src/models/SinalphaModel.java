package models;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;

public class SinalphaModel implements Model{
	private double start_alpha = (3 * Math.PI) / 2;
	private int lambda_increase = 1;
	private double lambda_decrease = -1.5;
	private double omega = Math.PI / 10;
	private double alpha = 0.0; // alpha value varies between 3pi/2 and 5pi/2
	private List<Interaction> database;
	
	public SinalphaModel() {
		database = new ArrayList<Interaction>();
	}
	
	public double calculateTrustworthiness(double... ParamsToCheck){
		if (ParamsToCheck.length != 1) {
	         throw new IllegalArgumentException();  // or return false
	    }
		double delay = ParamsToCheck[0];
		double trustworthiness = 0.0;
		if (delay == 0){
			alpha = start_alpha + lambda_increase * omega;
		}
		else if (delay > 0){
			alpha = start_alpha - lambda_decrease * omega;
		}
		else
			System.out.println("Wrong delay value.\n");
		if (alpha < ((3*Math.PI)/2)){
			alpha = 3*Math.PI/2;
		}
		else if (alpha < ((5*Math.PI)/2)){
			alpha = 5*Math.PI/2;
		}
		return trustworthiness = 0.5 * Math.sin(alpha) + 0.5;
	}

	@Override
	public void addInteraction(AID coord, AID coll, String type, double rating) {
		Interaction interaction = null;
		boolean newInteraction = true;
		// Get the interaction corresponding to the collaborator and task
		for (int i = 0; i < database.size(); i++) {
			if( database.get(i).collaboratorAID.equals(coll) && database.get(i).type.equals(type)) {
				interaction = database.get(i);
				newInteraction = false;
			}
		}
		
		// If there are no records of this collaborator and task
		if(newInteraction) {
			interaction = new Interaction(coord,coll,type,rating,0l);
			interaction.alpha = 3d * Math.PI / 2d;
			interaction.trust = 0d;
			database.add(interaction);
		}
		
		// transform rating(-1,1) into delay(0,1)
		double newRating = rating/2d + 1d;
		double newAlpha = 0d;
		if (newRating > 0.5d) {
			newAlpha = interaction.alpha + lambda_increase * omega;
		} else if (newRating < 0.5d) {
			newAlpha = interaction.alpha - lambda_decrease * omega;
		} else {
			System.out.println("Wrong delay value.\n");
		}
		
		if (newAlpha < ((3d * Math.PI) / 2d)) {
			newAlpha = 3d * Math.PI / 2d;
		} else if (newAlpha < ((5 * Math.PI) / 2d)) {
			newAlpha = 5d * Math.PI / 2d;
		}
		
		double trust = 0.5d * Math.sin(newAlpha) + 0.5d;
		interaction.alpha = newAlpha;
		interaction.trust = trust;
	}

	@Override
	public void print() {
		
	}

	@Override
	public double getCollaboratorTrustByTask(AID aid, String type) {
		for (int i = 0; i < database.size(); i++) {
			if( database.get(i).collaboratorAID.equals(aid) && database.get(i).type.equals(type)) {
				Interaction interaction = database.get(i);
				return interaction.trust; // This trust is in [0,1]
			}
		}
		return 0d;
	}
}
