import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class MergeConflicter implements MergeConflicterInterface{

	public MergeConflicter() {
		
	}

	/**
	 * Does a git merge
	 * @return true if there were conflicting files
	 */
	@Override
	public boolean doMerge() {
		try {
			Process mergeProcess = Runtime.getRuntime().exec("bash " + "scripts/" + "namnpåscript");
			BufferedReader br = new BufferedReader(new InputStreamReader(mergeProcess.getInputStream()));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}

	@Override
	public File getLeftConflict() {
		return null;
	}

	@Override
	public File getRightConflict() {
		return null;
	}

	@Override
	public File getCommonAncestor() {
		return null;
	}

	@Override
	public File getResolution() {
		return null;
	}
	
}
