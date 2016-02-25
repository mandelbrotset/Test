import java.io.File;


public class Conflict {

	private String mergeCommit;
	private String fileName;
	private File leftConflict;
	private File rightConflict;
	private File commonAncestor;
	private File resolution;
	
	public Conflict() {
		
	}
	
	

	public Conflict(String mergeCommit, File leftConflict, File rightConflict,
			File commonAncestor, File resolution) {
		this.mergeCommit = mergeCommit;
		this.leftConflict = leftConflict;
		this.rightConflict = rightConflict;
		this.commonAncestor = commonAncestor;
		this.resolution = resolution;
		
		fileName = resolution.getName();
	}



	public String getFileName() {
		return fileName;
	}

	public String getMergeCommit() {
		return mergeCommit;
	}

	public void setMergeCommit(String mergeCommit) {
		this.mergeCommit = mergeCommit;
	}

	public File getLeftConflict() {
		return leftConflict;
	}

	public void setLeftConflict(File leftConflict) {
		this.leftConflict = leftConflict;
	}

	public File getRightConflict() {
		return rightConflict;
	}

	public void setRightConflict(File rightConflict) {
		this.rightConflict = rightConflict;
	}

	public File getCommonAncestor() {
		return commonAncestor;
	}

	public void setCommonAncestor(File commonAncestor) {
		this.commonAncestor = commonAncestor;
	}

	public File getResolution() {
		return resolution;
	}

	public void setResolution(File resolution) {
		this.resolution = resolution;
	}
	
}
