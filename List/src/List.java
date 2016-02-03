import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;

public class List {
	Runtime rt = Runtime.getRuntime();
	String path = "";
	private HashMap<String, String> branchToCommit = new HashMap<String, String>();
	private HashMap<String, String> commitToBranch = new HashMap<String, String>();

	public List() {

	}

	private void displayVadViHarIListorna() {
		for (String s : branchToCommit.keySet()) {
			System.out.println(s + " - " + branchToCommit.get(s));
		}
		for (String s : commitToBranch.keySet()) {
			System.out.println(s + " - " + commitToBranch.get(s));
		}
	}
	
	public void go() {
		getBranchCommitInfo("/home/isak/Documents/Master/Test/Test/");
		displayVadViHarIListorna();
		branchToCommit.clear();
		commitToBranch.clear();
		getBranchCommitInfo("/home/isak/Documents/Master/example_project8/hadoop");
		displayVadViHarIListorna();
	}
	
	public void clone(String[] list) {
		for (String project : list) {
			exec("bash /home/isak/workspace/List/src/script/clone https://github.com/" + project);
		}
	}
	
	private void getBranchCommitInfo(String path) {
		Process p = exec("bash /home/isak/workspace/List/src/script/get-branches " + path);
		InputStream is = p.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String branch;
		try {
			while ((branch = br.readLine()) != null) {
				if (branch.contains("feature")) {
					Process p2 = exec("bash /home/isak/workspace/List/src/script/git-show " + path + " " + branch);
					InputStream is2 = p2.getInputStream();
					BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
					String line0 = br2.readLine();
					String commit = line0.substring(7);
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
		System.out.println(command);
		try {
			return rt.exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return null;
		}
	}
}
