import java.util.HashSet;


public class Commit {
	private String sha;
	private String message;
	private String ifVariables;
	private String settingVariables;
	private boolean isPullRequest;
	
	public Commit(String sha, String message, String ifVariables, String settingVariables, boolean isPullRequest) {
		this.sha = sha;
		this.message = message;
		this.ifVariables = ifVariables;
		this.settingVariables = settingVariables;
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

	public String getIfVariables() {
		return ifVariables;
	}

	public String getSettingVariables() {
		return settingVariables;
	}

	public boolean isPullRequest() {
		return isPullRequest;
	}

	public void setPullRequest(boolean isPullRequest) {
		this.isPullRequest = isPullRequest;
	}
	
	
	

}
