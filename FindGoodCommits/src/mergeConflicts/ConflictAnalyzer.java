package mergeConflicts;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import utils.WorkBookCreator;

public class ConflictAnalyzer {
	private WorkBookCreator wbc;
	private String cft;
	
	public ConflictAnalyzer() {
		wbc = new WorkBookCreator("ConflictInformation.xls");
		createSheets();
	}
	
	private void createSheets() {
		String name = new StringBuilder(new StringBuilder(cft).reverse().toString().split("/")[0]).reverse().toString();
		wbc.createSheet(name, "Commit SHA-1", "Message", "Pull request", "File name", "Size", "Conflicting lines", "Conflict Pattern", "Resolution Pattern");
	}

	private void gatherConflicts() {
		
	}
	
}
