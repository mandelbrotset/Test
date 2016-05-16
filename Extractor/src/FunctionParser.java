import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.print.attribute.standard.PrinterState;

public class FunctionParser {

	public static ArrayList<String> extractFunction(String mergecommitsha, String project, ArrayList<String> lines, String name, String... params) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			try {
				if (line.contains(name) && containsAllParams(line, params) && containsFunction(line)) {
					int noOfLinesOffset = linesInFunction(i, lines) + i;
					ArrayList<String> functionCodeLines = new ArrayList<String>();
					while (i < noOfLinesOffset) {
						String lineToAdd = lines.get(i++);
						functionCodeLines.add(lineToAdd);
					}
					return functionCodeLines;
				}
			} catch (NullPointerException e) {
				/*e.printStackTrace();
				System.out.println("Det var det här som kraschade:");
				System.out.println("project:" + project);
				System.out.println("mergecommitsha:" + mergecommitsha);
				System.out.println("name:" + name);
				System.out.println("lines:");
				for (String linee : lines) {
					System.out.println(linee);
				}
				System.out.println("params:");
				if (params != null) {
					for (String param : params) {
						System.out.println(param);
					}
				}	
				System.out.println("Slut på krasch");*/
				writeFile(project, mergecommitsha, lines, params, name);
				return null;
			}
		}

		return null;
	}
	
	private static void writeFile(String project, String mergecommit, ArrayList<String> lines, String[] params, String name) {
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
		if ((line.startsWith("protected") || line.startsWith("private") || line.startsWith("public")) && !line.contains("class") && line.endsWith("{"))
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
