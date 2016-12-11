package tools;

import parser.Parser;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicTabbedPaneUI.TabSelectionHandler;
import javax.swing.JFileChooser.*;


import agents.Collaborator;
import agents.Coordinator;
import data.Task;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Controller;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.gui.CircularGraphLayout;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.DrawableEdge;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.*;
import uchicago.src.sim.util.SimUtilities;

public class MyModel extends SimModelImpl{
	
	public static final int TIT_FOR_TAT = 0; 
	public static final int ALWAYS_DEFECT = 1; 
	//private int p1Strategy = TIT_FOR_TAT; 
	//private int p2Strategy = ALWAYS_DEFECT;
	
	private DisplaySurface dsurf;
	private OpenSequenceGraph plot;
	private int xSize, ySize;
	private ArrayList<DefaultDrawableNode> agentList;
	private Schedule schedule;
	
	private static Parser parser;
	private int numAgents, numAgentsCoord, numAgentsCol;
	private HashMap<String,HashMap<String,Float>> myCollaborators;
	private String coordinator;
	private static Coordinator coord;
	ArrayList<Collaborator> myCollaboratorsAgents;
	ArrayList<Coordinator> myCoordinatorAgent;
	
	private static Runtime rt;
	private static Profile p;
	private static ContainerController cc;
	
	public MyModel() {
		xSize = 300;
		ySize = 150;
		
		
		parser = new Parser();
		myCollaboratorsAgents = new ArrayList<Collaborator>();
		myCoordinatorAgent = new ArrayList<Coordinator>();
	}
	
	@Override
	public Schedule getSchedule() {
		// TODO Auto-generated method stub
		return schedule;
	}
	
	public String getName() {
		return "Testing Model";
	}

	public String[] getInitParam() {
		return new String[] { "numberOfAgents", "spaceSize", "movingMode"};
	}
/*
	public Schedule getSchedule() {
		return schedule;
	}
*/
	public int getNumberOfAgents() {
		return numAgents;
	}

	public void setNumberOfAgents(int numberOfAgents) {
		this.numAgents = numberOfAgents;
	}
	
	public ArrayList<Collaborator> getMyCollaborators() {
		return myCollaboratorsAgents;
	}

	public void addMyCollaborators(Collaborator myCollaborator) {
		System.out.println("AQUI "+myCollaborator.getLocalName());
		this.myCollaboratorsAgents.add(myCollaborator);
	}
	
	public void addMyCoordinator(Coordinator coord){
		this.myCoordinatorAgent.add(coord);
	}
	
	public ArrayList<Coordinator> getMycoordinator() {
		return this.myCoordinatorAgent;
	}

	
	public void setup(){
		
//		super.setup();
		
		schedule = new Schedule();
		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "Project Management Display");
		registerDisplaySurface("Project Management Display", dsurf);
		
		
		
