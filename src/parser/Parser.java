package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	private HashMap<String, List<String>> taskSkills = new HashMap<String, List<String>>();
	private HashMap<String, String> taskType = new HashMap<String, String>();
	private HashMap<String, List<String>> taskPrecs = new HashMap<String, List<String>>();
	
	// TODO: consider project id
	
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
	
	public void addTaskSkills(String task, List<String> skills) {
		this.taskSkills.put(task, skills);
	}
	
	public HashMap<String, List<String>> getTaskSkills(){
		return taskSkills;
	}
	
	public void addTaskPrecs(String task, List<String> precs) {
		this.taskPrecs.put(task, precs);
	}
	
	public HashMap<String, List<String>> getTaskPrecs(){
		return taskPrecs;
	}
	
	public HashMap<String, String> getTaskType() {
		return taskType;
	}

	public void addTaskType(String task, String type) {
		this.taskType.put(task, type);
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
			e.printStackTrace();
		}
		

	}
	
	public void parseProject(Node project){
		Node node, nnode;
		NodeList projectChild, nodeChild;
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
							
						}/*else if (nnode.getNodeName() == "collaborators") {
							nnodeChild = nnode.getChildNodes();
							for (int k = 0; k < nnodeChild.getLength(); k++) {
								nnnode = nnodeChild.item(k);
								if (nnnode.getNodeType() == Node.ELEMENT_NODE) {
									
									// COLLABORATORS
									addProjectCollaborators(nnnode.getAttributes().getNamedItem("id").getNodeValue());
								}
							}
							System.out.println("\t\t\t"+nnode.getNodeName()+": "+getProjectCollaborators());
						}*/else if (nnode.getNodeName() == "task") {
							
							// ADD TASKS
							tasks.add(nnode.getAttributes().getNamedItem("id").getNodeValue());
							
						}
					}
				}
				// TASKS
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
						skills.put(nnnode.getAttributes().getNamedItem("skill").getNodeValue(), Float.parseFloat(nnnode.getAttributes().getNamedItem("prob").getNodeValue()));
					}
				}
				addCollaborators(collaborator, skills);
			}
		}
		
		System.out.println("\t\t"+getCollaborators());
	}
	
	public void parseTasks(Node node){
		Node nnode, nnnode, nnnnode;
		NodeList nodeChild, nnodeChild, nnnodeChild;
		String task, type;
		ArrayList<String> skills = new ArrayList<String>();
		ArrayList<String> precTasks = new ArrayList<String>();
		
		nodeChild = node.getChildNodes();
		for (int i = 0; i < nodeChild.getLength(); i++) {
			nnode = nodeChild.item(i);
			if (nnode.getNodeType() == Node.ELEMENT_NODE) {
				task = nnode.getAttributes().getNamedItem("id").getNodeValue();
				type = nnode.getAttributes().getNamedItem("type").getNodeValue();
				addTaskType(task, type);
				nnodeChild = nnode.getChildNodes();
				for (int j = 0; j < nnodeChild.getLength(); j++) {
					nnnode = nnodeChild.item(j);
					if (nnnode.getNodeName() == "skills") {
						skills = new ArrayList<String>();
						nnnodeChild = nnnode.getChildNodes();
						for (int j2 = 0; j2 < nnnodeChild.getLength(); j2++) {
							nnnnode = nnnodeChild.item(j2);
							if (nnnnode.getNodeType() == Node.ELEMENT_NODE) {
								skills.add(nnnnode.getAttributes().getNamedItem("name").getNodeValue());
							}
						}
						addTaskSkills(task, skills);
					}
					else if (nnnode.getNodeName() == "tasks") {
						precTasks = new ArrayList<String>();
						nnnodeChild = nnnode.getChildNodes();
						for (int j2 = 0; j2 < nnnodeChild.getLength(); j2++) {
							nnnnode = nnnodeChild.item(j2);
							if (nnnnode.getNodeType() == Node.ELEMENT_NODE) {
								precTasks.add(nnnnode.getAttributes().getNamedItem("id").getNodeValue());
							}
							
						}
						addTaskPrecs(task, precTasks);
					}
				}
			}
		}
		System.out.println("\t\ttype: "+getTaskType());
		System.out.println("\t\tskills: "+getTaskSkills());
		System.out.println("\t\ttasks: "+getTaskPrecs());
	}
	
	public static void errorMsg(String msg){
		System.out.println("ERROR: "+msg);
	}

}
