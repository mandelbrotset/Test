package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class InformationGatherer {
	
	
	public String getCommitMessage(String repo, String commitSHA) {
		Process msgProcess;
		try {
			msgProcess = Runtime.getRuntime().exec("bash " + "scripts/" + "getCommitMessage " + repo + " " + commitSHA);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(msgProcess.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br2.readLine()) != null) {
				if (sb.length() != 0)
					sb.append("\n");
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "puttepung";
	}
	
	public String getCommitSHA(String commitPath) {
		String[] folders = commitPath.split("/");
		String commitSHA = folders[folders.length - 1];
		return commitSHA;
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
