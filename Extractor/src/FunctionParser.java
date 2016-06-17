/*
Copyright (C) 2016 Isak Eriksson, Patrik Wållgren

This file is part of ResolutionsAnalyzer.

    ResolutionsAnalyzer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ResolutionsAnalyzer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ResolutionsAnalyzer.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.util.ArrayList;
import java.util.Arrays;

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

	private static boolean containsAllParams(String line, String functionName, String... params) {
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
		String s = name;
		if (s.equals("")) {
			s = ".*";
		}
		String regex = "(.* " + s + "|" + s + ")( \\(|\\().*\\).*\\{.*";
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
		String splitRegex = functionName + "( \\(|\\()";
		
		String params = line.split(splitRegex)[1].split("\\)")[0];
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
