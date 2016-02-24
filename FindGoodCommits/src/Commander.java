import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class Commander {
	
	private final String SCRIPT_PATH = "/home/patrik/Documents/Chalmers/5an/MasterThesis/Test/Test/scripts/";
	private final String REPO = "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/elasticsearch";

	private HashMap<String, HashSet<String>> commitToDiffPlus;
	private HashMap<String, HashSet<String>> commitToDiffMinus;
	private HashMap<String, HashSet<String>> commitToBooleanVariables;
	private HashMap<String, ArrayList<String>> commitToCommitMessage;
	private ArrayList<String> goodCommits;
	
	
	public Commander() {
		commitToDiffPlus = new HashMap<String, HashSet<String>>();
		commitToDiffMinus = new HashMap<String, HashSet<String>>();
		commitToBooleanVariables = new HashMap<String, HashSet<String>>();
		commitToCommitMessage = new HashMap<String, ArrayList<String>>();
		goodCommits = new ArrayList<String>();
		getDiffs();
		
		findVariableBooleans(commitToDiffPlus, true);
		findVariableBooleans(commitToDiffMinus, false);
		findIfsWithBooleans();
		//printTheGoodCommits();
		//printTheVariables();
		printTheCommitMessages();
	}
	
	
	private void findVariableBooleans(HashMap<String, HashSet<String>> list, boolean plus) {
		for(String key : list.keySet()) {
			HashSet<String> diff = list.get(key);
			
			String variableName;
			HashSet<String> variables = new HashSet<String>();
			for(String line : diff) {
				if(line.contains("boolean ") && line.endsWith(";")) {
					int startIndex = line.indexOf("boolean") + 8;
					String fromBoolean = line.substring(startIndex);
					
					int endIndex = -1;
					if(fromBoolean.indexOf(" ") != -1)
						endIndex = fromBoolean.indexOf(" ");
					else if(fromBoolean.indexOf("=") != -1)
						endIndex = fromBoolean.indexOf("=");
					else if(fromBoolean.indexOf(";") != -1)
						endIndex = fromBoolean.indexOf(";");
					
					variableName = fromBoolean.substring(0, endIndex);
					if(!variableName.contains(",") && !variableName.contains(")") && isVariable(fromBoolean)) {
						variables.add(variableName);
					}
				}
				if(plus)
					commitToBooleanVariables.put(key, variables);
				else {
					HashSet<String> set = commitToBooleanVariables.get(key);
					if(set != null) {
						for(String name : variables)
							set.remove(name);
					}
						
				}
					
			}
			
		}
		
	}
	
	private void findIfsWithBooleans() {
		for(String key : commitToBooleanVariables.keySet()) {
			HashSet<String> variables = commitToBooleanVariables.get(key);
			HashSet<String> lines = commitToDiffPlus.get(key);
			for(String variable : variables) {
				for(String line : lines) {
					if(line.contains(variable) && line.contains("if"))
						if(!goodCommits.contains(key))
							goodCommits.add(key);
				}
			}
			
		}
	}
	
	private void printTheGoodCommits() {
		for(String commit : goodCommits) {
			System.out.println(commit);
		}
	}
	
	private void printTheVariables() {
		for(String commit : goodCommits) {
			StringBuilder sb = new StringBuilder();
			for(String str : commitToBooleanVariables.get(commit)) {
				if(sb.length() != 0)
					sb.append(", ");
				sb.append(str);
			}
			System.out.println(sb);
		}
		
	}
	
	private void printTheCommitMessages() {
		for(String commit : goodCommits) {
			ArrayList<String> msg = commitToCommitMessage.get(commit);
			StringBuilder sb = new StringBuilder();
			for(String line : msg) {
				if(sb.length() != 0)
					sb.append("|");
				sb.append(line);
			}
			System.out.println(sb);
		}
	}
	
	private boolean isVariable(String str) {
		if(str.contains("(")) {
			if(str.contains("=")) {
				if(str.indexOf("=") < str.indexOf("("))
					return true;
				else
					return false;
			}
			else
				return false;
		}
		
		return true;
	}
	
	private void findFunctionBooleans() {
		
	}
	
	
	private void getDiffs() {
		try {
			Process commitProcess = Runtime.getRuntime().exec("bash " + SCRIPT_PATH + "getCommits " + REPO);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(commitProcess.getInputStream()));
			
			String commitSHA;
			while((commitSHA = br1.readLine()) != null) {
				Process diffProcess = Runtime.getRuntime().exec("bash " + SCRIPT_PATH + "getDiff " + REPO + " " + commitSHA);
				
				BufferedReader br2 = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
				
				String line;
				HashSet<String> linesPlus = new HashSet<String>();
				HashSet<String> linesMinus = new HashSet<String>();
				//Get all lines that starts with "-" and "+"
				while((line = br2.readLine()) != null) {
					if(line.startsWith("+"))
						linesPlus.add(line);
					else if(line.startsWith("-"))
						linesMinus.add(line);
				}
				
				//Get the commit message
				Process msgProcess = Runtime.getRuntime().exec("bash " + SCRIPT_PATH + "getCommitMessage " + REPO + " " + commitSHA);
				br2 = new BufferedReader(new InputStreamReader(msgProcess.getInputStream()));
				ArrayList<String> commitMessage = new ArrayList<String>();
				while((line = br2.readLine()) != null) {
					commitMessage.add(line);
				}
				
				commitToDiffPlus.put(commitSHA, linesPlus);
				commitToDiffMinus.put(commitSHA, linesMinus);
				commitToCommitMessage.put(commitSHA, commitMessage);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
