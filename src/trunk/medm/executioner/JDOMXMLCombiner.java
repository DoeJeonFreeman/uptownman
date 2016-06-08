package trunk.medm.executioner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;




/**
 * @author meTomorrow
 * Test data outputter
 */
public class JDOMXMLCombiner {


	private Document myDoc;
	private Document clonedDoc; //toWriteXML

	private XPathFactory xFactory = XPathFactory.instance();

	

    // select all links
   
	
	private Properties prop;
	private String varShortcut;
	private String dateStr;

	private String memberAbsolutePath;
	
	public JDOMXMLCombiner(String varShortcut, String dateStr) {
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


	public void writeXML(){
		try {
			myDoc = new Document();
			Element rootElement = new Element(varShortcut);
			rootElement.setAttribute("issuedAt", getDateOfIssue(dateStr));
			rootElement.setAttribute("basedOn", "Ensemble MOS");
			myDoc.setRootElement(rootElement);

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			SAXBuilder sb = new SAXBuilder();
			Vector<Document> memberList = new Vector<Document>();
			//Ensemble
				
			for(int i=0; i<24; i++){
				String stepper = String.format("%02d", i);
				try{
				memberAbsolutePath = prop.getProperty("sourceDir")+ varShortcut+"/"+prop.getProperty("fileNamePrefix")+varShortcut+"_M"+stepper+"."+dateStr+".xml";
				Document eachMember = sb.build(memberAbsolutePath);///////////////  MEDM ENSEMBLE
				memberList.add(eachMember);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Target not found!\t" + memberAbsolutePath);
					continue;
				}
			}
			//read stationInfoMap.xml 
			Document stationInfoMap = sb.build(prop.getProperty("stationMapper"));

			
			XPathExpression<Element> expr = xFactory.compile("/stationInfo/regionGroup", Filters.element());
			List<Element> specifiedGroupList = expr.evaluate(stationInfoMap);
		    for (Element group : specifiedGroupList) {
		    	Element copyGroup = group.clone();
		    	copyGroup.removeChildren("station");
		    	rootElement.addContent(copyGroup);
		    }


			clonedDoc = new Document(rootElement.clone());
//			clonedDoc = deepCopyDocument(myDoc);


			// 24 members
			for (Document eachMember : memberList) {
				//loop regionGroup
				System.out.println(eachMember.getBaseURI());
				for(int j=0; j<specifiedGroupList.size(); j++){ // 51
					Element gList = specifiedGroupList.get(j);
					List<Element> stationList =  gList.getChildren();
					
					for(int q=0; q<stationList.size(); q++){
						Element stationFromMap = stationList.get(q);

						expr = xFactory.compile("//stn[@stnNo='"+stationFromMap.getAttributeValue("stnCode")+"']",Filters.element());
						List<Element> matchedStn = expr.evaluate(eachMember);
						if(matchedStn != null){
							for(Element stn : matchedStn){ //maybe matchedStn has one station
								Element stnCopy = stn.clone();
								stnCopy.detach();
								myDoc.getRootElement().getChildren().get(j).addContent(stnCopy);
							}
						}
					}
				}
			}// member loop


			
			expr = xFactory.compile("/stationInfo/regionGroup",Filters.element());
			List<Element> groupNodes = expr.evaluate(stationInfoMap);


			List dataSet_firstOne= myDoc.getRootElement().getChildren("regionGroup").get(0).getChildren("stn").get(0).getChildren();
			int length_ds = dataSet_firstOne.size();
			
			for(int me=0; me<groupNodes.size(); me++){
				//그룹내 지점
				Element group = groupNodes.get(me);
				List<Element> stationsInGroup = group.getChildren("station");
				for(int j=0; j<stationsInGroup.size(); j++){ 
					String stnCode = stationsInGroup.get(j).getAttributeValue("stnCode");
					String stnName = stationsInGroup.get(j).getAttributeValue("stnName");
					expr = xFactory.compile("//stn[@stnNo='" + stnCode + "']",Filters.element());
					List<Element> gotStations = expr.evaluate(myDoc);
					System.out.println("[" + stnCode + "] has " +gotStations.size()+ " ensemble member(s)");
					
					Element eachStation = getProcessdEnsembleStation(gotStations, length_ds);
					//JDOM
					  // use the default implementation
					eachStation.setAttribute("stnNo", stnCode);
					eachStation.setAttribute("stnName", stnName);
					
					Element eachStationCopy = eachStation.clone();
					eachStationCopy.detach();
					clonedDoc.getRootElement().getChildren("regionGroup").get(me).addContent(eachStationCopy);
				}	



			}
			writeDocumentAsXML(clonedDoc);
				
			//			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[FAILED TO LOAD] properties.getProperty(stationMapper)" );
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	//스테이션 던져~
	private Element getProcessdEnsembleStation(List<Element> gotStations, int length_ds) {
		XPathExpression<Element> xpre;
		Element stnEle = new Element("stn");
		for(int again=0; again<length_ds; again++){

			Vector<Double> values = new Vector<Double>();
			String lst="";
			//한지점 모든멤버만 있는 노드리스트 순회
			// !! 데이터셋 1에 껄리는 애들 데이터만 쫙 뽑아(gotStations에서)
			for(int jinx=0; jinx < gotStations.size(); jinx++){ //해당 지점  (멤버 수 만큼 루프) 
				Element n = gotStations.get(jinx); // 스테이션하나(24개멤버중에중에)  
				
				
				xpre = xFactory.compile("dataSet",Filters.element());
				List<Element> dsSets = xpre.evaluate(n);
				
				
				//걍 lst 프리트할려고 
				if(jinx==0)lst = dsSets.get(again).getAttributeValue("lst");

				//				System.out.print(dsSets.item(again).getAttributes().getNamedItem("lst") + " ");
				//				System.out.println(dsSets.item(again).getAttributes().getNamedItem("val"));
				if(dsSets.get(again).getAttributeValue("val")!=null){
					values.add(Double.parseDouble(dsSets.get(again).getAttributeValue("val")));
				}
			}
			//데이터셋 계산결과 스테이션에 어펜드
			//			System.out.println("====================================================values"+values.size());
			//계산해서 노드 만들어 던져
			Element wholeDataSet = printPercentile(lst, values);
			Element wholeDataSetCopy = wholeDataSet.clone();
			wholeDataSetCopy.detach();
			stnEle.addContent(wholeDataSetCopy);
		}


		return stnEle;
	}




	public Element printPercentile(String lst, Vector<Double> values){
		double sum_test=0;
		System.out.print(lst + " : ");
		Iterator<Double> iter = values.iterator();
		while(iter.hasNext()){
			double d = iter.next();
			System.out.print("["+d+"]");
			sum_test+=d;
		}
		System.out.println();

		Element dataSet = new Element("dataSet");
		dataSet.setAttribute("lst",lst);
		dataSet.setAttribute("sum_values",sum_test+"");
		return dataSet;
	}


	/**	

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

	 */	

	
	
	
	public void writeDocumentAsXML(Document doc){
			XMLOutputter outputter=new XMLOutputter();
			Format f=outputter.getFormat();
			f.setEncoding("utf-8");  
			f.setIndent(" ");
			f.setLineSeparator("\r\n"); 
			f.setTextMode(Format.TextMode.TRIM); 
			outputter.setFormat(f);
			
			try {
			FileOutputStream fStream;
				fStream = new FileOutputStream(prop.getProperty("targetDir")+varShortcut+"/"+prop.getProperty("fileNamePrefix")+varShortcut+"."+dateStr+".xml");
				OutputStreamWriter fWriter = new OutputStreamWriter(fStream,"utf-8"); 
				outputter.output(doc, fWriter);
				fWriter.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		JDOMXMLCombiner runner = new JDOMXMLCombiner("r12","201406160000");
	}
}
