package mergeConflicts;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class ModifierListTest {

	@Test
	public void test() {
		
		
		ConflictClassifier classifier = new ConflictClassifier();
		
		String left = "public class Case {\n \n int x;";
		String common = "class Case {\n \n int x;";
		String right = "abstract class Case {\n \n int x;";
		
		ArrayList<ConflictClassifier.Classifier> classifiers = classifier.classify(left, common, right);
		
		assertEquals(1, classifiers.size());
		assertEquals(ConflictClassifier.Classifier.MODIFIER_LIST, classifiers.get(0));
		
		
	}
	
	

}
