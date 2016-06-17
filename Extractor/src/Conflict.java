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
import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class Conflict {
	enum Result {
		LEFT, RIGHT, BOTH, NONE
	}

	enum Category {
		SUPERSET, INTERSECTION, RECENT
	}

	private String leftSha;
	private String type;
	private String rightSha;
	private String mergeCommitSha;
	private String ancestorSha;
	private String leftBody;
	private String rightBody;
	private String ancestorBody;
	private String filePath;
	private String leftDate;
	private String rightDate;
	private String functionName;
	private String[] parameterTypes;
	private String resultBody;
	private String repoPath;
	private HashSet<String> leftLines;
	private HashSet<String> rightLines;
	private ArrayList<Category> categories;
	private Result result;

	public Conflict(String conflict, String repoPath) {
		this.repoPath = repoPath;
		categories = new ArrayList<Category>();
		parseValues(conflict);
	}

	public void setResult() {
		if (isIntersection())
			categories.add(Category.INTERSECTION);
		if (isSuperset())
			categories.add(Category.SUPERSET);

		if (resultBody.equals(leftBody) && resultBody.equals(rightBody)) {
			result = Result.BOTH;
		} else if (resultBody.equals(leftBody)) {
			result = Result.LEFT;
		} else if(resultBody.equals(rightBody)){
			result = Result.RIGHT;
		} else {
			result = Result.NONE;
		}

		if(mostRecent() == result ) {
			categories.add(Category.RECENT);
		}
	}

	private void parseValues(String conflict) {
		leftSha = parseValue(conflict, "Parent1 SHA-1:");
		rightSha = parseValue(conflict, "Parent2 SHA-1:");
		type = parseValue(conflict, "Conflict type:");
		mergeCommitSha = parseValue(conflict, "Merge Commit SHA-1:");
		setBodies(conflict);
		parseFunction(leftBody);
		initHashSets();
		filePath = parseValue(conflict, "File path:");
		filePath = filePath.split("rev_....._.....\\/rev_.....\\-.....\\/")[1];
		leftDate = getDate(leftSha);
		rightDate = getDate(rightSha);
	}

	private String parseValue(String conflict, String parameterName) {
		try {
			return conflict.split(parameterName)[1].split("\n")[0].trim();
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			System.out.println(aioobe.getMessage());
			aioobe.printStackTrace();
		}
		return "";
	}

	private void parseFunction(String body) {
		String unNewlinedBody = body.replace("\n", "");
		for (String line : unNewlinedBody.split(";")) {
			if (FunctionParser.containsFunction(line, false)) {
				functionName = FunctionParser.extractFunctionName(line);
				parameterTypes = FunctionParser.extractFunctionParameters(line, functionName);
				break;
			}
		}
		if (functionName == null || parameterTypes == null) {
			System.out.println("jävel");
		}
	}

	public String getFunctionName() {
		return functionName;
	}

	public String[] getParameterTypes() {
		return parameterTypes;
	}

	private void setBodies(String conflict) {
		String left, anc, right;
		if (conflict.contains("##FSTMerge##")) {
			String generalBody = conflict.split("Conflict body:")[1].split("~~FSTMerge~~")[0];
			left = conflict.split("~~FSTMerge~~")[1].split("##FSTMerge##")[0];
			anc = conflict.split("##FSTMerge##")[1];
			right = conflict.split("##FSTMerge##")[2].split("File path:")[0];

			left = generalBody + left;
			anc = generalBody + anc;
			right = generalBody + right;

		} else {
			String generalBody = conflict.split("Conflict body:")[1].split("\\<\\<\\<\\<\\<\\<\\<")[0];
			String afterMath = conflict.split("\\>\\>\\>\\>\\>\\>\\>.*\\n")[1].split("File path:")[0];

			left = conflict.split("\\<\\<\\<\\<\\<\\<\\<")[1].split("\\|\\|\\|\\|\\|\\|\\|")[0];
			anc = conflict.split("\\|\\|\\|\\|\\|\\|\\|")[1].split("\\=\\=\\=\\=\\=\\=\\=")[0];
			right = conflict.split("\\=\\=\\=\\=\\=\\=\\=")[1].split("\\>\\>\\>\\>\\>\\>\\>")[0];

			left = left.substring(left.indexOf("\n"));
			anc = anc.substring(anc.indexOf("\n"));

			left = generalBody + left + afterMath;
			anc = generalBody + anc + afterMath;
			right = generalBody + right + afterMath;

		}

		leftBody = filterLines(left);
		rightBody = filterLines(right);
		ancestorBody = filterLines(anc);
		
		removeAnnotationsFromBodies();
	}
	
	private void initHashSets() {
		leftLines = new HashSet<String>();
		leftLines.addAll(Arrays.asList(leftBody.split("\n")));
		rightLines = new HashSet<String>();
		rightLines.addAll(Arrays.asList(rightBody.split("\n")));
	}

	public Result hasMoreOf(String word) {
		int numInLeft;
		int numInRight;
		if (word.equals("if")) {
			numInLeft = countIfs(leftBody);
			numInRight = countIfs(rightBody);
		} else {
			numInLeft = countNumberOf(leftBody, word);
			numInRight = countNumberOf(rightBody, word);
		}

		if (numInLeft == numInRight)
			return Result.NONE;

		if (numInLeft > numInRight)
			return Result.LEFT;
		else
			return Result.RIGHT;
	}

	private int countNumberOf(String body, String word) {
		if (!body.contains(word))
			return 0;

		int count = 0;
		for (int i = 0; i < body.length() - word.length(); i++) {
			if (body.substring(i, i + word.length()).equals(word))
				count++;
		}
		return count;
	}
	
	private int countIfs(String body) {
		if (!body.contains("if"))
			return 0;
		int count = 0;
		String[] list = body.split("[ |\\n|\\)|\\(|\\{|\\}]if[ |\\()|\\n]");
		count = list.length - 1;
		return count;
	}

	private void removeAnnotationsFromBodies() {
		leftBody = removeAnnotations(leftBody, functionName);
		ancestorBody = removeAnnotations(ancestorBody, functionName);
		rightBody = removeAnnotations(rightBody, functionName);
	}
	
	private String removeAnnotations(String body, String toFunctionName) {
		StringBuilder sb = new StringBuilder();
		boolean functionPassed = false;
		for (String line : body.split("\n")) {
			if(FunctionParser.containsFunction(line, true)) {
				if(line.startsWith("@")) {
					line = line.substring(line.indexOf(" "));
				}
				functionPassed = true;
			}
					
			if (!line.trim().startsWith("@") || functionPassed) {
				sb.append(line + "\n");
			}
			
		}

		return sb.toString().trim();
	}

	private String getDate(String sha) {
		try {
			BufferedReader br = Utils.readScriptOutput("getDate " + repoPath + " " + sha, true);
			String date = br.readLine();
			while ((br.readLine()) != null) {
			}
			return date;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public Result mostRecent() {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH);
		Date lDate = new Date();
		Date rDate = new Date();
		try {
			lDate = format.parse(leftDate);
			rDate = format.parse(rightDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if(lDate.after(rDate))
			return Result.LEFT;
		
		return Result.RIGHT;
	}
	
	public Result getResult() {
		return result;
	}

	public String getLeftSha() {
		return leftSha;
	}

	public String getRightSha() {
		return rightSha;
	}

	public String getMergeCommitSha() {
		return mergeCommitSha;
	}

	public String getAncestorSha() {
		return ancestorSha;
	}

	public String getLeftBody() {
		return leftBody;
	}

	public String getRightBody() {
		return rightBody;
	}

	public String getAncestorBody() {
		return ancestorBody;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getType() {
		return type;
	}

	public String getLeftDate() {
		return leftDate;
	}

	public String getRightDate() {

		return rightDate;
	}

	public String getResultBody() {
		return resultBody;
	}

	public String getCategoryList() {
		StringBuilder sb = new StringBuilder();
		for (Category c : categories) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(c.toString());
		}
		return sb.toString();
	}

	public void setResultBody(String resultBody) {
		this.resultBody = filterLines(resultBody);
	}

	private boolean isIntersection() {
		HashSet<String> resultWords = getWords(resultBody);
		HashSet<String> leftWords = getWords(leftBody);
		HashSet<String> rightWords = getWords(rightBody);
		HashSet<String> parentsWords = new HashSet<String>(leftWords);
		parentsWords.retainAll(rightWords);
		return resultWords.equals(parentsWords);
	}
	
	public boolean isLeftIntersection() {
		HashSet<String> leftWords = getWords(leftBody);
		HashSet<String> rightWords = getWords(rightBody);
		HashSet<String> parentsWords = new HashSet<String>(leftWords);
		parentsWords.retainAll(rightWords);
		return leftWords.equals(parentsWords);
	}
	
	public boolean isRightIntersection() {
		HashSet<String> leftWords = getWords(leftBody);
		HashSet<String> rightWords = getWords(rightBody);
		HashSet<String> parentsWords = new HashSet<String>(leftWords);
		parentsWords.retainAll(rightWords);
		return rightWords.equals(parentsWords);
	}
	
	public ArrayList<Category> getCategories() {
		return categories;
	}

	private boolean isSuperset() {
		HashSet<String> resultWords = getWords(resultBody);
		HashSet<String> leftWords = getWords(leftBody);
		HashSet<String> rightWords = getWords(rightBody);
		HashSet<String> parentsWords = new HashSet<String>(leftWords);
		parentsWords.addAll(rightWords);
		return resultWords.equals(parentsWords);
	}
	
	public boolean isLeftSuperset() {
		HashSet<String> leftWords = getWords(leftBody);
		HashSet<String> rightWords = getWords(rightBody);
		HashSet<String> parentsWords = new HashSet<String>(leftWords);
		parentsWords.addAll(rightWords);
		return leftWords.equals(parentsWords);
	}
	
	public boolean isRightSuperset() {
		HashSet<String> leftWords = getWords(leftBody);
		HashSet<String> rightWords = getWords(rightBody);
		HashSet<String> parentsWords = new HashSet<String>(leftWords);
		parentsWords.addAll(rightWords);
		return rightWords.equals(parentsWords);
	}
	
	private static HashSet<String> getWords(String body) {
		String regex = "[^\\w_]+";
		String[] words = body.split(regex);
		HashSet<String> set = new HashSet<String>();
		set.addAll(Arrays.asList(words));
		return set;
	}

	private String filterLines(String body) {
		StringBuilder sb = new StringBuilder();
		String[] split = body.split("\n");
		for (int i = 0; i < split.length; i++) {
			String line = split[i].trim();
			if(!line.isEmpty())
				sb.append(line + ((i < split.length-1) ? "\n" : ""));
		}
		return sb.toString();
	}
}
