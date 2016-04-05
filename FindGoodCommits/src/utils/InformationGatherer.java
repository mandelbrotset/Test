package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
		return false;
	}
	
	public String getFileName() {
		return "";
	}

}
