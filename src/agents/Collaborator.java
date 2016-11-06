package agents;

import java.util.HashMap;
import java.util.List;

import jade.core.Agent;
import data.Task;

public class Collaborator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private Coordinator projectCoordinator; //The project coordinator
	private HashMap<String,Integer> skills; //skillId -> value(probabilistic)
	private String currentTask; //The current task this collaborator is doing
}
