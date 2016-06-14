import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Extractor {
	public final static String TEMP_FOLDER = "/tmp/";
	// public final static String CONFLICT_REPORT_PATH =
	// "/home/isak/Documents/Master/Test/the/Paola/elasticsearch/ResultData/elasticsearch/ConflictsReport.csv";
	// public final static String CONFLICT_REPORT_PATH =
	// "/home/isak/Documents/Master/Test/Paola/ResultData/atmosphere/ConflictsReport.csv";
	private String conflictReport;
	private WorkBookCreator wbc;
	private int totalConflictsAnalyzed = 0;
	
	public enum Keywords { TRY, IF, PRINT, LOG }

	public Extractor() {
		// analyzeConflictReport(CONFLICT_REPORT_PATH,
		// "/home/isak/Documents/Master/projects/hej");
		wbc = new WorkBookCreator("ExtractorHactorResult.xls");
		wbc.createSheet("Conflicts", "Project", "Type", "Merge Commit SHA", "Result Body", "Left SHA", "Left Body", "Left Date",
				"Right SHA", "Right Body", "Right Date", "Chosen",
				"Most recent", "Most \"if\"", "Most \"print\"", "Most \"log\"", "Most \"try\"", "Superset", "Intersection", "Result: Categories", "Chosen: Categories");
		 analyzeConflictReport("android-async-http");
		 analyzeConflictReport("android-best-practices");
		 analyzeConflictReport("Android-Universal-Image-Loader");
		 analyzeConflictReport("curator");
		 analyzeConflictReport("elasticsearch");
		 analyzeConflictReport("EventBus");
		 analyzeConflictReport("fresco");
		 analyzeConflictReport("guava");
		 analyzeConflictReport("iosched");
		 analyzeConflictReport("java-design-patterns");
		 analyzeConflictReport("leakcanary"); 
		 analyzeConflictReport("libgdx");
		 analyzeConflictReport("okhttp");
		 analyzeConflictReport("react-native");
		 analyzeConflictReport("retrofit");
		 analyzeConflictReport("RxJava");
		 analyzeConflictReport("SlidingMenu");
		 analyzeConflictReport("spring-framework");
		 analyzeConflictReport("storm");
		 analyzeConflictReport("zxing");
		 
		//analyzeConflictReport("android-async-http");
		wbc.writeToWorkbook();
		System.out.println("Total number of conflicts analyzed: " + totalConflictsAnalyzed);
	}

	private String getConflictReport(String project) {
		String reportPath = project + "/ResultData/" + project
				+ "/ConflictsReport.csv";
		if (new File("/home/isak").exists())
			return "/home/isak/Documents/Master/braresults/paolaboarba/" + reportPath;
		else if(new File("/home/patrik").exists())
			return "/home/patrik/Documents/Chalmers/5an/MasterThesis/braresults/paolaboarba/" + reportPath;
		
		return "/home/mediaserver/Documents/MasterThesis/braresults/paolaboarba/" + reportPath;
	}

	private String getPathToRepo(String project) {
		File file = new File("/home/isak");
		if (new File("/home/isak").exists())
			return "/home/isak/Documents/Master/projects/" + project;
		else if(new File("/home/patrik").exists())
			return "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/" + project;
		
		return "/home/mediaserver/Documents/MasterThesis/GHProject/" + project;
	}

	private void analyzeConflictReport(String project) {
		Path path = Paths.get(getConflictReport(project));
		if (!path.toFile().exists()) {
			System.out.println(path.toString() + " not found, skipping " + project);
			return;
		}
		if (!Paths.get(getConflictReport(project)).toFile().exists()) {
			System.out.println(getConflictReport(project) + " not found, skipping " + project);
			return;
		}
		System.out.println("analyzing " + project);
		ArrayList<String> conflictReportLines;
		try {
			conflictReportLines = (ArrayList<String>) Files.readAllLines(path);
			System.out.println("conflictReport has " + conflictReportLines.size() + " lines");
			removeLines(conflictReportLines);
			System.out.println("conflictReport has " + conflictReportLines.size() + " good lines");
			makeString(conflictReportLines);
			ArrayList<String> conflictStrings = splitConflicts();
			System.out.println("found " + conflictStrings.size() + " conflicts");
			totalConflictsAnalyzed += conflictStrings.size();
			filterConflicts(conflictStrings);
			System.out.println(conflictStrings.size() + " of them are good");
			System.out.println("creating conflicts");
			ArrayList<Conflict> conflicts = createConflicts(conflictStrings, getPathToRepo(project));
			System.out.println("analyzing..");
			analyzeConflicts(project, conflicts, getPathToRepo(project));
			System.out.println("done analyzing " + project);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Conflict> createConflicts(ArrayList<String> conflictStrings, String pathToRepo) {
		ArrayList<Conflict> conflicts = new ArrayList<Conflict>();
		for (String conflict : conflictStrings) {
			conflicts.add(new Conflict(conflict, pathToRepo));
		}
		return conflicts;
	}

	private void analyzeConflicts(String project, ArrayList<Conflict> conflicts, String pathToRepo) {
		try {
			// Clean the repo
			System.out.println("Cleaning repository...");
			Utils.readScriptOutput("gitClean " + pathToRepo, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Conflict conflict : conflicts) {
			System.out.println("\t analyzing " + conflict.getMergeCommitSha());
			analyzeConflict(project, conflict, pathToRepo);
		}
	}

	private void analyzeConflict(String project, Conflict conflict, String pathToRepo) {
		try {
			// System.out.println("1");
			Utils.readScriptOutput("analyzeResolution " + pathToRepo + " " + conflict.getMergeCommitSha() + " "
					+ TEMP_FOLDER + "result.java " + pathToRepo + "/" + conflict.getFilePath(), false);
			// System.out.println("2");
			ArrayList<String> resultFile = (ArrayList<String>) Files
					.readAllLines(Paths.get(TEMP_FOLDER + "result.java"));
			// System.out.println("3");
			ArrayList<String> resultFunction = FunctionParser.extractFunction(resultFile, conflict.getFunctionName(),
					conflict.getParameterTypes());
			// System.out.println("4");
			if (resultFunction == null) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			// System.out.println("5");
			for (String line : resultFunction) {
				sb.append(line + "\n");
			}
			// System.out.println("6");
			String resultBody = sb.toString();
			conflict.setResultBody(resultBody);
			// System.out.println("7");
			conflict.setResult();
			if(conflict.getResult() != Conflict.Result.BOTH)
				wbc.addRow(project, conflict.getType(), conflict.getMergeCommitSha(), conflict.getResultBody(), conflict.getLeftSha(), conflict.getLeftBody(),
						conflict.getLeftDate(), conflict.getRightSha(), conflict.getRightBody(), conflict.getRightDate(), conflict.getResult().toString(),
						conflict.mostRecent().toString(), conflict.hasMoreOf("if").toString(), conflict.hasMoreOf("print").toString(), 
						conflict.hasMoreOf("log").toString(), conflict.hasMoreOf("try").toString(), getPutteSup(conflict), getPutteInt(conflict), conflict.getCategoryList(), chosenProperties(conflict));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getPutteInt(Conflict conflict) {
		if (conflict.isLeftIntersection() && conflict.isRightIntersection())
			return "LEFT, RIGHT";
		else if (conflict.isLeftIntersection())
			return "LEFT";
		else if(conflict.isRightIntersection())
			return "RIGHT";
		else
			return "NONE";
	}
	
	private String getPutteSup(Conflict conflict) {
		if (conflict.isLeftSuperset() && conflict.isRightSuperset())
			return "LEFT, RIGHT";
		else if (conflict.isLeftSuperset())
			return "LEFT";
		else if(conflict.isRightSuperset())
			return "RIGHT";
		else
			return "NONE";
	}
	
	private String chosenProperties(Conflict conflict) { //Keywords and categories of the chosen one
		Conflict.Result result = conflict.getResult();
		if(result == Conflict.Result.BOTH || result == Conflict.Result.NONE) return "";
		Keywords[] keywordList = { Keywords.IF, Keywords.LOG, Keywords.PRINT, Keywords.TRY };
		StringBuilder sb = new StringBuilder(conflict.getCategoryList());
		
		String[] words = { "if", "log", "print", "try" };
		
		for(int i = 0; i < words.length; i++) {
			if(conflict.hasMoreOf(keywordList[i].toString().toLowerCase()) == result) {
				if(sb.length() > 0)
					sb.append(", ");
				
				sb.append(keywordList[i].toString());
			}
		}
		
		return sb.toString();
	}

	private void makeString(ArrayList<String> lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}
		conflictReport = sb.toString();
	}

	private ArrayList<String> splitConflicts() {
		ArrayList<String> conflicts = new ArrayList<String>();
		String[] conflictsList = conflictReport.split("############## CONFLICT ##############");
		for (String conflict : conflictsList) {
			conflicts.add(conflict);
		}
		return conflicts;
	}

	private void filterConflicts(ArrayList<String> conflicts) {
		// TODO: fix
		// ~~FSTMerge~~ ##FSTMerge## ##FSTMerge##
		conflicts.removeIf(
				s -> !s.contains("Conflict type: SameSignatureCM") && !s.contains("Conflict type: EditSameMC"));
		conflicts.removeIf(s -> !s.contains("##FSTMerge##") && !s.contains("<<<<<"));

		conflicts.removeIf(s -> s.contains("~~FSTMerge~~ ##FSTMerge##")); // den
																			// ska
																			// finnas
																			// i
																			// left
		conflicts.removeIf(s -> s.contains("; ##FSTMerge##"));
		conflicts.removeIf(s -> s.contains(";\nFilePath"));
		conflicts.removeIf(s -> skadentasbort(s));
		conflicts.removeIf(s -> containsMoreThanOneConflict(s));

		// conflicts.removeIf(s -> true);
		// conflicts.removeIf(s ->
		// s.split("<<<<<<<")[1].split("\\|\\|\\|\\|\\|\\|\\|")[0].split("\n").length
		// == 1);

		// conflicts.removeIf(s -> s.contains("##FSTMerge##")); //den ska finnas
		// i right
		// System.out.println("What the fucc???");
		// }

	}
	
	private boolean containsMoreThanOneConflict(String s) {
		if(s.contains("<<<<<<<")) {
			return s.split("<<<<<<<").length > 2;
		} else if(s.contains("~~ FSTMerge ~~")) {
			return s.split("~~ FSTMerge ~~").length > 2;
		}
		
		return false;
	}

	private boolean skadentasbort(String s) {
		try {
			if (s.contains("<<<<<<<")) {
				String putte = s.split("<<<<<<<")[1].split("\\|\\|\\|\\|\\|\\|\\|")[0];
				String[] hejputte = putte.split("\n");
				int x = hejputte.length;
				return (x == 1) || s.split("=======")[1].split(">>>>>>>")[0].split("\n").length == 0;
			} else if (s.contains("##FSTMerge##")) {
				String putte = s.split("##FSTMerge##")[2].split("File path:")[0];
				String[] hejputte = putte.split("\n");
				int x = hejputte.length;
				return x == 0;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	private void removeLines(ArrayList<String> lines) {
		lines.removeIf(s -> s.startsWith("Revision: "));
		lines.removeIf(s -> s.startsWith("===================="));
	}


}
