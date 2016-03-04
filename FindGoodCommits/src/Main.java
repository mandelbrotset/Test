import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import findBooleans.Commander;
import getConflictCommits.ConflictFileTree;
import getVariantParameter.VariantParameter;


public class Main {
	
	private static HashMap<String, String> repos;
	
	private static final String findBooleans = "findBooleans";
	private static final String getConflictBooleans = "getConflictBooleans";
	private static final String getVariantParameter = "getVariantParameter";
	
	public static void main(String args[]) {
		args = new String[1];
		args[0] = getVariantParameter;
		
		if(args[0].equals("findBooleans")) {
			fillRepos();
			analyzeRepos();
		} else if(args[0].equals("getConflictCommits")) {
			ConflictFileTree cft = new ConflictFileTree("/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/");
			cft.createTree();
		} else if(args[0].equals("getVariantParameter")) {
			String pathToRepo = "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/elasticsearch";
			VariantParameter vp = new VariantParameter();
			System.out.println("Commit that introduced the parameter: " + vp.findParameterIntroctionCommitSHA(pathToRepo, "INDEX_FLUSH_ON_CLOSE", "bcaf22eac3242baa1d51bdc54aa2a71959923546"));
		} else
			System.out.println("Please enter a valid command!");
	}
	
	private static void analyzeRepos() {
		Commander comm = new Commander("Repos.xls");
		comm.createSheets(repos);
	}
	
	private static void fillRepos() {
		repos = new HashMap<String, String>();
		File path = new File("/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/");
		String[] directories = path.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		
		for(String repo : directories) {
			repos.put(repo, path.toString() + "/" + repo);
		}
		
		/*for(int i = 0; i < 3; i++) {
			repos.put(directories[i], path.toString() + "/" + directories[i]);
		}*/
	}
	
	
	
	
}
