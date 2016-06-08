package trunk.medm.executioner;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

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
public class XMLCombiner {

	public XMLCombiner() {
		getEnsembleMemberData();
		writeXML();
	}

	/**
	 * read stationMap.xml -> concat specified ensemble member data.. -> nodeList ->calcFunc(processedPercentileData)
	 */
	public void getEnsembleMemberData() {}

	public void writeXML(){
		File ensembleSummary = new File("summary.xml");

		int readCount=0;
		
		Document myDoc;
		try {
			myDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element rootElement = myDoc.createElement("varShortcut_XXX");
			myDoc.appendChild(rootElement);

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			String varShortcut = "R12";
			//		File dir = new File("ensemble2014/MMX");
			Vector<Document> memberList = new Vector<Document>();
			//Ensemble

			for(int i=0; i<24; i++){
				String stepper = String.format("%02d", i);
				memberList.add(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("e:/SFS_XMLOutput/ensembleSample/XML/PMOS/"+ varShortcut+"/DFS_MEDM_STN_EPSG_PMOS_"+varShortcut+"_M"+stepper+".201406160000.xml"));
			}

			//read stationInfoMap.xml 
			Document stationInfoMap = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("e:/SFS_XMLOutput/Stations_MEDM.xml");
			XPath path = XPathFactory.newInstance().newXPath();
			XPathExpression pathExp = path.compile("/stationInfo/regionGroup");
			NodeList specifiedGroupList = (NodeList)pathExp.evaluate(stationInfoMap, XPathConstants.NODESET);
			pathExp = path.compile("station");
			//loop regionGroup
			int count_openFileStream=0;
			for(int j=0; j<specifiedGroupList.getLength(); j++){
				Node node = specifiedGroupList.item(j);
				System.out.print("idx "+j+" "+ node.getAttributes().getNamedItem("grCode").getNodeValue()+" " + node.getAttributes().getNamedItem("grName").getNodeValue() + " : has "  );
				NodeList stationList = (NodeList) pathExp.evaluate(node, XPathConstants.NODESET);
				System.out.print(stationList.getLength()+ " children..  ");
				Element regionGroup = myDoc.createElement("regionGroup");
				regionGroup.setAttribute("grCode", node.getAttributes().getNamedItem("grCode").getNodeValue());
				regionGroup.setAttribute("grName", node.getAttributes().getNamedItem("grName").getNodeValue());
				//각 그룹별 지점들 
				for(int q=0; q<stationList.getLength(); q++){
					Node station = stationList.item(q);
					System.out.println(printEachStationInfo(station) + "\t ");

					// 맵파일 지점코드랑 일치하는  앙상블 지점(1~24)
					//					for (int k=0; k<memberList.size(); k++) {
					//						Document memberDoc  = (Document)memberList.get(k);
					//						count_openFileStream++; //여기서 23멤버 다돌리면 6000번 열림 ㅡ,.ㅡ
					//						XPath xPath = XPathFactory.newInstance().newXPath();
					//						XPathExpression xPathExpression = xPath.compile(exp);
					//						NodeList nodes = (NodeList) xPathExpression.evaluate(memberDoc,XPathConstants.NODESET); // 걍 스테이션 하나임 nodes.getLength=1

					//					if(station ==null)System.out.println(" null");
					//					System.out.println(station.getChildNodes().getLength());
					//					if(station.getChildNodes().getLength()==23){

//					String ds_exp = "//regionGroup/stn[@stnNo='"+printEachStationInfo(station)+"']/dataSet";
					XPath xPath = XPathFactory.newInstance().newXPath();
					XPathExpression xPathExpression;
//					XPathExpression xPathExpression = xPath.compile(ds_exp);
//					NodeList nodes = (NodeList) xPathExpression.evaluate(station,XPathConstants.NODESET); // 걍 스테이션 하나임 nodes.getLength=1
//					System.out.println(nodes.getLength());
					for(int a =1; a<=24; a++){
						for(Document currDoc : memberList){
							Vector<Double> values = new Vector<Double>();
//							String exp = "//regionGroup/stn[@stnNo='"+printEachStationInfo(station)+"']/dataSet["+a+"]";
//							xPathExpression = xPath.compile(exp);
//							Node dsNode = (Node)xPathExpression.evaluate(currDoc,XPathConstants.NODE); // 걍 스테이션 하나임 nodes.getLength=1
							
							Node dsNode = currDoc.getElementsByTagName("dataSet").item(q);
							if(dsNode == null){
								continue;
							}else{
								values.add(new Double(dsNode.getAttributes().getNamedItem("val").getNodeValue()));
							}
							readCount++;
//							 백터값계산
						}
					}
					//					}

					//각 멤버 해당 스테이션의 데이터셋 PMOS GDPS의 경우 23 set
					//						for (int k = 0; k < nodes.getLength(); k++) {
					//							Node dataSet = nodes.item(k);
					//							for(int a=0; a < ){
					//								
					//							}
					//							Node copyNode = myDoc.importNode(dataSet, true);
					//							regionGroup.appendChild(copyNode);
					//						}
					//					}
					//					myDoc.getDocumentElement().appendChild(regionGroup);
				}
				System.out.println();
				System.out.println(count_openFileStream);
			}
			//						printPrettyFormat(myDoc);

			//loop stations 

			//			String exp = "//regionGroup/stn[@stnNo='47192']";
			//			Element regionGroup = myDoc.createElement("regionGroup");
			//			for (File rootFile : memberList) {
			//				Document timeSeriesXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(rootFile.getAbsoluteFile());
			//
			//				XPath xPath = XPathFactory.newInstance().newXPath();
			//				XPathExpression xPathExpression = xPath.compile(exp);
			//				NodeList nodes = (NodeList) xPathExpression.evaluate(timeSeriesXML,XPathConstants.NODESET);
			//				
			//				for (int i = 0; i < nodes.getLength(); i++) {
			//					Node node = nodes.item(i);
			//					Node copyNode = myDoc.importNode(node, true);
			//					regionGroup.appendChild(copyNode);
			//				}
			//			}
			//			myDoc.getDocumentElement().appendChild(regionGroup);

			//			printPrettyFormat(myDoc);


			System.out.println("readCount: "+readCount);

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


	public void printXpathResult(Object result){
		System.out.println("XMLCombiner.printXpathResult()");
		NodeList nodes = (NodeList)result;
		System.out.println(nodes.getLength());
		for(int i=0; i<nodes.getLength(); i++){
			System.out.println(nodes.item(i).getNodeName());
		}
	}

	public String printEachStationInfo(Node stn) throws XPathExpressionException{
		//		XPath path = XPathFactory.newInstance().newXPath();
		//		return  path.evaluate("@stnCode",stn);

		return stn.getAttributes().getNamedItem("stnCode").getNodeValue();
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
		XMLCombiner runner = new XMLCombiner();
	}
}
