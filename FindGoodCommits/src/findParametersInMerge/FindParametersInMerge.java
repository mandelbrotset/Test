package findParametersInMerge;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import utils.Utils;

public class FindParametersInMerge {
	private String c1 = "/home/isak/test/diff/c1";
	private String c2 = "/home/isak/test/diff/c2";
	private String result = "/home/isak/test/diff/result";
	
	private ArrayList<String> c1resultplus = new ArrayList<String>();
	private ArrayList<String> c2resultplus = new ArrayList<String>();
	private ArrayList<String> c1resultspace = new ArrayList<String>();
	private ArrayList<String> c2resultspace = new ArrayList<String>();
	
	
	public FindParametersInMerge() {
		gatherData();
	}
	
	private void gatherData() {
		c1resultplus = getLines(c1, result, s -> s.startsWith("+"), 1);
		c2resultplus = getLines(c2, result, s -> s.startsWith("+"), 1);
		
		c1resultspace = getLines(c1, result, s -> s.startsWith(" "), 0);
		c2resultspace = getLines(c2, result, s -> s.startsWith(" "), 0);
		
		c1resultplus.removeIf(line -> c2resultspace.contains(line));
		c2resultplus.removeIf(line -> c1resultspace.contains(line));
		
		System.out.println("c1rp:");
		printList(c1resultplus);
		System.out.println("c2rp:");
		printList(c2resultplus);
	}
	
	private void printList(ArrayList<String> list) {
		for (String line : list) {
			System.out.println(line);
		}
	}
	
	private ArrayList<String> getLines(String c, String result, java.util.function.Predicate<String> tester, int startIndex) {
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
