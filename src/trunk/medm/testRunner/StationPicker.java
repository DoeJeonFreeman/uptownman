package trunk.medm.testRunner;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class StationPicker {

	
	public StationPicker(String whichVar) {
		
	}

	public void MergeEnsembleMembersUsingXPath(){
		
	}
	
	public void writeXML(){
		
	}
	
	public static void main(String args){
		StationPicker picker = new StationPicker("R12");
		
		
//		 DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//		    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//		    Document doc = docBuilder.newDocument();
//		    Element rootSetElement = doc.createElement("rootSet");
//		    Node rootSetNode = doc.appendChild(rootSetElement);
//		    Element creationElement = doc.createElement("creationDate");
//		    rootSetNode.appendChild(creationElement);
////		    creationElement.setTextContent(dateString); 
//		    File dir = new File("/tmp/rootFiles");
//		    String[] files = dir.list();
//		    if (files == null) {
//		        System.out.println("No roots to merge!");
//		    } else {
//		        Document rootDocument;
//		            for (int i=0; i<files.length; i++) {
//		                       File filename = new File(dir+"/"+files[i]);        
//		               rootDocument = docBuilder.parse(filename);
////		               Node tempDoc = doc.importNode((Node) Document.getElementsByTagName("root").item(0), true);
////		               rootSetNode.appendChild(tempDoc);
//		        }
//		    } 
		
		
	}
	
}
