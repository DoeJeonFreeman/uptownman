package best2015.merg;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
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


/**
 * @author myHome
 *
 */
/**
 * @author myHome
 *
 */
public class Best_T3H2XML {

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
	
	///////////////////////////////
	private boolean hasWGHTInfo = false;
	private HashSet<String> modelList;
	private Hashtable<String, List<String>> meTable = new Hashtable<String, List<String>>();
	private String weightData;
	private String updateOn;
	private String wghtMdls;
	///////////////////////////////
	
	
	public Best_T3H2XML(){
		super();
	}


	private void printIndent(int indentLevel){
		for (int i = 0; i < indentLevel; i++) {
			System.out.print("+");
		}
	}

	
	private void printModels(){
		if(modelList!=null){
			int indentLevel = 1;
			String indent = "";
			for(String name : modelList){
				printIndent(indentLevel);
				System.out.println(name);
				indentLevel++;
			}
			System.out.println("============================================================================================");
		}
	}
	
	
	public Best_T3H2XML(Vector<String> rawFileList){
		/////////////////////////////
		loadPropertyFile();
		/////////////////////////////

		String meTemperaturePath = rawFileList.get(0);
		
		
		//1
		//2
//		String weightData = "BEST/dfs_shrt_stn_blndW_reh_11s_00utc.201301-201412.doejeon";
		String envPath = meTemperaturePath.replace("_STN_", "_INF_");
		if(new File(envPath).isFile()){
			hasWGHTInfo = true;
			System.out.println("===========================================================================hasWGHTInfo==" + hasWGHTInfo);
			getWeightModelList(envPath);
			storeWeightInfoIntoArrayList(weightData);
			printModels();
		}
		
		
		
		
		XMLFolderPath = prop.getProperty("outputFolder");/////////////////////////
		tElementDocs = new Vector<Document>();
		doStuff(rawFileList);
	}

	
	
