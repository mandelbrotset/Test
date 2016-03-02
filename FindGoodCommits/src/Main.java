import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class Main {
	
	private static HashMap<String, String> repos;
	
	public static void main(String args[]) {
		fillRepos();
		analyzeRepos();
		
		//ConflictFileTree cft = new ConflictFileTree("/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/");
		//cft.createTree();
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
