import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author Administrator
 *
 */
public class MediumRangeFCSTDataJSONPopulator{

	private Properties prop;


	public MediumRangeFCSTDataJSONPopulator() {
		loadPropertyFile();
	}

	
	private void loadPropertyFile(){
		prop = new Properties();
		FileInputStream fis = null;
		try {
//			fis = new FileInputStream("/op/DFSM_GRPH/SHEL/dfsConfig.properties");
			fis = new FileInputStream("dfsConfig.properties");
			prop.load(fis);
		} catch (IOException e) {
			System.out.println("[IOException]dfsConfig.properties not found.");
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


	public JSONObject parseUsingScanner(String path, String nodeName)  {
		return makeMEDMRangeSeries(path, nodeName);
	}


	private JSONObject makeMEDMRangeSeries(String path, String nodeName)  {
		String fileToParse = path;
		JSONObject json_ice = new JSONObject();
		final String DELIMITER = " +"; //multiple whitespace 
		
		BufferedReader fileReader = null;
		
		ArrayList<String[]> arrStored = new ArrayList<String[]>();
		
		try{
			String line = "";
			fileReader = new BufferedReader(new FileReader(fileToParse));
			while ((line = fileReader.readLine()) != null) {
				
				String[] tokens = line.trim().split(DELIMITER);

				if(tokens.length <=2) continue;
				if(tokens[0].contains(new String("STNID").toUpperCase())) continue;

				arrStored.add(tokens);
			}
			
			SAXBuilder sb = new SAXBuilder();
			Document stnInfoDoc = sb.build(prop.getProperty("TBLMapper"));///////////////
			Element root = stnInfoDoc.getRootElement();
			Element districts = root.getChild(nodeName);
			Iterator meIter = districts.getChildren().iterator();
			
			JSONArray a1 = new JSONArray();
			
			while(meIter.hasNext()){
				Element district = (Element)meIter.next();
				JSONObject dObj = new JSONObject();
				String distCode = district.getAttributeValue("id").trim();
				String distName = district.getAttributeValue("name").trim();
				dObj.put("id", distCode);
				dObj.put("name", distName);
				System.out.println(">> "+dObj.get("id")+" >>> "+dObj.get("name"));
				
				Iterator leafIter = district.getChildren().iterator();
				JSONArray list = new JSONArray();
				while(leafIter.hasNext()){
					Element dist = (Element)leafIter.next();
					String id = dist.getAttributeValue("id").trim();
					String name = dist.getAttributeValue("name").trim();
					
					for(String[] dArr : arrStored){
						if(dArr[0].equals(id)){
							
							JSONObject obj = new JSONObject();
							
							obj.put("id", id);
							obj.put("name", name);
							JSONArray data = new JSONArray();
							if(nodeName.equals("districts")){
								data.addAll(Arrays.asList(dArr).subList(1, dArr.length)); // haha get rid of last data n restrict code
							}else{
								data.addAll(hadleMissingValue_TMX(Arrays.asList(dArr).subList(1, dArr.length))); // haha get rid of last data n restrict code
							}
							obj.put("data", data);
							if(nodeName.equals("districts")){
								obj.put("strData", getLabel(Arrays.asList(dArr).subList(1, dArr.length)));
							}
							list.add(obj);
							continue;
						}
					}
					dObj.put("data", list);
				}
				a1.add(dObj);
			}
			String issuedAt = path.substring(path.lastIndexOf(".")+1);
			json_ice.put("dFrom", issuedAt.substring(0, 10));
			json_ice.put("FCST", a1);
		} 
		catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return json_ice;
	}

	private ArrayList<String> hadleMissingValue_TMX(List<String> data) {
		ArrayList<String> values = new ArrayList<String>();
		for(String val : data){
			if(Math.abs(Double.parseDouble(val))==999){
				values.add(" - ");
			}else{
				values.add(val);
			}
		}
		return values;
	}	
	
	private boolean containMissingCode(String cld, String pty, String reliabilityCode){
		boolean isMissing = false;
		int cloudCover = Integer.parseInt(cld);
		int prcpType = Integer.parseInt(pty);
		int reliability = Integer.parseInt(reliabilityCode);
		if(cloudCover < 1 || cloudCover > 4) isMissing = true;
		if(prcpType < 0 || prcpType > 3) isMissing = true;
		if(reliability < 1 || reliability > 3) isMissing = true;
		return isMissing;
	}
	
	private String nullifyLabel(){
		return "&nbsp;";
	}
	
	private ArrayList<String> getLabel(List<String> codes) {
		ArrayList<String> labels = new ArrayList<String>();
		for(String code : codes){
			String str2return = "";
			String cloudCover = code.substring(0, 1);
			String prcpType = code.substring(1, 2);
			String reliability = code.substring(2, 3);
			
			
			if(prcpType.equals("0")){
				if(cloudCover.equals("1")){
					str2return += "맑음";
				}else if(cloudCover.equals("2")){
					str2return += "구름조금";
				}else if(cloudCover.equals("3")){
					str2return += "구름많음";
				}else if(cloudCover.equals("4")){
					str2return += "흐림";
				}
			}else {
				if(cloudCover.equals("1")){
					str2return += "맑고<br>";
				}else if(cloudCover.equals("2")){
					str2return += "구름조금<br>";
				}else if(cloudCover.equals("3")){
					str2return += "구름많고<br>";
				}else if(cloudCover.equals("4")){
					str2return += "흐리고<br>";
				}
				
				if(prcpType.equals("1")){
					str2return += "비";
				}else if(prcpType.equals("2")){
					str2return += "비/눈";
				}else if(prcpType.equals("3")){
					str2return += "눈";
				}
			}
			//+reilability
			if(reliability.equals("1")){
				str2return += "<br>(낮음)";
			}else if(reliability.equals("2")){
				str2return += "<br>(보통)";
			}else if(reliability.equals("3")){
				str2return += "<br>(높음)";
			}
			
			if(containMissingCode(cloudCover, prcpType, reliability)){
				str2return = nullifyLabel();
			}
			labels.add(str2return);
		}
		return labels;
	}


	protected boolean isNumeric(String str)  {  
		try  {  
			double d = Double.parseDouble(str);  
		}  
		catch(NumberFormatException nfe)  {  
			return false;  
		}  
		return true;  
	}


	private String getOutputName(String path) {
		String storeInto = prop.getProperty("TBLDAOU");
		return storeInto + path.substring(path.lastIndexOf("_")+1).replace(".", "_")+".json";
	}
	
	/**
	 * @param args[0] SPY
	 * @param args[1] TMXN
	 */
	public static void main(String[] args) {
		MediumRangeFCSTDataJSONPopulator pop = new MediumRangeFCSTDataJSONPopulator();
		try {							
//			FileWriter file = new FileWriter(pop.getOutputName("sampleData/DFS_MEDM_GRP_EPSG_BLTN_SPY.201601010000"));
//			file.write(pop.parseUsingScanner("sampleData/DFS_MEDM_GRP_EPSG_BLTN_SPY.201601010000", "districts").toJSONString());
			FileWriter file = new FileWriter(pop.getOutputName(args[0]));
			file.write(pop.parseUsingScanner(args[0], "districts").toJSONString());
			file.flush();
			file.close();
			
//			file = new FileWriter(pop.getOutputName("sampleData/DFS_MEDM_STN_EPSG_BLTN_TMXN.201601010000"));
//			file.write(pop.parseUsingScanner("sampleData/DFS_MEDM_STN_EPSG_BLTN_TMXN.201601010000", "stations").toJSONString());
			file = new FileWriter(pop.getOutputName(args[1]));
			file.write(pop.parseUsingScanner(args[1], "stations").toJSONString());
			file.flush();
			file.close();
			
			System.out.println("[MRFCSTD]DONE.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

