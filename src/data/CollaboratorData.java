package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import jade.core.AID;

public class CollaboratorData {
	private AID aid;
	private boolean occupied;
	private HashMap<String,Double> skills; //skillId -> value(probabilistic)
	
	public CollaboratorData(AID id) {
		aid = id;
		occupied = false;
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
	
	public void addSkill(String skillId, double performance){
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
	
	public boolean isOcuppied() {
		return occupied;
	}
}
