package models;

import jade.core.AID;

/**
 * Interface for a model to implement so that other classes can
 * use their functions
 */
public interface Model {
	
	void addInteraction(AID coord, AID coll, String type, double rating);
	
	double getCollaboratorTrustByTask(AID aid, String type);
	
	void print();
}

