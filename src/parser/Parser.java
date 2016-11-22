package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.ErrorManager;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.helpers.*;

public class Parser {
	private String modelName;
	private ArrayList<String> project_collaborators = new ArrayList<String>();
	//private ArrayList<String> collaborators = new ArrayList<String>();
	private String project_coordinator;
	private ArrayList<String> project_tasks;
	private HashMap<String,HashMap<String,Float>> collaborators = new HashMap<String,HashMap<String,Float>>();
	private HashMap<String, ArrayList<String>> taskSkills = new HashMap<String, ArrayList<String>>();
	
	
	public void setModelName(String name){
		this.modelName = name;
	}
	
	public String getModelName(){
		return this.modelName;
	}
	
	public void setCoordinator(String name){
		this.project_coordinator = name;
	}
	
	public String getCoordinator(){
		return this.project_coordinator;
	}
	
	public void addProjectCollaborators(String collaborator){
		this.project_collaborators.add(collaborator);
	}
	
	public ArrayList<String> getProjectCollaborators() {
		return this.project_collaborators;
	}
	
	public void setProjectTasks(ArrayList<String> tasks){
		this.project_tasks = tasks;
	}
	
	public ArrayList<String> getProjectTasks(){
		return this.project_tasks;
	}
	
	public void addCollaborators(String collaborator, HashMap<String, Float> skills){
		this.collaborators.put(collaborator, skills);
	}
	
	public HashMap<String,HashMap<String,Float>> getCollaborators() {
		return this.collaborators;
	}
	
	public void addTaskSkills(String task, ArrayList<String> skills) {
		this.taskSkills.put(task, skills);
	}
	
	public HashMap<String, ArrayList<String>> getTaskSkills(){
		return taskSkills;
	}
	

	public void execute(File inputFile) {
		Node node;
		
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			
			
			
			Node root = doc.getDocumentElement();
			System.out.println("INIT PARSER:" + root.getNodeName());
			
			NodeList rootChild = root.getChildNodes();
			for (int i = 0; i < rootChild.getLength(); i++) {
				node = rootChild.item(i);
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					System.out.println("\t"+node.getNodeName());
					
					if (node.getNodeName() == "project") {
						parseProject(node);
					}else if (node.getNodeName() == "coordinators") { // nao e necessario
						
					}else if (node.getNodeName() == "collaborators") {
						parseCollaborators(node);	
					}else if (node.getNodeName() == "tasks") {
						parseTasks(node);
					}else if (node.getNodeName() == "skills") {
						
					}
				}
			}
			System.out.println("END PARSER");
		} catch (Exception e){
			// TODO catch
		}
		

	}
	
	public void parseProject(Node project){
		Node node, nnode, nnnode;
		NodeList projectChild, nodeChild, nnodeChild;
		ArrayList<String> tasks = new ArrayList<String>();
		
		// MODEL
		setModelName(project.getAttributes().getNamedItem("model").getNodeValue());
		System.out.println("\t\tmodel: "+getModelName());
		
		
		projectChild = project.getChildNodes();
		for (int i = 0; i < projectChild.getLength(); i++) {
			node = projectChild.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				System.out.println("\t\t"+node.getNodeName());
				nodeChild = node.getChildNodes();
				for (int j = 0; j < nodeChild.getLength(); j++) {
					nnode = nodeChild.item(j);
					if (nnode.getNodeType() == Node.ELEMENT_NODE) {
						if (nnode.getNodeName() == "coordinator") {
							
							// COORDINATORS
							setCoordinator(nnode.getAttributes().getNamedItem("id").getNodeValue());
							System.out.println("\t\t\tcoordinator: "+getCoordinator());
							
						}else if (nnode.getNodeName() == "collaborators") {
							nnodeChild = nnode.getChildNodes();
							for (int k = 0; k < nnodeChild.getLength(); k++) {
								nnnode = nnodeChild.item(k);
								if (nnnode.getNodeType() == Node.ELEMENT_NODE) {
									
									// COLLABORATORS
									addProjectCollaborators(nnnode.getAttributes().getNamedItem("id").getNodeValue());
								}
							}
							System.out.println("\t\t\t"+nnode.getNodeName()+": "+getProjectCollaborators());
						}else if (nnode.getNodeName() == "task") {
							
							// ADD TASKS
							tasks.add(nnode.getAttributes().getNamedItem("id").getNodeValue());
							//System.out.println(nnode.getAttributes().getNamedItem("id").getNodeValue());
							
						}
					}
				}
				setProjectTasks(tasks);
				
			}
		}
		System.out.println("\t\t\t"+getProjectTasks());
	}
	
	public void parseCollaborators(Node node){
		Node nnode, nnnode;
		NodeList nodeChild, nnodeChild;
		
		HashMap<String, Float> skills = new HashMap<String, Float>();
		
		nodeChild = node.getChildNodes();
		
		for (int i = 0; i < nodeChild.getLength(); i++) {
			nnode = nodeChild.item(i);
			if (nnode.getNodeType() == Node.ELEMENT_NODE) {
				String collaborator = nnode.getAttributes().getNamedItem("id").getNodeValue();
				skills = new HashMap<String, Float>();
				nnodeChild = nnode.getChildNodes();
				for (int j = 0; j < nnodeChild.getLength(); j++) {
					nnnode = nnodeChild.item(j);
					 
					if (nnnode.getNodeType() == Node.ELEMENT_NODE) {
						//System.out.println("\t\t\t"+nnnode.getAttributes().getNamedItem("skill").getNodeValue()+"   "+Float.parseFloat(nnnode.getAttributes().getNamedItem("prob").getNodeValue()));
						skills.put(nnnode.getAttributes().getNamedItem("skill").getNodeValue(), Float.parseFloat(nnnode.getAttributes().getNamedItem("prob").getNodeValue()));
					}
				}
				//System.out.println("\t\t"+collaborator+"  "+skills.toString());
				addCollaborators(collaborator, skills);
			}
		}
		
		System.out.println("\t\t"+getCollaborators());
	}
	
	public void parseTasks(Node node){
		Node nnode, nnnode;
		NodeList nodeChild, nnodeChild;
		String task;
		ArrayList<String> skills = new ArrayList<String>();
		
		nodeChild = node.getChildNodes();
		for (int i = 0; i < nodeChild.getLength(); i++) {
			nnode = nodeChild.item(i);
			if (nnode.getNodeType() == Node.ELEMENT_NODE) {
				task = nnode.getAttributes().getNamedItem("id").getNodeValue();
				//System.out.println("\t\t"+task);
				
				nnodeChild = nnode.getChildNodes();
				skills = new ArrayList<String>();
				for (int j = 0; j < nnodeChild.getLength(); j++) {
					nnnode = nnodeChild.item(j);
					if (nnnode.getNodeType() == Node.ELEMENT_NODE) {
						skills.add(nnnode.getAttributes().getNamedItem("name").getNodeValue());
					}
				}
				//System.out.println(skills);
				addTaskSkills(task, skills);
			}
			
		}
		System.out.println("\t\t"+getTaskSkills());
	}
	
	public static void errorMsg(String msg){
		System.out.println("ERROR: "+msg);
	}
	

}
