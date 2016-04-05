package findParametersInMerge;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ParameterFinder {
	
	private String path;
	private HashMap<String, ArrayList<String>> commitToParameters;

	public ParameterFinder(String cftFolder) {
		path = cftFolder;
		commitToParameters = new HashMap<String, ArrayList<String>>();
	}
	
	public void searchMergeCommits() {
		getGoodCommits();
		for(String key : commitToParameters.keySet()) {
			ArrayList<String> lines = commitToParameters.get(key);
			System.out.println("\t\tCommit " + key);
			for(String line : lines) {
				System.out.println(line);
			}
		}
	}
	
	private void getGoodCommits() {
		File cftFolder = new File(path);
		for(File commitFolder : getFiles(f -> f.isDirectory(), cftFolder)) {
			for(File javaFolder : getFiles(f -> f.isDirectory(), commitFolder)) {
				String res  = javaFolder.getAbsolutePath() + "/res_" + javaFolder.getName();
				String left  = javaFolder.getAbsolutePath() + "/left_" + javaFolder.getName();
				String right  = javaFolder.getAbsolutePath() + "/right_" + javaFolder.getName();
				
				ArrayList<String> addedLines = FindParametersInMerge.gatherData(left, right, res);
				addedLines.removeIf(line -> !line.contains("getAsBoolean("));
				if(addedLines.size() > 0)
					commitToParameters.put(commitFolder.getName(), addedLines);
			}
		}
	}
	
	private ArrayList<File> getFiles(java.util.function.Predicate<File> tester, File folder) {
		ArrayList<File> listOfFiles = new ArrayList<File>();
		for(File file : folder.listFiles()) {
			if(tester.test(file))
				listOfFiles.add(file);
		}
		
		return listOfFiles;
	}
	
}
