package agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREInitiator;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

import data.CollaboratorData;
import data.MetaData;
import data.Task;
import models.FireModel;
import models.Model;
import models.SinalphaModel;

public class Coordinator extends Agent {
	private static final long serialVersionUID = 1L;
	
	// Project Meta Information
	private Model model; // The trust model chosen
	private boolean hasModel; // If this coordinator has a trust model
	private ArrayList<Collaborator> myCollaborators = new ArrayList<Collaborator>();
	private List<CollaboratorData> collaboratorsData; //
	//private List<CollaboratorData> collaboratorsData; // All the collaborators AIDs
	private List<Task> tasksList;
	private boolean projectFinished; // Boolean flag indicating the project is over
	private long projectDuration; // The duration of the project when ended
	private long startTime; // Date for the beginning and ending of the project
	private ACLMessage searchMessage;
	
	// Used for the main loop behaviour
	private int tasksListIndex;
	private int potencialCollaboratorIndex;
	private List<Task> available;
	private List<CollaboratorData> collaboratorsForTask;
	private boolean assigning;
	
	// Coordinator Behaviours
	private CyclicBehaviour recieveMessageBehaviour, recieveTaskDoneBehaviour;
	private WakerBehaviour startProjectBehaviour, newIterationBehaviour;
	private OneShotBehaviour assignTaksBehaviour, createNewProjectBehaviour;
	
	// Test variables
	private int numberOfProjects = MetaData.numberOfProjects;
	private String modelName; // The name of the trust model
	private int projectIndex = 0;
	
	private String coord_id;
	private int tasksDone = 0;
	
	public void setId(String id){
		coord_id = id;
	}
	public String getId(){
		return coord_id;
	}
	
	public int getDoneTasks(){
		return tasksList.size()-tasksDone;
	}
	
	public void setModel(String name) {
		modelName = name;
	}
	
	/**
	 * Method before the agent runs. Initializes most fields and creates 
	 * all the behaviours needed and launches the startup ones.
	 */
	@Override
	protected void setup() {
		tasksList = new ArrayList<Task>();
		collaboratorsData = new ArrayList<CollaboratorData>();
		projectFinished = false;
		model = new FireModel();
		
		//Tests
		/*
		List<String> skills = new ArrayList<String>();
		skills.add("skill1");
		skills.add("skill2");
		List<String> dep = new ArrayList<String>();
		dep.add("ID1");
		Task task = new Task("ID0", 2000);
		task.setSkillsToPerformTask(skills);
		tasksList.add(task);
		
		
		List<String> skills2 = new ArrayList<String>();
		skills2.add("skill1");
		skills2.add("skill2");
		Task task2 = new Task("ID1", "CONTACT", 2000);
		task2.setSkillsToPerformTask(skills2);
		tasksList.add(task2);
		
		List<String> skills3 = new ArrayList<String>();
		skills3.add("skill1");
		skills3.add("skill2");
		Task task3 = new Task("ID2", "CONTACT", 2000);
		task3.setSkillsToPerformTask(skills3);
		tasksList.add(task3);
		
		
		
		List<String> skills5 = new ArrayList<String>();
		skills5.add("skill1");
		skills5.add("skill2");
		Task task5 = new Task("ID4","CONTACT" , 2000);
		task5.setSkillsToPerformTask(skills5);
		tasksList.add(task5);
		
	
		// Tests
		for (int i = 0; i < 6; i++) {
			List<String> skills = new ArrayList<String>();
			skills.add("organize");
			Task task = new Task("ID" + i, "ORGANIZE", 2000);
			task.setSkillsToPerformTask(skills);
			tasksList.add(task);
		}
		*/
		// Set the trust model
		/*
		String modelName = "FIRE";
		hasModel = true;
		if(modelName.equals("FIRE")) {
			model = new FireModel();
		} else if(modelName.equals("SINALPHA")) {
			model = new SinalphaModel();
		} else {
			hasModel = false;
		}
		*/
		
		// Set the trust model
		hasModel = true;
		Object[] args = getArguments();
        if (args != null) {
            modelName = (String) args[0];
        }
		
		if(modelName.equals("FIRE")) {
			model = new FireModel();
		} else if(modelName.equals("SINALPHA")) {
			model = new SinalphaModel();
		} else {
			hasModel = false;
		}
		
		// Create Behaviours
		createStartProjectBehaviour();
		createAssignTaskBehaviour(); 
		createRecieveMessageBehaviour();
		createRecieveTaskDoneBehaviour();
		
		// Builds message to search for agents
		buildSearchMessage();
		
		addBehaviour(recieveMessageBehaviour);
		addBehaviour(recieveTaskDoneBehaviour);
		addBehaviour(startProjectBehaviour);
	}
	
