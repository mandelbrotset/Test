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
		//fillRepos();
		//analyzeRepos();
		createConflicts();
	}
	
	private static void createConflicts() {
		String repo = "elasticsearch";
		String commit = "2203f439e211be5510cfb0d6e1d2483dba15c6fe";
		MergeConflicter cm = new MergeConflicter();
		if(cm.doMerge(commit)) {
			for(Conflict conflict : cm.getConflicts()) {
				String location = repo + "/" + commit + "/" + conflict.getFileName() + "/";
				File dir = new File(location);
				File leftConflict = new File(location + "left_" + conflict.getFileName());
				File rightConflict = new File(location + "right_" + conflict.getFileName());
				File commonAncestor = new File(location + "anc_" + conflict.getFileName());
				File resolution = new File(location + "res_" + conflict.getFileName());
				dir.mkdirs();
				
				try {
					leftConflict.createNewFile();
					rightConflict.createNewFile();
					commonAncestor.createNewFile();
					resolution.createNewFile();
					writeToFile(leftConflict, conflict.getLeftConflict());
					writeToFile(rightConflict, conflict.getRightConflict());
					writeToFile(commonAncestor, conflict.getCommonAncestor());
					writeToFile(resolution, conflict.getResolution());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void writeToFile(File file, String text) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			pw.print(text);
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
