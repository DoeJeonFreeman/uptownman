


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


public class T3H_ToXML {

	private Document xmlDoc;
	private Element root;
	private String XMLFolderPath;
	private String fileName;
	private Vector<String> LST;
	private String gCodeStr;
	private String gNameStr;
	private String kindOfModel;
	private String localFilePath;
	private Vector<Document> tElementDocs;

	/////////////////////////////
	private Properties prop;
	/////////////////////////////
	
	
	public T3H_ToXML(){
		super();
	}


	public T3H_ToXML(Vector<String> rawFileList){
		/////////////////////////////
		loadPropertyFile();
		/////////////////////////////

//		XMLFolderPath = prop.getProperty("outputFolder_local");///////////////////
		XMLFolderPath = prop.getProperty("outputFolder");/////////////////////////

		tElementDocs = new Vector<Document>();

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
	
	
	private void doStuff(Vector<String> fileList){
		fileName = (fileList.get(0)).substring(getFileNameBeginIdx(fileList.get(0)));
		try {
			for (String rawfile : fileList) {
				LST = new Vector<String>();
				localFilePath = rawfile;
				initXML(rawfile.substring(getFileNameBeginIdx(rawfile)));
				buildXML(rawfile.substring(getFileNameBeginIdx(rawfile)));
				tElementDocs.add(xmlDoc);
			}
			appendMaxMinData();
			writeXML();
		} catch (Exception e) { e.printStackTrace();}
	}


	private int getFileNameBeginIdx(String wholeStr){
		return	wholeStr.indexOf("DFS_SHRT_STN_");
	}


	private void initXML(String fileName) throws ParseException{
		kindOfModel = whatKindOfModel(fileName);
		String method_n_element = "T3H";

		root = new Element(method_n_element); 
		xmlDoc = new Document();
		xmlDoc.setRootElement(root);

		String dateOfIssue = getDateOfIssue(fileName);
		String baseModel = getBaseModel(kindOfModel); 

		root.setAttribute("issuedAt",dateOfIssue);
		root.setAttribute("basedOn",baseModel);
	}



	private void buildXML(String rawFile) throws JDOMException, IOException{
		SAXBuilder sb = new SAXBuilder();
//		Document stnInfoDoc = sb.build(prop.getProperty("stationInfoDocs_local"));
		Document stnInfoDoc = sb.build(prop.getProperty("stationInfoDocs"));
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
				String stnCode = stn.getAttributeValue("stnCode");
				String stnName = stn.getAttributeValue("stnName");
				BufferedReader bReader;
				String strLine="";
				bReader = new BufferedReader(new FileReader(localFilePath)); 
				
				boolean isExist = false;
				
				for(int i=1; (strLine = bReader.readLine())!=null; i++){
					if(j == 0 && i == 3 && isFirstAttempt==true){
						setLST(strLine);  
						isFirstAttempt = false;
					}if(i <= 3){
						continue;
					}
					StringTokenizer st = new StringTokenizer(strLine,"  ");
					String stnNo = st.nextToken(); 
					if(stnNo.equals(stnCode)){
						stnGroup.add(stnName+" "+ stnCode + " " + strLine); 
						isExist = true;
					}
				} 
				bReader.close();		
				if(!isExist){
					stnGroup.add(stnName+" "+stnCode+ " isMissing"); 
//					System.out.println("STNCode [" + stnCode + "] isMissing: " + !isExist );
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
				if(token.lastIndexOf("LST")==-1){
					LST.add(token.substring(2));
				}else{
					LST.add(token.substring(2,6));
				}
			}
		}
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


	private void appendMaxMinData(){
		Document t3hDoc = tElementDocs.get(0);
		Iterator<Element> stnIter = t3hDoc.getDescendants(new ElementFilter("stn"));
		while(stnIter.hasNext()){
			Element stn = stnIter.next();
			String sNum = stn.getAttributeValue("stnNo");

			for(int i=1; i<=tElementDocs.size()-1; i++){
				List<Element>dataSetList = getDescendantByStnNum(sNum, i);
				Iterator iter2 = stn.getChildren().iterator();
				while(iter2.hasNext()){
					Element t3hDataSet = (Element)iter2.next();
					for (Element minMaxDS : dataSetList) {
						if(t3hDataSet.getAttributeValue("lst").equals(minMaxDS.getAttributeValue("lst"))){
							if(minMaxDS.getAttributeValue("val") != null){
								if(i==1)
									t3hDataSet.setAttribute("max", minMaxDS.getAttributeValue("val"));
								else if(i==2)
									t3hDataSet.setAttribute("min", minMaxDS.getAttributeValue("val"));
							}
						}
					}
				}
			}
		}
	}


	private List getDescendantByStnNum(String sNum, int fileNum){
		Document minMaxDoc = tElementDocs.get(fileNum);
		Iterator<Element> iter = minMaxDoc.getDescendants(new ElementFilter("stn"));
		Element stn = null;
		while(iter.hasNext()){
			stn = iter.next();
			if(stn.getAttributeValue("stnNo").equals(sNum)){
				break;
			}
		}
		return stn.getChildren();
	}


	public String whatKindOfModel(String fileName){
		String model="";
		if(fileName.indexOf("STN_RDPS_NPPM") != -1){
			model = "RDPS";
		}else if(fileName.indexOf("STN_KWRF_NPPM") != -1){
			model = "KWRF";
		}else if(fileName.indexOf("STN_RDPS_PMOS") != -1){
			model = "PMOS";
		}else if(fileName.indexOf("STN_ECMW_NPPM") != -1){
			model = "ECMWF";
		}else if(fileName.indexOf("STN_BEST_MERG") != -1){
			model = "BEST";
		}

		return model;
	}

	public String getMethod(String fileName){
		int endIdx = fileName.lastIndexOf(".");
		return fileName.substring(endIdx-3, endIdx); 
	}


	public String getDateOfIssue(String fileName) {
		String strDate = fileName.substring(fileName.indexOf(".")+1);
		//convert String into date, date into strDatePattern
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
		}else if(model.equals("ECMWF")){
			basedOn = "PPM/ECMWF";
		}else if(model.equals("BEST")){
			basedOn = "BEST(UM 12km L70)";
		}

		return basedOn;
	}


