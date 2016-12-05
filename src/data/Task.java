package data;

import java.util.ArrayList;
import java.util.List;

public class Task implements Comparable<Task>{
	private static int id; //Global counter for taskIds
	private int taskId;
	private String taskType;
	private List<String> skillsToPerform; //Skills to perform this task
	private List<String> precedences; //list of taskId
	private boolean isDone;
	private int duration = 0;
	
	public Task(List<String> dp) {
		taskId = id;
		id++;
		skillsToPerform = new ArrayList<String>();
		this.isDone = false;
		this.taskId = taskId;
		precedences = dp;
	}
	
	public Task() {
		taskId = id;
		id++;
		skillsToPerform = new ArrayList<String>();
		this.isDone = false;
		this.taskId = taskId;
	}
	
	public boolean isDone(){
		return this.isDone;
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
	
	public int getTaskId() {
		return taskId;
	}
	
	@Override
	public String toString() {
		String s = "Task: " + taskId + " (";
		for (int i = 0; i < precedences.size(); i++) {
			s += " " + precedences.get(i);
		}
		s += " )";
		return s;
	}
	
}