	private void getWeightModelList(String AbsPath) {
		String varName = AbsPath.substring(AbsPath.lastIndexOf("_"), AbsPath.lastIndexOf("."));
		BufferedReader bReader = null;
		HashSet<String> models = new HashSet<String>();
		
		String line="";
		try {
			bReader = new BufferedReader(new InputStreamReader(new FileInputStream(AbsPath), "utf-8"));
			for(int i=1; (line = bReader.readLine())!=null; i++){
				if(line.contains("work date")){
//					updateOn = line.split(":")[1].trim();
					updateOn = line.split(":")[1].trim() + ":" + line.split(":")[2].trim();
					System.out.println("[update on] "+getDateOfIssue(updateOn,"yyyy-MM-dd-HH:mm","yyyy-MM-dd HH:mm'LST'"));
				}else if(line.contains("wght file")){
weightData = line.split(":")[1].trim();
System.out.println("[wght file] " + weightData);
//weightData = "BEST/sample_DEC/dfs_shrt_stn_blndW_tmp_12s_00utc.201301-201412";
				}else if(line.contains("DFS_") &&  line.endsWith(varName)){
					models.add(line.substring(line.indexOf("DFS_")));
				}else if(line.contains("work modl")){
					wghtMdls = line.split(":")[1].trim();
					System.out.println("[work modl] " + wghtMdls);
				}	
			} 
			modelList = models;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("[_INF_ not exists]");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				bReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	

	private void storeWeightInfoIntoArrayList(String weightData) {
		BufferedReader br = null;
		try {
			 br = new BufferedReader(new InputStreamReader(new FileInputStream(weightData),"utf-8"));
//			 br = new BufferedReader(new FileReader(weightData)); nono
			String strLine ="";
			ArrayList<String> ans= new ArrayList<String>();
			// Read rows
			while ((strLine = br.readLine()) != null) {
				if(strLine.toUpperCase().startsWith("POINT")){
					String trimmed = strLine.replaceAll("\\s+", "");
					int stnCodeNum = Integer.parseInt(trimmed.substring(trimmed.indexOf(":")+1,trimmed.indexOf(",")));
					int hourIndex = Integer.parseInt(trimmed.substring(trimmed.indexOf("/")+1,trimmed.indexOf("hr")))/3 -1; //단기일때!!
					ans.add(stnCodeNum + ":" + hourIndex );
					
				}else if (strLine.toUpperCase().startsWith("DFS")){
					String model = strLine.replaceAll("\\s+", ",").split(",")[0];
					String weight = strLine.replaceAll("\\s+", ",").split(",")[3];
					ans.add(model + ":" + weight);
				}
			} 
			
			ArrayList<String> horhor= new ArrayList<String>();
			
			String haha = "";
			for (String result: ans) {
				if(Character.isDigit(result.charAt(0))){
					if(haha!=""){horhor.add(haha);} 
					horhor.add(haha);
					haha="";
					haha = result;
				}else {
					haha+= "/"+result;	
				}
			}
			
			
			
			for(String herr : horhor){
				if(!herr.contains("/")){
					continue; //has no weight data haha
				}
				String[] arr = herr.split("/");
				String key = arr[0];
//				System.out.print(key + " ==>  ");
				Vector<String> mdls = new Vector<String>();
				if(arr.length>1){//0:stnCode+hrs  1~n weightdata
					for(String weight : arr){
						if(! Character.isDigit(weight.charAt(0))){
							mdls.add(weight);
//							System.out.print(" ["+weight +"] ");
						}
					}
					
				}
				meTable.put(key, mdls);
//				System.out.println();
			}
			
			
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	

	
	private String computeEachWeightPercentage(List<String> list){
		
		Hashtable<String, Double> computedWeightSet = new Hashtable<String, Double>();
		 
		for(String mdlStr : modelList){
			List<Double> values = new Vector<Double>();
			Iterator<String> mdlIter = list.iterator();
			while(mdlIter.hasNext()){
				String itm = mdlIter.next();
				if(itm.startsWith(mdlStr)){
					values.add(Double.parseDouble(itm.split(":")[1]));
				}
			}
			
			if(values.size()==0){
				System.out.println("size=0");
			}else if(values.size()>0){
//				System.out.println(mdlStr + " [" + getMinMaxVal(values, "max") + "] count:" + values.size());
				String mName = mdlStr.substring(mdlStr.indexOf("_STN_")+5 ,mdlStr.lastIndexOf("_"));
				computedWeightSet.put(mName, getMinMaxVal(values, "max"));
			}
		}
		
		
		Enumeration<String> enKey = computedWeightSet.keys();
		double sum = 0;	
		while(enKey.hasMoreElements()){
			String key = enKey.nextElement();
			sum +=computedWeightSet.get(key);
		}
		
		enKey = computedWeightSet.keys();
		String str2return = "";
		while(enKey.hasMoreElements()){
			String key = enKey.nextElement();
			double value = computedWeightSet.get(key);
			double percentageVal = Math.round(((value/sum)*100)*100d)/100d;
//			System.out.println(key+" : "+ percentageVal);
			str2return += key+":"+percentageVal + " ";
		}
		return str2return;
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
		System.out.println(">>>>> Creating an XML data.....");
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

		String dateOfIssue = getDateOfIssue(fileName.substring(fileName.indexOf(".")+1),"yyyyMMddHHmm","HH'UTC' dd MMM yyyy");
		String baseModel = getBaseModel(kindOfModel); 

		root.setAttribute("issuedAt",dateOfIssue);
		
		root.setAttribute("basedOn",(wghtMdls!=null)?baseModel+"("+wghtMdls+")" : baseModel);
//		root.setAttribute("updatedOn",getDateOfIssue(updateOn,"yyyy-MM-dd-HH:mm","HH:mm'LST' dd MMM yyyy"));
		if(hasWGHTInfo){
			root.setAttribute("updatedOn",getDateOfIssue(updateOn,"yyyy-MM-dd-HH:mm","HH:mm'LST'"));
			root.setAttribute("mdls",wghtMdls);
		}
	}



	private void buildXML(String rawFile) throws JDOMException, IOException{
		SAXBuilder sb = new SAXBuilder();
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
					}
					
					if(i <= 3){continue;}
					if(strLine.trim().length()==0){continue;}
					
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
					System.out.println(" - doesn\'t match any station code..["+stnCode+"]" );
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
			
			String hasWeighInfo = stn.getAttributeValue("stnNo")+":1";
			if(meTable.get(hasWeighInfo)!=null){
				stn.setAttribute("hasWeightData", "true");
			}
			
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
						//haha wght
						String key = stn.getAttributeValue("stnNo")+":"+(i+1);
//						System.out.print("["+key +"] ");
						if(meTable.get(key)!=null){
							String weight = computeEachWeightPercentage(meTable.get(key));
							data.setAttribute("weight",weight);
//							System.out.println();
//							Iterator<String> lol = meTable.get(key).iterator();
//							while(lol.hasNext()){
//								String fu = lol.next();
//								System.out.println(fxxku);
//							}
						}else{
//							System.out.println("has no weightData haha..");
						}
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
		}else if(fileName.indexOf("BEST_BLND") != -1){
			model = "BEST";
		}	

		return model;
	}

	public String getMethod(String fileName){
		int endIdx = fileName.lastIndexOf(".");
		return fileName.substring(endIdx-3, endIdx); 
	}


	
	public String getDateOfIssue(String dStr, String formatStr, String convFormat) {
//		String strDate = fileName.substring(fileName.indexOf(".")+1);
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr); //yyyyMMddHHmm
		Date date = null;
		try {
			date = sdf.parse(dStr);
		} catch (ParseException e) {e.printStackTrace();}

		DateFormatSymbols dfSymbol = new DateFormatSymbols();
		String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
				"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		dfSymbol.setShortMonths(months);
//		String pattern = "HH'UTC' dd MMM yyyy";
		String pattern = convFormat;
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
			basedOn = "BEST";
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
		
		
		
		FileOutputStream fStream = new FileOutputStream(XMLFolderPath +kindOfModel+"/"+ fileName + ".xml");
		OutputStreamWriter fWriter = new OutputStreamWriter(fStream,"utf-8"); 
		outputter.output(tElementDocs.get(0), fWriter);
		fWriter.close();

		System.out.println("<<<<< " + XMLFolderPath +kindOfModel+"/"+ fileName + ".xml" );
		System.out.println("[Done]");
	}


	/**
	 * @param args 0[T3H] 1[TMX] 2[TMN]
	 * @throws Exception fileNotFoundException
	 */
	public static void main(String[] args) throws Exception {
		Vector<String> rawFileNames = new Vector<String>();
		rawFileNames.add(args[0]); 
		rawFileNames.add(args[1]);
		rawFileNames.add(args[2]);
		
//		rawFileNames.add("BEST/sample_DEC/DFS_SHRT_STN_BEST_BLND_T3H.201512110000");
//		rawFileNames.add("BEST/sample_DEC/DFS_SHRT_STN_BEST_BLND_TMX.201512110000");
//		rawFileNames.add("BEST/sample_DEC/DFS_SHRT_STN_BEST_BLND_TMN.201512110000");
		
		new Best_T3H2XML(rawFileNames);
	}

}
