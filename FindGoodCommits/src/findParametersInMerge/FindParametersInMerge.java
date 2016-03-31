package findParametersInMerge;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.Utils;

public class FindParametersInMerge {
	
	public static ArrayList<String> gatherData(String c1, String c2, String result) {
		ArrayList<String> c1resultplus;
		ArrayList<String> c2resultplus;
		ArrayList<String> c1resultspace;
		
		c1resultplus = getLines(c1, result, s -> s.startsWith("+"), 1);
		c2resultplus = getLines(c2, result, s -> s.startsWith("+"), 1);
		
		c1resultspace = getLines(c1, result, s -> s.startsWith(" "), 0);
		final ArrayList<String> c2resultspace = getLines(c2, result, s -> s.startsWith(" "), 0);
		
		c1resultplus.removeIf(line -> c2resultspace.contains(line));
		//c2resultplus.removeIf(line -> c1resultspace.contains(line));
		return c1resultplus;
	}
	
	public static ArrayList<String> getLines(String c, String result, java.util.function.Predicate<String> tester, int startIndex) {
		ArrayList<String> plus = new ArrayList<String>();
		try {
			BufferedReader br = Utils.readCommandOutput("diff -c " + c + " " + result);
			String line;
			while ((line = br.readLine()) != null) {
				if (tester.test(line)) {
					plus.add(line.trim().substring(startIndex).trim());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return plus;
	}
}
