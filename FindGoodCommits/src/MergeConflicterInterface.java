import java.io.File;


public interface MergeConflicterInterface {
	public boolean doMerge();
	public File getLeftConflict();
	public File getRightConflict();
	public File getCommonAncestor();
	public File getResolution();
}
