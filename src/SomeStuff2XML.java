

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;



public class SomeStuff2XML {

	private Document xmlDoc;
	private Element root;
	private String XMLFolderPath;
	private String fileName;
	private Vector<String> LST;
	private String gCodeStr;
	private String gNameStr;
	private String kindOfModel;
	private String localFilePath;

	private String varShortcut;
	private Properties prop;
	private boolean isMEDM = false;

	public SomeStuff2XML(){
		super();
		loadPropertyFile();
	}


	public void setStuff(String rawDataPath_n_FileName, String varShourtcut){
		XMLFolderPath = prop.getProperty("targetDir");////////////
		this.varShortcut = varShourtcut;
		localFilePath = rawDataPath_n_FileName;
		fileName = rawDataPath_n_FileName.substring(getFileNameBeginIdx(rawDataPath_n_FileName));
		LST = new Vector<String>();
		doStuff(fileName);
	}

	
	private void loadPropertyFile(){
		prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("ensembleMOS.properties");
			prop.load(fis);
		} catch (IOException e) {
			System.out.println("[IOException]ensembleMOS.properties not found.");
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
		kindOfModel = whatKindOfModel(fileName);

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
		Document stnInfoDoc = sb.build(prop.getProperty("stationMapper"));
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
					System.out.println("NO MATCHING DATA ["+stnCode+"]" );
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
			
			Pattern p = Pattern.compile("\\s+");
			String[] result = p.split(strLine);
			
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
						if(varShortcut.equals("CLD")){
							Double numericVal = Double.parseDouble(val);
							if(numericVal <= 10 || numericVal >= 0){
								data.setAttribute("val",val);
							}else{
								System.out.println("[CLD]OUT_OF_RANGE : " + numericVal);
							}
						}else{
							data.setAttribute("val",val);
						}
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
		if(varShortcut.equals("CLD")){
			fileName = fileName.replace("_CLD_", "_SKY_");
			varShortcut = "SKY";
		}
		FileOutputStream fStream = new FileOutputStream(XMLFolderPath +varShortcut+"/"+ fileName + ".xml");
		OutputStreamWriter fWriter = new OutputStreamWriter(fStream,"utf-8"); 
		outputter.output(xmlDoc, fWriter);
		fWriter.close();

		System.out.println("[creation complete] " + XMLFolderPath +varShortcut+"/"+ fileName + ".xml");
	}


	public String whatKindOfModel(String fileName){
		if(fileName.indexOf("MEDM") != -1)
			isMEDM = true;
		else	
			isMEDM = false;
		
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
		
		//ensemble
		}else if(fileName.indexOf("DFS_MEDM_STN_EPSG_PMOS") != -1){
			model = "PMOS";
		}	
		//ensemble
		
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
		}else{
			basedOn = "undefined..";
		}		
		return basedOn;
	}


	public static void main(String[] args) throws Exception {
		
		String sourceDir= args[0]+"/" ;
		String dateTime = args[1];
		String varShortcut = args[2].toUpperCase();
		
		SomeStuff2XML doIt = new SomeStuff2XML();
		String prefix = doIt.prop.getProperty("fileNamePrefix");

		for(int i=0; i<24; i++){
			try{
				String stepper = String.format("%02d", i);
				doIt.setStuff(sourceDir + prefix + varShortcut+"_M" + stepper + "." + dateTime,varShortcut);
			}catch(Exception e){
				continue;
			}
		}
		
		
	}

}
