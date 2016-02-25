import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


public class MergeConflicter{
	
	private ArrayList<String> conflictingFilePaths;
	private final String REPO = "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/elasticsearch";
	private String leftCommit;
	private String rightCommit;
	private String mergeCommit;
	
	private ArrayList<Conflict> conflicts;

	public MergeConflicter() {
		conflictingFilePaths = new ArrayList<String>();
		conflicts = new ArrayList<Conflict>();
	}

	/**
	 * Does a git merge
	 * @return true if there were conflicting files, false otherwise
	 */
	public boolean doMerge() {
		try {
			BufferedReader br = executeCommand("mergeHistorical " + REPO + " " + "com1" + "com2" + "hashash");
			
			String line;
			final String conflictingLinePattern = "CONFLICT (content): Merge conflict in ";
			while((line = br.readLine()) != null) {
				
				if(line.startsWith(conflictingLinePattern) && line.endsWith(".java")) {
					String filePath = line.substring(conflictingLinePattern.length());
					conflictingFilePaths.add(filePath);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(conflictingFilePaths.size() > 0) {
			createConflicts();
			return true;
		}
		
		return false;
	}
	
	public ArrayList<Conflict> getConflicts() {
		return conflicts;
	}
	
	private void createConflicts() {
		for(String filePath : conflictingFilePaths) {
			File leftConflictFile = getConflictFile(leftCommit, filePath);
			File rightConflictFile = getConflictFile(rightCommit, filePath);
			File commonAncestorFile = getCommonAncestor(filePath);
			File resolutionFile = getConflictFile(mergeCommit, filePath);
			
			Conflict conflict = new Conflict(mergeCommit, leftConflictFile, rightConflictFile, commonAncestorFile, resolutionFile);
			conflicts.add(conflict);
		}
	}
	
	private File getConflictFile(String commit, String filePath) {
		executeCommand("checkoutCommit " + REPO + " " + commit);
		return new File(filePath);
	}

	private File getCommonAncestor(String filePath) {
		String ancestorCommit = "";
		try {
			ancestorCommit = executeCommand("getCommonAncestor " + REPO + " " + leftCommit + " " + rightCommit).readLine();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getConflictFile(ancestorCommit, filePath);
	}
	
	private BufferedReader executeCommand(String command) {
		BufferedReader br = null;
		try {
			Process p = Runtime.getRuntime().exec("scripts/bash " + command);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return br;
	}
	
}
