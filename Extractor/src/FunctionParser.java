import java.util.ArrayList;

public class FunctionParser {

	public static ArrayList<String> extractFunction(ArrayList<String> lines, String name, String... params) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains(name) && containsAllParams(line, params) && containsFunction(line)) {
				int noOfLinesOffset = linesInFunction(i, lines) + i;
				ArrayList<String> functionCodeLines = new ArrayList<String>();
				while (i < noOfLinesOffset) {
					String lineToAdd = lines.get(i++);
					functionCodeLines.add(lineToAdd);
				}
				return functionCodeLines;
			}
		}

		return null;
	}

	private static boolean containsAllParams(String line, String... params) {
		int hits = 0;
		for(String p : params) {
			if(line.contains(p))
				hits++;
		}
		
		if(params.length == hits)
			return true;
		
		return false;
	}
	
	private static int linesInFunction(int startLineOfFunction, ArrayList<String> lines) {
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
	
	public static boolean containsFunction(String line) {
		line = line.trim();
		if ((line.startsWith("private") || line.startsWith("public")) && !line.contains("class") && line.endsWith("{"))
			return true;

		return false;
	}
	
	public static String[] extractFunctionParameters(String line, String functionName) {
		String[] paramList = new String[1];
		String funcName = functionName + "\\(";
		String params = line.split(funcName)[1].split("\\)")[0];
		if(params.contains(",")) {
			paramList = params.split(",");
		} else {
			paramList[0] = params;
		}
		
		for(int i = 0; i < paramList.length; i++) {
			paramList[i] = paramList[i].trim().split(" ")[0];
		}
		
		return paramList;
	}
	
	public static String extractFunctionName(String line) {
		line = line.split("\\(")[0];
		StringBuilder sb = new StringBuilder(line);
		sb = sb.reverse();
		String name = new StringBuilder(sb.toString().split(" ")[0]).reverse().toString();
		
		return name;
	}

}