	private void writeXML( ) throws IOException{
		XMLOutputter outputter=new XMLOutputter();
		Format f=outputter.getFormat();
		f.setEncoding("utf-8");  
		f.setIndent(" ");
		f.setLineSeparator("\r\n"); 
		f.setTextMode(Format.TextMode.TRIM); 
		outputter.setFormat(f);
		
//		kindOfModel = "PMOS";
//		fileName = fileName.replace("DFS_SHRT_STN_ECMW_NPPM", "DFS_SHRT_STN_RDPS_PMOS");
		
		
		FileOutputStream fStream = new FileOutputStream(XMLFolderPath +kindOfModel+"/"+ fileName + ".xml");
		OutputStreamWriter fWriter = new OutputStreamWriter(fStream,"utf-8"); 
		outputter.output(tElementDocs.get(0), fWriter);
		fWriter.close();

		System.out.println("ToXML::  " + XMLFolderPath + kindOfModel + "/" + fileName + ".xml" + "\t-creation complete-\t");
	}


	/**
	 * @param args 
	 * @throws Exception fileNotFoundException
	 */
	public static void main(String[] args) throws Exception {
		Vector<String> rawFileNames = new Vector<String>();
		rawFileNames.add(args[0]);
		rawFileNames.add(args[1]);
		rawFileNames.add(args[2]);
		
		
		
		
		
		
		
//		RDAPS
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_T3H.201308210000");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_TMX.201308210000");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_TMN.201308210000");
		
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_T3H.201308211200");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_TMX.201308211200");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_TMN.201308211200");
		
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_T3H.201308220000");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_TMX.201308220000");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_TMN.201308220000");
		
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_T3H.201308221200");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_TMX.201308221200");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_TMN.201308221200");
		
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_T3H.201308230000");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_TMX.201308230000");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_TMN.201308230000");
		
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_T3H.201308231200");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_TMX.201308231200");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_TMN.201308231200");
		
		
//		KWARF
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_KWRF_NPPM_T3H.201308210000");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_KWRF_NPPM_TMX.201308210000");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_KWRF_NPPM_TMN.201308210000");
		
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_KWRF_NPPM_T3H.201308211200");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_KWRF_NPPM_TMX.201308211200");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_KWRF_NPPM_TMN.201308211200");
		
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_KWRF_NPPM_T3H.201308220000");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_KWRF_NPPM_TMX.201308220000");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_KWRF_NPPM_TMN.201308220000");
		
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_KWRF_NPPM_T3H.201308221200");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_KWRF_NPPM_TMX.201308221200");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_KWRF_NPPM_TMN.201308221200");
		
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_KWRF_NPPM_T3H.201308230000");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_KWRF_NPPM_TMX.201308230000");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_KWRF_NPPM_TMN.201308230000");
		
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_KWRF_NPPM_T3H.201308231200");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_KWRF_NPPM_TMX.201308231200");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_KWRF_NPPM_TMN.201308231200");
		
		
//		ECMWF
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_T3H.201308210000");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_TMX.201308210000");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_TMN.201308210000");

