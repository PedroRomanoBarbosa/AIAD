package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
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
	private boolean projectFinished; // Boolean flag indicating the project is over
	private double projectDuration; // The duration of the project when ended
	private long startTime, endTime; // Date for the beginning and ending of the project
	private Task selectedTask;
	private int potencialCollaboratorIndex;
	private ACLMessage searchMessage;
	
	// Coordinator Behaviours
	private OneShotBehaviour startProjectBehaviour;
	private OneShotBehaviour assignTaksBehaviour, sendTaskBehaviour;

	@Override
	protected void setup() {
		collaborators = new ArrayList<AID>();
		tasks = new PriorityQueue<Task>();
		tasksList = new ArrayList<>(tasks);
		tasksCompleted = new ArrayList<String>();
		collaboratorsData = new ArrayList<CollaboratorData>();
		projectFinished = false;
		
		//Tests
		/*
		tasks.add(new Task("ID0"));
		tasks.add(new Task("ID1"));
		tasks.add(new Task("ID2"));
		*/
		
		// Create Behaviours
		createStartProjectBehaviour();
		createAssignTaskBehaviour();
		createSendTaskBehaviour();
		
		buildSearchMessage();
		
		registerProject();
		
		// Add Behaviours
		//System.out.println(Arrays.toString(collaboratorsData.toArray()));
		searchForCollaborators();
		
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
		startProjectBehaviour = new OneShotBehaviour() {
			@Override
			public void action() {
				startTime = System.nanoTime();
				//getAgents();
				//addBehaviour(assignTaksBehaviour);	//TODO
			}
		};
	}
	
	// TEST METHOD
	private void getAgents() {
		try {
			SearchConstraints c = new SearchConstraints();
	        c.setMaxResults(-1l);
			AMSAgentDescription[] agents = AMSService.search(this, new AMSAgentDescription(), c);
			System.out.println("All Collaborator agents:");
			for (AMSAgentDescription amsAgentDescription : agents) {
				
				if(amsAgentDescription.getName().getLocalName().equals("Lulu")){
					System.out.println(amsAgentDescription.getName());
					//collaborators.add(amsAgentDescription.getName());		//TODO
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}
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
				} else {
					// End project!
					endTime = System.nanoTime();
					projectDuration = endTime - startTime;
					System.out.println("Project finished!");
					System.out.println("Duration: " + projectDuration);
					projectFinished = true;
				}
			}
		};
	}
	
	private void createSendTaskBehaviour() {
		sendTaskBehaviour = new OneShotBehaviour() {
			
			@Override
			public void action() {
				//TODO se estiverem todos ocupados esperar
				
				//TODO Verificar se o colaborador pode efetuar a tarefa
				
				//TODO Verificar qual o melhor(TRUST) para fazer a tarefa
				
				// Send FIPA complient request protocol message
				ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
				message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
				Task taskToAssign = selectedTask;
				
				//Select best candidate for this task //TODO
				List<CollaboratorData> candidates = getCandidateColaborators(taskToAssign);
				AID collaborator = candidates.get(0).getAID(); //TODO change to best collaborator
				message.addReceiver(collaborator);
				message.setContent("ASSIGN TASK " + taskToAssign.getTaskId());
				addBehaviour(new AchieveREInitiator(getAgent(), message) {

					@Override
					protected void handleInform(ACLMessage inform) {
						System.out.println("Agent "+ inform.getSender().getName() + " successfully performed the requested action");
						tasksCompleted.add(taskToAssign.getTaskId());
					}
					
					@Override
					protected void handleAgree(ACLMessage agree) {
						System.out.println("Agent "+ agree.getSender().getName() + " agreed to perform the requested action");
						addBehaviour(assignTaksBehaviour);
					}

					@Override
					protected void handleRefuse(ACLMessage refuse) {
						System.out.println("Agent " + refuse.getSender().getName() + " is ocuppied");
						potencialCollaboratorIndex = 0; //TODO change this to the second best available
						addBehaviour(sendTaskBehaviour);
					}

					@Override
					protected void handleFailure(ACLMessage failure) {
						System.out.println("There was a failure!");
					}
					
				});
			}
		};
	}
	
	public List<CollaboratorData> getCandidateColaborators(Task t) {
		ArrayList<CollaboratorData> candidates = new ArrayList<CollaboratorData>();
		for (int i = 0; i < collaboratorsData.size(); i++) {
			if(collaboratorsData.get(i).canExecuteTask(t)) {
				candidates.add(collaboratorsData.get(i));
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
	
	private void registerProject() {
  		DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName("AIAD project");
  		sd.setType("project");
  		// Agents that want to use this service need to "speak" the FIPA-SL language
  		sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
  		dfd.addServices(sd);
  		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean checkCollaboratorAlreadyInProject(AID aid) {
		for (int i = 0; i < collaboratorsData.size(); i++) {
			if(collaboratorsData.get(i).getAID().equals(aid)){
				return true;
			}
		}
		return false;
	}
	
	private void searchForCollaborators() {
	  	System.out.println("Agent " + getLocalName() + " searching for services of type \"collaborator\"");
	  	try {
	  		DFAgentDescription template = new DFAgentDescription();
	  		ServiceDescription templateSd = new ServiceDescription();
	  		templateSd.setType("collaborator");
	  		template.addServices(templateSd);
	  		SearchConstraints sc = new SearchConstraints();
	  		sc.setMaxResults(-1l);
	  		DFAgentDescription[] results = DFService.search(this, template, sc);
	  		if (results.length > 0) {
	  			iterateResults(results);
	  		} else {
	  			System.out.println("Agent " + getLocalName() + " did not find any collaborator service");
	  			//subscribe();
	  		}
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
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
