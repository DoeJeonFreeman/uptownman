


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
 * @author 2c.me
 */
public class EnsembleMemberCombiner {


	private Document myDoc;
	private Document clonedDoc; //deep 

	private XPathFactory xFactory = XPathFactory.instance();

	
//	private ComputePercentiles percentiles;
   
	
	private Properties prop;
	private String varShortcut;
	private String dateStr;
	private int count_members;
	
	private String memberAbsolutePath;
	
	 
	public EnsembleMemberCombiner() {
		// TODO Auto-generated constructor stub
		super();
		System.out.println("\nEPSG EnsembleMemberCombiner.run()");
		loadPropertyFile();
	}
	
	public void combineEnsembleMember(String varShortcut, String dateStr) {
		this.varShortcut = varShortcut.toUpperCase();
		this.dateStr = dateStr;
		
//		percentiles = new ComputePercentiles();
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

	
	public void getEnsembleMemberData() {}


	public void writeXML(){
		try {
			count_members=0;
			myDoc = new Document();
			Element rootElement = new Element(varShortcut);
			rootElement.setAttribute("issuedAt", getDateOfIssue(dateStr));
			rootElement.setAttribute("basedOn", "Ensemble MOS");
			myDoc.setRootElement(rootElement);

			SAXBuilder sb = new SAXBuilder();
			Vector<Document> memberList = new Vector<Document>();
			
			for(int i=0; i<24; i++){
				String stepper = String.format("%02d", i);
				try{
				memberAbsolutePath = prop.getProperty("sourceDir")+ varShortcut+"/"+prop.getProperty("fileNamePrefix")+varShortcut+"_M"+stepper+"."+dateStr+".xml";
				Document eachMember = sb.build(memberAbsolutePath);
				memberList.add(eachMember);
				count_members++;
				} catch (IOException e) {
//					e.printStackTrace();
					System.out.println("--> Target not found!\t" + memberAbsolutePath);
					continue;
				}
			}
			
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
//System.out.println(eachMember.getBaseURI());
				for(int j=0; j<specifiedGroupList.size(); j++){ // 51
					Element gList = specifiedGroupList.get(j);
					List<Element> stationList =  gList.getChildren();
					
					for(int q=0; q<stationList.size(); q++){
						Element stationFromMap = stationList.get(q);

						expr = xFactory.compile("//stn[@stnNo='"+stationFromMap.getAttributeValue("stnCode")+"']",Filters.element());
						List<Element> matchedStn = expr.evaluate(eachMember);
						if(matchedStn != null){
							for(Element stn : matchedStn){ //list matchedStn has one station
								Element stnCopy = stn.clone();
								stnCopy.detach();
								myDoc.getRootElement().getChildren().get(j).addContent(stnCopy);
							}
						}
					}
				}
			}// member 


			
			expr = xFactory.compile("/stationInfo/regionGroup",Filters.element());
			List<Element> groupNodes = expr.evaluate(stationInfoMap);

			List dataSet_firstOne= myDoc.getRootElement().getChildren("regionGroup").get(0).getChildren("stn").get(0).getChildren();
			int length_ds = dataSet_firstOne.size();
			
			for(int me=0; me<groupNodes.size(); me++){
				Element group = groupNodes.get(me);
				List<Element> stationsInGroup = group.getChildren("station");
				for(int j=0; j<stationsInGroup.size(); j++){ 
					String stnCode = stationsInGroup.get(j).getAttributeValue("stnCode");
					String stnName = stationsInGroup.get(j).getAttributeValue("stnName");
					expr = xFactory.compile("//stn[@stnNo='" + stnCode + "']",Filters.element());
					List<Element> gotStations = expr.evaluate(myDoc);
//System.out.println("[" + stnCode + "] has " +gotStations.size()+ " ensemble member(s)");
					
					Element eachStation = getProcessdEnsembleStation(gotStations, length_ds);
					//JDOM2
					// use the default implementation
					eachStation.setAttribute("stnNo", stnCode);
					eachStation.setAttribute("stnName", stnName);
					
					Element eachStationCopy = eachStation.clone();
					eachStationCopy.detach();
					clonedDoc.getRootElement().getChildren("regionGroup").get(me).addContent(eachStationCopy);
				}	

			}
			writeDocumentAsXML(clonedDoc,count_members);
				
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[FAILED TO LOAD] properties.getProperty(stationMapper)" );
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private Element getProcessdEnsembleStation(List<Element> gotStations, int length_ds) {
		XPathExpression<Element> xpre;
		Element stnEle = new Element("stn");
		for(int again=0; again<length_ds; again++){

			Vector<Double> values = new Vector<Double>();
			String lst="";
			for(int jinx=0; jinx < gotStations.size(); jinx++){ 
				Element n = gotStations.get(jinx); 
				xpre = xFactory.compile("dataSet",Filters.element());
				List<Element> dsSets = xpre.evaluate(n);
				
				if(jinx==0)lst = dsSets.get(again).getAttributeValue("lst");

				if(dsSets.get(again).getAttributeValue("val")!=null){
					values.add(Double.parseDouble(dsSets.get(again).getAttributeValue("val")));
				}
			}
			Element wholeDataSet = printPercentile(lst, values);
			Element wholeDataSetCopy = wholeDataSet.clone();
			wholeDataSetCopy.detach();
			stnEle.addContent(wholeDataSetCopy);
		}


		return stnEle;
	}




	public Element printPercentile(String lst, Vector<Double> values){
		Element dataSet = new Element("dataSet");
		dataSet.setAttribute("lst",lst);
		
		if(values.size()!=0 && values.size()<25) {
			//degub
//			System.out.print(lst + " : ");
//			Iterator<Double> iter = values.iterator();
//			while(iter.hasNext()){
//				double d = iter.next();
//				System.out.print("["+d+"] ");
//			}
//			System.out.println();
			//degub
			
			if(varShortcut.toLowerCase().equals("pty")){
				dataSet.setAttribute("rain",getPTYPercentage(values, count_members, "rain")+"");
				dataSet.setAttribute("sleet",getPTYPercentage(values, count_members, "sleet")+"");
				dataSet.setAttribute("snow",getPTYPercentage(values, count_members, "snow")+"");
			}else if(varShortcut.toLowerCase().equals("sky")){
//System.out.println("---->before getCLDPercentage() values.size: "+values.size() + " member(s): " + count_members);
				dataSet.setAttribute("clear",getCLDPercentage(values, count_members, "clear")+"");
				dataSet.setAttribute("scattered",getCLDPercentage(values, count_members, "scattered")+"");
				dataSet.setAttribute("broken",getCLDPercentage(values, count_members, "broken")+"");
				dataSet.setAttribute("overcast",getCLDPercentage(values, count_members, "overcast")+"");
				dataSet.setAttribute("median",evaluate(values, 50)+"");
				dataSet.setAttribute("mn",getMinMaxVal(values, "MIN")+"");
				dataSet.setAttribute("mx",getMinMaxVal(values, "MAX")+"");
				dataSet.setAttribute("pr10th",evaluate(values, 10)+"");
				dataSet.setAttribute("pr25th",evaluate(values, 25)+"");
				dataSet.setAttribute("pr75th",evaluate(values, 75)+"");
				dataSet.setAttribute("pr90th",evaluate(values, 90)+"");
				
			}else{
				dataSet.setAttribute("median",evaluate(values, 50)+"");
				dataSet.setAttribute("mn",getMinMaxVal(values, "MIN")+"");
				dataSet.setAttribute("mx",getMinMaxVal(values, "MAX")+"");
				dataSet.setAttribute("pr10th",evaluate(values, 10)+"");
				dataSet.setAttribute("pr25th",evaluate(values, 25)+"");
				dataSet.setAttribute("pr75th",evaluate(values, 75)+"");
				dataSet.setAttribute("pr90th",evaluate(values, 90)+"");
			}
		}else{
//System.out.println(lst + " :  has no data..");
		}
		
		return dataSet;
	}

	
	public double evaluate( List<Double> list, int p ){
		if ((p > 100) || (p <= 0)) {
			throw new IllegalArgumentException("invalid quantile value: " + p);
		}

		if (list.size() == 0) {
			return Double.NaN;
		}
		if (list.size() == 1) {
			return list.get(0); 
		}

		double[] sorted = new double[list.size()];
		for( int i = 0; i < list.size(); i++ ){
			sorted[i] = list.get(i);
		}
		Arrays.sort(sorted);
		return Math.round((evaluateSorted( sorted, p )*100d))/100d;
//		return evaluateSorted( sorted, p );    
	}
	
	
	private double evaluateSorted( final double[] sorted, final double p ){
		double n = sorted.length;
		double pos = p * (n + 1) / 100;
		double fpos = Math.floor(pos);
		int intPos = (int) fpos;
		double dif = pos - fpos;

		if (pos < 1) {
			return sorted[0];
		}
		if (pos >= n) {
			return sorted[sorted.length - 1];
		}
		double lower = sorted[intPos - 1];
		double upper = sorted[intPos];
		return lower + dif * (upper - lower);
	}
	
	public double getMinMaxVal(List<Double> list, String flag){
		double[] sorted = new double[list.size()];
		for( int i = 0; i < list.size(); i++ ){
			sorted[i] = list.get(i);
		}
		Arrays.sort(sorted);
		double val = 0;
		if(flag.toUpperCase().equals("MIN")) val = sorted[0];
		else if(flag.toUpperCase().equals("MAX"))val = sorted[sorted.length-1];
		return val;
	}
	
	
	public double getPTYPercentage(List<Double> list, int memberCount, String flag){
		double currNum=0;
		double counter=0;
		if(flag.equals("rain")){
			currNum = 1; 
		}else if(flag.equals("sleet")){
			currNum = 2; 
		}else if(flag.equals("snow")){
			currNum = 3; 
		}	
		for(double val : list){
			if(currNum==val){
				counter++;
			}
		}
		return Math.round(((counter/list.size())*100)*100d)/100d;
	}

	public double getCLDPercentage(List<Double> list, int memberCount, String flag){
//System.out.println("EnsembleMemberCombiner.getCLDPercentage()" + memberCount);
		double valFrom = 0;
		double valTo = 0;
		double counter=0;
		if(flag.equals("clear")){
			valFrom = 0; valTo = 2.4;
		}else if(flag.equals("scattered")){
			valFrom = 2.5; valTo = 4.9;
		}else if(flag.equals("broken")){
			valFrom = 5; valTo = 7.4;
		}else if(flag.equals("overcast")){
			valFrom = 7.5; valTo = 10;
		}	
		for (double val : list){
			if(valFrom<=val && val<=valTo){
				counter++;
			}
//			else if(val<0 || val>10){
//				System.out.println("--> [CLOUD AMOUNT] OUT OF RANGE : " + val);
//			}
		}
//		System.out.println("couter/memberCount \t"+counter +" \t/ \t" +list.size() +"\t = " + Math.round(((counter/list.size())*100)*100d)/100d);
//		return (counter/memberCount)*100;
		return Math.round(((counter/list.size())*100)*100d)/100d;
	}
/**
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

	
	
	public void writeDocumentAsXML(Document doc, int memberCount){
		
			clonedDoc.getRootElement().setAttribute("ensembleMember", memberCount+"");	
			
			XMLOutputter outputter=new XMLOutputter();
			Format f=outputter.getFormat();
			f.setEncoding("utf-8");  
			f.setIndent(" ");
			f.setLineSeparator("\r\n"); 
			f.setTextMode(Format.TextMode.TRIM); 
			outputter.setFormat(f);
			
			try {
			FileOutputStream fStream;
				fStream = new FileOutputStream(prop.getProperty("targetDir")+prop.getProperty("fileNamePrefix")+varShortcut+"."+dateStr+".xml");
				OutputStreamWriter fWriter = new OutputStreamWriter(fStream,"utf-8"); 
				outputter.output(doc, fWriter);
				fWriter.close();
				System.out.println("==============================================================================================================================");
				System.out.println(prop.getProperty("targetDir")+prop.getProperty("fileNamePrefix")+varShortcut+"."+dateStr+".xml");
				System.out.println("[EnsembleMemberCombiner] "+varShortcut+" "+dateStr+" Done.  "+memberCount+" member(s)");
				System.out.println("==============================================================================================================================");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
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

	
//  testRunner.run()
	public static void main(String[] args) {
		String dateTime = args[0]; //201406160000 meBirthDay
		EnsembleMemberCombiner combiner = new EnsembleMemberCombiner();
		String[] var2BProcessed = combiner.prop.getProperty("var2BProcessed").split(",");
		//sky, pty, r12, mmx
		for(String variable : var2BProcessed){
			combiner.combineEnsembleMember(variable, dateTime);
		}
		System.out.println("DONE.");
	}
	
	
}
