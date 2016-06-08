package trunk.medm.executioner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
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
public class XMLCombiner2 {


	private Document myDoc;
	private Document clonedDoc; //toWriteXML

	private XPath path;
	private XPathExpression pathExp;

	private Properties prop;
	private String varShortcut;
	private String dateStr;

	public XMLCombiner2(String varShortcut, String dateStr) {
		this.varShortcut = varShortcut.toUpperCase();
		this.dateStr = dateStr;
		loadPropertyFile();
		getEnsembleMemberData();
		writeXML();
	}

	private void loadPropertyFile(){
		prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("ensembleMOS.properties");
			prop.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[Err.] ensembleMOS.properties not found.");
		} finally{
			if(fis != null){
				try{
					fis.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * read stationMap.xml -> concat specified ensemble member data.. -> nodeList ->calcFunc(processedPercentileData)
	 */
	public void getEnsembleMemberData() {}

	private  void removeAllChildNodes(Node node) {
		NodeList childNodes = node.getChildNodes();
		int length = childNodes.getLength();
		for (int i = 0; i < length; i++) {
			Node childNode = childNodes.item(i);
			if(childNode instanceof Element) {
				if(childNode.hasChildNodes()) {
					removeAllChildNodes(childNode);                
				}        
				node.removeChild(childNode);  
			}
		}
	}

	public void writeXML(){
		try {
			myDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element rootElement = myDoc.createElement(varShortcut);
			rootElement.setAttribute("issuedAt", getDateOfIssue(dateStr));
			rootElement.setAttribute("basedOn", "Ensemble MOS");
			myDoc.appendChild(rootElement);

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			Vector<Document> memberList = new Vector<Document>();
			//Ensemble

			for(int i=0; i<24; i++){
				String stepper = String.format("%02d", i);
				memberList.add(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(prop.getProperty("sourceDir")+ varShortcut+"/"+prop.getProperty("fileNamePrefix")+varShortcut+"_M"+stepper+"."+dateStr+".xml"));
			}

			//read stationInfoMap.xml 
			Document stationInfoMap = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(prop.getProperty("stationInfoDocs"));

			
			path = XPathFactory.newInstance().newXPath();
			pathExp = path.compile("/stationInfo/regionGroup");
			NodeList specifiedGroupList = (NodeList)pathExp.evaluate(stationInfoMap, XPathConstants.NODESET);

			for (int k = 0; k < specifiedGroupList.getLength(); k++) {
				Node group = specifiedGroupList.item(k);
				Node copyGroup = myDoc.importNode(group, true);
				removeAllChildNodes(copyGroup);
				myDoc.getDocumentElement().appendChild(copyGroup);
			}

			clonedDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			//			clonedDoc.importNode(myDoc.getDocumentElement(),true);
			clonedDoc = deepCopyDocument(myDoc);


			// 24 members
			for (Document eachMember : memberList) {
				//loop regionGroup
				System.out.println(eachMember.getDocumentURI());
				for(int j=0; j<specifiedGroupList.getLength(); j++){ // 51
					Node node = specifiedGroupList.item(j);
					pathExp = path.compile("station");
					NodeList stationList = (NodeList) pathExp.evaluate(node, XPathConstants.NODESET);
					//Element regionGroup = myDoc.createElement("regionGroup");
					//regionGroup.setAttribute("grCode", node.getAttributes().getNamedItem("grCode").getNodeValue());
					//regionGroup.setAttribute("grName", node.getAttributes().getNamedItem("grName").getNodeValue());

					//각 그룹별 지점들 
					for(int q=0; q<stationList.getLength(); q++){
						Node stationFromMap = stationList.item(q);
						// System.out.print(printEachStationInfo(station) + "\t ");

						// 맵파일 지점코드랑 일치하는  앙상블 지점(1~24)
						// 한그룹에 해당지점들 다 때려넘 24멤범버면 해당그룹에 24멤버 반복해서 다 들어감ㅋㅋㅋㅋㅋ
						String exp = "//regionGroup/stn[@stnNo='"+printEachStationInfo(stationFromMap)+"']";
						path = XPathFactory.newInstance().newXPath();
						pathExp = path.compile(exp);

						Node matchedStn = (Node)pathExp.evaluate(eachMember,XPathConstants.NODE);
						if(matchedStn != null){
							Node copyNode = myDoc.importNode(matchedStn, true);
							//exp = "//regionGroup[grCode="+node.getAttributes().getNamedItem("grCode").getNodeValue() + "]";
							//xPathExpression = xPath.compile(exp);
							myDoc.getDocumentElement().getElementsByTagName("regionGroup").item(j).appendChild(copyNode);
						}
					}
				}


			}// member loop




			//맵에서 모든 스테이션 그룹상관없이 리스트로 받음 
			pathExp = path.compile("/stationInfo/regionGroup");
			NodeList groupNodes = (NodeList)pathExp.evaluate(stationInfoMap, XPathConstants.NODESET);

			//아웃풋xml에 있는 첫번째 스테이션 뽑아서 데이터셋 렝스 체킹
			pathExp = path.compile("//stn[1]");
			Node dataSet_firstOne = (Node)pathExp.evaluate(myDoc, XPathConstants.NODE);
			int length_ds = getNodeChildrenCount(dataSet_firstOne);
			// 그룹 루프 쓰루~
			for(int me=0; me<groupNodes.getLength(); me++){
				//그룹내 지점
				Element group = (Element)groupNodes.item(me);
				NodeList stationsInGroup = group.getElementsByTagName("station");
				for(int j=0; j<stationsInGroup.getLength(); j++){ 
					String stnCode = stationsInGroup.item(j).getAttributes().getNamedItem("stnCode").getNodeValue();
					String stnName = stationsInGroup.item(j).getAttributes().getNamedItem("stnName").getNodeValue();
					pathExp = path.compile("//stn[@stnNo='" + stnCode + "']");
					//루프 인덱스 순서대로 한지점의 모든 앙상블 노드리스트로 뽑음 서울만 24멤버 뽑은게 gotStations
					NodeList gotStations = (NodeList)pathExp.evaluate(myDoc, XPathConstants.NODESET);
					System.out.println("[" + stnCode + "] has " +gotStations.getLength()+ " ensemble member(s)");

					Element eachStation = getProcessdEnsembleStation(gotStations, length_ds);
					eachStation.setAttribute("stnNo", stnCode);
					eachStation.setAttribute("stnName", stnName);
					//해당스테이션이 상위 그룹 노드 뽑아
					//글고 eachStation append!!
					clonedDoc.getDocumentElement().getElementsByTagName("regionGroup").item(me).appendChild(eachStation);
				}	



			}
			writeDocumentAsXML(clonedDoc);
			//			printPrettyFormat(clonedDoc);



			//			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("target file not found!");
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//스테이션 던져~
	private Element getProcessdEnsembleStation(NodeList gotStations, int length_ds) throws XPathExpressionException{

		Element stnEle = clonedDoc.createElement("stn");
		//데이터셋 수만큼 루프 돌면서 
		for(int again=0; again<length_ds; again++){

			Vector<Double> values = new Vector<Double>();
			String lst="";
			//한지점 모든멤버만 있는 노드리스트 순회
			// !! 데이터셋 1에 껄리는 애들 데이터만 쫙 뽑아(gotStations에서)
			for(int jinx=0; jinx < gotStations.getLength(); jinx++){ //해당 지점  (멤버 수 만큼 루프) 
				Node n = gotStations.item(jinx); // 스테이션하나(24개멤버중에중에)  
				pathExp = path.compile("dataSet");
				NodeList dsSets = (NodeList)pathExp.evaluate(n, XPathConstants.NODESET);
				//걍 lst 프리트할려고 
				if(jinx==0)lst = dsSets.item(again).getAttributes().getNamedItem("lst").getNodeValue();

				//				System.out.print(dsSets.item(again).getAttributes().getNamedItem("lst") + " ");
				//				System.out.println(dsSets.item(again).getAttributes().getNamedItem("val"));
				if(dsSets.item(again).getAttributes().getNamedItem("val")!=null){
					values.add(Double.parseDouble(dsSets.item(again).getAttributes().getNamedItem("val").getNodeValue()));
				}
			}
			//데이터셋 계산결과 스테이션에 어펜드
			//			System.out.println("====================================================values"+values.size());
			//계산해서 노드 만들어 던져
			stnEle.appendChild(printPercentile(lst, values));
		}


		return stnEle;
	}


	public void removeAll(Node node, short nodeType, String name) {
		if (node.getNodeType() == nodeType && (name == null || node.getNodeName().equals(name))) {
			node.getParentNode().removeChild(node);
		} else {
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				removeAll(list.item(i), nodeType, name);
			}
		}
	}


	public Element printPercentile(String lst, Vector<Double> values){

		System.out.print(lst + " : ");
		Iterator<Double> iter = values.iterator();
		while(iter.hasNext()){
			System.out.print("["+iter.next()+"]");
		}
		System.out.println();

		return clonedDoc.createElement("dataSet");
	}

	public int getNodeChildrenCount(Node dataSet_firstOne){
		Element firstLevel = (Element)dataSet_firstOne;
		NodeList dsNodes = firstLevel.getElementsByTagName("dataSet");
		int length_dsNodes = dsNodes.getLength();
		return length_dsNodes;
	}


	public void printXpathResult(Object result){
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

	private Document deepCopyDocument(Document doc) throws TransformerException{
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer tx   = tfactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		DOMResult result = new DOMResult();
		tx.transform(source,result);
		return (Document)result.getNode();
	}


	public void writeDocumentAsXML(Document doc){
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			StreamResult result =  new StreamResult(new StringWriter());

			//t.setParameter(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
			transformer.transform(source, result);

			//writing to file
			FileOutputStream fop = null;
			File file;

			String path = prop.getProperty("targetDir")+varShortcut+"/"+prop.getProperty("fileNamePrefix")+varShortcut+"."+dateStr+".xml";
			file = new File(path);
			fop = new FileOutputStream(file);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			String xmlString = result.getWriter().toString();
//			System.out.println(xmlString);
			byte[] contentInBytes = xmlString.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			
			System.out.print(path+"\t");
			System.out.println("Done");

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {

		}

	}

	public String getDateOfIssue(String strDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm"); 
		Date date = null;
		try {
			date = sdf.parse(strDate);
		} catch (ParseException e) {e.printStackTrace();}

		DateFormatSymbols dfSymbol = new DateFormatSymbols();
		String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
				"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		dfSymbol.setShortMonths(months);
		String pattern = "HH'UTC' dd MMM yyyy";
		sdf = new SimpleDateFormat(pattern);
		sdf.setDateFormatSymbols(dfSymbol);
		String str_dateOfIssue = sdf.format(date); 

		return str_dateOfIssue;
	}

	
	

	public static void main(String[] args) {
		XMLCombiner2 runner = new XMLCombiner2("r12","201406160000");
	}
}
