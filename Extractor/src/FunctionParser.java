import java.util.ArrayList;

public class FunctionParser {

	public static ArrayList<String> extractFunction(ArrayList<String> lines, String name, String... params) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.contains(name) && containsAllParams(line, params)) {
				int noOfLines = linesInFunction(i, lines);
				ArrayList<String> functionCodeLines = new ArrayList<String>();
				while (i <= noOfLines) {
					functionCodeLines.add(lines.get(i++));
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

}
