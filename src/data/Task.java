package data;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;

public class Task implements Comparable<Task>{
	private String taskId;
	private String taskType;
	private List<String> skillsToPerform; //Skills to perform this task
	private List<String> precedences; //list of taskId
	private List<AID> collaborators;
	private boolean done;
	private boolean assigned;
	private long normalDuration;
	private long startTime;
	
	public Task(String id, List<String> dp) {
		taskId = id;
		skillsToPerform = new ArrayList<String>();
		precedences = dp;
		collaborators = new ArrayList<AID>();
		done = false;
		assigned = false;
	}
	
	public Task(String id, String type, long duration) {
		taskId = id;
		taskType = type;
		skillsToPerform = new ArrayList<String>();
		precedences = new ArrayList<String>();
		collaborators = new ArrayList<AID>();
		done = false;
		assigned = false;
		normalDuration = duration;
	}
	
	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String type) {
		taskType = type;
	}	
	
	public long getNormalDuration() {
		return normalDuration;
	}
	
	public void setNormalDuration(long duration){
		normalDuration = duration;
	}
	
	public void setStartTime(long time) {
		startTime = time;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public List<String> getSkillsToPerformTask(){
		return this.skillsToPerform;
	}
	
	public void setSkillsToPerformTask(List<String> e){
		this.skillsToPerform.addAll(e);
	}
	
	/**
	 * Returns the number of precedences this task has.
	 * @return number of precedences.
	 */
	public int getPrecedenceNumber() {
		return precedences.size();
	}
	
	public List<String> getPrecedences(){
		return precedences;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public void done() {
		done = true;
	}
	
	public void assign() {
		assigned = true;
	}
	
	public boolean isAssigned() {
		return assigned;
	}
	
	@Override
	public int compareTo(Task task) {
		if(getPrecedenceNumber() > task.getPrecedenceNumber()) {
			return 1;
		}else if(getPrecedenceNumber() < task.getPrecedenceNumber()) {
			return -1;
		}else{
			return 0;
		}
	}
	
	public String getPrecedence(int index) {
		return precedences.get(index);
	}
	
	public String getTaskId() {
		return taskId;
	}
	
	public List<AID> getCollaborators() {
		return collaborators;
	}
	
	public void addCollaborator(AID aid) {
		if(!collaborators.contains(aid)) {
			collaborators.add(aid);
		}
	}
	
	@Override
	public String toString() {
		String s = "Task: " + taskId + " (";
		for (int i = 0; i < skillsToPerform.size(); i++) {
			s += " " + skillsToPerform.get(i);
		}
		s += " )";
		return s;
	}

	@Override
	public boolean equals(Object obj) {
		return taskId.equals(((Task)obj).getTaskId());
	}
	
	
	
}
