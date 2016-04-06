package mergeConflicts;

import mergeConflicts.ConflictClassifier.Classifier;

public class SameSignatureCMChecker implements ClassChecker {

	private boolean hasDifferentBodies(String leftBody, String rightBody) {
		return leftBody.equals(rightBody);
	}
	
	@Override
	public Classifier getConflictClass() {
		return Classifier.SAME_SIGNATURE_CM;
	}

	@Override
	public boolean checkClass(String left, String common, String right) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