	public void addTask(Task task){
		tasksList.add(task);
	}
	
	/**
	 * Adds behaviour that creates a whole new project. For test purposes.
	 */
	private void addNewProjectBehaviour() {
		createNewProjectBehaviour = new OneShotBehaviour() {

			@Override
			public void action() {
				// Tests
				tasksList = new ArrayList<Task>();
				for (int i = 0; i < 2; i++) {
					List<String> skills = new ArrayList<String>();
					skills.add("organize");
					Task task = new Task("ID" + i, "ORGANIZE", 2000);
					task.setSkillsToPerformTask(skills);
					tasksList.add(task);
				}
				createStartProjectBehaviour();
				addBehaviour(startProjectBehaviour);
			}
		};
		addBehaviour(createNewProjectBehaviour);
	}
	
	/**
	 * Method that receives messages from all the collaborators that were requested
	 * to do a certain task but weren't available that moment confirming that they are
	 * available.
	 */
	private void createRecieveMessageBehaviour() {
		recieveMessageBehaviour = new CyclicBehaviour() {
			
			@Override
			public void action() {
				ACLMessage msg;
				MessageTemplate pattern = MessageTemplate.and(MessageTemplate.MatchContent("AVAILABLE"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			    msg = receive(pattern);
			    if(msg != null) {
			    	updateCollaboratorAvailability(msg.getSender(), false);
			    	if(!assigning) {
			    		startNewIterationBehaviour(0l);
			    	}
			    } else {
			    	block();
			    }
			}
		};
	}
	
	/**
	 * Method that receives messages from all collaborators that are doing a task
	 * confirming that the task is already done.
	 */
	private void createRecieveTaskDoneBehaviour() {
		recieveTaskDoneBehaviour = new CyclicBehaviour() {
			
			@Override
			public void action() {
				ACLMessage msg;
				MessageTemplate pattern = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), new MessageTemplate(new MatchExpression() {

					@Override
					public boolean match(ACLMessage m) {
						String[] args = m.getContent().split(" ");
						if(args[0].equals("DONE")) {
							return true;
						} else {
							return false;
						}
					}
				}));
				msg = receive(pattern);
			    if(msg != null) {
			    	System.out.println(msg.getContent());
			    	String[] args = msg.getContent().split(" ");
			    	for (int i = 0; i < tasksList.size(); i++) {
						if(tasksList.get(i).getTaskId().equals(args[1])) {
							Task task = tasksList.get(i);
							task.done();
							tasksDone++;
							long duration = System.nanoTime() - task.getStartTime();
							double rating = calculateRating(task.getNormalDuration(), duration/1000000l);
							if(hasModel) {
								model.addInteraction(getAID(), msg.getSender(), args[2], rating);
							}
							updateCollaboratorAvailability(msg.getSender(), false);
							if(checkIfAllTasksDone()) {
								projectDuration = System.nanoTime() - startTime;
								System.out.println("PROJECT HAS ENDED!");
								System.out.println("DURATION: " + projectDuration/1000000000d + " SECONDS");
								System.out.println("TRUST DATABASE: ");
								if(hasModel) {
									model.print();
								}
								System.out.println();
								projectFinished = true;
								
								// TESTING
								projectIndex++;
								if(projectIndex < numberOfProjects) {
									addNewProjectBehaviour();
								}
							}
						}
					}
			    	if(!assigning) {
			    		startNewIterationBehaviour(0l);
			    	}
			    } else {
			    	block();
			    }
			}
		};
	}
	
	/**
	 * Updates collaborator availability in the collaborators list.
	 * @param aid The collaborator.
	 * @param occupied If the collaborator now to occupied or not.
	 */
	private void updateCollaboratorAvailability(AID aid, boolean occupied) {
		for (int i = 0; i < collaboratorsData.size(); i++) {
			if(collaboratorsData.get(i).getAID().equals(aid)) {
				collaboratorsData.get(i).setAvailability(occupied);
			}
		}
	}
	
	/**
	 * Creates the start project behaviour responsible for bootstrapping the
	 * whole agent assignment process.
	 */
	private void createStartProjectBehaviour() {
		startProjectBehaviour = new WakerBehaviour(this, MetaData.projectSetupTime) {

			@Override
			protected void onWake() {
				projectFinished = false;
				System.out.println("Searching for collaborators...");
				List<CollaboratorData> colaborators = getDFCollaborators();
				for (CollaboratorData collaborator : colaborators) {
					addToContactList(collaborator);
				}
				printContactsList();
				subscribe();
				System.out.println("PROJECT STARTED!\n");
				startTime = System.nanoTime();
				startNewIterationBehaviour(0l);
			}
			
		};
	}
	
