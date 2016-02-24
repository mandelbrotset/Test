import java.util.HashSet;


public class Commit {
	private String sha;
	private String message;
	private String variables;
	
	public Commit(String sha, String message, String variables) {
		this.sha = sha;
		this.message = message;
		this.variables = variables;
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
	

}
