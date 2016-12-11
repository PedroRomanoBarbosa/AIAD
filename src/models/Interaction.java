package models;

import jade.core.AID;
import data.Task;

public class Interaction {
	static int interactionCounter;
	int interaction_id;
	String type;
	AID coordinatorAID;
	AID collaboratorAID;
	double rating;
	
	// for FIRE
	long time;
	
	// for Sinalpha
	double alpha;
	double trust;
	
	public Interaction(AID coord, AID coll, String t, double r, long tm){
		coordinatorAID = coord;
		collaboratorAID = coll;
		type = t;
		rating = r;
		time = tm;
		interactionCounter++;
		interaction_id = interactionCounter;
	}
	
}
