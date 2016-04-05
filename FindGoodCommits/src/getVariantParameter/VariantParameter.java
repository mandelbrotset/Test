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
		try {
			Utils.checkoutCommit(repo, initialCommit);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int initialCommitNo = parameterExistsInCommit(repo, parameter, initialCommit);
		int commonAncNo = 0;
		String[] parents;
		
		int commitsBeforeIntroduction = 0;
		int commitsAfterIntroduction = 0;
		
		try {
			parents = Utils.readScriptOutput("getAncestralCommits " + repo + " " + initialCommit, true).readLine().split(" ");//härligt härligt men farligt
			String commonAncestor = Utils.readScriptOutput("getCommonAncestor " + repo + " " + parents[0] + " " + parents[1], true).readLine();
			commonAncNo = parameterExistsInCommit(repo, parameter, commonAncestor);
			if(initialCommitNo > commonAncNo) {// och då vet vi att snart är det jul
				String currentCommit = parents[1];
				String prevCommit = initialCommit;
				System.out.println("init: " + initialCommitNo + " commonAnc: " + commonAncNo);
				
				while(parameterExistsInCommit(repo, parameter, currentCommit) > commonAncNo) {
					prevCommit = currentCommit;
					commitsAfterIntroduction++; // Count the commits from the introduction of the parameter to the merge commit parent
					currentCommit = Utils.readScriptOutput("getParent " + repo + " " + currentCommit, true).readLine();
				}
				commitsAfterIntroduction--; // Don't count the introduction commit to this!
				if(!currentCommit.equals(commonAncestor)){
					do {
						commitsBeforeIntroduction++; // Count the commits between the commit where the parameter was introduced and the common ancestor
					} while(!(currentCommit = Utils.readScriptOutput("getParent " + repo + " " + currentCommit, true).readLine()).equals(commonAncestor));
				}
				
				int totalCommitsInBranch = commitsBeforeIntroduction + commitsAfterIntroduction+1;
				float introducedInBranch = ((float)(totalCommitsInBranch-commitsAfterIntroduction)/(float)totalCommitsInBranch);
				System.out.println("Commits before: " + commitsBeforeIntroduction + " Commits after: " + commitsAfterIntroduction + "\nIntroduced at " + introducedInBranch*100 + "% of the branch life");
				
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
			BufferedReader br = Utils.readScriptOutput("grepInJava " + repo + " " + parameter, true);
			while (br.readLine() != null) {
				noOfExists++;
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
