package trunk.medm.testRunner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;


import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class TMM_ToXML {

	private Document xmlDoc;
	private Element root;
	private String XMLFolderPath;
	private Vector<String> LST ; 
	private String fileName_min;
	private String fileName_max;
	private String gCodeStr;
	private String gNameStr;
	private String kindOfModel;
	private String localFilePath1;
	private String localFilePath2;

	private Properties prop;

	///////////////////////////////////////////////////////////////////////April 25, 2013
	private boolean is00UTC;
	private String strDate;
	///////////////////////////////////////////////////////////////////////April 25, 2013


	public TMM_ToXML(){
		super();
	}


	public TMM_ToXML(Vector<String> rawFileList){
		/////////////////////////////
		loadPropertyFile();
		/////////////////////////////
		XMLFolderPath = prop.getProperty("outputFolder");//////////
		doStuff(rawFileList);
	}



	private void loadPropertyFile(){
		prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("dfsConfig.properties");
			prop.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[Err occured while loading dfsConfig.properties!! ]");
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



	private void doStuff(Vector<String> fileList ){
		fileName_min = (fileList.get(0)).substring(getFileNameBeginIdx(fileList.get(0)));
		fileName_max = (fileList.get(1)).substring(getFileNameBeginIdx(fileList.get(0)));
		try {
			LST = new Vector<String>();
			localFilePath1 = fileList.get(0);
			localFilePath2 = fileList.get(1);
			initXML(fileName_min.substring(getFileNameBeginIdx(fileName_min)));
			buildXML();
			writeXML();
		} catch (Exception e) { e.printStackTrace();}
	}


	private int getFileNameBeginIdx(String wholeStr){
		if(wholeStr.indexOf("DFS_SHRT_STN_") != -1){
			return  wholeStr.indexOf("DFS_SHRT_STN_");
		}else if(wholeStr.indexOf("DFS_MEDM_STN_") != -1){
			return  wholeStr.indexOf("DFS_MEDM_STN_");
		}
		return -1;
	}


	private void initXML(String fileName){
		kindOfModel = whatKindOfModel(fileName);

		root = new Element("minMax"); 
		xmlDoc = new Document();
		xmlDoc.setRootElement(root);

		String dateOfIssue = getDateOfIssue(fileName);
		//////////////////////////////////////////////////////////////////////////April 25, 2013
		is00UTC = (dateOfIssue.indexOf("00UTC")!=-1)? true:false;
		//////////////////////////////////////////////////////////////////////////
		String baseModel = getBaseModel(kindOfModel); 

		root.setAttribute("issuedAt",dateOfIssue);
		root.setAttribute("basedOn",baseModel);
	}



	private void buildXML() throws JDOMException, IOException{
		SAXBuilder sb = new SAXBuilder();
//		Document stnInfoDoc = sb.build(prop.getProperty("stationInfoDocs_local"));////////////
//		Document stnInfoDoc = sb.build(prop.getProperty("stationInfoDocs"));////////////
		Document stnInfoDoc = sb.build(prop.getProperty("stationInfoDoc_MEDM"));///////////////  MEDM ENSEMBLE

		Element root = stnInfoDoc.getRootElement();
		List<Element> list = root.getChildren("regionGroup");

		boolean isFirstAttempt = true;
		Vector<String> stnGroup_n ; 
		Vector<String> stnGroup_x ; 
		for(int j=0; j<list.size(); j++){
			Element rGroup = list.get(j);
			gCodeStr = rGroup.getAttributeValue("grCode"); 
			gNameStr = rGroup.getAttributeValue("grName");
			stnGroup_n = new Vector<String>();	
			stnGroup_x = new Vector<String>();	

			Iterator iter = rGroup.getChildren().iterator();
			while(iter.hasNext()){  
				Element stn = (Element)iter.next();
				String stnCode = stn.getAttributeValue("stnCode");
				String stnName = stn.getAttributeValue("stnName");

				BufferedReader bReader;
				String strLine="";
				bReader = new BufferedReader(new FileReader((is00UTC)?localFilePath2:localFilePath1));

				boolean isExsist = false;

				for(int i=1; (strLine = bReader.readLine())!=null; i++){
					if(j == 0 && i == 3 && isFirstAttempt==true){
						setLST(strLine);  
						isFirstAttempt = false;
					}if(i <= 3)
						continue;
					StringTokenizer st = new StringTokenizer(strLine,"  ");
					String stnNo = st.nextToken(); 
					if(stnNo.equals(stnCode)){
//						stnGroup_n.add(stnName+" "+ stnCode + " " +strLine); 
						stnGroup_n.add(stnName+" "+ strLine);  
						isExsist = true;
					}
				}   
				bReader.close();
				if(!isExsist){
					stnGroup_n.add(stnName + " " +stnCode+ " isMissing");
//					System.out.println("MIN missing data occ..["+stnCode+"]" + stnName + " " +stnCode+ " isMissing" );
				}

				strLine = "";
				bReader = new BufferedReader(new FileReader((is00UTC)?localFilePath1:localFilePath2));
				for(int i=1; (strLine = bReader.readLine())!=null; i++){
					if(j == 0 && i == 3 && isFirstAttempt==true){
						setLST(strLine);  
						isFirstAttempt = false;
					}if(i <= 3)
						continue;
					StringTokenizer st = new StringTokenizer(strLine,"  ");
					String stnNo = st.nextToken(); 
					if(stnNo.equals(stnCode)){
//						stnGroup_x.add(stnName+" "+ stnCode + " " +strLine); 
						stnGroup_x.add(stnName+" "+ strLine);  
						isExsist = true;
					}
				}   
				bReader.close();	
				if(!isExsist){
					stnGroup_x.add(stnName + " " +stnCode+ " isMissing");
//					System.out.println("MAX missing data occ..["+stnCode+"]" + stnName + " " +stnCode+ " isMissing" );
				}
			} 

			appendRegionGroup(stnGroup_n,stnGroup_x); 
		} 
	}


	private void setLST(String lstStr){
		//		System.out.println(lstStr);
		//		StringTokenizer tokenizer = new StringTokenizer(lstStr,"  ");
		//		while(tokenizer.hasMoreTokens()){
		//			String token = tokenizer.nextToken();
		//			if(!(token.equals("STNID")) || !(token.startsWith("S")) || !(Pattern.matches("^[a-zA-Z]*", token))){
		//				if(kindOfModel.equals("GDPS") || kindOfModel.equals("PMOS2")){
		//					LST.add(token.substring(0,2)+"/"+token.substring(2,4)+"_n");
		//					LST.add(token.substring(0,2)+"/"+token.substring(2,4)+"_x");
		//				}
		//			}
		//		}
		////////////////////////////////////////////////////////////////////////////April 25, 2013
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm"); 
		Calendar cal = Calendar.getInstance();
		Date date = null;
		try {
			date = sdf.parse(strDate);
			cal.setTime(date);
			cal.add(Calendar.HOUR_OF_DAY, (is00UTC)? 15:18);
		} catch (ParseException e) {e.printStackTrace();}

		DateFormatSymbols dfSymbol = new DateFormatSymbols();
		String pattern = "MM/dd_HH";
		//		String pattern = "MM/dd";
		sdf = new SimpleDateFormat(pattern);
		sdf.setDateFormatSymbols(dfSymbol);
		LST.add(sdf.format(cal.getTime()));
		for(int i=1; i<=23; i++){
			if(i%2==1)
				cal.add(Calendar.HOUR_OF_DAY, (is00UTC)?15:9);
			else
				cal.add(Calendar.HOUR_OF_DAY, (is00UTC)?9:15);

			LST.add(sdf.format(cal.getTime()));

		}
		////////////////////////////////////////////////////////////////////////////April 25, 2013
	}


	/**
	 * @param stns_n
	 * @param stns_x
	 * @throws IOException
	 */
	/**
	 * @param stns_n
	 * @param stns_x
	 * @throws IOException
	 */
	private void appendRegionGroup(List stns_n, List stns_x) throws IOException{
		String strLine_n;
		String strLine_x;
		StringTokenizer tokenizer_n;
		StringTokenizer tokenizer_x;

		Element rGroup = new Element("regionGroup");
		rGroup.setAttribute("grCode",gCodeStr);
		rGroup.setAttribute("grName",gNameStr);
		root.addContent(rGroup); 

		Iterator iter_n = stns_n.iterator();
		Iterator iter_x = stns_x.iterator();
		while(iter_n.hasNext()){
			strLine_n = (String)iter_n.next();
			strLine_x = (String)iter_x.next();

			int count = 0;
			tokenizer_n = new StringTokenizer(strLine_n,"  ");
			tokenizer_x = new StringTokenizer(strLine_x,"  ");

			Vector minMaxVals = new Vector();
			while(tokenizer_n.hasMoreTokens()){
				String minToken = tokenizer_n.nextToken();
				String maxToken = tokenizer_x.nextToken();
				if(count==0){
					minMaxVals.add(minToken);
				}else if(count==1){
					minMaxVals.add(minToken);
				}else{
					minMaxVals.add(minToken);
					minMaxVals.add(maxToken);
				}
				count++;
			}

			Element stn = new Element("stn");


			/**
			 * AUG 20, 2013
			 * GDAPS MEDM
			 * setSize(25)
			 * 0 == stnName
			 * 1 == stnNo
			 * 2 to 25 == min/max val (23)
			 * */
			if(minMaxVals.size()>=26){
				minMaxVals.setSize(25);
				//				System.out.println("[GDPAS MEDM MMX] get rid of last data.. " + "minMaxVals.setSize(23)");
			}
			boolean isMissing = (((String)minMaxVals.get(2)).equals("isMissing"))? true:false;

//			for(int i=0; i<minMaxVals.size();i++){
			for(int i=0; i<25;i++){
				if(i==0){
					stn.setAttribute("stnName",(String)minMaxVals.get(i));
				}else if(i==1){
					stn.setAttribute("stnNo",(String)minMaxVals.get(i));
				}else{
					Element data = new Element("dataSet");
					data.setAttribute("lst",LST.get(i-2)); 
					if(! isMissing){
						double d = Math.abs(Double.parseDouble((String)minMaxVals.get(i)));
						if(d != 999){
							data.setAttribute("val",(String)minMaxVals.get(i));
						}
					}
					stn.addContent(data);
				}
			}
			rGroup.addContent(stn);
		}
	}


	public String whatKindOfModel(String fileName){
		String model="";
		if(fileName.indexOf("STN_RDPS_NPPM") != -1){
			model = "RDPS";
		}else if(fileName.indexOf("STN_KWRF_NPPM") != -1){
			model = "KWRF";
		}else if(fileName.indexOf("STN_RDPS_PMOS") != -1){
			model = "PMOS";
		}else if(fileName.indexOf("MEDM_STN_GDPS_NPPM") != -1){
			model = "GDPS";
		}//Aug 2012	
		else if(fileName.indexOf("MEDM_STN_GDPS_PMOS") != -1){
			model = "PMOS2";
		}//May 2013	
		else if(fileName.indexOf("MEDM_STN_ECMW_NPPM") != -1){
			model = "ECMWF";
		}
		//ensemble
		else if(fileName.indexOf("DFS_MEDM_STN_EPSG_PMOS") != -1){
			model = "PMOS";
		}	
		return model;
	}


	public String getMethod(String fileName){
		int endIdx = fileName.lastIndexOf(".");
		return fileName.substring(endIdx-3, endIdx); 
	}


	public String getDateOfIssue(String fileName) {
		strDate = fileName.substring(fileName.indexOf(".")+1);
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


	public String getBaseModel(String kindOfModel){
		String model = kindOfModel;
		String basedOn = "";
		if(model.equals("RDPS")){
			basedOn = "RDAPS(UM 12km L70)";
		}else if(model.equals("KWRF")){
			basedOn = "KWRF(WRF 10km L40)";
		}else if(model.equals("PMOS")){
			basedOn = "MOS(UM 12km L70)";
		}else if(model.equals("GDPS")){
			basedOn = "GDAPS(UM N512 L70)";
		}//Aug 2012
		else if(model.equals("PMOS2")){
			basedOn = "MOS(GDPS)";
		}//May 2013
		else if(model.equals("ECMWF")){
			basedOn = "PPM/ECMWF";
		}else{
			basedOn = "undefined..";
		}
		return basedOn;
	}


	private void writeXML( ) throws IOException{
		XMLOutputter outputter=new XMLOutputter();
		Format f=outputter.getFormat();
		f.setEncoding("utf-8");  //defaultEncoding
		f.setIndent(" ");
		f.setLineSeparator("\r\n"); 
		f.setTextMode(Format.TextMode.TRIM); 
		outputter.setFormat(f);
		FileOutputStream fStream = new FileOutputStream(XMLFolderPath  +kindOfModel+"/" + fileName_min.replace("TMN", "MMX") + ".xml");
		OutputStreamWriter fWriter = new OutputStreamWriter(fStream,"utf-8"); 
		outputter.output(xmlDoc, fWriter);
		fWriter.close();

		System.out.println("ToXML::  " + XMLFolderPath +kindOfModel+"/"  + fileName_min.replace("TMN", "MMX") + ".xml" + "\t-creation complete-");
	}


	public static void main(String[] args) throws Exception {
//		Vector<String> rawFileNames = new Vector<String>();
		//		rawFileNames.add(args[0]);
		//		rawFileNames.add(args[1]);
//		new TMM_ToXML(rawFileNames);
		

		//Ensemble
			for(int i=0; i<24; i++){
				String stepper = String.format("%02d", i);
				Vector<String> rawFileNames = new Vector<String>();
				rawFileNames.add("ensemble2014/DFS_MEDM_STN_EPSG_PMOS_TMN_M"+stepper+".201406160000");
				rawFileNames.add("ensemble2014/DFS_MEDM_STN_EPSG_PMOS_TMX_M"+stepper+".201406160000");
//				
				new TMM_ToXML(rawFileNames);
			}

	}

}
