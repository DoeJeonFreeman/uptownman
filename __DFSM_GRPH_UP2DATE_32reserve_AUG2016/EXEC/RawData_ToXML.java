
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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



public class RawData_ToXML {

	private Document xmlDoc;
	private Element root;
	private String XMLFolderPath;
	private String fileName;
	private Vector<String> LST;
	private String gCodeStr;
	private String gNameStr;
	private String kindOfModel;
	private String localFilePath;

	/////////////////////////////
	private Properties prop;
	
	private boolean isMEDM = false;
	
	/////////////////////////////

	public RawData_ToXML(){
		super();
	}


	public RawData_ToXML(String rawDataPath_n_FileName){
		/////////////////////////////
		loadPropertyFile();
		/////////////////////////////

		XMLFolderPath = prop.getProperty("outputFolder");////////////

		localFilePath = rawDataPath_n_FileName;
		fileName = rawDataPath_n_FileName.substring(getFileNameBeginIdx(rawDataPath_n_FileName));
		LST = new Vector<String>();
		doStuff(fileName);
	}

	
	private void loadPropertyFile(){
		prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("dfsConfig.properties");
			prop.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[Err occured] dfsConfig.properties!! ]");
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
	
	
	private void doStuff(String fileName){
		try {
			initXML(fileName);
			buildXML();
			writeXML();
		} catch (Exception e) { e.printStackTrace();}

	}


	private int getFileNameBeginIdx(String wholeStr){
		if(wholeStr.indexOf("DFS_SHRT_STN_") != -1){
			return  wholeStr.indexOf("DFS_SHRT_STN_");
		}else if(wholeStr.indexOf("DFS_MEDM_STN_") != -1){
			return  wholeStr.indexOf("DFS_MEDM_STN_");
		}else if(wholeStr.indexOf("KOPP_") != -1){
			return  wholeStr.indexOf("KOPP_RDPS_5KM");
		}
		return -1;
	}


	private void initXML(String fileName){ 
		kindOfModel = whatKindOfModel(fileName);//+ isMEDM

		String method_n_element = getMethod(fileName);

		root = new Element(method_n_element); 
		xmlDoc = new Document();
		xmlDoc.setRootElement(root);

		String dateOfIssue = getDateOfIssue(fileName);
		String baseModel = getBaseModel(kindOfModel); 

		root.setAttribute("issuedAt",dateOfIssue);
		root.setAttribute("basedOn",baseModel);


	}


	private void buildXML() throws JDOMException, IOException{
		SAXBuilder sb = new SAXBuilder();
		Document stnInfoDoc = sb.build(prop.getProperty("stationInfoDocs"));///////////////
		Element root = stnInfoDoc.getRootElement();
		List<Element> list = root.getChildren("regionGroup");

		boolean isFirstAttempt = true;
		Vector<String> stnGroup ; 
		for(int j=0; j<list.size(); j++){ 
			Element rGroup = list.get(j);
			gCodeStr = rGroup.getAttributeValue("grCode");
			gNameStr = rGroup.getAttributeValue("grName");
			stnGroup = new Vector<String>();	

			Iterator iter = rGroup.getChildren().iterator();
			while(iter.hasNext()){  
				Element stn = (Element)iter.next();
				String stnCode = stn.getAttributeValue("stnCode").trim();  
				String stnName = stn.getAttributeValue("stnName");
				BufferedReader bReader;
				String strLine="";
//				bReader = new BufferedReader(new FileReader(localFilePath)); 
				bReader = new BufferedReader(new InputStreamReader(new FileInputStream(localFilePath), "utf-8")); 

				boolean isExsist = false;

				for(int i=1; (strLine = bReader.readLine())!=null; i++){
					if(j == 0 && i == 3 && isFirstAttempt==true){
						setLST(strLine);  
						isFirstAttempt = false;
					}
					if(i <= 3){
						continue;
					}

					StringTokenizer st = new StringTokenizer(strLine,"  ");
					String stnNo = st.nextToken(); 
					if(stnNo.equals(stnCode)){
						stnGroup.add(stnName+" "+ stnCode + " " +strLine); 
						isExsist = true;
					}
				} 
				bReader.close();

				if(!isExsist){
					stnGroup.add(stnName+" " + stnCode + " " +"isMissing");
					System.out.println("missing data occ..["+stnCode+"]" );
				}
			} 
			appendRegionGroup(stnGroup); 
		} 
	}


	private void setLST(String lstStr){
		StringTokenizer tokenizer = new StringTokenizer(lstStr,"  ");
		while(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken();
			if(!(token.equals("STNID")) || !(token.startsWith("S")) || !(Pattern.matches("^[a-zA-Z]*", token))){
//				if(kindOfModel.equals("GDPS")){
				if(isMEDM){
					if(token.endsWith("LST")){
						LST.add(token.substring(0,6));
					}else{
						LST.add(token);
					}
				}else{
					if(token.endsWith("LST")){
						LST.add(token.substring(2,6));
					}else{
						LST.add(token.substring(2));
					}
				}
			}
		}
		///////////////////////////////////////ECMWF
		///////////////////////////////////////ECMWF
		///////////////////////////////////////ECMWF
//		if(LST.size()>21){
//			LST.setSize(21);
//		}
		///////////////////////////////////////ECMWF
		///////////////////////////////////////ECMWF
		///////////////////////////////////////ECMWF
	}


