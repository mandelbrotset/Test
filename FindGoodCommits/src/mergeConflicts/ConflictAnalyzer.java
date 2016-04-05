package mergeConflicts;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import utils.InformationGatherer;
import utils.WorkBookCreator;

public class ConflictAnalyzer {
	private WorkBookCreator wbc;
	private String cft;
	
	public ConflictAnalyzer() {
		wbc = new WorkBookCreator("ConflictInformation.xls");
		createSheets();
	}
	
	private void createSheets() {
		String name = extractTheName(cft);
		wbc.createSheet(name, "Commit SHA-1", "Message", "Pull request", "File name", "Size", "Conflicting lines", "Conflict Pattern", "Resolution Pattern");
	}

	private void gatherConflicts(String repoPath, String javaFilePath) {
		String fileName = extractTheName(javaFilePath);
		String left = javaFilePath + "/left_" + fileName;
		String right = javaFilePath + "/right_" + fileName;
		String anc = javaFilePath + "/anc_" + fileName;
		String commitSHA = InformationGatherer.getCommitSHA(javaFilePath);
		String commitMessage = InformationGatherer.getCommitMessage(repoPath, commitSHA);
		boolean isPullRequest = InformationGatherer.isPullRequest(repoPath, commitSHA);
		
		ArrayList<Conflict> conflicts = Conflict.getConflicts(left, anc, right, commitSHA, commitMessage, fileName, isPullRequest);
		
	}
	
	private String extractTheName(String path) {
		return new StringBuilder(new StringBuilder(cft).reverse().toString().split("/")[0]).reverse().toString();
	}
	
}
