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
	private String fileName;
	private int leftSize = 0;
	private int rightSize = 0;
	private String commonConflict;

	private Conflict(String leftFile, String ancFile, String rightFile, String commitSHA, String commitMessage,
			String fileName, boolean isPullRequest) {
		this.leftFile = leftFile;
		this.ancFile = ancFile;
		this.rightFile = rightFile;
		this.commitSHA = commitSHA;
		this.isPullRequest = isPullRequest;
		this.commitMessage = commitMessage;
		this.fileName = fileName;
	}

	public static ArrayList<Conflict> getConflicts(String leftFile, String ancFile, String rightFile, String commitSHA,
			String commitMessage, String fileName, boolean isPullRequest) {
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
		try {
			BufferedReader br = Utils.readScriptOutput("mergeFiles " + leftFile + " " + ancFile + " " + rightFile,
					true);
			String line;
			StringBuilder sbLeft = new StringBuilder();
			StringBuilder sbRight = new StringBuilder();
			StringBuilder sbCommon = new StringBuilder();
			boolean readingLeft = false;
			boolean readingCommon = false;
			boolean readingRight = false;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("<<<<<<<")) {
					Conflict conflict = new Conflict(leftFile, ancFile, rightFile, commitSHA, commitMessage, fileName,
							isPullRequest);
					conflicts.add(conflict);
					readingLeft = true;
					sbLeft = new StringBuilder();
					sbRight = new StringBuilder();
				} else if (line.startsWith("|||||||")) {
					readingCommon = true;
					readingLeft = false;
				} else if (line.startsWith("=======")) {
					readingCommon = false;
					readingRight = true;
				} else if (line.startsWith(">>>>>>>")) {
					readingRight = false;
					Conflict conflict = conflicts.get(conflicts.size() - 1);
					conflict.leftConflict = sbLeft.toString();
					conflict.rightConflict = sbRight.toString();
					conflict.commonConflict = sbCommon.toString();
				} else {
					if (readingLeft) {
						Conflict conflict = conflicts.get(conflicts.size() - 1);
						conflict.leftSize++;
						sbLeft.append(line);
						sbLeft.append("\n");
					} else if (readingCommon) {
						sbCommon.append(line);
						sbCommon.append("\n");
					} else if (readingRight) {
						Conflict conflict = conflicts.get(conflicts.size() - 1);
						conflict.rightSize++;
						sbRight.append(line);
						sbRight.append("\n");
					}
				}
			}
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

	public boolean isPullRequest() {
		return isPullRequest;
	}

	public String getFileName() {
		return fileName;
	}

	public int getLeftSize() {
		return leftSize;
	}

	public int getRightSize() {
		return rightSize;
	}

	public String getCommonConflict() {
		return commonConflict;
	}

	
	
}
