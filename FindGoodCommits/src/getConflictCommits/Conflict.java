package getConflictCommits;
import java.io.File;


public class Conflict {

	private String mergeCommit;
	private String fileName;
	private String leftConflict;
	private String rightConflict;
	private String commonAncestor;
	private String resolution;
	private String leftAncestorCommit, rightAncestorCommit;
	
	public Conflict() {
		
	}
	
	

	public Conflict(String mergeCommit, String leftConflict, String rightConflict,
			String commonAncestor, String resolution, File filePath, String leftAncestorCommit, String rightAncestorCommit) {
		this.mergeCommit = mergeCommit;
		this.leftConflict = leftConflict;
		this.rightConflict = rightConflict;
		this.commonAncestor = commonAncestor;
		this.resolution = resolution;
		this.leftAncestorCommit = leftAncestorCommit;
		this.rightAncestorCommit = rightAncestorCommit;
		
		fileName = filePath.getName();
		System.out.println("File name: " + fileName);
	}



	public String getLeftAncestorCommit() {
		return leftAncestorCommit;
	}



	public String getRightAncestorCommit() {
		return rightAncestorCommit;
	}



	public String getMergeCommit() {
		return mergeCommit;
	}



	public void setMergeCommit(String mergeCommit) {
		this.mergeCommit = mergeCommit;
	}



	public String getFileName() {
		return fileName;
	}



	public void setFileName(String fileName) {
		this.fileName = fileName;
	}



	public String getLeftConflict() {
		return leftConflict;
	}



	public void setLeftConflict(String leftConflict) {
		this.leftConflict = leftConflict;
	}



	public String getRightConflict() {
		return rightConflict;
	}



	public void setRightConflict(String rightConflict) {
		this.rightConflict = rightConflict;
	}



	public String getCommonAncestor() {
		return commonAncestor;
	}



	public void setCommonAncestor(String commonAncestor) {
		this.commonAncestor = commonAncestor;
	}



	public String getResolution() {
		return resolution;
	}



	public void setResolution(String resolution) {
		this.resolution = resolution;
	}


	
}
