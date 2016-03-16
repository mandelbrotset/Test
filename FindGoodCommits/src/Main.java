import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import utils.Machine;
import findBooleans.Commander;
import getConflictCommits.ConflictFileTree;
import getVariantParameter.VariantParameter;


public class Main {
	
	private static ConcurrentHashMap<String, String> repos;
	
	private static final String findBooleans = "findBooleans";
	private static final String getConflictBooleans = "getConflictBooleans";
	private static final String getVariantParameter = "getVariantParameter";
	
	private static Machine machine;
	
	
	public static void main(String args[]) {
		args = new String[1];
		args[0] = getVariantParameter;
		
		machine = Machine.getInstance();
		
		String parameter = "threadedListener";
		String commit = "01d6f0dc1d569f4d7e947a322129e492092724ee";
		
		if(args[0].equals("findBooleans")) {
			fillRepos();
			analyzeRepos();
		} else if(args[0].equals("getConflictCommits")) {
			ConflictFileTree cft = new ConflictFileTree(machine.getRepoPath());
			cft.createTree();
		} else if(args[0].equals("getVariantParameter")) {
			String pathToRepo = "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/elasticsearch";
			VariantParameter vp = new VariantParameter();
			System.out.println("Commit that introduced the parameter: " + vp.findParameterIntroctionCommitSHA(pathToRepo, parameter, commit));
		} else
			System.out.println("Please enter a valid command!");
	}
	
	private static void analyzeRepos() {
		Commander comm = new Commander("Repos.xls");
		comm.createSheets(repos);
	}
	
	private static void fillRepos() {
		repos = new ConcurrentHashMap<String, String>();
		File path = new File(machine.getRepoPath());
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
