package data;

import java.util.ArrayList;
import java.util.HashMap;

import jade.core.AID;

public class CollaboratorData {
	private AID aid;
	private boolean occupied;
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
	
	public AID getAID() {
		return aid;
	}
	
	public void setAID(AID id) {
		aid = id;
	}
	
	public boolean isOcuppied() {
		return occupied;
	}
	
	@Override
	public String toString() {
		String s = "Collaborator " + aid.getName() + "\n";
		s += skills + "\n";
		return s;
	}
}
