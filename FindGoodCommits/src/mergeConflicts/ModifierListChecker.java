package mergeConflicts;

import mergeConflicts.ConflictClassifier.Classifier;

public class ModifierListChecker implements ClassChecker {

	@Override
	public boolean checkClass(String left, String common, String right) {
		TypeDeclaration leftTD = new TypeDeclaration();
		TypeDeclaration commonTD = new TypeDeclaration();
		TypeDeclaration rightTD = new TypeDeclaration();
		try {
			if (leftTD.parseDeclaration(left) && rightTD.parseDeclaration(right) && commonTD.parseDeclaration(common)) {
				if (leftTD.identifier.equals(commonTD.identifier) && leftTD.identifier.equals(rightTD.identifier)) {
					if (!leftTD.modifiers.equals(commonTD.modifiers) && !rightTD.modifiers.equals(commonTD.modifiers)) {
						if (!leftTD.modifiers.equals(rightTD.modifiers)) {
							return true;
						}
					}
				}
			}
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			return false;
		}
		return false;
	}

	@Override
	public Classifier getConflictClass() {
		return Classifier.MODIFIER_LIST;
	}
}
