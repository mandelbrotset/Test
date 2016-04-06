package mergeConflicts;

import java.io.File;
import java.util.ArrayList;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import utils.InformationGatherer;
import utils.WorkBookCreator;

public class ConflictAnalyzer {
	private WorkBookCreator wbc;
	private String cftPath;
	private String repoPath;
	private ArrayList<Conflict> conflicts;
	
	public ConflictAnalyzer() {
		wbc = new WorkBookCreator("ConflictInformation.xls");
		conflicts = new ArrayList<Conflict>();
	}
	
	public void produceAnalyzement(String cftPath, String repoPath) {
		this.cftPath = cftPath;
		this.repoPath = repoPath;
		createSheets();
		fillConflicts();
		writeToSheet();
	}
	
	private void writeToSheet() {
		for(Conflict c : conflicts) {
			try {
				wbc.addRow(c.getCommitSHA(), c.getCommitMessage(), Boolean.toString(c.isPullRequest()), c.getFileName(), Integer.toString(c.getLeftSize()), Integer.toString(c.getRightSize()), c.getLeftConflict(), c.getCommonConflict(), c.getRightConflict(), c.getClassifiers(), "N/A");
			} catch (RowsExceededException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (WriteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		wbc.writeToWorkbook();
	}
	
	private void fillConflicts() {
		File cftFoler = new File(cftPath);
		for(File commitFolder : getFiles(f -> f.isDirectory(), cftFoler)) {
			for(File javaFolder : getFiles(f -> f.isDirectory(), commitFolder)) {
				System.out.println("Processing " + javaFolder.getName());
				conflicts.addAll(gatherConflicts(repoPath, javaFolder.getAbsolutePath()));
			}
		}
	}
	
	private void createSheets() {
		String name = extractTheName(cftPath);
		wbc.createSheet(name, "Commit SHA-1", "Message", "Pull request", "File name", "Left Size", "Right Size", "Left Conflict code", "Common Ancestor code", "Right Conflict code", "Conflict Pattern", "Resolution Pattern");
	}

	private ArrayList<Conflict> gatherConflicts(String repoPath, String javaFilePath) {
		String fileName = extractTheName(javaFilePath);
		String left = javaFilePath + "/left_" + fileName;
		String right = javaFilePath + "/right_" + fileName;
		String anc = javaFilePath + "/anc_" + fileName;
		String commitSHA = InformationGatherer.getCommitSHA(javaFilePath);
		String commitMessage = InformationGatherer.getCommitMessage(repoPath, commitSHA);
		boolean isPullRequest = InformationGatherer.isPullRequest(repoPath, commitSHA);
		ArrayList<Conflict> newConflicts;
		try {
			newConflicts = Conflict.getConflicts(left, anc, right, commitSHA, commitMessage, fileName, isPullRequest);
		} catch (OldConflictFoundException e) {
			System.out.println("oldConflictFound: " + e.getMessage());
			newConflicts = new ArrayList<Conflict>();
		}
		return newConflicts;
	}
	
	private String extractTheName(String path) {
		return new StringBuilder(new StringBuilder(path).reverse().toString().split("/")[0]).reverse().toString();
	}
	
	private ArrayList<File> getFiles(java.util.function.Predicate<File> tester, File folder) {
		ArrayList<File> listOfFiles = new ArrayList<File>();
		for(File file : folder.listFiles()) {
			if(tester.test(file))
				listOfFiles.add(file);
		}
		
		return listOfFiles;
	}
	
}
