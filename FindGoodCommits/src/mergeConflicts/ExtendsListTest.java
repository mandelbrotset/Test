package mergeConflicts;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExtendsListTest {

	@Test
	public void test() {
		ExtendsListChecker elc = new ExtendsListChecker();
		String left = "";
		String common = "";
		String right = "";
		
		//both edited extends declaration
		left =   "public class Case extends B {";
		common = "public class Case extends A {";
		right =  "public class Case extends C {";
		assertTrue(elc.checkClass(left, common, right));
		left =   "public class Case extends D {";
		common = "public class Case extends A {";
		right =  "public class Case extends D {";
		assertFalse(elc.checkClass(left, common, right));
		
		//both added extends declaration
		left =   "public class Case extends B {";
		common = "public class Case {";
		right =  "public class Case extends C {";
		assertTrue(elc.checkClass(left, common, right));
		left =   "public class Case extends D {";
		common = "public class Case {";
		right =  "public class Case extends D {";
		assertFalse(elc.checkClass(left, common, right));
		
		//left edited and right removed extends declaration
		left =   "public class Case extends B {";
		common = "public class Case extends A {";
		right =  "public class Case {";
		assertTrue(elc.checkClass(left, common, right));
		left =   "public class Case extends D {";
		common = "public class Case extends D {";
		right =  "public class Case {";
		assertFalse(elc.checkClass(left, common, right));
		
		//right edited and left removed extends declaration
		left =   "public class Case {";
		common = "public class Case extends A {";
		right =  "public class Case extends C {";
		assertTrue(elc.checkClass(left, common, right));
		left =   "public class Case {";
		common = "public class Case extends D {";
		right =  "public class Case extends D {";
		assertFalse(elc.checkClass(left, common, right));
	}

}
