package agents;

import java.util.HashMap;
import java.util.List;

import jade.core.Agent;
import logic.Task;

public class Colaborator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private HashMap<String,Integer> skills;
	private List<Task> taskQueue;
	private String currentTask;
	private boolean working;
	
}
