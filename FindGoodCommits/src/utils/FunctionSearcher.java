package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class FunctionSearcher {

	private String repoPath;
	private HashMap<String, ArrayList<String>> functionToFunctionCode;

	public FunctionSearcher(String repoPath) {
		this.repoPath = repoPath;
	}

	public HashMap<String, ArrayList<String>> findNewFunctions(String commit) {
		ArrayList<String> lines = getDiff(commit);
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			makeTheLineNice(line);
			
			if(containsFunction(line)) {
				int noOfLines = linesInFunction(i, lines);
				ArrayList<String> functionCodeLines = new ArrayList<String>();
				String functionName = extractFunctionName(line);
				while(i <= noOfLines) {
					functionCodeLines.add(lines.get(i++));
				}
				functionToFunctionCode.put(functionName, functionCodeLines);
			}
		}

		return null;
	}

	private boolean containsFunction(String line) {
		if ((line.startsWith("private") || line.startsWith("public")) && !line.contains("class") && line.endsWith("{"))
			return true;

		return false;
	}

	private void makeTheLineNice(String line) {
		// Remove ending comments and starting/ending whitespaces
		removeComments(line);
		line = line.trim();
	}

	private int linesInFunction(int startLineOfFunction, ArrayList<String> lines) {
		int linesInFunction = 0;
		int currentBrackets = 0;
		for(int i = startLineOfFunction; i < lines.size(); i++) {
			String line = lines.get(i);
			
			if(line.endsWith("{"))
				currentBrackets++;
			else if(line.endsWith("}"))
				currentBrackets--;
			
			linesInFunction++;
			
			if(currentBrackets == 0)
				break;
		}
		
		return linesInFunction;
	}

	private void removeComments(String line) { // At the end of a line
		if (line.contains("//"))
			line = line.split("//")[0];

		if (line.contains("/*"))
			line = line.split("/*")[0];
	}

	private String extractFunctionName(String line) {
		line = line.split("\\(")[0];
		StringBuilder sb = new StringBuilder(line);
		sb = sb.reverse();
		String name = new StringBuilder(sb.toString().split(" ")[0]).reverse().toString();
		
		return name;
	}

	private ArrayList<String> getDiff(String commit) {
		ArrayList<String> linesPlus = new ArrayList<String>();
		try {
			BufferedReader br = Utils.readScriptOutput("getDiff " + repoPath
					+ commit);
			String line;
			boolean isJavaFile = false;

			while ((line = br.readLine()) != null) {
				if (line.startsWith("diff --git ") && line.endsWith(".java"))
					isJavaFile = true;
				else if (line.startsWith("diff --git "))
					isJavaFile = false;

				if (isJavaFile) {
					if (line.startsWith("+"))
						linesPlus.add(line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return linesPlus;
	}
}
