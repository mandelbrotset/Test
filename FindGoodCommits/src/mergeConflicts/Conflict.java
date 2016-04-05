package mergeConflicts;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.Utils;

public class Conflict {
	private String leftConflict;
	private String rightConflict;
	private String commitSHA;
	private String leftFile;
	private String ancFile;
	private String rightFile;
	private String conflictFile;
	private String commitMessage;
	private boolean isPullRequest;
	
	
	private Conflict(String leftFile, String ancFile, String rightFile, String commitSHA) {
		this.leftFile = leftFile;
		this.ancFile = ancFile;
		this.rightFile = rightFile;
		this.commitSHA = commitSHA;
		merge();
	}
	
	public static ArrayList<Conflict> getConflicts(String leftFile, String ancFile, String rightFile, String commitSHA) {
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
		try {
			BufferedReader br = Utils.readScriptOutput("mergeFiles " + leftFile + " " + ancFile + " " + rightFile);
			String line;
			StringBuilder sbConflictFile = new StringBuilder();
			StringBuilder sbLeft = new StringBuilder();
			StringBuilder sbRight = new StringBuilder();
			boolean readingLeft = false;
			boolean readingRight = false;
			while((line = br.readLine()) != null) {
				sbConflictFile.append(line);
				sbConflictFile.append("\n");
				if (line.startsWith("<<<<<<<")) {
					Conflict conflict = new Conflict(leftFile, ancFile, rightFile, commitSHA);
					conflicts.add(conflict);
					readingLeft = true;
					sbLeft = new StringBuilder();
					sbRight = new StringBuilder();
				} else if (line.startsWith("=======")) {
					readingLeft = false;
					readingRight = true;
				} else if (line.startsWith(">>>>>>>")) {
					readingRight = false;
					Conflict conflict = conflicts.get(conflicts.size()-1); 
					conflict.leftConflict = sbLeft.toString();
					conflict.rightConflict = sbRight.toString();
				} else {
					if (readingLeft) {
						sbLeft.append(line);
						sbLeft.append("\n");
					} else if (readingRight) {
						sbRight.append(line);
						sbRight.append("\n");
					}
				}
			}
			conflictFile = sbConflictFile.toString();
			leftConflict = sbLeft.toString();
			rightConflict = sbRight.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return conflicts;
	}

	public String getLeftConflict() {
		return leftConflict;
	}

	public String getRightConflict() {
		return rightConflict;
	}

	public String getCommitSHA() {
		return commitSHA;
	}

	public String getLeftFile() {
		return leftFile;
	}

	public String getAncFile() {
		return ancFile;
	}

	public String getRightFile() {
		return rightFile;
	}

	public String getConflictFile() {
		return conflictFile;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	public boolean isPullRequest() {
		return isPullRequest;
	}

	public void setPullRequest(boolean isPullRequest) {
		this.isPullRequest = isPullRequest;
	}
	
	
	
}
