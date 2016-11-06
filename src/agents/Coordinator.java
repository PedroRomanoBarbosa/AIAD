package agents;

import java.util.List;
import java.util.Queue;

import jade.core.Agent;
import data.Task;
import models.Model;

public class Coordinator extends Agent{
	private static final long serialVersionUID = 1L;
	
	private Model chosenModel; //The trust model chosen
	private List<Collaborator> collaborators; //All the collaborators
	private Queue<Task> tasks; //A task Queue ordered by crescent number of precedences
	private boolean endProject;
	private double projectDuration; //The duration of the project when ended
}
