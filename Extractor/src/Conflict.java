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
		LEFT, RIGHT, SUPERSET, INTERSECTION, RECENT, BOTH
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
	private String tabortdennasen;
	private HashSet<String> leftLines;
	private HashSet<String> rightLines;
	private HashSet<String> resultLines;
	private HashSet<String> intersectionLines;
	private ArrayList<Result> results;
	
	public Conflict(String conflict, String repoPath) {
		this.repoPath = repoPath;
		this.tabortdennasen = conflict;
		results = new ArrayList<Result>();
		parseValues(conflict);
	}

	private void setResult() {
		if (isIntersection()) results.add(Result.INTERSECTION);
		if (isSuperset()) results.add(Result.SUPERSET);
		if (resultBody.equals(leftBody)) {
			results.add(Result.LEFT);
		}
		if (resultBody.equals(rightBody)) {
			results.add(Result.RIGHT);
		}
		if (results.contains(Result.LEFT) && results.contains(Result.RIGHT)) {
			results.remove(Result.LEFT);
			results.remove(Result.RIGHT);
			results.add(Result.BOTH);
		}
		if (results.contains(Result.LEFT)) {
			if (isRecent(Result.LEFT)) {
				results.add(Result.RECENT);
			}	
		}
		if (results.contains(Result.RIGHT)) {
			if (isRecent(Result.RIGHT)) {
				results.add(Result.RECENT);
			}	
		}
	}
	
	private void parseValues(String conflict) {
		leftSha = parseValue(conflict, "Parent1 SHA-1:");
		rightSha = parseValue(conflict, "Parent2 SHA-1:");
		type = parseValue(conflict, "Conflict type:");
		mergeCommitSha = parseValue(conflict, "Merge Commit SHA-1:");
		setBodies(conflict);
		parseFunction(leftBody);
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
		for(String line : unNewlinedBody.split(";")) {
			if(FunctionParser.containsFunction(line, false)) {
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
		if(conflict.contains("##FSTMerge##")) {
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
		
		leftBody = left.trim();
		rightBody = right.trim();
		ancestorBody = anc.trim();
		leftLines = new HashSet<String>();
		leftLines.addAll(Arrays.asList(getLines(leftBody)));
		leftLines.forEach(s -> s.trim());
		rightLines = new HashSet<String>();
		rightLines.addAll(Arrays.asList(getLines(rightBody)));
		rightLines.forEach(s -> s.trim());
	}

	private String getDate(String sha) {
		try {
			BufferedReader br = Utils.readScriptOutput("getDate " + repoPath + " " + sha, true);
			String date = br.readLine();
			while((br.readLine()) != null) {}
			return date;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private boolean isRecent(Result whichWasChosen) {
		DateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss Z", Locale.ENGLISH);
		Date lDate = new Date();
		Date rDate = new Date();
		try {
			lDate = format.parse(leftDate);
			rDate = format.parse(rightDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if(whichWasChosen == Result.LEFT)
			return lDate.after(rDate);
		
		return rDate.after(lDate);
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
	
	public String getResults() {
		StringBuilder sb = new StringBuilder();
		for(Result r : results) {
			if(sb.length() > 0)
				sb.append(", ");
			sb.append(r.toString());
		}
		return sb.toString();
	}

	public void setResultBody(String resultBody) {
		this.resultBody = resultBody.trim();
		resultLines = new HashSet<String>(Arrays.asList(resultBody.split("\n")));
		resultLines.forEach(s -> s.trim());
	}

	private boolean isIntersection() {
		HashSet<String> intersectionLines = new HashSet<String>(leftLines);
		intersectionLines.retainAll(rightLines);
		return resultLines.containsAll(intersectionLines) && resultLines.size() == intersectionLines.size();
	}

	private boolean isSuperset() {
		HashSet<String> lines = new HashSet<String>();
		lines.addAll(leftLines);
		lines.addAll(rightLines);
		return resultLines.containsAll(lines);
	}
	
	private String[] getLines(String body) {
		return body.split("\n");
	}
}
