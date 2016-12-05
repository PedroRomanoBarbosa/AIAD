package agents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import data.CollaboratorData;
import data.Task;
import models.Model;

public class Coordinator extends Agent{
	private static final long serialVersionUID = 1L;
	
	// Project Meta Information
	private Model chosenModel; // The trust model chosen
	private List<CollaboratorData> collaboratorsData; // All the collaborators AIDs
	private Queue<Task> tasks; // A task Queue ordered by crescent number of precedences
	private List<Task> tasksCompleted; // List of Tasks already done
	private boolean projectFinished; // Boolean flag indicating the project is over
	private double projectDuration; // The duration of the project when ended
	
	// Coordinator Behaviours
	private OneShotBehaviour createProjectBehaviour;
	private OneShotBehaviour endProjectBehaviour;
	
	public Coordinator(){
		collaboratorsData = new ArrayList<CollaboratorData>();
		tasks = new PriorityQueue<Task>(); // se calhar outra queue....
		tasksCompleted = new ArrayList<Task>();
		projectFinished = false;
		
		// Create Behaviours 
		createProjectBehaviour();
		
		// Add Behaviours
		addBehaviour(createProjectBehaviour);
	}
	
	public ArrayList<CollaboratorData> getCollaboratorsAIDs(){
		return (ArrayList<CollaboratorData>) this.collaboratorsData;
	}
	
	public void setModel(Model model){
		this.chosenModel = model;
	}
	
	public boolean isProjectFinish(){
		return projectFinished;
	}
	
	/*public void addCollaborator(AID collaboratorAID){
		collaboratorsData.add(collaboratorAID);
	}*/
	
	public void addTask(Task task){
		tasks.add(task);
	}
	
	public void createProjectBehaviour(){
		createProjectBehaviour = new OneShotBehaviour() {
			@Override
			public void action() {
				JFrame frame = new JFrame("Project Manager Simulator");
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						doDelete();
						super.windowClosing(e);
					}
				});
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Select project file");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setFileFilter(new FileNameExtensionFilter(".proj files",".proj", ".PROJ"));
				fileChooser.setSize(300, 300);
				fileChooser.setVisible(true);
				fileChooser.setAcceptAllFileFilterUsed(false);
				
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
		};
	}
	
	public void orderTasks(){
		
	}
	
}
