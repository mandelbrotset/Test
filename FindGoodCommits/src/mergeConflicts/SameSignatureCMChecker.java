package mergeConflicts;

import java.util.ArrayList;
import java.util.HashMap;

import mergeConflicts.ConflictClassifier.Classifier;

public class SameSignatureCMChecker implements ClassChecker {

	private boolean hasDifferentBodies(String leftBody, String rightBody) {
		return leftBody.equals(rightBody);
	}
	
	@Override
	public Classifier getConflictClass() {
		return Classifier.SAME_SIGNATURE_CM;
	}

	@Override
	public boolean checkClass(String left, String common, String right) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean containsFunction(String line) {
		if ((line.startsWith("private") || line.startsWith("public")) && !line.contains("class") && line.endsWith("{"))
			return true;

		return false;
	}

	private int linesInFunction(int startLineOfFunction, ArrayList<String> lines) {
		int linesInFunction = 0;
		int currentBrackets = 0;
		for (int i = startLineOfFunction; i < lines.size(); i++) {
			String line = lines.get(i);

			if (line.endsWith("{"))
				currentBrackets++;
			else if (line.endsWith("}"))
				currentBrackets--;

			linesInFunction++;

			if (currentBrackets == 0)
				break;
		}

		return linesInFunction;
	}

	private String extractFunctionName(String line) {
		line = line.split("\\(")[0];
		StringBuilder sb = new StringBuilder(line);
		sb = sb.reverse();
		String name = new StringBuilder(sb.toString().split(" ")[0]).reverse().toString();

		return name;
	}

	private HashMap<String, String> extractFunctionBody(ArrayList<String> conflictLines) {
		HashMap<String, String> functionToFunctionBody = new HashMap<String, String>();
		for(int i = 0; i < conflictLines.size(); i++) {
			String line = conflictLines.get(i);
			
			if(containsFunction(line)) {
				int noOfLines = linesInFunction(i, conflictLines);
				StringBuilder functionBodyLines = new StringBuilder();
				String functionName = extractFunctionName(line);
				while(i <= noOfLines) {
					if(functionBodyLines.length() > 0)
						functionBodyLines.append("\n");
					functionBodyLines.append(conflictLines.get(i++));
				}
				functionToFunctionBody.put(functionName, functionBodyLines.toString());
			}
		}
		return functionToFunctionBody;
	}
}
