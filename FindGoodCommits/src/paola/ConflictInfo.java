package paola;

public class ConflictInfo {

	private String mergeSHA;
	private String parent1SHA;
	private String parent2SHA;
	private String conflictType;
	private String conflictBody;
	private String fileName;
	
	public ConflictInfo(String mergeSHA, String parent1sha, String parent2sha, String conflictType, String conflictBody,
			String fileName) {
		this.mergeSHA = mergeSHA;
		this.parent1SHA = parent1sha;
		this.parent2SHA = parent2sha;
		this.conflictType = conflictType;
		this.conflictBody = conflictBody;
		this.fileName = fileName;
	}

	public String getMergeSHA() {
		return mergeSHA;
	}

	public String getParent1SHA() {
		return parent1SHA;
	}

	public String getParent2SHA() {
		return parent2SHA;
	}

	public String getConflictType() {
		return conflictType;
	}

	public String getConflictBody() {
		return conflictBody;
	}

	public String getFileName() {
		return fileName;
	}
	
	
	
}
