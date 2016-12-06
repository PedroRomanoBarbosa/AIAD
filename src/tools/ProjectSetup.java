package tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import agents.Collaborator;
import agents.Coordinator;
import data.Task;
import jade.Boot;
import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import parser.Parser;

public class ProjectSetup {
	private static File ficheiro;
	private static Parser parser;
	
	private static Coordinator coord;
	private ArrayList<Collaborator> colls;
	
	private static Runtime rt;
	private static Profile p;
	private static ContainerController cc;
	
	public static void main(String[] args){
		parser = new Parser();
		
		setup();
		
	}
	
	public static void setup(){
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
					ficheiro = fileChooser.getSelectedFile();
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
	
	public static void initAgents() {
		initCoordinator();
		initCollaborators();
	}
	
	public static int initCoordinator(){
		HashMap<String, List<String>> taskSkills;
		HashMap<String, List<String>> taskPrecs;
		List<String> skills;
		List<String> precs;
		String task;
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
			System.out.println(skills);
			
			taskPrecs = parser.getTaskPrecs();
			//System.out.println(taskPrecs);
			precs = taskPrecs.get(task);
			System.out.println(precs);
			
			
			myTask = new Task(precs);
			myTask.setSkillsToPerformTask(skills);
			System.out.println(myTask.getSkillsToPerformTask());
			System.out.println(myTask.getPrecedences());
			
			coord.addTask(myTask);
		}
		System.out.println("TASKS with precedencies and skills ADDED TO COORDINATOR");
		
		
		return 0;
	}
	
	public static void initCollaborators(){
		Collaborator col;
		//ArrayList<Collaborator> myCollaborators = null;
		ArrayList<AgentController> collaboratorsAgents;
		AgentController collaboratorAgent;
		ArrayList<String> collaborators = parser.getProjectCollaborators();
		String collaborator;
		
		for (int i = 0; i < collaborators.size(); i++) {
			collaborator = collaborators.get(i);
			col = new Collaborator();
			col.setId(collaborator);
			
			coord.addMyCollaborators(col);
			// CREATE AGENTS COLLABORATORS
			try {
				collaboratorAgent = cc.acceptNewAgent(collaborator, col);
				collaboratorAgent.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			System.out.println("AGENT COLLABORATOR "+collaborator+" CREATED");
		}
		

		
		// TODO: add skills
		
		
	}
	
	
}
