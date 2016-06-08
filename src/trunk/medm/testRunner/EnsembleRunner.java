package trunk.medm.testRunner;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author meTomorrow
 * Test data outputter
 */
public class EnsembleRunner {

	public EnsembleRunner() {
		getEnsembleMemberData();
		writeXML();
	}

	/**
	 * read stationMap.xml -> concat specified ensemble member data.. -> nodeList ->calcFunc(processedPercentileData)
	 */
	public void getEnsembleMemberData() {}

	public void writeXML(){
		File ensembleSummary = new File("summary.xml");

		Document myDoc;
		try {
			myDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element rootElement = myDoc.createElement("varShortcut_XXX");
			myDoc.appendChild(rootElement);


			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			String varShortcut = "R12";
			//		File dir = new File("ensemble2014/MMX");
			File[] rootFiles = new File[24]; 
			//Ensemble
			for(int i=0; i<24; i++){
				String stepper = String.format("%02d", i);
				rootFiles[i] = new File("e:/SFS_XMLOutput/ensembleSample/XML/PMOS/"+ varShortcut+"/DFS_MEDM_STN_EPSG_PMOS_"+varShortcut+"_M"+stepper+".201406160000.xml");
			}

			String exp = "//regionGroup/stn[@stnNo='47192']";

			Element regionGroup = myDoc.createElement("regionGroup");
			for (File rootFile : rootFiles) {
				Document timeSeriesXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rootFile.getAbsoluteFile());

				XPath xPath = XPathFactory.newInstance().newXPath();
				XPathExpression xPathExpression = xPath.compile(exp);
				NodeList nodes = (NodeList) xPathExpression.evaluate(timeSeriesXML,XPathConstants.NODESET);
				
//				if(nodes.getLength() < 23){
					//MEDM MOS     nodes.getLength() == 23   
					//MEDM ECMWF   nodes.getLength() == 20
//					continue;
//				}
				
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					Node copyNode = myDoc.importNode(node, true);
					regionGroup.appendChild(copyNode);
				}
				//HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted.
				//myDoc.appendChild(regionGroup); 
			}
			myDoc.getDocumentElement().appendChild(regionGroup);
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			printPrettyFormat(myDoc);

			
			
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	
	
	
	
	
	
	/**
	 * @param pretty print XML using optimus-prime
	 * @note  results may vary depending on the java version, search for workarounds specific to your platform
	 */
	public void printPrettyFormat(Document doc){
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			//initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			String xmlString = result.getWriter().toString();
			System.out.println(xmlString);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		EnsembleRunner runner = new EnsembleRunner();
	}
}
