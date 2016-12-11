package data;

import jade.core.AID;

import java.util.ArrayList;
import java.util.HashMap;

import jade.core.AID;

public class CollaboratorData implements Comparable<CollaboratorData> {
	private AID aid;
	private boolean occupied;
	private double trust; // Only used when comparing collaborators
	private HashMap<String,Float> skills; //skillId -> value(probabilistic)
	
	public CollaboratorData() {
		occupied = false;
		skills = new HashMap<String, Float>();
	}
	
	public CollaboratorData(AID id) {
		aid = id;
		occupied = false;
		skills = new HashMap<String, Float>();
	}
	
	public CollaboratorData(AID id, double t) {
		aid = id;
		trust = t;
		occupied = false;
		skills = new HashMap<String, Float>();
	} 
	
	public boolean hasSkills(Task t){
		ArrayList<String> skillsToPerformTask = (ArrayList<String>) (t.getSkillsToPerformTask());
		int s = 0;
		while (s < skillsToPerformTask.size()) {
			String skill = skillsToPerformTask.get(s);
			if (this.skills.containsKey(skill))
				s++;
			else
				return false;
		}
		return true;
	}
	
	public void addSkill(String skillId, Float performance){
		this.skills.put(skillId, performance);
	}
	
	public boolean canExecuteTask(Task t) {
		for (int i = 0; i < t.getSkillsToPerformTask().size(); i++) {
			String skill = t.getSkillsToPerformTask().get(i);
			if(!skills.containsKey(skill)) {
				return false;
			}
		}
		return true;
	}
	
	public double getTrustValue() {
		return trust;
	}
	
	public AID getAID() {
		return aid;
	}
	
	public void setAID(AID id) {
		aid = id;
	}
	
	public boolean isOcuppied() {
		return occupied;
	}
	
	public void setAvailability(boolean occ) {
		occupied = occ;
	}
	
	@Override
	public String toString() {
		String s = "Collaborator " + aid.getName() + "\n";
		if(occupied) {
			s += "UNAVAILABLE";
		} else {
			s += "AVAILABLE";
		}
		s += "\n";
		s += skills + "\n";
		s += trust;
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		return aid.equals(((CollaboratorData)obj).getAID());
	}

	@Override
	public int compareTo(CollaboratorData cd) {
		if(trust > cd.getTrustValue()) {
			return -1;
		} else if(trust < cd.getTrustValue()){
			return 1;
		} else {
			return 0;
		}
	}
	
}
