package getVariantParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import utils.Utils;

public class VariantParameter {
	
	private HashSet<String> mergeCommits;
	private String REPO_PATH;
	private String initialCommit;
	private String parameter;

	public VariantParameter(String repoPath, String initialCommit, String parameter) {
		mergeCommits = new HashSet<String>();
		REPO_PATH = repoPath;
		this.initialCommit = initialCommit;
		this.parameter = parameter;
	}
	
	public HashMap<String, HashSet<String>> getCommitsWithParameterIntroduced() {
		HashMap<String, HashSet<String>> commitToParameters = new HashMap<String, HashSet<String>>();
		
		
		return commitToParameters;
	}
	
	private HashSet<String> searchForParameters(String commit) {
		HashSet<String> parameterNames = new HashSet<String>();
		try {
			BufferedReader br = Utils.readScriptOutput("bash scripts/getDiff " + REPO_PATH + " " + commit);
			String line;
			
			while((line = br.readLine()) != null) {
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
		
	}
	
	private String extractParameter(String line) {
		line = line.replace(" ", "");
		return line.split("getAsBoolean(")[1].split(",")[0];
	}
	
}
