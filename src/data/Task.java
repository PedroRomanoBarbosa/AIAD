package data;

import java.util.List;

public class Task {
	private static int id; //Global counter for taskIds
	private int taskId;
	private String taskType;
	private List<String> skillsToPerform; //Skills to perform this task
	private List<Integer> dependencies; //list of taskId
}
