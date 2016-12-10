package models;

import jade.core.AID;
import data.Task;

public class Interaction {
	static int interactionCounter;
	private int interaction_id;
	String type;
	AID coordinatorAID;
	AID collaboratorAID;
	Task taskType;
	double rating;
	long time;
	
	public Interaction(AID coord, AID coll, String t, double rating, long tm){
		coordinatorAID = coord;
		collaboratorAID = coll;
		type = t;
		interactionCounter++;
		interaction_id = interactionCounter;
		time = tm;
	}
	
	public double getRatingValue() {
		return rating;
	}
	
}
