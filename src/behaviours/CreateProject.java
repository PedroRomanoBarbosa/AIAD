package behaviours;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import jade.core.behaviours.Behaviour;

public class CreateProject extends Behaviour{

	@Override
	public void action() {
		System.out.println("Setting up project...");
		JFileChooser fileChooser = new JFileChooser();
		JFrame frame = new JFrame();
		
		fileChooser.setDialogTitle("Select project file");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new FileNameExtensionFilter(".proj files",".proj", ".PROJ"));
		fileChooser.setSize(300, 300);
		fileChooser.setVisible(true);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.showDialog(frame, "Open");
	}

	@Override
	public boolean done() {
		System.out.println("Project setup complete!");
		return true;
	}

}
