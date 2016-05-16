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
		LEFT, RIGHT, SUPERSET, INTERSECTION, RECENT
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
	private boolean isIntersection;
	private boolean isSuperset;
	private HashSet<String> leftLines;
	private HashSet<String> rightLines;
	private HashSet<String> resultLines;
	private HashSet<String> intersectionLines;
	private ArrayList<Result> results;
	
	public Conflict(String conflict, String repoPath) {
		this.repoPath = repoPath;
		this.tabortdennasen = conflict;
		parseValues(conflict);
	}

	private void setResult() {
		
	}

	public void setIntersection() {
		isIntersection = isIntersection();
	}
	
	public void setSuperSet() {
		isSuperset = isSuperset();
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
			if(FunctionParser.containsFunction(line)) {
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
		
		leftBody = left;
		rightBody = right;
		ancestorBody = anc;
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
	
	private boolean isRecent(Result result) {
		DateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss Z", Locale.ENGLISH);
		Date lDate = new Date();
		Date rDate = new Date();
		try {
			lDate = format.parse(leftDate);
			rDate = format.parse(rightDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if(result == Result.LEFT)
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

	private boolean isIntersection() {
		HashSet<String> leftLines = new HashSet<String>();
		HashSet<String> rightLines = new HashSet<String>();
		leftLines.addAll(Arrays.asList(getLines(leftBody)));
		rightLines.addAll(Arrays.asList(getLines(rightBody)));
		
		leftLines.retainAll(rightLines);
		HashSet<String> resultLines = new HashSet<String>();
		resultLines.addAll(Arrays.asList(getLines(resultBody)));
		intersectionLines = leftLines;
		return resultLines.containsAll(leftLines) && resultLines.size() == leftLines.size();
	}
	
	private boolean isSuperset() {
		HashSet<String> lines = new HashSet<String>();
		lines.addAll(Arrays.asList(getLines(leftBody)));
		lines.addAll(Arrays.asList(getLines(rightBody)));
		HashSet<String> resultLines = new HashSet<String>();
		resultLines.addAll(Arrays.asList(getLines(resultBody)));
		return resultLines.containsAll(lines);
	}
	
	private String[] getLines(String body) {
		return body.split("\n");
	}
}