	private void appendRegionGroup(List stations) throws IOException{
		String strLine;
		StringTokenizer tokenizer;

		Element rGroup = new Element("regionGroup");
		rGroup.setAttribute("grCode",gCodeStr);
		rGroup.setAttribute("grName",gNameStr);
		root.addContent(rGroup); 

		Iterator iter = stations.iterator();
		while(iter.hasNext()){
			strLine = (String)iter.next();

			tokenizer = new StringTokenizer(strLine,"  ");
			Element stn = new Element("stn");
			stn.setAttribute("stnName",tokenizer.nextToken());
			stn.setAttribute("stnNo",tokenizer.nextToken());
			boolean isMissing = (tokenizer.nextToken().equals("isMissing"))? true:false;	
			for(int i=0; i<LST.size(); i++){
				Element data = new Element("dataSet");
				data.setAttribute("lst",LST.get(i)); 
				//June 04, 2013   
				//eliminates missing value abs(999) 
				if(!isMissing){
//					data.setAttribute("val",tokenizer.nextToken());
					String val = tokenizer.nextToken();
					if(Math.abs(Double.parseDouble(val)) != 999){
						data.setAttribute("val",val);
					}
				}
				stn.addContent(data);
			}
			rGroup.addContent(stn);
		}
	}


	private void writeXML() throws IOException{
		XMLOutputter outputter=new XMLOutputter();
		Format f=outputter.getFormat();
		f.setEncoding("utf-8");  
		f.setIndent(" ");
		f.setLineSeparator("\r\n"); 
		f.setTextMode(Format.TextMode.TRIM); 
		outputter.setFormat(f);

		FileOutputStream fStream = new FileOutputStream(XMLFolderPath +kindOfModel+"/"+ fileName + ".xml");
		OutputStreamWriter fWriter = new OutputStreamWriter(fStream,"utf-8"); 
		outputter.output(xmlDoc, fWriter);
		fWriter.close();

		System.out.println("[creation complete] " + XMLFolderPath +kindOfModel+"/"+ fileName + ".xml" );
	}


	public String whatKindOfModel(String fileName){
		//////////////////////////////////////////////////////
		if(fileName.indexOf("MEDM") != -1)
			isMEDM = true;
		else	
			isMEDM = false;
		//////////////////////////////////////////////////////
		
		String model="";
		if(fileName.indexOf("STN_RDPS_NPPM") != -1){
			model = "RDPS";
		}else if(fileName.indexOf("STN_KWRF_NPPM") != -1){
			model = "KWRF";
		}else if(fileName.indexOf("STN_RDPS_PMOS") != -1){
			model = "PMOS";
		}else if(fileName.indexOf("MEDM_STN_GDPS_NPPM") != -1){
			model = "GDPS";
		}else if(fileName.indexOf("STN_ECMW_NPPM") != -1){
			model = "ECMWF";
		}else if(fileName.indexOf("KOPP_RDPS_5KM") != -1){
			model = "KOPP";
		}	
		return model;
	}


	public String getMethod(String fileName){
		int endIdx = fileName.lastIndexOf(".");
		return fileName.substring(endIdx-3, endIdx); 
	}


