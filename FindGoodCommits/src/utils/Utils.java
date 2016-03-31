package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

public class Utils {

	public static ArrayList<String> getAllMergeCommits(String repo) {
		ArrayList<String> mergeCommits = new ArrayList<String>();
		try {
			Process p = Runtime.getRuntime().exec("bash scripts/getCommits " + repo);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String commitSHA;
			while((commitSHA = br.readLine()) != null) {
				mergeCommits.add(commitSHA);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return mergeCommits;
	}
	
	public static BufferedReader readScriptOutput(String command) throws IOException {
		HashSet<String> parameters = new HashSet<String>();
		Process p = Runtime.getRuntime().exec("bash scripts/" + command);
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}
	
	public static BufferedReader readCommandOutput(String command) throws IOException {
		Process p = Runtime.getRuntime().exec(command);
		return new BufferedReader(new InputStreamReader(p.getInputStream()));
	}
	
	public static void checkoutCommit(String repo, String commit) {
		try {
			readScriptOutput("checkoutCommit " + repo + " " + commit).readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
