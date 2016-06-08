package trunk.medm.executioner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class EnsembleOutputter {

	private Properties property;
	
	private boolean allVariablesExist = true; 
	
	public EnsembleOutputter(String dateStr){
		
		loadPropertyFile();
		String[] var2beProcessed = property.getProperty("var2BProcessed").split(",");
		//런타임에 모든 변수 존재 유무 (일부변수만 있어도 표출되어야 할꺼같음ㅋㅋㅋㅋ )
		//모든 변수 존재한다면 member 수 체크 
		//글고 dataSet nodeList length 체크해야함니
		
		//암튼 변수가 있는지 체크 (1개이상)
		//있음 멤버수 체크
		for(String varName : var2beProcessed){
			System.out.println(varName);
			if(EnsembleDataHandler.getInstance().isFileThere(varName) != true){
				allVariablesExist = false;
				continue;
			}
		}
		
		if(allVariablesExist){
			System.out.println("JCV good to go, sir~");
			EnsembleDataHandler.getInstance().mergeSeveralEnsembleMembersIntoOne(var2beProcessed);
		}else{
			System.out.println("일부 변수 데이터 != 24 members");
		}
	
	}
	
	private void loadPropertyFile(){
		property = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("ensembleMOS.properties");
			property.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("[Err.] An IO Error occurred while loading \'ensembleMOS.properties\' !! ");
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
	
	
	public static void main(String[] args) {
		new EnsembleOutputter("201406160000");
	}
}