	public String getDateOfIssue(String fileName) {
		String strDate = fileName.substring(fileName.indexOf(".")+1);
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
		}else if(model.equals("ECMWF")){
			basedOn = "PPM/ECMWF";
		}else if(model.equals("KOPP")){
			basedOn = "KOPP";
		}	
		return basedOn;
	}

	
	public static void main(String[] args) throws Exception {
		for(String rawFileName: args){
			new RawData_ToXML(rawFileName);
		}
		
		// ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_PTY.201305011200  --> SHRT.ECMWF_MOS.PTY 201305011200

//		new RawData_ToXML("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_PTY.201305011200");
//		new RawData_ToXML("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_SKY.201305011200");
//		new RawData_ToXML("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_REH.201305011200");
//		new RawData_ToXML("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_RN6.201305011200");
//		new RawData_ToXML("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_SN6.201305011200");
//		new RawData_ToXML("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_RN3.201305011200");
//		new RawData_ToXML("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_SN3.201305011200");
		

		
//		May 13, 2013
//		May 13, 2013
//		May 13, 2013
//		SHRT
		
		
//		ECMWF 
//		new RawData_ToXML("20130513/DFS_SHRT_STN_ECMW_NPPM_PTY.201305130000_min");
		
//		new RawData_ToXML("20130513/DFS_SHRT_STN_ECMW_NPPM_PTY.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_ECMW_NPPM_SKY.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_ECMW_NPPM_REH.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_ECMW_NPPM_RN6.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_ECMW_NPPM_SN6.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_ECMW_NPPM_RN3.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_ECMW_NPPM_SN3.201305130000");
		
		
//		PMOS
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_PMOS_PTY.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_PMOS_SKY.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_PMOS_REH.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_PMOS_POP.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_PMOS_SN6.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_PMOS_S12.201305130000");
		
		
//		RDAPS
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_NPPM_PTY.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_NPPM_SKY.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_NPPM_REH.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_NPPM_POP.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_NPPM_RN3.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_NPPM_RN6.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_NPPM_SN3.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_RDPS_NPPM_SN6.201305130000");
		
		
//		KWARF
//		new RawData_ToXML("20130513/DFS_SHRT_STN_KWRF_NPPM_PTY.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_KWRF_NPPM_SKY.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_KWRF_NPPM_REH.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_KWRF_NPPM_POP.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_KWRF_NPPM_RN3.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_KWRF_NPPM_RN6.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_KWRF_NPPM_SN3.201305130000");
//		new RawData_ToXML("20130513/DFS_SHRT_STN_KWRF_NPPM_SN6.201305130000");
		
		
		
		// MEDM
		// MEDM
		// MEDM
		
		// ECMWF
//		new RawData_ToXML("20130513/DFS_MEDM_STN_ECMW_NPPM_PTY.201305130000");
//		new RawData_ToXML("20130513/DFS_MEDM_STN_ECMW_NPPM_R12.201305130000");
//		new RawData_ToXML("20130513/DFS_MEDM_STN_ECMW_NPPM_S12.201305130000");
//		new RawData_ToXML("20130513/DFS_MEDM_STN_ECMW_NPPM_SKY.201305130000");
//		new RawData_ToXML("2013_dfs_DATA/MEDM_ECMW_NPPM/DFS_MEDM_STN_ECMW_NPPM_S12.201304160000");
//		new RawData_ToXML("2013_dfs_DATA/MEDM_ECMW_NPPM/DFS_MEDM_STN_ECMW_NPPM_S12.201304161200");
//		new RawData_ToXML("2013_dfs_DATA/MEDM_ECMW_NPPM/DFS_MEDM_STN_ECMW_NPPM_R12.201304160000");
//		new RawData_ToXML("2013_dfs_DATA/MEDM_ECMW_NPPM/DFS_MEDM_STN_ECMW_NPPM_R12.201304161200");
		
		// GDPS
//		new RawData_ToXML("20130513/DFS_MEDM_STN_GDPS_NPPM_PTY.201305130000");
//		new RawData_ToXML("20130513/DFS_MEDM_STN_GDPS_NPPM_R12.201305130000");
//		new RawData_ToXML("20130513/DFS_MEDM_STN_GDPS_NPPM_S12.201305130000");
//		new RawData_ToXML("20130513/DFS_MEDM_STN_GDPS_NPPM_SKY.201305130000");
		
//		yk seo
//		new RawData_ToXML("UKPP/KOPP_RDPS_5KM_STN_T3H.201207100000");
		
		
//		AUG 20, 2013 (~287Hrs) MEDM GDPS
		
//		new RawData_ToXML("proj288/DFS_MEDM_STN_GDPS_NPPM_PTY.201307030000");
//		new RawData_ToXML("proj288/DFS_MEDM_STN_GDPS_NPPM_R12.201307030000");
//		new RawData_ToXML("proj288/DFS_MEDM_STN_GDPS_NPPM_S12.201307030000");
//		new RawData_ToXML("proj288/DFS_MEDM_STN_GDPS_NPPM_SKY.201307030000");
//		
//		new RawData_ToXML("proj288/DFS_MEDM_STN_GDPS_NPPM_PTY.201307031200");
//		new RawData_ToXML("proj288/DFS_MEDM_STN_GDPS_NPPM_R12.201307031200");
//		new RawData_ToXML("proj288/DFS_MEDM_STN_GDPS_NPPM_S12.201307031200");
//		new RawData_ToXML("proj288/DFS_MEDM_STN_GDPS_NPPM_SKY.201307031200");
		
		
//		new RawData_ToXML("ECM/MEDM/DFS_MEDM_STN_ECMW_NPPM_PTY.201307030000");
//		new RawData_ToXML("ECM/MEDM/DFS_MEDM_STN_ECMW_NPPM_R12.201307030000");
//		new RawData_ToXML("ECM/MEDM/DFS_MEDM_STN_ECMW_NPPM_S12.201307030000");
//		new RawData_ToXML("ECM/MEDM/DFS_MEDM_STN_ECMW_NPPM_SKY.201307030000");
//		
//		new RawData_ToXML("ECM/MEDM/DFS_MEDM_STN_ECMW_NPPM_PTY.201307031200");
//		new RawData_ToXML("ECM/MEDM/DFS_MEDM_STN_ECMW_NPPM_R12.201307031200");
//		new RawData_ToXML("ECM/MEDM/DFS_MEDM_STN_ECMW_NPPM_S12.201307031200");
//		new RawData_ToXML("ECM/MEDM/DFS_MEDM_STN_ECMW_NPPM_SKY.201307031200");
		
		
		
	}

}
