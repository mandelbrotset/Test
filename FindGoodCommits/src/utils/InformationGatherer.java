package utils;

import java.io.File;
import java.util.ArrayList;

public class InformationGatherer {
	
	private String cftRepo;
	
	public InformationGatherer(String cftRepo) {
		this.cftRepo = cftRepo;
	}
	
	public String getCommitMessage() {
		return "";
	}
	
	public String getCommitSHA() {
		return "";
	}
	
	public boolean isPullRequest() {
		if (getCommitMessage().contains("Merge pull request #"))
			return true;
		
		return false;
	}
	
	public ArrayList<String> getConflictFileNames(String commit) {
		ArrayList<String> javaFiles = new ArrayList<String>();
		File repo = new File(cftRepo + "/" + commit);
		for(File javaFile : getFiles(f -> f.isDirectory(), repo))
			javaFiles.add(javaFile.getName());
		return null;
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
