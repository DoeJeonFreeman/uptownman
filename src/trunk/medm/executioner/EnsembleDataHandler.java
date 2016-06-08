package trunk.medm.executioner;

import java.io.File;
import java.util.Vector;


public class EnsembleDataHandler {

	private static EnsembleDataHandler instance;
	
	private final static int ENSEMBLE_MEMBER_COUNT = 24;
	
	private EnsembleDataHandler() {}
	
	public static EnsembleDataHandler getInstance(){
		if(instance == null){
			instance  = new EnsembleDataHandler();
		}
		return instance;
	}
	

	public boolean isFileThere(String varShortcut){
		Vector<File> fileList = new Vector<File>();
		boolean isEnsembleDataExist = false;
		int fileCount=0;
		//		File dir = new File("e:/SFS_XMLOutput/ensembleSample/XML/PMOS/"+ varShortcut+"/DFS_MEDM_STN_EPSG_PMOS_"+varShortcut+"_M"+stepper+".201406160000.xml");
		File dir = new File("e:/SFS_XMLOutput/ensembleSample/XML/PMOS/"+ varShortcut);
		String[] files = dir.list();
		if (files == null) {
			System.out.println("[" + varShortcut + "] No Ensemble data to merge!!");
		} else {
			for (int i=0; i<files.length; i++) {
				File currFile = new File(dir+"/"+files[i]);  
//				System.out.println(currFile);
				if(currFile.getName().indexOf("DFS_MEDM_STN_EPSG_PMOS") != -1){ // regex로 바꿩
					fileList.add(currFile);
					fileCount++;
				}
			}
			System.out.println(fileList.size());
		} 
		isEnsembleDataExist = (fileList.size() == ENSEMBLE_MEMBER_COUNT)? Boolean.TRUE : Boolean.FALSE;

		return isEnsembleDataExist;
	}
	
	
	
	/**
	 * 이 펑션 호출 전에 RawData2XML 루프로 때려줘잉	
	 */
	public void mergeSeveralEnsembleMembersIntoOne(String[] timeseriesVars){
		for(String currVar : timeseriesVars){
			System.out.println("asldkjaslkdfjslkdjfkl");
		}
	}
	
	
	/**
	 *  Max-min Temperature
	 *  Precipitation(R12)
	 */
	public void getPercentileStatistics(){
		
	}
	
	
	/**
	 *  Cloud amount
	 *  Precipitation Type
	 */
	public void getPercentageStatistics(){
		
	}
	
	
}
