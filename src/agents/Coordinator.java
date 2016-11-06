package agents;

import java.util.List;

import jade.core.Agent;
import logic.Task;
import models.Model;

public class Coordinator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private Model chosenModel;
	private List<Colaborator> colaborators;
	private List<Task> tasks;
	
}
