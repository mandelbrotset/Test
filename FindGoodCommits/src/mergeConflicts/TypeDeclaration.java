package mergeConflicts;

import java.util.regex.PatternSyntaxException;

public class TypeDeclaration {
	public String type;
	public String identifier;
	public String modifiers;
	public boolean hasExtends;
	public boolean hasImplements;
	private String declarationLine;
	public static final String[] typeModifiers = { "public", "protected", "private", "abstract", "static", "final",
			"strictfp" };
	public static final String[] types = { "class", "interface", "enum" };

	public TypeDeclaration() {

	}

	public boolean parseDeclaration(String lines) throws NullPointerException, IndexOutOfBoundsException {
		String type;
		for (String line : lines.split("\n")) {
			if ((type = contains(line, types)) != null) {
				String identifier = parseTypeIdentifier(line, type); 
				if (identifier == null) {
					return false;
				}
				declarationLine = line;
				this.identifier = identifier;
				this.type = type;
				this.hasExtends = hasExtends(line);
				this.hasImplements = hasImplements(line);
				extractTypeModifiers(line);
				return true;
			}
		}
		return false;
	}

	private boolean hasExtends(String line) {
		return line.contains(" extends ");
	}
	
	private boolean hasImplements(String line) {
		return line.contains(" implements ");
	}

	private String contains(String line, String[] words) {
		for (String word : words) {
			if (line.contains(word + " "))
				return word;
		}
		return null;
	}

	private String parseTypeIdentifier(String line, String type)
			throws NullPointerException, IndexOutOfBoundsException {
		try {
			return line.split(type)[1].trim().split(" |\\{")[0];
		} catch (PatternSyntaxException | IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void extractTypeModifiers(String line) throws NullPointerException, IndexOutOfBoundsException {
		if (line.contains(this.identifier) && line.contains(this.type)) {
			this.modifiers = line.split(this.type)[0].trim();
		}
	}
	
	public String parseInterfaces() {
		try {
			return declarationLine.split(" implements ")[1].trim().split("\\{")[0].trim();
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public String parseExtendsClass() {
		try {
			String[] ex = declarationLine.split(" extends ");
			String ex1 = ex[1];
			String ex1trim = ex1.trim();
			String[] split2 = ex1trim.split(" |\\{");
			String name = split2[0];
			return name;
			//return declarationLine.split(" extends ")[1].trim().split(" |\\{")[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
}
