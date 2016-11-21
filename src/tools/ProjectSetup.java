package tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import agents.Coordinator;
import jade.Boot;
import parser.Parser;

public class ProjectSetup {
	private static File ficheiro;
	private static Parser parser;
	
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
					
					initAgents();
					//initCoordinator();
					//initCollaborators();
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
		//Boot boot = new Boot();
		String[] arguments = {"-gui", parser.getCoordinator()+":agents.Coordinator",};
		Boot.main(arguments);
	}
	
	public static void initCoordinator(){
		//Boot boot = new Boot();
		
		//String[] arguments = {"-gui",parser.getCoordinator()+":agents.Coordinator"};
		//boot.main(arguments);
		
	}
	
	public static void initCollaborators(){
		ArrayList<String> collaborators = parser.getCollaborators();
		for (int i = 0; i < collaborators.size(); i++) {
		}
	}
}
