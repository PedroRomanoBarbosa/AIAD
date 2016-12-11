package agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import data.Task;
import models.FireModel;
import models.Model;

public class Coordinator extends Agent{
	private static final long serialVersionUID = 1L;
	
	// Project Meta Information
	private Model model; // The trust model chosen
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
	private List<AID> collaboratorsForTask;
	private boolean assigning;
	
	// Coordinator Behaviours
	private CyclicBehaviour recieveMessageBehaviour, recieveTaskDoneBehaviour;
	private WakerBehaviour startProjectBehaviour, newIterationBehaviour;
	private OneShotBehaviour assignTaksBehaviour;

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
		*/
		
		List<String> skills2 = new ArrayList<String>();
		skills2.add("skill1");
		skills2.add("skill2");
		Task task2 = new Task("ID1", "CONTACT", 5000);
		task2.setSkillsToPerformTask(skills2);
		tasksList.add(task2);
		
		List<String> skills3 = new ArrayList<String>();
		skills3.add("skill1");
		skills3.add("skill2");
		Task task3 = new Task("ID2", "CONTACT", 5000);
		task3.setSkillsToPerformTask(skills3);
		tasksList.add(task3);
		
		List<String> skills4 = new ArrayList<String>();
		skills4.add("skill1");
		skills4.add("skill2");
		Task task4 = new Task("ID3","CONTACT" , 5000);
		task4.setSkillsToPerformTask(skills4);
		tasksList.add(task4);
		
		List<String> skills5 = new ArrayList<String>();
		skills5.add("skill1");
		skills5.add("skill2");
		Task task5 = new Task("ID4","CONTACT" , 5000);
		task5.setSkillsToPerformTask(skills5);
		tasksList.add(task5);
		
		// Create Behaviours
		createStartProjectBehaviour();
		createAssignTaskBehaviour();
		createRecieveMessageBehaviour();
		createRecieveTaskDoneBehaviour();
		
		buildSearchMessage();
		
		addBehaviour(recieveMessageBehaviour);
		addBehaviour(recieveTaskDoneBehaviour);
		addBehaviour(startProjectBehaviour);
	}
	
	public void addTask(Task task){
		tasksList.add(task);
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
							long duration = System.nanoTime() - task.getStartTime();
							double rating = calculateRating(task.getNormalDuration(), duration/1000000l);
							model.addInteraction(getAID(), msg.getSender(), args[1], rating);
							updateCollaboratorAvailability(msg.getSender(), false);
							if(checkIfAllTasksDone()) {
								projectDuration = System.nanoTime() - startTime;
								System.out.println("Project finished!");
								System.out.println("Duration: " + projectDuration/1000000000d + " seconds");
								System.out.println("TRUST DATABASE: ");
								model.print();
								double trust = model.getCollaboratorTrustByTask(msg.getSender(), args[1]);
								System.out.println("LAST AGENT TRUST VALUE: " + trust);
								System.out.println();
								projectFinished = true;
							}
						}
					}
			    	if(!assigning) {
			    		System.out.println("NEW ITERATION!");
			    		startNewIterationBehaviour(0l);
			    	}
			    } else {
			    	block();
			    }
			}
		};
	}
	
	private void updateCollaboratorAvailability(AID aid, boolean occupied) {
		for (int i = 0; i < collaboratorsData.size(); i++) {
			if(collaboratorsData.get(i).getAID().equals(aid)) {
				collaboratorsData.get(i).setAvailability(occupied);
			}
		}
	}
	
	private void createStartProjectBehaviour() {
		startProjectBehaviour = new WakerBehaviour(this, 5000l) {

			@Override
			protected void onWake() {
				System.out.println("Searching for collaborators...");
				List<CollaboratorData> colaborators = getDFCollaborators();
				for (CollaboratorData collaborator : colaborators) {
					addToContactList(collaborator);
				}
				printContactsList();
				subscribe();
				System.out.println("Started Project!\n");
				startTime = System.nanoTime();
				startNewIterationBehaviour(0l);
			}
			
		};
	}
	
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
	
	private boolean checkIfAllTasksDone() {
		for (int i = 0; i < tasksList.size(); i++) {
			if(!tasksList.get(i).isDone()) {
				return false;
			}
		}
		return true;
	}
	
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
				//TODO For each available task choose the best collaborator(TRUST)
				
				/*
				System.out.println("SIZE: " + available.size());
				System.out.println("TASK INDEX: " + tasksListIndex);
				System.out.println("COLLABORATOR INDEX: " + potencialCollaboratorIndex);
				System.out.println();
				*/
				
				// While there are tasks available in the available list select collaborator candidates
				if(tasksListIndex < available.size()) {
					Task selectedTask = available.get(tasksListIndex);
					collaboratorsForTask = available.get(tasksListIndex).getCollaborators();
					if(potencialCollaboratorIndex < available.get(tasksListIndex).getCollaborators().size()) {
						AID collaborator = available.get(tasksListIndex).getCollaborators().get(potencialCollaboratorIndex); //TODO Change
						ACLMessage message = prepareRequestMessage(selectedTask);
						sendRequestMessage(collaborator, message, selectedTask);
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
	
	private List<Task> getAvailableTasks() {
		List<Task> availableTasks = new ArrayList<Task>();
		for (Task task : tasksList) {
			if(!task.isDone() && !task.isAssigned() && !hasPrecedentsLeft(task) && hasCollaborators(task)) {
				availableTasks.add(task);
			}
		}
		return availableTasks;
	}
	
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
	
	private void sendRequestMessage(AID aid, ACLMessage message, Task task) {
		message.addReceiver(aid);
		addBehaviour(new AchieveREInitiator(this, message) {
			
			@Override
			protected void handleInform(ACLMessage inform) {
				System.out.println("Agent " + inform.getSender().getName() + " " + inform.getContent());
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
				System.out.println("Agent "+ agree.getSender().getName() + " agreed to perform task " + task.getTaskId());
			}

			@Override
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("Agent " + refuse.getSender().getName() + " is ocuppied");
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
	
	private ACLMessage prepareRequestMessage(Task task) {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		String content = "REQUEST " + task.getTaskId();
		content += " " + task.getNormalDuration();
		for (String skill : task.getSkillsToPerformTask()) {
			content += " " + skill;
		}
		message.setContent(content);
		return message;
	}
	
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
	
	public CollaboratorData getCollaboratorDataByAID(AID aid) {
		for (int i = 0; i < collaboratorsData.size(); i++) {
			if(collaboratorsData.get(i).equals(aid)) {
				return collaboratorsData.get(i);
			}
		}
		return null;
	}
	
	public ArrayList<Collaborator> getMyCollaborators() {
		return myCollaborators;
	}

	public void addMyCollaborators(Collaborator myCollaborator) {
		this.myCollaborators.add(myCollaborator);
	}
	
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
  						collaborators.add(cd);
  					}
  				}
  			}
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
	  	return collaborators;
	}
	
	private void buildSearchMessage() {
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription templateSd = new ServiceDescription();
		templateSd.setType("collaborator");
		template.addServices(templateSd);
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(-1l);
		searchMessage = DFService.createSubscriptionMessage(this, getDefaultDF(), template, sc);
	}
	
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
	
}
