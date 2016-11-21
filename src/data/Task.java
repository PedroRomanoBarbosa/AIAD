package data;

import java.util.List;

public class Task implements Comparable<Task>{
	private static int id; //Global counter for taskIds
	private int taskId;
	private String taskType;
	private List<String> skillsToPerform; //Skills to perform this task
	private List<Integer> precedences; //list of taskId
	private boolean isDone;
	private int duration = 0;
	
	public boolean isDone(){
		return this.isDone;
	}
	
	/**
	 * Returns the number of precedences this task has.
	 * @return number of precedences.
	 */
	public int getPrecedenceNumber(){
		return precedences.size();
	}
	
	@Override
	public int compareTo(Task task) {
		if(getPrecedenceNumber() > task.getPrecedenceNumber()){
			return 1;
		}else if(getPrecedenceNumber() < task.getPrecedenceNumber()){
			return -1;
		}else{
			return 0;
		}
	}
	
}
