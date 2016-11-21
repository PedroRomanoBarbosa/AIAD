package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.ErrorManager;

import javax.xml.parsers.*;

import org.w3c.dom.*;

public class Parser {
	private String modelName;
	private ArrayList<String> collaborators = new ArrayList<String>();
	private String coordinator;
	
	public void setModelName(String name){
		this.modelName = name;
	}
	
	public String getModelName(){
		return this.modelName;
	}
	
	public void setCoordinator(String name){
		this.coordinator = name;
	}
	
	public String getCoordinator(){
		return this.coordinator;
	}
	
	public void addCollaborators(String collaborator){
		this.collaborators.add(collaborator);
	}
	
	public ArrayList<String> getCollaborators() {
		return this.collaborators;
	}
	

	public void execute(File inputFile) {
		Node node;
		
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			
			
			
			Node root = doc.getDocumentElement();
			System.out.println("Root element :" + root.getNodeName());
			
			NodeList rootChild = root.getChildNodes();
			for (int i = 0; i < rootChild.getLength(); i++) {
				node = rootChild.item(i);
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					System.out.println("\t"+node.getNodeName());
					
					if (node.getNodeName() == "project") {
						parseProject(node);
					}else if (node.getNodeName() == "coordinators") { // nao e necessario
						
					}else if (node.getNodeName() == "collaborators") {
						
					}else if (node.getNodeName() == "task") {
						
					}else if (node.getNodeName() == "tasks") {
						
					}else if (node.getNodeName() == "skills") {
						
					}
				}
			}
		} catch (Exception e){
			// TODO catch
		}
		

	}
	
	public void parseProject(Node project){
		Node node, nnode, nnnode;
		NodeList projectChild, nodeChild, nnodeChild;
		
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
									addCollaborators(nnnode.getAttributes().getNamedItem("id").getNodeValue());
								}
							}
							System.out.println("\t\t\t"+nnode.getNodeName()+": "+getCollaborators());
						}
					}
				}
			}
		}
	}
	
	
	public static void errorMsg(String msg){
		System.out.println("ERROR: "+msg);
	}
	

}
