package mergeConflicts;

import mergeConflicts.ConflictClassifier.Classifier;

public class ImplementsListChecker implements ClassChecker {

	@Override
	public Classifier getConflictClass() {
		return Classifier.IMPLEMENTS_LIST;
	}

	@Override
	public boolean checkClass(String left, String common, String right) {
		TypeDeclaration leftTD = new TypeDeclaration();
		TypeDeclaration commonTD = new TypeDeclaration();
		TypeDeclaration rightTD = new TypeDeclaration();
		
		if (leftTD.parseDeclaration(left) && commonTD.parseDeclaration(common) && rightTD.parseDeclaration(right)) {
			if (leftTD.identifier.equals(commonTD.identifier) && leftTD.identifier.equals(rightTD.identifier)) {
				String leftInterfaces = leftTD.parseInterfaces();
				String commonInterfaces = commonTD.parseInterfaces();
				String rightInterfaces = rightTD.parseInterfaces();
				
				if (leftTD.hasImplements && commonTD.hasImplements && rightTD.hasImplements) {//both edited implements declaration
					if (!leftInterfaces.equals(rightInterfaces)) {//different edits
						if (!leftInterfaces.equals(commonInterfaces) && !rightInterfaces.equals(commonInterfaces)) {//both different from common
							return true;
						}
					}
				}
				if (leftTD.hasImplements && !commonTD.hasImplements && rightTD.hasImplements) {//both added extends declaration
					if (!leftInterfaces.equals(rightInterfaces)) {//different declarations
						return true;
					}
				}
				if (leftTD.hasImplements && commonTD.hasImplements && !rightTD.hasImplements) {//left edited and right removed extends declaration
					if (!leftInterfaces.equals(commonInterfaces)) {//left is different from common
						return true;
					}
				}
				if (!leftTD.hasImplements && commonTD.hasImplements && rightTD.hasImplements) {//right edited and left removed extends declaration
					if (!rightInterfaces.equals(commonInterfaces)) {//right is different from common
						return true;
					}
				}
			}
		}
		return false;
	}
}
