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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;


import agents.Collaborator;
import jade.core.Agent;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Controller;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.gui.CircularGraphLayout;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.*;
import uchicago.src.sim.util.SimUtilities;

public class MyModel extends SimpleModel{
	
	public static final int TIT_FOR_TAT = 0; 
	public static final int ALWAYS_DEFECT = 1; 
	private int p1Strategy = TIT_FOR_TAT; 
	private int p2Strategy = ALWAYS_DEFECT;
	private DisplaySurface dsurf;
	private OpenSequenceGraph plot;
	private Parser parser;
	private File ficheiro;
	private int numAgents, numAgentsCoord, numAgentsCol;
	private HashMap<String,HashMap<String,Float>> myCollaborators;
	private int xSize, ySize;
	private ArrayList<DefaultDrawableNode> agentList;
	private String coordinator;
	
	public MyModel() {
		xSize = 20;
		ySize = 10;
	}
	
	public String getName() {
		return "Testing Model";
	}

	public String[] getInitParam() {
		return new String[] { "numberOfAgents", "spaceSize", "movingMode"};
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public int getNumberOfAgents() {
		return numAgents;
	}

	public void setNumberOfAgents(int numberOfAgents) {
		this.numAgents = numberOfAgents;
	}

	
	public void setup(){
		super.setup();
		
		schedule = new Schedule();
		if (dsurf != null) dsurf.dispose();
		dsurf = new DisplaySurface(this, "Project Management Display");
		registerDisplaySurface("Project Management Display", dsurf);
		
		File file = new File("./data/data.xml");
		Parser parser = new Parser();
		parser.execute(file);
		
		//int numAgentsCoord, numAgentsCol;
		numAgentsCoord = 1;					// TODO
		numAgentsCol = parser.getCollaborators().size();
		
		numAgents = numAgentsCoord + numAgentsCol;
		
		myCollaborators = parser.getCollaborators();
		coordinator = parser.getCoordinator();
	}
	
	public void begin() {
		buildModel();
		buildDisplay();
		buildSchedule();
	}
	
	public void buildModel(){
		agentList = new ArrayList<DefaultDrawableNode>();
		
		
		// creates DefaultNodes and adds them to a list
		// DefaultNode nodeCoord = new DefaultNode(parser.getCoordinator());
		OvalNetworkItem item_coord = new OvalNetworkItem(1, 10);
		DefaultDrawableNode nodeCoord = new DefaultDrawableNode(coordinator, item_coord);
		nodeCoord.setX(40);
		nodeCoord.setY(numAgents*10/2);
		nodeCoord.setColor(Color.red);
		System.out.println(coordinator+":: color "+nodeCoord.getColor()+"; ("+nodeCoord.getX()+","+nodeCoord.getY()+")");
		// System.out.println(coordinator);
		agentList.add(nodeCoord);
		int k = 1;
		for(String coll_id : myCollaborators.keySet()){
			// DefaultNode nodeCol = new DefaultNode(coll_id);
			// System.out.println(coll_id);
			OvalNetworkItem item = new OvalNetworkItem(1, 10);
			DefaultDrawableNode nodeCol = new DefaultDrawableNode(coll_id, item);
			nodeCol.setColor(Color.green);
			nodeCol.setLabelColor(Color.black);
			if (k <= numAgents/2) {
				nodeCol.setX(0);
				nodeCol.setY(k*20);
			}
			else{
				nodeCol.setX(80);
				nodeCol.setY((k-numAgents/2)*20);
			}
			
			System.out.println(coll_id+":: color "+nodeCol.getColor()+"; ("+nodeCol.getX()+","+nodeCol.getY()+")");
			agentList.add(nodeCol);
			k++;
		}

	    // iterate through the agentList creating edges between 
	    // the current node and the previous node.
		Node nodeC = (Node)agentList.get(0);
	    for (int i = 1; i < agentList.size(); i++) {
	    	Node node = (Node)agentList.get(i);
	    	//EdgeFactory.createEdge(nodeC, node);
	    	EdgeFactory.createDrawableEdge(nodeC, node);
	    }
	 
	}
	
	public void buildDisplay(){
		
		CircularGraphLayout layout = new CircularGraphLayout(agentList, 200, 150);
		Network2DDisplay display = new Network2DDisplay(layout);
		dsurf.addDisplayableProbeable(display, "Network Display");
		dsurf.display();
		
		Controller c = (Controller)getController();
		c.addStopListener(layout);
		c.addPauseListener(layout);
		c.addExitListener(layout);

		
		// graph
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
	


	/*
	private void buildSchedule() {
		schedule.scheduleActionBeginning(0, new MainAction());
		schedule.scheduleActionAtInterval(1, dsurf, "updateDisplay", Schedule.LAST);
		schedule.scheduleActionAtInterval(1, plot, "step", Schedule.LAST);
	}
	*/
	class MainAction extends BasicAction {

		public void execute() {
			
		}

	}
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new MyModel(), null, false);
	}
}
