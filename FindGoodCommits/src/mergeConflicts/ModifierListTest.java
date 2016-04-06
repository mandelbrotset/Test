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
		
		left = "public class Hej {\n \n int x;";
		common = "class Hej {\n \n int x;";
		right = "abstract class Hejsan {\n \n int x;";
		assertFalse(mlc.checkClass(left, common, right));
		
		left = "public class IndexingMemoryController extends AbstractLifecycleComponent<IndexingMemoryController> {";
		common = "import java.util.*;\nimport java.util.concurrent.ScheduledFuture;\npublic class IndexingMemoryController extends AbstractLifecycleComponent<IndexingMemoryController> implements IndexEventListener {";
		right = "import java.util.ArrayList;\nimport java.util.EnumSet;\nimport java.util.List;\nimport java.util.concurrent.ScheduledFuture;\n\npublic class IndexingMemoryController extends AbstractLifecycleComponent<IndexingMemoryController> implements IndexEventListener {";
		assertFalse(mlc.checkClass(left, common, right));
	}
	
	

}
