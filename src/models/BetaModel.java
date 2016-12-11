package models;

import jade.core.AID;

public class BetaModel implements Model {

	@Override
	public void addInteraction(AID coord, AID coll, String type, double rating) {
		
	}

	@Override
	public void print() {
		
	}

	@Override
	public double getCollaboratorTrustByTask(AID aid, String type) {
		return 0;
	}

}