	/**
	 * Utility method to print all the contacts as well as all the collaborators
	 * available after searched for each task.
	 */
	private void printContactsList() {
		System.out.println("ALL CONTACTS: \n" + collaboratorsData);
		System.out.println("TASKS:");
		for (Task task : tasksList) {
			System.out.println(task + ":");
			for (AID aid : task.getCollaborators()) {
				System.out.println("\t - " + aid.getName());
			}
		}
	}
	
	/**
	 * Adds a collaborator AID to the contacts list and puts it in the tasks list
	 * which task this collaborator can do.
	 * @param collaborator The collaborator data.
	 */
	private void addToContactList(CollaboratorData collaborator) {
		for (Task t : tasksList) {
			if(collaborator.canExecuteTask(t)) {
				t.addCollaborator(collaborator.getAID());
				if(!collaboratorsData.contains(collaborator)) {
					collaboratorsData.add(collaborator);
				}
			}
		}
	}
	
	public List<CollaboratorData> getCollaboratorsData() {
		return collaboratorsData;
	}
	
	/**
	 * Method responsible for checking if all the tasks are done or not.
	 * @return Whether all the tasks are done or not.
	 */
	private boolean checkIfAllTasksDone() {
		for (int i = 0; i < tasksList.size(); i++) {
			if(!tasksList.get(i).isDone()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This starts a new iteration of assignment. basically another round of
	 * checking which tasks can be done or not.
	 * @param time The delay in which this behaviour can occur.
	 */
	private void startNewIterationBehaviour(Long time) {
		newIterationBehaviour = new WakerBehaviour(this, time) {
			
			@Override
			protected void onWake() {
				available = getAvailableTasks();
				System.out.println("AVAILABLE TASKS: \n" + available);
				if(available.size() > 0) {
					assigning = true;
					tasksListIndex = 0;
					potencialCollaboratorIndex = 0;
					addBehaviour(assignTaksBehaviour);
				}
			}
		};
		addBehaviour(newIterationBehaviour);
	}
	
	/**
	 * Main assignment loop method.
	 */
	private void createAssignTaskBehaviour() {
		assignTaksBehaviour = new OneShotBehaviour() {
			
			@Override
			public void action() {
				// While there are tasks available in the available list select collaborator candidates
				if(tasksListIndex < available.size()) {
					Task selectedTask = available.get(tasksListIndex);
					/* If the coordinator has a trust model then order collaborators by trust. If not 
					 * get the collaborators of a task by the original ordered that they were found.
					 */
					if(hasModel) {
						collaboratorsForTask = getCollaboratorsForTaskOrderedByTrust(selectedTask);
					} else {
						collaboratorsForTask = available.get(tasksListIndex).getCollaboratorsData();
					}
					if(potencialCollaboratorIndex < available.get(tasksListIndex).getCollaborators().size()) {
						CollaboratorData collaborator = collaboratorsForTask.get(potencialCollaboratorIndex);
						if(!checkIfCollaboratorIsOccupied(collaborator)) {
							ACLMessage message = prepareRequestMessage(selectedTask);
							sendRequestMessage(collaborator.getAID(), message, selectedTask);
						} else {
							potencialCollaboratorIndex++;
							if(potencialCollaboratorIndex >= collaboratorsForTask.size()) {
								tasksListIndex++;
								potencialCollaboratorIndex = 0;
							}
							addBehaviour(assignTaksBehaviour);
						}
					} else {
						potencialCollaboratorIndex = 0;
						tasksListIndex++;
					}
				} else {
					/* If at the end of assignment there are new tasks to be done(new available 
					collaborators or tasks finished while assigning) then repeat process */
					available = getAvailableTasks();
					if(available.size() != 0) {
						addBehaviour(assignTaksBehaviour);
					} else {
						System.out.println("END OF AVAILABLE TASKS");
						assigning = false;
					}
				}
			}
		};
	}
	
	private boolean checkIfCollaboratorIsOccupied(CollaboratorData coll) {
		for (int i = 0; i < collaboratorsData.size(); i++) {
			if(collaboratorsData.get(i).getAID().equals(coll.getAID())) {
				return collaboratorsData.get(i).isOcuppied();
			}
		}
		return false;
	}
	
	
	/**
	 * Gets a list of all collaborators of a task ordered by trust.
	 * @param task The task to search for.
	 * @return List with collaborators ordered.
	 */
	private List<CollaboratorData> getCollaboratorsForTaskOrderedByTrust(Task task) {
		List<CollaboratorData> collaborators = new ArrayList<CollaboratorData>();
		for (int i = 0; i < task.getCollaborators().size(); i++) {
			AID aid = task.getCollaborators().get(i);
			double trust = model.getCollaboratorTrustByTask(aid, task.getTaskType());
			CollaboratorData cd = new CollaboratorData(aid,trust);
			collaborators.add(cd);
		}
		Collections.sort(collaborators);
		return collaborators;
	}
	
	/**
	 * Returns all the available tasks. The ones that aren't done, assigned nor
	 * have any precedences and also that have at least one collaborator unoccupied.
	 * @return
	 */
	private List<Task> getAvailableTasks() {
		List<Task> availableTasks = new ArrayList<Task>();
		for (Task task : tasksList) {
			if(!task.isDone() && !task.isAssigned() && !hasPrecedentsLeft(task) && hasCollaborators(task)) {
				availableTasks.add(task);
			}
		}
		return availableTasks;
	}
	
	/**
	 * Checks if a task has any precedent tasks left to be done.
	 * @param t The task.
	 * @return If task has precedents or not.
	 */
	public boolean hasPrecedentsLeft(Task t) {
		for (String pre : t.getPrecedences()) {
			for (int i = 0; i < tasksList.size(); i++) {
				if(tasksList.get(i).getTaskId().equals(pre)) {
					if(!tasksList.get(i).isDone()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if a task has at least one collaborator unoccupied.
	 * @param t The task.
	 * @return if task has free collaborators.
	 */
	public boolean hasCollaborators(Task t) {
		for (int i = 0; i < t.getCollaborators().size(); i++) {
			AID collaboratorAID = t.getCollaborators().get(i);
			for (int j = 0; j < collaboratorsData.size(); j++) {
				if(collaboratorsData.get(j).getAID().equals(collaboratorAID)) {
					if(!collaboratorsData.get(j).isOcuppied()) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * This method sends a FIPA request protocol message to a collaborator with
	 * a task for him to do.
	 * @param aid The collaborator.
	 * @param message The ACLMessage.
	 * @param task The task to be done.
	 */
	private void sendRequestMessage(AID aid, ACLMessage message, Task task) {
		message.addReceiver(aid);
		addBehaviour(new AchieveREInitiator(this, message) {
			
			@Override
			protected void handleInform(ACLMessage inform) {
				String[] args = inform.getContent().split(" ");
				if(args[0].equals("ASSIGNED") && args[1].equals(task.getTaskId())) {
					task.assign();
					task.setStartTime(System.nanoTime());
					tasksListIndex++;
					addBehaviour(assignTaksBehaviour);
				}
			}
			
			@Override
			protected void handleAgree(ACLMessage agree) {
				System.out.println("AGENT "+ agree.getSender().getName() + " AGREED TO " + task.getTaskId());
			}

			@Override
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("AGENT " + refuse.getSender().getName() + " REFUSED");
				potencialCollaboratorIndex++;
				if(potencialCollaboratorIndex >= collaboratorsForTask.size()) {
					tasksListIndex++;
					potencialCollaboratorIndex = 0;
				}
				updateCollaboratorAvailability(refuse.getSender(), true);
				addBehaviour(assignTaksBehaviour);
			}
			
		});
	}
	
	/**
	 * This method prepares the FIPA request method.
	 * @param task The task with the information to put in the message.
	 * @return The ACLMessaeg to send.
	 */
	private ACLMessage prepareRequestMessage(Task task) {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		String content = "REQUEST " + task.getTaskId();
		content += " " + task.getTaskType();
		content += " " + task.getNormalDuration();
		for (String skill : task.getSkillsToPerformTask()) {
			content += " " + skill;
		}
		message.setContent(content);
		return message;
	}
	
	/**
	 * Gets all the candidate collaborators of a given task from a collaborators list.
	 * @param data The original collaborators list.
	 * @param t The task.
	 * @return The candidates list.
	 */
	public List<CollaboratorData> getCandidateColaborators(List<CollaboratorData> data, Task t) {
		ArrayList<CollaboratorData> candidates = new ArrayList<CollaboratorData>();
		for (int i = 0; i < data.size(); i++) {
			if(data.get(i).canExecuteTask(t)) {
				candidates.add(data.get(i));
			}
		}
		return candidates;
	}
	
	public List<CollaboratorData> getCollaboratorData() {
		return collaboratorsData;
	}
	
	/**
	 * Gets a collaborator by AID.
	 * @param aid The collaborator AID.
	 * @return The collaborator.
	 */
	public CollaboratorData getCollaboratorDataByAID(AID aid) {
		for (int i = 0; i < collaboratorsData.size(); i++) {
			if(collaboratorsData.get(i).equals(aid)) {
				return collaboratorsData.get(i);
			}
		}
		return null;
	}
	
	
	
	/**
	 * Searches for all the collaborators in the network.
	 * @return list of all the collaborators.
	 */
	private List<CollaboratorData> getDFCollaborators() {
	  	List<CollaboratorData> collaborators = new ArrayList<CollaboratorData>();
	  	try {
	  		DFAgentDescription template = new DFAgentDescription();
	  		ServiceDescription templateSd = new ServiceDescription();
	  		templateSd.setType("collaborator");
	  		template.addServices(templateSd);
	  		SearchConstraints sc = new SearchConstraints();
	  		sc.setMaxResults(-1l);
	  		DFAgentDescription[] results = DFService.search(this, template, sc);
  			for (int i = 0; i < results.length; i++) {
  				DFAgentDescription dfd = results[i];
  				AID provider = dfd.getName();
  				Iterator it = dfd.getAllServices();
  				while (it.hasNext()) {
  					ServiceDescription sd = (ServiceDescription) it.next();
  					if (sd.getType().equals("collaborator")) {
  						Iterator it2 = sd.getAllProperties();
  						CollaboratorData cd = new CollaboratorData(provider);
  						while(it2.hasNext()) {
  							Property p = (Property) it2.next();
  							Float value = Float.parseFloat((String)p.getValue());
  							cd.addSkill(p.getName(), value);
  						}
  					}
  				}
  			}
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
	  	return collaborators;
	}
	
	/**
	 *  Builds the search message to send to the DF agent for searching for
	 *  'collaborator' services.
	 */
	private void buildSearchMessage() {
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription templateSd = new ServiceDescription();
		templateSd.setType("collaborator");
		template.addServices(templateSd);
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(-1l);
		searchMessage = DFService.createSubscriptionMessage(this, getDefaultDF(), template, sc);
	}
	
	/**
	 *  Subscribes to a 'collaborator' service so that new collaborator agents can
	 *  inserted in the contacts list.
	 */
	private void subscribe() {
		addBehaviour(new SubscriptionInitiator(this, searchMessage) {
			protected void handleInform(ACLMessage inform) {
	  			try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
					for (int i = 0; i < results.length; ++i) {
						DFAgentDescription dfd = results[i];
						AID provider = dfd.getName();
						CollaboratorData cd = new CollaboratorData(provider);
						if(!collaboratorsData.contains(cd)) {
							Iterator it = dfd.getAllServices();
							while (it.hasNext()) {
								ServiceDescription sd = (ServiceDescription) it.next();
								if (sd.getType().equals("collaborator")) {
									Iterator it2 = sd.getAllProperties();
									while(it2.hasNext()) {
										Property p = (Property) it2.next();
										Float value = Float.parseFloat((String)p.getValue());
										cd.addSkill(p.getName(), value);
									}
									addToContactList(cd);
									if(!assigning) {
										startNewIterationBehaviour(0l);
									}
								}
							}
						}
					}
			  	} catch (FIPAException fe) {
			  		fe.printStackTrace();
			  	}
			}
		} );
	}
	
	/**
	 * Calculates the rating based on the performance in time compared to the 
	 * normal duration of the task. All time units in milliseconds.
	 * @param normalDur The normal duration of the task.
	 * @param dur The duration of the task.
	 * @return Rating between -1 and 1.
	 */
	private double calculateRating(long normalDur, long dur) {
		long timeUpper = normalDur + ((long) (normalDur*0.5f));
		long timeBottom = normalDur - ((long) (normalDur*0.5f));
		long timeAll = timeUpper - timeBottom;
		long timeInterval = dur - timeBottom;
		double rating = 1d - ( timeInterval*1.0d/timeAll*1.0d );
		rating = 2d*rating;
		rating = rating - 1d;
		if(rating > 1.0d) {
			rating = 1.0d;
		} else if(rating < -1.0d) {
			rating = -1.0d;
		}
		return rating;
	}
	
	public void step(){
		System.out.println("COORDINATOR STEP");
	}
	
}
