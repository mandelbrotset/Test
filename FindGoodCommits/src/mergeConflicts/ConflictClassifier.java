package mergeConflicts;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

public class ConflictClassifier {
	private final String[] typeModifiers = { "public", "protected", "private", "abstract", "static", "final", "strictfp" };
	private final String[] types = { "class", "interface", "enum" };

	private final String[] fieldModifiers = { "public", "protected", "private", "transient", "volatile", "static", "final" };

	public enum Classifier {
		EDIT_SAME_MC, SAME_SIGNATURE_CM, EDIT_SAME_FD, ADD_SAME_FD, MODIFIER_LIST, IMPLEMENTS_LIST, EXTENDS_LIST, FEFAULT_VALUE_A
	}

	public ArrayList<Classifier> classify(String left, String common, String right) {
		ArrayList<Classifier> classifiers = new ArrayList<Classifier>();

		if (isModifierList(left, common, right)) {
			classifiers.add(Classifier.MODIFIER_LIST);
		}

		return classifiers;
	}

	private boolean isModifierList(String left, String common, String right) {
		TypeDeclaration leftTypeDeclaration = new TypeDeclaration();
		TypeDeclaration commonTypeDeclaration = new TypeDeclaration();
		TypeDeclaration rightTypeDeclaration = new TypeDeclaration();
		try {
			extractIdentifierAndType(left, leftTypeDeclaration);
			extractIdentifierAndType(common, commonTypeDeclaration);
			extractIdentifierAndType(right, rightTypeDeclaration);
			
			if (leftTypeDeclaration.identifier.equals(commonTypeDeclaration.identifier) && leftTypeDeclaration.identifier.equals(rightTypeDeclaration.identifier)) {
				extractTypeModifiers(left, leftTypeDeclaration);
				extractTypeModifiers(common, commonTypeDeclaration);
				extractTypeModifiers(right, rightTypeDeclaration);
				
				if (!leftTypeDeclaration.modifiers.equals(commonTypeDeclaration.modifiers) && !rightTypeDeclaration.modifiers.equals(commonTypeDeclaration.modifiers)) {
					if (!leftTypeDeclaration.modifiers.equals(rightTypeDeclaration.modifiers)) {
						return true;
					}
				}
			}
		} catch (NullPointerException npe) {
			
		}
		return false;
	}
	
	private void extractTypeModifiers(String lines, TypeDeclaration typeDeclaration) {
		for (String line : lines.split("\n")) {
			if (line.contains(typeDeclaration.identifier) && line.contains(typeDeclaration.type)) {
				typeDeclaration.modifiers = line.split(typeDeclaration.type)[0].trim();
			}
		}
	}

	private class TypeDeclaration {
		public String type;
		public String identifier;
		public String modifiers;
	}
	
	private void extractIdentifierAndType(String lines, TypeDeclaration typeDeclaration) throws NullPointerException {
		String type;
		for (String line : lines.split("\n")) {
			if ((type = contains(line, types)) != null) {
				String identifier = parseTypeIdentifier(line, type); 
				if (identifier == null) {
					throw new NullPointerException();
				}
				typeDeclaration.identifier = identifier;
				typeDeclaration.type = type;
				return;
			}
		}
		throw new NullPointerException();
	}

	private String contains(String line, String[] words) {
		for (String word : words) {
			if (line.contains(word))
				return word;
		}
		return null;
	}

	private String parseTypeIdentifier(String line, String type) {
		try {
			return line.split(type)[1].trim().split(" |\\{")[0];
		} catch (PatternSyntaxException | IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return null;
	}
}
