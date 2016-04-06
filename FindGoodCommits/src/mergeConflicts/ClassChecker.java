package mergeConflicts;

public interface ClassChecker {
	public ConflictClassifier.Classifier getConflictClass();
	public boolean checkClass(String left, String common, String right);
}
