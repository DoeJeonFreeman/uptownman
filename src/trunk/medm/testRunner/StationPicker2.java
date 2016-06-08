package trunk.medm.testRunner;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

public class StationPicker2 {
	public StationPicker2() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		String varShortcut = "CLD";
		//		File dir = new File("ensemble2014/MMX");
		File[] rootFiles = new File[24]; 
		//Ensemble
		for(int i=0; i<24; i++){
			String stepper = String.format("%02d", i);
			rootFiles[i] = new File("e:/SFS_XMLOutput/ensembleSample/XML/PMOS/"+ varShortcut+"/DFS_MEDM_STN_EPSG_PMOS_"+varShortcut+"_M"+stepper+".201406160000.xml");
		}

		String exp = "//regionGroup/stn[@stnNo='47192']";
		try {
//				Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Document firstOne = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element rootElement = firstOne.createElement("varShortcut");
			for (File rootFile : rootFiles) {
				Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rootFile.getAbsoluteFile());

				XPath xPath = XPathFactory.newInstance().newXPath();
				XPathExpression xPathExpression = xPath.compile(exp);
				NodeList nodes = (NodeList) xPathExpression.evaluate(xmlDocument,XPathConstants.NODESET);

				Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element regionGroup = newXmlDocument.createElement("regionGroup");
				newXmlDocument.appendChild(regionGroup);
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					Node copyNode = newXmlDocument.importNode(node, true);
					//					newXmlDocument.appendChild(copyNode);
//					regionGroup.appendChild(copyNode);
				}
				firstOne.appendChild(regionGroup);

//			printXmlDocument(newXmlDocument);
			}
//			printXmlDocument(newXmlDocument);
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
	}



	public static void getSpecifiedStation() throws Exception{
		String varShortcut = "CLD";
		File[] rootFiles = new File[24]; 
		//Ensemble
		for(int i=0; i<24; i++){
			String stepper = String.format("%02d", i);
			rootFiles[i] = new File("e:/SFS_XMLOutput/ensembleSample/XML/PMOS/"+ varShortcut+"/DFS_MEDM_STN_EPSG_PMOS_"+varShortcut+"_M"+stepper+".201406160000.xml");
		}

		String exp = "//regionGroup/stn[@stnNo='47192']";
		for (File rootFile : rootFiles) {
			Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rootFile.getAbsoluteFile());
		}
	}
	
	public static void printXmlDocument(Document document) {
		DOMImplementationLS domImplementationLS = 
			(DOMImplementationLS) document.getImplementation();
		LSSerializer lsSerializer = 
			domImplementationLS.createLSSerializer();
		String string = lsSerializer.writeToString(document);


		System.out.println(string);
	}
	
	
}
