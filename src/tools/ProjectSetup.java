package tools;

import agents.Coordinator;
import jade.Boot;

public class ProjectSetup {
	
	public static void main(String[] args){
		Coordinator coordinator = new Coordinator();
		Boot boot = new Boot();
		String[] arguments = {"-gui","Pedro:agents.Coordinator"};
		boot.main(arguments);
	}
	
}
