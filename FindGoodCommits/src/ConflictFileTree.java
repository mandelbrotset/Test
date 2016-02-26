import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class ConflictFileTree {

	private HashMap<String, String> reposToReposPath;
	private HashMap<String, ArrayList<String>> reposToCommits;
	
	public ConflictFileTree(String pathToRepos) {
		reposToCommits = new HashMap<String, ArrayList<String>>();
		reposToReposPath = new HashMap<String, String>();
		fillRepos(pathToRepos);
		fillCommits();
		reposToCommits.remove("test");
		reposToReposPath.remove("test");
	}
	
	public void createTree() {
		MergeConflicter cm = new MergeConflicter();
		//for(String repo : reposToReposPath.keySet()) {
		String repo="elasticsearch";
			for(String commit : reposToCommits.get(repo)) {
				if(cm.doMerge(commit, reposToReposPath.get(repo))) {
					Conflict conflict = null;
					for(int i = 0; i < cm.getConflicts().size(); i++) {
						conflict = cm.getConflicts().get(i);
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
					//}
					String outputLocation = "/home/patrik/Documents/Chalmers/5an/MasterThesis/Test/Test/" + repo + "/" + commit + "/" + "diff.txt";
					if(conflict != null) {
						writeDiffFile(reposToReposPath.get(repo), conflict.getLeftAncestorCommit(), conflict.getRightAncestorCommit(), outputLocation);
					}
				}
			try {
				//Clean and reset git repository
				Process p = Runtime.getRuntime().exec("bash scripts/gitClean " + reposToReposPath.get(repo));
				new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void writeDiffFile(String pathToRepo, String leftCommit, String rightCommit, String pathToOutput) {
		try {
			Process p = Runtime.getRuntime().exec("bash scripts/createDiffFile " + pathToRepo + " "  + leftCommit + " " + rightCommit + " " + pathToOutput);
			new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void writeToFile(File file, String text) {
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
	
	private void fillRepos(String pathToRepos) {
		File path = new File(pathToRepos);
		String[] directories = path.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		
		for(String repo : directories) {
			reposToReposPath.put(repo, path.toString() + "/" + repo);
		}
	}
	
	private void fillCommits() {
			for(String repo : reposToReposPath.keySet()) {
				String repoPath = reposToReposPath.get(repo);
				try {
					Process commitProcess = Runtime.getRuntime().exec("bash " + "scripts/" + "getCommits " + repoPath);
					BufferedReader br = new BufferedReader(new InputStreamReader(commitProcess.getInputStream()));
					
					String commitSHA;
					ArrayList<String> shas = new ArrayList<String>();
					while((commitSHA = br.readLine()) != null) {
						shas.add(commitSHA);
					}
					reposToCommits.put(repo, shas);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
}
