package getConflictCommits;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import utils.Machine;
import utils.Utils;

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
		// for(String repo : reposToReposPath.keySet()) {
		String repo = "elasticsearch";
		try {
			System.out.println("Cleaning repository...");
			Utils.readScriptOutput("gitClean " + reposToReposPath.get(repo), false);
			Utils.readScriptOutput("gitUpdate " + reposToReposPath.get(repo), false);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Building tree...");
		for (String commit : reposToCommits.get(repo)) {
			System.out.println("checking commit " + commit);
			if (cm.doMerge(commit, reposToReposPath.get(repo))) {
				System.out.println("Conflict found!");
				Conflict conflict = null;
				for (int i = 0; i < cm.getConflicts().size(); i++) {
					conflict = cm.getConflicts().get(i);
					String location = "CFT/" + repo + "/" + commit + "/" + conflict.getFileName() + "/";
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
				// }
				String outputLocation = Machine.getInstance().getCftFolderPath() + "/" + repo + "/" + commit + "/" + "diff.txt";
				if (conflict != null) {
					writeDiffFile(reposToReposPath.get(repo), conflict.getLeftAncestorCommit(),
							conflict.getRightAncestorCommit(), outputLocation);
				}
			}
			try {
				// Clean and reset git repository
				Utils.readScriptOutput("gitClean " + reposToReposPath.get(repo), false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void writeDiffFile(String pathToRepo, String leftCommit, String rightCommit, String pathToOutput) {
		try {
			Utils.readScriptOutput("createDiffFile " + pathToRepo + " " + leftCommit + " " + rightCommit + " " + pathToOutput, false);
		} catch (IOException e) {
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

		for (String repo : directories) {
			reposToReposPath.put(repo, path.toString() + "/" + repo);
		}
	}

	private void fillCommits() {
		for (String repo : reposToReposPath.keySet()) {
			String repoPath = reposToReposPath.get(repo);
			try {
				BufferedReader br = Utils.readScriptOutput("getCommits " + repoPath, true);

				String commitSHA;
				ArrayList<String> shas = new ArrayList<String>();
				while ((commitSHA = br.readLine()) != null) {
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
