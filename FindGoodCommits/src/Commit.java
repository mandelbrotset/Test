import java.util.HashSet;


public class Commit {
	private String sha;
	private String message;
	private String variables;
	private boolean isPullRequest;
	
	public Commit(String sha, String message, String variables, boolean isPullRequest) {
		this.sha = sha;
		this.message = message;
		this.variables = variables;
		this.isPullRequest = isPullRequest;
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getVariables() {
		return variables;
	}

	public void setVariables(String variables) {
		this.variables = variables;
	}

	public boolean isPullRequest() {
		return isPullRequest;
	}

	public void setPullRequest(boolean isPullRequest) {
		this.isPullRequest = isPullRequest;
	}
	
	
	

}
