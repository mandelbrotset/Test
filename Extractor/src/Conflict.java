import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

public class Conflict {
	private String leftSha;
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
	
	public Conflict(String conflict, String repoPath) {
		this.repoPath = repoPath;
		parseValues(conflict);
	}
	
	private void parseValues(String conflict) {
		leftSha = parseValue(conflict, "Parent1 SHA-1:");
		rightSha = parseValue(conflict, "Parent2 SHA-1:");
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
			}
		}
	}
	
	public String getFunctionName() {
		return functionName;
	}

	public String[] getParameterTypes() {
		return parameterTypes;
	}

	private void setBodies(String conflict) {
		String left = conflict.split("\\<\\<\\<\\<\\<\\<\\<")[1].split("\\|\\|\\|\\|\\|\\|\\|")[0];
		String anc = conflict.split("\\|\\|\\|\\|\\|\\|\\|")[1].split("\\=\\=\\=\\=\\=\\=\\=")[0];
		String right = conflict.split("\\=\\=\\=\\=\\=\\=\\=")[1].split("\\>\\>\\>\\>\\>\\>\\>")[0];
		left = left.substring(left.indexOf("\n"));
		anc = anc.substring(anc.indexOf("\n"));
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

	public String getLeftDate() {
		return leftDate;
	}

	public String getRightDate() {
		return rightDate;
	}
}
