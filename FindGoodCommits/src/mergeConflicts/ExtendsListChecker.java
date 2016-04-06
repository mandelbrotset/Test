package mergeConflicts;

import mergeConflicts.ConflictClassifier.Classifier;

public class ExtendsListChecker implements ClassChecker {

	@Override
	public Classifier getConflictClass() {
		return Classifier.EXTENDS_LIST;
	}

	@Override
	public boolean checkClass(String left, String common, String right) {
		TypeDeclaration leftTD = new TypeDeclaration();
		TypeDeclaration commonTD = new TypeDeclaration();
		TypeDeclaration rightTD = new TypeDeclaration();
		
		if (leftTD.parseDeclaration(left) && commonTD.parseDeclaration(common) && rightTD.parseDeclaration(right)) {
			if (leftTD.identifier.equals(commonTD.identifier) && leftTD.identifier.equals(rightTD.identifier)) {
				String leftExtend = leftTD.parseExtendsClass();
				String commonExtend = commonTD.parseExtendsClass();
				String rightExtend = rightTD.parseExtendsClass();
				if (leftTD.hasExtends && commonTD.hasExtends && rightTD.hasExtends) {//both edited extends declaration
					if (!leftExtend.equals(rightExtend)) {//different edits
						if (!leftExtend.equals(commonExtend) && !rightExtend.equals(commonExtend)) {//both different from common
							return true;
						}
					}
				}
				if (leftTD.hasExtends && !commonTD.hasExtends && rightTD.hasExtends) {//both added extends declaration
					if (!leftExtend.equals(rightExtend)) {//different declarations
						return true;
					}
				}
				if (leftTD.hasExtends && commonTD.hasExtends && !rightTD.hasExtends) {//left edited and right removed extends declaration
					if (!leftExtend.equals(commonExtend)) {//left is different from common
						return true;
					}
				}
				if (!leftTD.hasExtends && commonTD.hasExtends && rightTD.hasExtends) {//right edited and left removed extends declaration
					if (!rightExtend.equals(commonExtend)) {//right is different from common
						return true;
					}
				}
			}
		}
		return false;
	}
}
