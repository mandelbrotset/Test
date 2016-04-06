package mergeConflicts;

import static org.junit.Assert.*;

import org.junit.Test;

public class ModifierListTest {

	@Test
	public void test() {
		ModifierListChecker mlc = new ModifierListChecker();
		String left = "";
		String common = "";
		String right = "";
		
		left = "public class Case {\n \n int x;";
		common = "class Case {\n \n int x;";
		right = "abstract class Case {\n \n int x;";
		assertTrue(mlc.checkClass(left, common, right));
		
		left = "public class. Case {\n \n int x;";
		common = "class. Case {\n \n int x;";
		right = "abstract class. Case {\n \n int x;";
		assertFalse(mlc.checkClass(left, common, right));
		
		left = "public interface Case {\n \n int x;";
		common = "interface Case {\n \n int x;";
		right = "abstract interface Case {\n \n int x;";
		assertTrue(mlc.checkClass(left, common, right));
	}
	
	

}
