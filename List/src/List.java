import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class List {
	private Runtime rt = Runtime.getRuntime();
	private String pathToProjects = "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/";
	private HashMap<String, String> branchToCommit = new HashMap<String, String>();
	private HashMap<String, String> commitToBranch = new HashMap<String, String>();
	private HashMap<String, String> mergeCommitToMessage = new HashMap<String, String>();

	public List() {

	}

	private void displayVadViHarIListorna() {
		for (String s : branchToCommit.keySet()) {
			System.out.println(s + " - " + branchToCommit.get(s));
		}
		
		for (String s : mergeCommitToMessage.keySet()) {
			System.out.println(s + " - " + mergeCommitToMessage.get(s));
		}
	}
	
	public void go() {
		for(String project : getProjects()) {
			getBranchCommitInfo(project);
			getMergeCommitInfo(project);
			displayVadViHarIListorna();
			branchToCommit.clear();
			commitToBranch.clear();
			mergeCommitToMessage.clear();
		}
	}
	
	private ArrayList<String> getProjects() {
		ArrayList<String> projects = new ArrayList<String>();
		Process p = exec("ls " + pathToProjects);
		InputStream is = p.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String projectName;
		try {
			while ((projectName = br.readLine()) != null) {
				projects.add(projectName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		return projects;
	}
	
	private void getMergeCommitInfo(String project) {
		Process p = exec("bash /home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/test/List/src/script/get-merge-commits " + pathToProjects + project);
		InputStream is = p.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = "";
		String commitSHA = "";
		String klump = "";
		try {
			while((line = br.readLine()) != null) {
				
				if(line.contains("commit")) {
					commitSHA = line;
					if(klump.toLowerCase().contains("feature") && klump.toLowerCase().contains("conflict")) {
						System.out.println(project + ":\t\t" +klump);
					}
					br.readLine();
					br.readLine();
					br.readLine();
					klump = "";
				}
				klump += line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void getBranchCommitInfo(String project) {
		Process p = exec("bash /home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/test/List/src/script/get-branches " + pathToProjects + project);
		InputStream is = p.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String branch;
		try {
			while ((branch = br.readLine()) != null) {
				if (branch.contains("feature")) {
					Process p2 = exec("bash /home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/test/List/src/script/git-show " + pathToProjects + project + " " + branch);
					InputStream is2 = p2.getInputStream();
					BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
					String line0 = br2.readLine();
					String commit = line0.substring(7);
					branch = project + ": " + branch;
					branchToCommit.put(branch, commit);
					commitToBranch.put(commit, branch);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Process exec(String command) {
		//System.out.println(command);
		try {
			return rt.exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
	}
}
