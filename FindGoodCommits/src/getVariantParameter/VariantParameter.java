package getVariantParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import utils.Utils;

public class VariantParameter {

	public VariantParameter() {

	}
	
	
	
	public String findParameterIntroctionCommitSHA(String repo, String parameter, String initialCommit) {
		Utils.checkoutCommit(repo, initialCommit);
		int initialCommitNo = parameterExistsInCommit(repo, parameter, initialCommit);
		int commonAncNo = 0;
		String[] parents;
		try {
			parents = Utils.readScriptOutput("getAncestralCommits " + repo + " " + initialCommit).readLine().split(" ");//härligt härligt men farligt
			String commonAncestor = Utils.readScriptOutput("getCommonAncestor " + repo + " " + parents[0] + " " + parents[1]).readLine();
			commonAncNo = parameterExistsInCommit(repo, parameter, commonAncestor);
			System.out.println("init: " + initialCommitNo + " commonAnc: " + commonAncNo);
			if(initialCommitNo > commonAncNo) {// och då vet vi att snart är det jul
				String currentCommit = parents[1];
				String prevCommit = initialCommit;
				System.out.println("init: " + initialCommit + " commonAnc: " + commonAncNo);
				
				while(parameterExistsInCommit(repo, parameter, currentCommit) > commonAncNo) {
					prevCommit = currentCommit;
					currentCommit = Utils.readScriptOutput("getParent " + repo + " " + currentCommit).readLine();
					Utils.checkoutCommit(repo, currentCommit);
					System.out.println(prevCommit);
				}
				return prevCommit;
			} else {
				System.out.println("Börst är dålig på OpenArena");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Den introducerades inte i den nya branchen!");
		return null;
	}

	private int parameterExistsInCommit(String repo, String parameter, String commit) {
		int noOfExists = 0;
		try {
			Utils.checkoutCommit(repo, commit);
			BufferedReader br = Utils.readScriptOutput("grepInJava " + repo + " " + parameter);
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				noOfExists++;
				//return true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return noOfExists;

	}

	private String extractParameter(String line) {
		line = line.replace(" ", "");
		return line.split("getAsBoolean(")[1].split(",")[0];
	}

}
