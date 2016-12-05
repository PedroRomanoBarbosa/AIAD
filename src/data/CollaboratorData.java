package data;

import jade.core.AID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class CollaboratorData {
	private HashMap<String,Double> skills; //skillId -> value(probabilistic)
	private boolean isBusy = false;
	private AID collaboratorAID;
	
	public AID getCollaboratorAID(){
		return this.collaboratorAID;
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
	
	public boolean isDoingTask(){
		return this.isBusy = true;	
	}
}
