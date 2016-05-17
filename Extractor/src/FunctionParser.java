import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.print.attribute.standard.PrinterState;

public class FunctionParser {

	public static ArrayList<String> extractFunction(ArrayList<String> lines, String name, String... params) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			try {
				if (line.contains(name) && containsFunction(line, name, true) && containsAllParams(line, name, params)) {
					int noOfLinesOffset = linesInFunction(i, lines) + i;
					ArrayList<String> functionCodeLines = new ArrayList<String>();
					while (i < noOfLinesOffset) {
						String lineToAdd = lines.get(i++);
						functionCodeLines.add(lineToAdd);
					}
					return functionCodeLines;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;
	}

	private static void writeFile(String project, String mergecommit, ArrayList<String> lines, String[] params,
			String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("project: " + project + "\n");
		sb.append("mergecommitsha:" + mergecommit + "\n");
		sb.append("name:" + name + "\n");
		sb.append("project: " + project + "\n");
		sb.append("lines:\n");
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}
		sb.append("params:\n");
		if (params != null) {
			for (String param : params) {
				sb.append(param);
				sb.append("\n");
			}
		}
		int counter = 0;
		try {
			while (true) {
				File file = new File("/tmp/cft/" + project + "-" + mergecommit + "-conflict" + counter);
				if (file.exists()) {
					counter++;
				} else {
					BufferedWriter bw = new BufferedWriter(new FileWriter(file));
					bw.write(sb.toString());
					bw.close();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean containsAllParams(String line, String functionName, String... params) {
		int hits = 0;
		ArrayList<String> lineParams = new ArrayList<String>(
				Arrays.asList(extractFunctionParameters(line, functionName)));
		ArrayList<String> functionParams = new ArrayList<String>(Arrays.asList(params));

		if (lineParams.equals(functionParams))
			return true;

		return false;
	}

	private static int linesInFunction(int startLineOfFunction, ArrayList<String> lines) {
		int linesInFunction = 0;
		int currentBrackets = 0;
		for (int i = startLineOfFunction; i < lines.size(); i++) {
			String line = lines.get(i);

			if (line.contains("{"))
				currentBrackets++;

			if (line.contains("}"))
				currentBrackets--;

			linesInFunction++;

			if (currentBrackets == 0)
				break;
		}

		return linesInFunction;
	}

	private static boolean isBefore(String line, String before, String after) {
		if (line.contains(before) && !line.contains(after))
			return true;

		return line.indexOf(before) < line.indexOf(after);
	}

	public static boolean containsFunction(String line, String name, boolean splittedOnLines) {
		line = line.trim();
		String hej = name;
		if (hej.equals("")) {
			hej = ".*";
		}
		String regex = "(.* " + hej + "|" + hej + ")( \\(|\\().*\\).*\\{.*";
		if (line.matches(regex))
			if (isBefore(line, name, "(") && isBefore(line, "{", " new ")) {
				if (line.indexOf("(") < line.indexOf(")") && line.indexOf(")") < line.indexOf("{"))
					if (splittedOnLines) {
						if (!line.endsWith(";"))
							return true;
					} else
						return true;
			}

		return false;
	}

	public static boolean containsFunction(String line, boolean splittedOnLines) {
		return containsFunction(line, "", splittedOnLines);
	}

	public static String[] extractFunctionParameters(String line, String functionName) {
		String[] paramList = new String[1];
		String funcName = functionName + "\\(";
		String params = line.split(funcName)[1].split("\\)")[0];
		if (params.contains(",")) {
			paramList = params.split(",");
		} else {
			paramList[0] = params;
		}

		for (int i = 0; i < paramList.length; i++) {
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