		JFrame frame = new JFrame("Project Manager Simulator");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				// TODO check this
			}
		});
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setDialogTitle("Select project file");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileNameExtensionFilter(".xml files","xml"));
		fileChooser.setSize(300, 300);
		fileChooser.setVisible(true);
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		
		
		fileChooser.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if (command.equals(JFileChooser.APPROVE_SELECTION)) {
					File ficheiro = fileChooser.getSelectedFile();
					System.out.println("FILE: "+ficheiro);
					
					parser.execute(ficheiro);
					
					// TODO: consider project id
					
					
					// Get a hold on JADE runtime
					rt = Runtime.instance();
					// Create a default profile
					p = new ProfileImpl();
					
					// Create a new non-main container, connecting to the default
					// main container (i.e. on this host, port 1099)
					cc = rt.createMainContainer(p); 
					
					try {
					    AgentController rma = cc.createNewAgent("rma", "jade.tools.rma.rma", null);
					    rma.start();
					} catch(StaleProxyException e1) {
					    e1.printStackTrace();
					}
					
					//int numAgentsCoord, numAgentsCol;
					numAgentsCoord = 1;					// TODO
					numAgentsCol = parser.getCollaborators().size();
					
					numAgents = numAgentsCoord + numAgentsCol;
					
					myCollaborators = parser.getCollaborators();
					coordinator = parser.getCoordinator();
					
					
					initAgents();
					
				} 
			}
		});
		
		JPanel mainPanel = new JPanel();
		
		JButton createProjectButton = new JButton("Create New Project");
		createProjectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.showDialog(frame, "Open");
			}
		});
		
		mainPanel.add(createProjectButton);
		frame.add(mainPanel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		
	}
	
	public void begin() {
		buildModel();
		buildDisplay();
		buildSchedule();
	}
	
	public void buildModel(){
		agentList = new ArrayList<DefaultDrawableNode>();
		
		// TODO
		// creates DefaultNodes and adds them to a list
		OvalNetworkItem item_coord = new OvalNetworkItem(xSize/2, 10);
		item_coord.setColor(Color.blue);
		System.out.println("COORDINATOR: "+coordinator);
		DefaultDrawableNode nodeCoord = new DefaultDrawableNode(coordinator, item_coord);
		
		//System.out.println(coordinator+":: color "+nodeCoord.getColor()+"; ("+nodeCoord.getX()+","+nodeCoord.getY()+") w: "+nodeCoord.getWidth()+" h: "+nodeCoord.getHeight());
		agentList.add(nodeCoord);
		int k = 1, kk;
		for(String coll_id : myCollaborators.keySet()){
			OvalNetworkItem item = new OvalNetworkItem(1, 10);
			DefaultDrawableNode nodeCol = new DefaultDrawableNode(coll_id, item);
			nodeCol.setColor(Color.green);
			nodeCol.setLabelColor(Color.black);
			
			if (k <= numAgents/2) {
				nodeCol.setX(xSize/4);
				nodeCol.setY(k*ySize/numAgents);
			}
			else{
				kk=k-numAgents/2;
				nodeCol.setX(xSize*3/4);
				nodeCol.setY(kk*ySize/numAgents);
			}
			
			System.out.println(coll_id+":: color "+nodeCol.getColor()+"; ("+nodeCol.getX()+","+nodeCol.getY()+" w: "+nodeCol.getWidth()+" h: "+nodeCol.getHeight());
			agentList.add(nodeCol);
			k++;
		}

	    // iterate through the agentList creating edges between 
	    // the current node and the previous node.
		
		Node nodeC = (Node)agentList.get(0);
		ArrayList<DefaultDrawableEdge> edges;
	    for (int i = 1; i < agentList.size(); i++) {
	    	Node node = (Node)agentList.get(i);
	    	//EdgeFactory.createEdge(nodeC, node);
	    	EdgeFactory.createDrawableEdge(nodeC, node);
	    	edges = nodeC.getOutEdges();
	    	for (int j = 0; j < edges.size(); j++) {
				edges.get(j).setColor(Color.black);
			}
	    }
	    
	 
	}
	
	public void buildDisplay(){
		
		CircularGraphLayout layout = new CircularGraphLayout(agentList, xSize, ySize);
		Network2DDisplay display = new Network2DDisplay(layout);
		dsurf.addDisplayableProbeable(display, "Network Display");
		dsurf.display();
		
		Controller c = (Controller)getController();
		c.addStopListener(layout);
		c.addPauseListener(layout);
		c.addExitListener(layout);

		
		// graph
		// this is default! TODO
		if (plot != null) plot.dispose();
		plot = new OpenSequenceGraph("Colors and Agents", this);
		plot.setAxisTitles("time", "n");
		// plot number of different existing colors
		plot.addSequence("Number of colors", new Sequence() {
			public double getSValue() {
				return 0;
				//return agentColors.size();
			}
		});
		// plot number of agents with the most abundant color
		plot.addSequence("Top color", new Sequence() {
			public double getSValue() {
				return 0;
				/*
				int n = 0;
				Enumeration<Integer> agentsPerColor = agentColors.elements();
				while(agentsPerColor.hasMoreElements()) {
					int c = agentsPerColor.nextElement();
					if(c>n) n=c;
				}
				return n;
				*/
			}
		});
		plot.display();
	}
	


	
	
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new MyModel(), null, false);
	}
	
	public void initAgents() {
		initCoordinator();
		initCollaborators();
	}
	
	public int initCoordinator(){
		HashMap<String, List<String>> taskSkills;
		HashMap<String, List<String>> taskPrecs;
		HashMap<String, String[]> taskType;
		String[] taskTypeTemp;
		List<String> skills;
		List<String> precs;
		String task, type;
		long duration;
		Task myTask = null;
		AgentController coordinatorAgent = null;
		
		coord = new Coordinator();
		
		// CREATE AGENT COORDINATOR
		try {
			coordinatorAgent = cc.acceptNewAgent(parser.getCoordinator(), coord);
			coordinatorAgent.start();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		System.out.println("AGENT COORDINATOR CREATED");
		
		// ADD PROJECT TASKS
		ArrayList<String> projectTasks = parser.getProjectTasks();
		for (int i = 0; i < projectTasks.size(); i++) {
			task = projectTasks.get(i);
			System.out.println(task);
			
			taskSkills = parser.getTaskSkills();
			//System.out.println(taskSkills);
			skills = taskSkills.get(task);
			//System.out.println(skills);
			
			taskPrecs = parser.getTaskPrecs();
			//System.out.println(taskPrecs);
			precs = taskPrecs.get(task);
			//System.out.println(precs);
			
			taskType = parser.getTaskType();
			//System.out.println(taskType);
			taskTypeTemp = taskType.get(task);
			type = taskTypeTemp[0];
			duration =  Long.parseLong(taskTypeTemp[1]);
			
			
			
			myTask = new Task(task, precs);
			myTask.setSkillsToPerformTask(skills);
			myTask.setTaskType(type);
			myTask.setNormalDuration(duration);
			//System.out.println(myTask.getSkillsToPerformTask());
			//System.out.println(myTask.getPrecedences());
			
			coord.addTask(myTask);
		}
		System.out.println("TASKS with precedencies and skills ADDED TO COORDINATOR");
		coord.setId(parser.getCoordinator());
		addMyCoordinator(coord);
		return 0;
	}
	
	public void initCollaborators(){
		Collaborator col;
		AgentController collaboratorAgent;
//		ArrayList<String> collaborators = parser.getProjectCollaborators();
		HashMap<String,HashMap<String,Float>> myCollaborators;
		
		
		myCollaborators = parser.getCollaborators();
		for (String coll_id : myCollaborators.keySet()) {
			col = new Collaborator();
			col.setId(coll_id);
			
			System.out.println(coll_id);
			
			// ADD SKILLS
			col.setSkills(myCollaborators.get(coll_id));
			//System.out.println("my skills: "+col.getSkills());
			System.out.println("SKILLS ADDED TO AGENT COLLABORATOR "+coll_id);
			
			// CREATE AGENTS COLLABORATORS
			try {
				collaboratorAgent = cc.acceptNewAgent(coll_id, col);
				collaboratorAgent.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			System.out.println("AGENT COLLABORATOR "+coll_id+" CREATED");
			
			addMyCollaborators(col);
		}
	}
	
	/*
	* ---------------------------- ACTIONS TO PERFORM ----------------------------------------------------
	* ------------------------------------- & ------------------------------------------------------------
	* --------------------------------- SCHEDULING -------------------------------------------------------
	*/
	
	private void buildSchedule() {
		
		/*
	    class DummyPrint extends BasicAction {
	    	public void execute() {
	    		for (int i = 0; i < agentList.size(); i++) {
					System.out.println("ALI "+agentList.get(i).getNodeLabel());
				}
		      }
	    }
		*/
		
		class ShowEdges extends BasicAction {

			@Override
			public void execute() {
				DefaultDrawableNode colNode;
				DefaultDrawableNode coordNode = agentList.get(0);
				System.out.println("LABEL "+coordNode.getNodeLabel());
				System.out.println("EDGES "+coordNode.getOutEdges().size());
				System.out.println("OUT NODES:");
				for (int i = 0; i < coordNode.getOutNodes().size(); i++) {
					colNode = (DefaultDrawableNode) coordNode.getOutNodes().get(i);
					System.out.println("\t"+colNode.getNodeLabel());
					if (colNode.getNodeLabel().equals("col01")) {
						DrawableEdge edge = (DrawableEdge) colNode.getInEdges().get(0);
						System.out.println("AQUI "+edge.getFrom().getNodeLabel());
						//colNode.getInEdges().clear();
						colNode.clearInEdges();
					}
				}
			}
			
		}
		
		//schedule.scheduleActionBeginning(0, new MainAction());
		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
		schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
		//schedule.scheduleActionAtInterval(100, new DummyPrint(), Schedule.LAST);
		schedule.scheduleActionAtInterval(1, new ChangeEdgesColor(), Schedule.LAST);
		schedule.scheduleActionAtInterval(1, new ChangeAgentsColor(), Schedule.LAST);
	}
	
	
	class ChangeAgentsColor extends BasicAction {

		@Override
		public void execute() {
			for (int i = 0; i < agentList.size(); i++) {
				for (int j = 0; j < myCollaboratorsAgents.size(); j++) {
					Collaborator col = myCollaboratorsAgents.get(j);
					if (col.getId().equals(agentList.get(i).getNodeLabel())) {
						if (col.isOcuppied()) {
							agentList.get(i).setColor(Color.red);
							//System.out.println("--> "+col.getId()+" está OCUPADO");
						}
						else{
							agentList.get(i).setColor(Color.green);
							//System.out.println("--> "+col.getId()+" está LIVRE");
						}
					}
				}
			}			
		}
	};
	
	
	class ChangeEdgesColor extends BasicAction {
		DefaultDrawableEdge colEdge;
		ArrayList<DefaultDrawableEdge> colEdges;
		DefaultDrawableNode colNode;
		Collaborator col;
		
		@Override
		public void execute() {
			
				for (int i = 0; i < agentList.size(); i++) {
					for (int j = 0; j < myCollaboratorsAgents.size(); j++) {
						col = myCollaboratorsAgents.get(j);
						if (agentList.get(i).getNodeLabel().equals(col.getId())) {
							colNode = agentList.get(i);
							if (col.isOcuppied()) {
								//System.out.println("ESTOU OCUPADO "+col.getId());
								colEdges = colNode.getInEdges();
								for (int m = 0; m < colEdges.size(); m++) {
									colEdge = colEdges.get(m);
									//System.out.println("OTHER NODE "+colEdge.getFrom().getNodeLabel()+"; "+col.getCurrentCoordinator().getLocalName());
									if (colEdge.getFrom().getNodeLabel().equals(col.getCurrentCoordinator().getLocalName())) {
										colEdge.setColor(Color.red);
									}
									else{
										colEdge.setColor(Color.black);
									}
								}
								
							}
							else {
								colEdges = colNode.getInEdges();
								for (int n = 0; n < colEdges.size(); n++) {
									colEdges.get(n).setColor(Color.black);
								}
							}
						}
					}
				}
			
		}
	};
}
