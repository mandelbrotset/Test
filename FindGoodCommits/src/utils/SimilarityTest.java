package utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimilarityTest {

	@Test
	public void test() {
		float similarity;
		
		String[] block1 = {"test", "hehe", "haha"};
		String[] block2 = {"test", "hehe", "haha"};
		similarity = SimilarityDetection.compare(block1, block2);
		assertEquals(1.0f, similarity, 0.2f);
		
		String[] block3 = {"awdawdawdawda"};
		String[] block4 = {"drg", "grgr", "po"};
		similarity = SimilarityDetection.compare(block3, block4);
		assertEquals(0.0f, similarity, 0.2f);
	}

}
