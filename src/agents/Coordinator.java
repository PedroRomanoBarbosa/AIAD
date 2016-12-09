package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.core.messaging.MatchAllFilter;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.MessageTemplate.MatchExpression;
import jade.proto.AchieveREInitiator;
import jade.proto.SimpleAchieveREInitiator;
import jade.proto.SimpleAchieveREResponder;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;
import data.CollaboratorData;
import data.Task;

import models.Model;

public class Coordinator extends Agent{
	private static final long serialVersionUID = 1L;
	
	// Project Meta Information
	private Model chosenModel; // The trust model chosen
	private ArrayList<Collaborator> myCollaborators = new ArrayList<Collaborator>();
	private List<AID> collaborators; // All the collaborators AIDs //TODO delete this
	private List<CollaboratorData> collaboratorsData; //
	private Queue<Task> tasks; // A task Queue ordered by crescent number of precedences
	private List<Task> tasksList;
	private List<String> tasksCompleted; // List of Tasks Ids already done
	private List<String> tasksNotDone; // List of Task Ids that are not done
	private List<AID> availableInbox;
	private boolean projectFinished; // Boolean flag indicating the project is over
	private double projectDuration; // The duration of the project when ended
	private long startTime, endTime; // Date for the beginning and ending of the project
	private Task selectedTask;
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
	private OneShotBehaviour assignTaksBehaviour, sendTaskBehaviour, searchCollaboratorsBehaviour;

	@Override
	protected void setup() {
		collaborators = new ArrayList<AID>();
		tasks = new PriorityQueue<Task>();
		tasksCompleted = new ArrayList<String>();
		collaboratorsData = new ArrayList<CollaboratorData>();
		availableInbox = new ArrayList<AID>();
		projectFinished = false;
		
		//Tests
		List<String> skills = new ArrayList<String>();
		skills.add("skill1");
		Task task = new Task("ID0");
		task.setSkillsToPerformTask(skills);
		tasks.add(task);
		
		List<String> skills2 = new ArrayList<String>();
		skills2.add("skill1");
		Task task2 = new Task("ID1");
		task2.setSkillsToPerformTask(skills2);
		tasks.add(task2);
		//tasks.add(new Task("ID1"));
		//tasks.add(new Task("ID2"));
		
		tasksList = new ArrayList<Task>(tasks);
		tasksNotDone = new ArrayList<String>();
		for (int i = 0; i < tasksList.size(); i++) {
			tasksNotDone.add(tasksList.get(i).getTaskId());
		}
		
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
	
	public ArrayList<AID> getCollaboratorsAIDs(){
		return (ArrayList<AID>) this.collaborators;
	}
	
	public void setModel(Model model){
		this.chosenModel = model;
	}
	
	public boolean isProjectFinished(){
		return projectFinished;
	}
	
	public void addCollaborator(AID collaboratorAID){
		collaborators.add(collaboratorAID);
	}
	
	public void addTask(Task task){
		tasks.add(task);
	}
	
	private void createRecieveMessageBehaviour() {
		recieveMessageBehaviour = new CyclicBehaviour() {
			
			@Override
			public void action() {
				ACLMessage msg;
				MessageTemplate pattern = MessageTemplate.and(MessageTemplate.MatchContent("AVAILABLE"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			    msg = receive(pattern);
			    if(msg != null) {
			    	updateCollaboratorAvailability(msg.getSender(), false);
			    } else {
			    	block();
			    }
			}
		};
	}
	
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
							tasksList.get(i).done();
							if(tasksList.isEmpty()) { //TODO
								projectDuration = System.nanoTime() - startTime;
								System.out.println("Project finished!");
								System.out.println("Duration: " + projectDuration);
								projectFinished = true;
							}
						}
					}
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
				System.out.println("Started Project!");
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
	
	private void createAssignTaskBehaviour() {
		assignTaksBehaviour = new OneShotBehaviour() {
			
			@Override
			public void action() {
				//TODO For each available task choose the best collaborator(TRUST)
				
				//TODO Assign to that collaborator and wait for callback
				System.out.println("SIZE: " + available.size());
				System.out.println("TASK INDEX: " + tasksListIndex);
				System.out.println("COLLABORATOR INDEX: " + potencialCollaboratorIndex);
				System.out.println();
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
					System.out.println("END OF AVAILABLE TASKS");
					assigning = false;
				}
			}
		};
	}
	
	private Task getTaskById(String id) {
		for (int i = 0; i < tasksList.size(); i++) {
			if(tasksList.get(i).getTaskId().equals(id)) {
				return tasksList.get(i);
			}
		}
		return null;
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
	
	public void printState() {
		System.out.println("#### STATE ####");
		List<Task> l = new ArrayList<Task>(tasks);
		System.out.println("Number of tasks: " + l.size());
		for (Task task : l) {
			System.out.println(task);
		}
		System.out.print("TASKS DONE: (");
		for (String t : tasksCompleted) {
			System.out.print(" " + t);
		}
		System.out.println(" )\n");
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
	
}