//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_T3H.201308211200");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_TMX.201308211200");
//		rawFileNames.add("SHRT_20130821_to_23/21/DFS_SHRT_STN_ECMW_NPPM_TMN.201308211200");
		
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_T3H.201308220000");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_TMX.201308220000");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_TMN.201308220000");
		
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_T3H.201308221200");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_TMX.201308221200");
//		rawFileNames.add("SHRT_20130821_to_23/22/DFS_SHRT_STN_ECMW_NPPM_TMN.201308221200");
		
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_T3H.201308230000");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_TMX.201308230000");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_TMN.201308230000");
		
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_T3H.201308231200");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_TMX.201308231200");
//		rawFileNames.add("SHRT_20130821_to_23/23/DFS_SHRT_STN_ECMW_NPPM_TMN.201308231200");
		
		
		
		
		
//		rawFileNames.add("2013_dfs_DATA/SHRT_RDPS_NPPM/DFS_SHRT_STN_RDPS_NPPM_T3H.201304160000");
//		rawFileNames.add("2013_dfs_DATA/SHRT_RDPS_NPPM/DFS_SHRT_STN_RDPS_NPPM_TMX.201304160000");
//		rawFileNames.add("2013_dfs_DATA/SHRT_RDPS_NPPM/DFS_SHRT_STN_RDPS_NPPM_TMN.201304160000");
		
//		rawFileNames.add("2013_dfs_DATA/SHRT_RDPS_NPPM/DFS_SHRT_STN_RDPS_NPPM_T3H.201304161200");
//		rawFileNames.add("2013_dfs_DATA/SHRT_RDPS_NPPM/DFS_SHRT_STN_RDPS_NPPM_TMX.201304161200");
//		rawFileNames.add("2013_dfs_DATA/SHRT_RDPS_NPPM/DFS_SHRT_STN_RDPS_NPPM_TMN.201304161200");

		
//		May 2013
//		ECMWF 
//		SHRT
//		rawFileNames.add("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_T3H.201305010000");
//		rawFileNames.add("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_TMX.201305010000");
//		rawFileNames.add("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_TMN.201305010000");
		
//		rawFileNames.add("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_T3H.201305011200");
//		rawFileNames.add("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_TMX.201305011200");
//		rawFileNames.add("ECM/SHRT/DFS_SHRT_STN_ECMW_NPPM_TMN.201305011200");
		
		
		
//		May 13, 2013
		
//		ECMWF
//		rawFileNames.add("20130513/DFS_SHRT_STN_ECMW_NPPM_T3H.201305130000");
//		rawFileNames.add("20130513/DFS_SHRT_STN_ECMW_NPPM_TMX.201305130000");
//		rawFileNames.add("20130513/DFS_SHRT_STN_ECMW_NPPM_TMN.201305130000");
		
//		PMOS
//		rawFileNames.add("20130513/DFS_SHRT_STN_RDPS_PMOS_T3H.201305130000");
//		rawFileNames.add("20130513/DFS_SHRT_STN_RDPS_PMOS_TMX.201305130000");
//		rawFileNames.add("20130513/DFS_SHRT_STN_RDPS_PMOS_TMN.201305130000");
		
//		RDPS
//		rawFileNames.add("20130513/DFS_SHRT_STN_RDPS_NPPM_T3H.201305130000");
//		rawFileNames.add("20130513/DFS_SHRT_STN_RDPS_NPPM_TMX.201305130000");
//		rawFileNames.add("20130513/DFS_SHRT_STN_RDPS_NPPM_TMN.201305130000");
		
//		KWRF
//		rawFileNames.add("20130513/DFS_SHRT_STN_KWRF_NPPM_T3H.201305130000");
//		rawFileNames.add("20130513/DFS_SHRT_STN_KWRF_NPPM_TMX.201305130000");
//		rawFileNames.add("20130513/DFS_SHRT_STN_KWRF_NPPM_TMN.201305130000");
		
		
		
		new T3H_ToXML(rawFileNames);
	}

}
