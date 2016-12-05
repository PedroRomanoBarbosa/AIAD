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
					/*
					String[] arguments = {"-gui"};
					Boot.main(arguments);
					*/
					
					// Get a hold on JADE runtime
					rt = Runtime.instance();
					// Create a default profile
					p = new ProfileImpl();
					
					// Create a new non-main container, connecting to the default
					// main container (i.e. on this host, port 1099)
					cc = rt.createMainContainer(p); 
					
					
					initAgents(cc);
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
	
	public static void initAgents(ContainerController cc) {
		/*
		String agents = "";
		
		agents += initCoordinator();
		agents += ";";
		agents += initCollaborators();
		
		
		String[] arguments = {"-gui", agents};
		Boot.main(arguments);
		
		//Boot.parseCmdLineArgs(arguments)
		*/
		initCoordinator();
	}
	
	public static int initCoordinator(){
//		return parser.getCoordinator()+":agents.Coordinator";
		HashMap<String, List<String>> taskSkills;
		HashMap<String, List<String>> taskPrecs;
		List<String> skills;
		List<String> precs;
		String task;
		
		coord = new Coordinator();
		
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
			
			
			Task myTask = new Task(precs);
			myTask.setSkillsToPerformTask(skills);
			System.out.println(myTask.getSkillsToPerformTask());
			System.out.println(myTask.getPrecedences());
			
			//coord.addTask(myTask);		// A DAR ERRO !!!!!
			
			
		}
		
		
		System.out.println("TASKS with precedencies and skills ADDED TO COORDINATOR");
		try {
			AgentController coordinatorAgent = cc.acceptNewAgent("Coord", coord);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		System.out.println("AGENT COORDINATOR CREATED");
		
		
		
		
		return 0;
	}
	
	public static String initCollaborators(){
		String tempColl = "";
		
		String sufix = ":agents.Collaborator;";
		ArrayList<String> collaborators = parser.getProjectCollaborators();
		
		for (int i = 0; i < collaborators.size(); i++) {
			tempColl += collaborators.get(i);
			tempColl += sufix;
		}
		
		return tempColl;
	}
}
