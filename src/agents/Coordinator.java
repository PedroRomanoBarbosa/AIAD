package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
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
	private List<CollaboratorData> collaboratorsData;
	private Queue<Task> tasks; // A task Queue ordered by crescent number of precedences
	private List<Task> tasksList;
	private List<String> tasksCompleted; // List of Tasks Ids already done
	private List<String> tasksNotDone; // List of Task Ids that are not done
	private boolean projectFinished; // Boolean flag indicating the project is over
	private double projectDuration; // The duration of the project when ended
	private long startTime, endTime; // Date for the beginning and ending of the project
	private Task selectedTask;
	private int potencialCollaboratorIndex;
	private ACLMessage searchMessage;
	
	// Coordinator Behaviours
	private WakerBehaviour startProjectBehaviour;
	private OneShotBehaviour assignTaksBehaviour, sendTaskBehaviour;

	@Override
	protected void setup() {
		collaborators = new ArrayList<AID>();
		tasks = new PriorityQueue<Task>();
		tasksCompleted = new ArrayList<String>();
		collaboratorsData = new ArrayList<CollaboratorData>();
		projectFinished = false;
		
		//Tests
		List<String> skills = new ArrayList<String>();
		skills.add("skill1");
		Task task = new Task("ID0");
		task.setSkillsToPerformTask(skills);
		tasks.add(task);
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
		createSendTaskBehaviour();
		
		buildSearchMessage();
		
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
	
	public void createStartProjectBehaviour(){
		startProjectBehaviour = new WakerBehaviour(this, 5000l) {

			@Override
			protected void onWake() {
				System.out.println("Starting project...");
				startTime = System.nanoTime();
				addBehaviour(assignTaksBehaviour);	
			}
			
		};
	}
	
	public List<CollaboratorData> getCollaboratorsData() {
		return collaboratorsData;
	}
	
	private void createAssignTaskBehaviour() {
		assignTaksBehaviour = new OneShotBehaviour() {
			
			@Override
			public void action() {
				//printState();
				boolean assign = true;
				
				// If the task queue still has tasks
				if(!tasks.isEmpty()) {
					Task task = tasks.poll();
					
					// Check if task precedences are all done or not
					for(int i = 0; (i < task.getPrecedenceNumber()) && (assign == true); i++) {
						String id = task.getPrecedence(i);
						
						// If there is at least one precedence to be done do not assign this task and end loop
						if(!tasksCompleted.contains(id)){
							assign = false;
							
							// Push this task to the end of the tasks queue
							tasks.add(task);
						}
					}
					
					// If there are no precedences to be done in this task try to assign it
					if(assign) {
						selectedTask = task;
						addBehaviour(sendTaskBehaviour);
					}
				}
			}
		};
	}
	
	private void createSendTaskBehaviour() {
		sendTaskBehaviour = new OneShotBehaviour() {
			
			@Override
			public void action() {
				List<CollaboratorData> collaborators = getDFCollaborators();
				if(!collaborators.isEmpty()) {
					List<CollaboratorData> candidates = getCandidateColaborators(collaborators, selectedTask);
					if(!candidates.isEmpty()) {
						System.out.println("Candidates: " + Arrays.toString(candidates.toArray()));
						//TODO Verificar qual o melhor(TRUST) para fazer a tarefa
						AID collaborator = candidates.get(0).getAID(); //TODO change to best collaborator
						String taskId = selectedTask.getTaskId();
						ACLMessage message = prepareRequestMessage(collaborator);
						addBehaviour(new AchieveREInitiator(getAgent(), message) {
							
							@Override
							protected void handleInform(ACLMessage inform) {
								System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
								System.out.println(inform.getContent());
								tasksCompleted.add(taskId);
								tasksNotDone.remove(taskId);
								if(tasksNotDone.isEmpty()){
									projectDuration = System.nanoTime() - startTime;
									System.out.println("Project finished!");
									System.out.println("Duration: " + projectDuration);
									projectFinished = true;
								}
							}
							
							@Override
							protected void handleAgree(ACLMessage agree) {
								System.out.println("Agent "+ agree.getSender().getName() + " agreed to perform the requested action");
								addBehaviour(assignTaksBehaviour);
							}

							@Override
							protected void handleRefuse(ACLMessage refuse) {
								// The collaborator is ocuppied. Chose another one. If there are none available.
								System.out.println("Agent " + refuse.getSender().getName() + " is ocuppied");
								potencialCollaboratorIndex = 0; //TODO change this to the second best available
							}
							
						});
					} else {
						
					}
				} else {
					
				}
			}
		};
	}
	
	private ACLMessage prepareRequestMessage(AID aid) {
		ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
		message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		message.addReceiver(aid);
		String content = "REQUEST " + selectedTask.getTaskId();
		for (String skill : selectedTask.getSkillsToPerformTask()) {
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
	
	private boolean checkCollaboratorAlreadyInProject(AID aid) {
		for (int i = 0; i < collaboratorsData.size(); i++) {
			if(collaboratorsData.get(i).getAID().equals(aid)){
				return true;
			}
		}
		return false;
	}
	
	private List<CollaboratorData> getDFCollaborators() {
	  	System.out.println("Agent " + getLocalName() + " searching for services of type \"collaborator\"");
	  	List<CollaboratorData> collaborators = new ArrayList<CollaboratorData>();
	  	try {
	  		DFAgentDescription template = new DFAgentDescription();
	  		ServiceDescription templateSd = new ServiceDescription();
	  		templateSd.setType("collaborator");
	  		template.addServices(templateSd);
	  		SearchConstraints sc = new SearchConstraints();
	  		sc.setMaxResults(-1l);
	  		DFAgentDescription[] results = DFService.search(this, template, sc);
	  		if (results.length > 0) {
	  			for (int i = 0; i < results.length; i++) {
	  				DFAgentDescription dfd = results[i];
	  				AID provider = dfd.getName();
	  				Iterator it = dfd.getAllServices();
	  				System.out.println("Agent " + provider.getName() + " providing a collaborator service");
	  				while (it.hasNext()) {
	  					ServiceDescription sd = (ServiceDescription) it.next();
	  					if (sd.getType().equals("collaborator")) {
	  						Iterator it2 = sd.getAllProperties();
	  						CollaboratorData cd = new CollaboratorData(provider);
	  						while(it2.hasNext()) {
	  							Property p = (Property) it2.next();
	  							System.out.println(p.getName() + ": " + p.getValue());
	  							Float value = Float.parseFloat((String)p.getValue());
	  							cd.addSkill(p.getName(), value);
	  						}
	  						collaborators.add(cd);
	  					}
	  				}
	  			}
	  		} else {
	  			System.out.println("Agent " + getLocalName() + " did not find any collaborator service");
	  		}
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
	  	return collaborators;
	}
	
	private void iterateResults(DFAgentDescription[] results) {
		for (int i = 0; i < results.length; ++i) {
			DFAgentDescription dfd = results[i];
			AID provider = dfd.getName();
			Iterator it = dfd.getAllServices();
			System.out.println("Agent " + provider.getName() + " providing a collaborator service");
			while (it.hasNext()) {
				ServiceDescription sd = (ServiceDescription) it.next();
				if (sd.getType().equals("collaborator")) {
					Iterator it2 = sd.getAllProperties();
					CollaboratorData cd = new CollaboratorData(provider);
					while(it2.hasNext()) {
						Property p = (Property) it2.next();
						System.out.println(p.getName() + ": " + p.getValue());
						Float value = Float.parseFloat((String)p.getValue());
						cd.addSkill(p.getName(), value);
					}
					collaboratorsData.add(cd);
				}
			}
		}
	}
	
	private List<Task> checkIfTasksCanBeDone() {
		List<Task> tasksNotCovered = new ArrayList<Task>();
		tasksList = new ArrayList<Task>(tasks);
		for (int i = 0; i < tasksList.size(); i++) {
			Task task = tasksList.get(i);
			if(!checkIfTaskCanBeDone(task)){
				tasksNotCovered.add(task);
			}
		}
		return tasksNotCovered;
	}
	
	private boolean checkIfTaskCanBeDone(Task t) {
		for (int j = 0; j < collaboratorsData.size(); j++) {
			if(collaboratorsData.get(j).canExecuteTask(t)) {
				return true;
			}
		}
		return false;
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
	  			System.out.println("Agent " + getLocalName() + ": Notification received from DF");
	  			try {
					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
			  		if (results.length > 0) {
			  			iterateResults(results);
			  		}
			  	} catch (FIPAException fe) {
			  		fe.printStackTrace();
			  	}
			}
		} );
	}
	
}
