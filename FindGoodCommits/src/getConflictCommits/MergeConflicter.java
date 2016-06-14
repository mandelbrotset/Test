package getConflictCommits;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import utils.Machine;
import utils.Utils;


public class MergeConflicter{
	
	private ArrayList<String> conflictingFilePaths;
	private String REPO;
	private String leftCommit;
	private String rightCommit;
	private String mergeCommit;
	
	
	private ArrayList<Conflict> conflicts;
	
	private Machine machine;

	public MergeConflicter() {
		conflictingFilePaths = new ArrayList<String>();
		conflicts = new ArrayList<Conflict>();
		machine = Machine.getInstance();
	}

	/**
	 * Does a git merge
	 * @return true if there were conflicting files, false otherwise
	 */
	public boolean doMerge(String mergeCommit, String repoPath) {
		REPO = repoPath;
		conflictingFilePaths.clear();
		conflicts.clear();
		try {
			String ancestrals = Utils.readScriptOutput("getAncestralCommits " + REPO + " " + mergeCommit, true).readLine();
			this.mergeCommit = mergeCommit;
			leftCommit = ancestrals.substring(0, 7);
			rightCommit = ancestrals.substring(8, 15);
			
			//System.out.println("Left: " + leftCommit + " Right: " + rightCommit);
			
			String branchName = "tempbranch";
			
			BufferedReader br = Utils.readScriptOutput("mergeHistorical " + REPO + " " + leftCommit + " " + rightCommit + " " + branchName, true);
			
			String line;
			final String conflictingLinePattern = machine.getConflictMessage();
			while((line = br.readLine()) != null) {
				if(line.startsWith(conflictingLinePattern) && line.endsWith(".java")) {
					String filePath = line.substring(conflictingLinePattern.length());
					conflictingFilePaths.add(REPO + "/" + filePath);
				}
			}
			Utils.readScriptOutput("abortMerge " + REPO, false);
			Utils.readScriptOutput("deleteBranch " + REPO + " " + branchName, false);
			
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
			System.out.println("Creating conflict on: " + filePath);
			String leftConflictFile = getConflictFile(leftCommit, filePath);
			String rightConflictFile = getConflictFile(rightCommit, filePath);
			String commonAncestorFile = getCommonAncestor(filePath);
			String resolutionFile = getConflictFile(mergeCommit, filePath);
			Conflict conflict = new Conflict(mergeCommit, leftConflictFile, rightConflictFile, commonAncestorFile, resolutionFile, new File(filePath), leftCommit, rightCommit);
			conflicts.add(conflict);
		}
	}
	
	private String getConflictFile(String commit, String filePath) {
			//Utils.readScriptOutput("gitCleanNoPull " + REPO);
			//Utils.readScriptOutput("checkoutCommit " + REPO + " " + commit);
		try {
			Utils.checkoutCommit(REPO, commit);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File file = new File(filePath);
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while((line = br.readLine()) != null) {
				if(sb.length() > 0)
					sb.append("\n");
				sb.append(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

	private String getCommonAncestor(String filePath) {
		String ancestorCommit = "";
		try {
			ancestorCommit = Utils.readScriptOutput("getCommonAncestor " + REPO + " " + leftCommit + " " + rightCommit, true).readLine();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return getConflictFile(ancestorCommit, filePath);
	}
	
	/*private BufferedReader Utils.readScriptOutput(String command) {
		BufferedReader br = null;
		try {
			Process p = Runtime.getRuntime().exec("bash scripts/" + command);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return br;
	}*/
	
}
