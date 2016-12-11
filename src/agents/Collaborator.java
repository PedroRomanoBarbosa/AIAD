package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class Collaborator extends Agent {
	private static final long serialVersionUID = 1l;
	private HashMap<String, Float> skills; // skillId -> value(probabilistic)
	private boolean ocuppied; // Whether this agent is occupied or not
	private List<AID> requested; // Queue with the coordinators that requested this collaborators service
	private WakerBehaviour doingTaskBehaviour;
	
	// Used when agreeing and doing a task
	private String currentTask; // The current task this collaborator is doing
	private String currentTaskType;
	private AID currentCoordinator;
	private long currentTaskTime;
	private long currentTaskFinalTime;
	private ArrayList<String> skillsForCurrentTask;
	
	
	public Collaborator() {
		skills = new HashMap<String, Float>();
	}
	
	public HashMap<String, Float> getSkills() {
		return this.skills;
	}
	
	public void addSkill(String skillId, Float performance) {
		this.skills.put(skillId, performance);
	}
	
	public void setSkills(HashMap<String, Float> skills) {
		this.skills = skills;
	}

	@Override
	protected void setup() {
		skills = new HashMap<String, Float>();
		requested = new ArrayList<AID>();
		ocuppied = false;
		
		// TO CREATE COLLABORATORS FROM GUI
		Object[] args = getArguments();
        if (args != null) {
            for (int i = 0; i < args.length-1; i+=2) {
                skills.put((String)args[i], Float.parseFloat((String) args[i+1]));
            }
        }

        // Create Behaviours
        addFIPARequestBehaviour();
        
        // Register in the Yellow Pages
        registerService();
	}
	
	/**
	 * Creates a FIPA request protocol behaviour.
	 */
	private void addFIPARequestBehaviour() {
		MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
		addBehaviour(new AchieveREResponder(this, template) {
			
			@Override
			protected ACLMessage handleRequest(ACLMessage request) {
				String[] args = request.getContent().split(" ");
				if(args != null && args[0].equals("REQUEST")) {
					System.out.println(getAID().getLocalName() + " << " + request.getContent() + " << " + request.getSender().getLocalName());
					if(!ocuppied) {
						ocuppied = true;
						ACLMessage agree = request.createReply();
						agree.setPerformative(ACLMessage.AGREE);
						currentTask = args[1];
						currentTaskType = args[2];
						currentTaskTime = Long.parseLong(args[3]);
						skillsForCurrentTask = new ArrayList<String>();
						for (int i = 4; i < args.length; i++) {
							skillsForCurrentTask.add(args[i]);
						}
						currentCoordinator = request.getSender();
						return agree;
					} else {
						if(!currentCoordinator.equals(request.getSender())) {
							requested.add(request.getSender());
						}
					}
				}
				ACLMessage refuse = request.createReply();
				refuse.setPerformative(ACLMessage.REFUSE);
				return refuse;
			}
			
			@Override
			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
				calculateTime(currentTaskTime, skillsForCurrentTask);
				System.out.println(getAID().getLocalName() + ": " + currentTaskFinalTime);
				doingTaskBehaviour = new WakerBehaviour(getAgent(), currentTaskFinalTime) {

					@Override
					protected void onWake() {
						ocuppied = false;
						notifyRequesters();
						notifyCoordinator(currentCoordinator);
					}
					
				};
				ACLMessage inform = request.createReply();
				inform.setContent("ASSIGNED " + currentTask + " " + currentTaskType);
				inform.setPerformative(ACLMessage.INFORM);
				
				addBehaviour(doingTaskBehaviour);
				
				return inform;
			}
		} );
	}
	
	/**
	 * Notifies all the requesters in the requested list that this collaborator is available.
	 */
	public void notifyRequesters() {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.setContent("AVAILABLE");
		for (AID aid : requested) {
			message.addReceiver(aid);
		}
		requested = new ArrayList<AID>();
		send(message);
	}
	
	/**
	 * Notifies a coordinator that a task is done.
	 */
	public void notifyCoordinator(AID coord) {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		message.setContent("DONE " + currentTask + " " + currentTaskType);
		message.addReceiver(coord);
		send(message);
	}
	
	/**
	 * Method to simulate the collaborator performing a task.
	 */
	private void calculateTime(long normalTime, ArrayList<String> taskSkills) {
		long timeUpper = normalTime + ((long) (normalTime*0.5f));
		long timeBottom = normalTime - ((long) (normalTime*0.5f));
		float skillsValue = 0;
		float skillsAverage;
		
		for (String skill : taskSkills) {
			skillsValue += skills.get(skill);
		}
		skillsAverage = 1 - (skillsValue/taskSkills.size());
		
		Random rand = new Random();
		float randomFactor = rand.nextFloat(); //TODO later
		
		currentTaskFinalTime = ( (long) ( (timeUpper - timeBottom) * skillsAverage) ) + timeBottom; 
	}
	
	/**
	 * Register the 'collaborator' service in the Yellow Pages with skills as properties.
	 */
	private void registerService() {
		DFAgentDescription dfd = new DFAgentDescription();
  		dfd.setName(getAID());
  		ServiceDescription sd = new ServiceDescription();
  		sd.setName("Projects Collaborator");
  		sd.setType("collaborator");
  		for(Map.Entry<String, Float> entry : skills.entrySet()) {
  			sd.addProperties(new Property(entry.getKey(), entry.getValue()));
  		}
  		sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
  		dfd.addServices(sd);
  		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
}
