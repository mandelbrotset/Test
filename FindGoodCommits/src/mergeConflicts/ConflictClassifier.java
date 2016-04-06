package mergeConflicts;

import java.util.ArrayList;

public class ConflictClassifier {
	
	private final String[] fieldModifiers = { "public", "protected", "private", "transient", "volatile", "static", "final" };

	public enum Classifier {
		EDIT_SAME_MC, SAME_SIGNATURE_CM, EDIT_SAME_FD, ADD_SAME_FD, MODIFIER_LIST, IMPLEMENTS_LIST, EXTENDS_LIST, FEFAULT_VALUE_A
	}

	public ArrayList<Classifier> classify(String left, String common, String right) {
		ArrayList<Classifier> classifiers = new ArrayList<Classifier>();
		
		checkClass(ModifierListChecker.class, left, common, right, classifiers);
		checkClass(ExtendsListChecker.class, left, common, right, classifiers);
		
		return classifiers;
	}
	
	private void checkClass(Class<? extends ClassChecker> checker, String left, String common, String right, ArrayList<Classifier> classifiers) {
		try {
			ClassChecker classChecker = checker.newInstance();
			if (classChecker.checkClass(left, common, right))
				classifiers.add(classChecker.getConflictClass());
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
