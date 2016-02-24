import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class Commander {
	
	private final String SCRIPT_PATH = "/home/patrik/Documents/Chalmers/5an/MasterThesis/Test/Test/scripts/";
	private final String REPO = "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/elasticsearch";

	private HashMap<String, ArrayList<String>> commitToDiff;
	private HashMap<String, ArrayList<String>> commitToBooleanVariables;
	private ArrayList<String> goodCommits;
	
	
	public Commander() {
		commitToDiff = new HashMap<String, ArrayList<String>>();
		commitToBooleanVariables = new HashMap<String, ArrayList<String>>();
		goodCommits = new ArrayList<String>();
		getDiffs();
		
		findVariableBooleans();
	}
	
	
	private void findVariableBooleans() {
		for(String key : commitToDiff.keySet()) {
			ArrayList<String> diff = commitToDiff.get(key);
			
			String variableName;
			ArrayList<String> variables = new ArrayList<String>();
			for(String line : diff) {
				if(line.contains("boolean ") && line.endsWith(";")) {
					int startIndex = line.indexOf("boolean") + 8;
					String fromBoolean = line.substring(startIndex);
					//System.out.println(fromBoolean);
					
					int endIndex = -1;
					if(fromBoolean.indexOf(" ") != -1)
						endIndex = fromBoolean.indexOf(" ");
					else if(fromBoolean.indexOf("=") != -1)
						endIndex = fromBoolean.indexOf("=");
					else if(fromBoolean.indexOf(";") != -1)
						endIndex = fromBoolean.indexOf(";");
					
					//System.out.println(endIndex);
					variableName = fromBoolean.substring(0, endIndex);
					if(!variableName.contains(",") && !variableName.contains(")") && isVariable(fromBoolean)) {
						System.out.println(variableName);
						variables.add(variableName);
						//System.out.println("The line was: " + line);
					}
				}
				commitToBooleanVariables.put(key, variables);
			}
			
		}
		
	}
	
	private void findIfsWithBooleans() {
		for(String key : commitToBooleanVariables.keySet()) {
			ArrayList<String> variables = commitToBooleanVariables.get(key);
			ArrayList<String> lines = commitToDiff.get(key);
			for(String variable : variables) {
				for(String line : lines) {
					if(line.contains(variable) && line.contains("if"))
						if(!goodCommits.contains(key))
							goodCommits.add(key);
				}
			}
			
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
			Process commitProcess = Runtime.getRuntime().exec("bash " + SCRIPT_PATH + "diffs " + REPO);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(commitProcess.getInputStream()));
			
			String commitSHA;
			while((commitSHA = br1.readLine()) != null) {
				Process diffProcess = Runtime.getRuntime().exec("bash " + SCRIPT_PATH + "diffs2 " + REPO + " " + commitSHA);
				
				BufferedReader br2 = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
				
				String line;
				ArrayList<String> lines = new ArrayList<String>();
				while((line = br2.readLine()) != null) {
					if(line.startsWith("+"))
						lines.add(line);
				}
				commitToDiff.put(commitSHA, lines);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
