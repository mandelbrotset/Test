package mergeConflicts;

import static org.junit.Assert.*;

import org.junit.Test;

public class ImplementsListTest {

	@Test
	public void test() {
		ImplementsListChecker elc = new ImplementsListChecker();
		String left = "";
		String common = "";
		String right = "";

		// both edited implements declaration
		left = "public class Case implements B, O {";
		common = "public class Case implements A, G {";
		right = "public class Case implements C, P {";
		assertTrue(elc.checkClass(left, common, right));
		left = "public class Case implements D {";
		common = "public class Case implements A {";
		right = "public class Case implements D {";
		assertFalse(elc.checkClass(left, common, right));

		// both edited implements declaration
		left = "public class Case implements B, F {";
		common = "public class Case implements A {";
		right = "public class Case implements C {";
		assertTrue(elc.checkClass(left, common, right));
		left = "public class Case implements D {";
		common = "public class Case implements A {";
		right = "public class Case implements D {";
		assertFalse(elc.checkClass(left, common, right));

		// both added implements declaration
		left = "public class Case implements B {";
		common = "public class Case {";
		right = "public class Case implements C {";
		assertTrue(elc.checkClass(left, common, right));
		left = "public class Case implements D {";
		common = "public class Case {";
		right = "public class Case implements D {";
		assertFalse(elc.checkClass(left, common, right));

		// left edited and right removed implements declaration
		left = "public class Case implements B {";
		common = "public class Case implements A {";
		right = "public class Case {";
		assertTrue(elc.checkClass(left, common, right));
		left = "public class Case implements D {";
		common = "public class Case implements D {";
		right = "public class Case {";
		assertFalse(elc.checkClass(left, common, right));

		// right edited and left removed implements declaration
		left = "public class Case {";
		common = "public class Case implements A {";
		right = "public class Case implements C {";
		assertTrue(elc.checkClass(left, common, right));
		left = "public class Case {";
		common = "public class Case implements D {";
		right = "public class Case implements D {";
		assertFalse(elc.checkClass(left, common, right));
	}

}
