package utils;

import java.util.ArrayList;
import java.util.function.Function;

public class SimilarityDetection {
	
	public static float compare(String[] block1, String[] block2) {
		float sim1 = compareNrOfRows(block1, block2);
		float sim2 = compareNrOfCharacters(block1, block2);
		float sim3 = compareWords(block1, block2);
		float similarity = 0.2f*sim1 + 0.1f*sim2 + 0.7f*sim3;
		return similarity;
	}
	
	private static float compareNrOfRows(String[] block1, String[] block2) {
		float b1 = block1.length;
		float b2 = block2.length;
		float diff;
		float similarity;
		return compareInts(b1, b2);
	}
	
	private static float compareNrOfCharacters(String[] block1, String[] block2) {
		int b1 = 0;
		int b2 = 0;
		for (String row : block1) {
			b1 += row.length();
		}
		for (String row : block2) {
			b2 += row.length();
		}
		return compareInts(b1, b2);
	}
	
	private static float compareInts(float i1, float i2) {
		if (i2 > i1) {
			return 1-(i2-i1)/i2;
		} else {
			return 1-(i1-i1)/i1;
		}
	}
	
	private static ArrayList<String> getWords(String[] block) {
		ArrayList<String> blockWords = new ArrayList<String>();
		for (String row : block) {
			String[] words = row.split("( ,.,\\(,\\),\t)");
			for (String word : words) {
				blockWords.add(word);
			}
		}
		return blockWords;
	}
	
	private static float compareWords(String[] block1, String[] block2) {
		ArrayList<String> block1Words = getWords(block1);
		ArrayList<String> block2Words = getWords(block2);
		int matches1 = 0;
		int matches2 = 0;
		
		for (String word : block1Words) {
			if (block2Words.contains(word)) matches1++;
		}
		for (String word : block2Words) {
			if (block1Words.contains(word)) matches2++;
		}
		
		int diff1 = block2Words.size() - matches1;
		float similarity1 = 1 - diff1 / block2Words.size();
		int diff2 = block1Words.size() - matches2;
		float similarity2 = 1 - diff2 / block1Words.size();	
		float similarity = (similarity2 + similarity1) / 2; 
		return similarity;
	}
	
	private static float compareRows(String[] block1, String[] block2) {
		String[] trimmed1 = doOnBlock(block1, s -> s.trim().toLowerCase());
		String[] trimmed2 = doOnBlock(block2, s -> s.trim().toLowerCase());
		int matches1 = 0;
		
//		for (String row : trimmed1) {
			//if (trimmed2.)
	//	}
		
		return 0;
	}
	
	private static String[] doOnBlock(String[] block, Function<String, String> operation) {
		String[] result = new String[block.length];
		for (int i = 0; i < block.length; i++) {
			result[i] = operation.apply(block[i]);
		}
		return result;
	}
}
