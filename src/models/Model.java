package models;

import jade.core.AID;

public interface Model {
	double calculateTrustworthiness(double... ParamsToCheck);
	
	void addInteraction(AID coord, AID coll, String type, double rating);
	
	double getCollaboratorTrustByTask(AID aid, String type);
	
	void print();
}

