package parser;

import java.io.File;
import java.util.logging.ErrorManager;

import javax.xml.parsers.*;

import org.w3c.dom.*;

public class Parser {
	private String modelName;
	private String[] intervenients;
	
	public void setModelName(String name){
		this.modelName = name;
	}
	
	public String getModelName(){
		return this.modelName;
	}
	

	public void execute(File inputFile) {
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
			
			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("project");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	//certo
					Element e = (Element) nNode;
					System.out.println("\tnodeName: "+nNode.getNodeName());
					NamedNodeMap attr = nNode.getAttributes();
					
					setModelName(attr.getNamedItem("model").getNodeValue());
					System.out.println("\t\tmodel: "+getModelName());
					
					NodeList nnList = e.getElementsByTagName("intervenients");
					Node nnNode = nList.item(0);
					Element f = (Element) nnNode;
					nnList = f.getElementsByTagName("coordinator");
					for (int j = 0; j < nnList.getLength(); j++) {
						nnNode = nnList.item(j);
					}
				}
				else {	//errado
					errorMsg("project bad definition");
					break;
				}
			}
			
		} catch (Exception e){
			// TODO catch
		}
		

	}
	
	
	public static void errorMsg(String msg){
		System.out.println("ERROR: "+msg);
	}
	

}
