package tests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class SuperSetTest {

	@Test
	public void test() {
		String rightFunc = "        protected boolean doEquals(DummyQueryBuilder other) {\n" +
"            return true;\n" +
"        }";
		
		String leftFunc = "        protected boolean doEquals(DummyQueryBuilder other) {\n" +
				"			 int hej = 5;\n" +
				"            return true;\n" +
				"        }";
		
		String resultFunc = "        protected boolean doEquals(DummyQueryBuilder other) {\n" +
				"			 int hej = 5;\n" +
				"            return true;\n" +
				"        }";
		
		HashSet<String> leftLines = new HashSet<String>(Arrays.asList(leftFunc.split("\n")));
		HashSet<String> rightLines = new HashSet<String>(Arrays.asList(rightFunc.split("\n")));
		HashSet<String> resultLines = new HashSet<String>(Arrays.asList(resultFunc.split("\n")));
		
		assertEquals(true, isSuperset(leftLines, rightLines, resultLines));
		
		}

	private boolean isSuperset(HashSet<String> leftLines, HashSet<String> rightLines, HashSet<String> resultLines) {
		HashSet<String> lines = new HashSet<String>();
		lines.addAll(leftLines);
		lines.addAll(rightLines);
		
		return resultLines.containsAll(lines);
	}
	
}
