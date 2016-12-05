package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Task implements Comparable<Task>{
	private static int id; //Global counter for taskIds
	private int taskId;
	private String taskType;
	private List<String> skillsToPerform; //Skills to perform this task
	private int[] precedences; //list of taskId
	private boolean isDone;
	private int duration = 0;
	
	public Task(int... dp) {
		taskId = id;
		id++;
		skillsToPerform = new ArrayList<String>();
		precedences = dp;
	}
	
	public boolean isDone(){
		return this.isDone;
	}
	
	/**
	 * Returns the number of precedences this task has.
	 * @return number of precedences.
	 */
	public int getPrecedenceNumber() {
		return precedences.length;
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
	
	public Integer getPrecedence(int index) {
		return precedences[index];
	}
	
	public int getTaskId() {
		return taskId;
	}
	
	@Override
	public String toString() {
		String s = "Task: " + taskId + " (";
		for (int i : precedences) {
			s += " " + i;
		}
		s += " )";
		return s;
	}
	
}
