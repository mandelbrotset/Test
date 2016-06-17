/*
Copyright (C) 2016 Isak Eriksson, Patrik Wållgren

This file is part of ResolutionsAnalyzer.

    ResolutionsAnalyzer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ResolutionsAnalyzer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ResolutionsAnalyzer.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ResolutionsAnalyzer {
	public final static String TEMP_FOLDER = "/tmp/";
	private String conflictReport;
	private WorkBookCreator wbc;
	private int totalConflictsAnalyzed = 0;
	private String pathToRepos;
	private String pathToReports;
	
	public enum Keywords { TRY, IF, PRINT, LOG }

	public ResolutionsAnalyzer(String pathToRepos, String pathToReports) {
		this.pathToReports = pathToReports;
		this.pathToRepos = pathToRepos;
		wbc = new WorkBookCreator("Results.xls");
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
		 
		wbc.writeToWorkbook();
		System.out.println("Total number of conflicts analyzed: " + totalConflictsAnalyzed);
	}

	private String getConflictReport(String project) {
		return pathToReports + project + "/ResultData/" + project + "/ConflictsReport.csv";
	}

	private String getPathToRepositories(String project) {
		return pathToRepos + project;
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
			removeLines(conflictReportLines);
			makeString(conflictReportLines);
			ArrayList<String> conflictStrings = splitConflicts();
			totalConflictsAnalyzed += conflictStrings.size();
			filterConflicts(conflictStrings);
			ArrayList<Conflict> conflicts = createConflicts(conflictStrings, getPathToRepositories(project));
			System.out.println("analyzing..");
			analyzeConflicts(project, conflicts, getPathToRepositories(project));
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
			Utils.readScriptOutput("analyzeResolution " + pathToRepo + " " + conflict.getMergeCommitSha() + " "
					+ TEMP_FOLDER + "result.java " + pathToRepo + "/" + conflict.getFilePath(), false);
			ArrayList<String> resultFile = (ArrayList<String>) Files
					.readAllLines(Paths.get(TEMP_FOLDER + "result.java"));
			ArrayList<String> resultFunction = FunctionParser.extractFunction(resultFile, conflict.getFunctionName(),
					conflict.getParameterTypes());
			if (resultFunction == null) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			for (String line : resultFunction) {
				sb.append(line + "\n");
			}
			String resultBody = sb.toString();
			conflict.setResultBody(resultBody);
			conflict.setResult();
			if(conflict.getResult() != Conflict.Result.BOTH)
				wbc.addRow(project, conflict.getType(), conflict.getMergeCommitSha(), conflict.getResultBody(), conflict.getLeftSha(), conflict.getLeftBody(),
						conflict.getLeftDate(), conflict.getRightSha(), conflict.getRightBody(), conflict.getRightDate(), conflict.getResult().toString(),
						conflict.mostRecent().toString(), conflict.hasMoreOf("if").toString(), conflict.hasMoreOf("print").toString(), 
						conflict.hasMoreOf("log").toString(), conflict.hasMoreOf("try").toString(), getSuperset(conflict), getIntersection(conflict), conflict.getCategoryList(), chosenProperties(conflict));

		} catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	private String getIntersection(Conflict conflict) {
		if (conflict.isLeftIntersection() && conflict.isRightIntersection())
			return "LEFT, RIGHT";
		else if (conflict.isLeftIntersection())
			return "LEFT";
		else if(conflict.isRightIntersection())
			return "RIGHT";
		else
			return "NONE";
	}
	
	private String getSuperset(Conflict conflict) {
		if (conflict.isLeftSuperset() && conflict.isRightSuperset())
			return "LEFT, RIGHT";
		else if (conflict.isLeftSuperset())
			return "LEFT";
		else if(conflict.isRightSuperset())
			return "RIGHT";
		else
			return "NONE";
	}
	
	private String chosenProperties(Conflict conflict) { //Keywords and categories of the chosen version
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
		conflicts.removeIf(s -> !s.contains("Conflict type: SameSignatureCM") && !s.contains("Conflict type: EditSameMC"));
		conflicts.removeIf(s -> !s.contains("##FSTMerge##") && !s.contains("<<<<<"));
		conflicts.removeIf(s -> s.contains("~~FSTMerge~~ ##FSTMerge##"));
		conflicts.removeIf(s -> s.contains("; ##FSTMerge##"));
		conflicts.removeIf(s -> s.contains(";\nFilePath"));
		conflicts.removeIf(s -> notRealConflict(s));
		conflicts.removeIf(s -> containsMoreThanOneConflict(s));
	}
	
	private boolean containsMoreThanOneConflict(String s) {
		if(s.contains("<<<<<<<")) {
			return s.split("<<<<<<<").length > 2;
		} else if(s.contains("~~ FSTMerge ~~")) {
			return s.split("~~ FSTMerge ~~").length > 2;
		}
		return false;
	}

	private boolean notRealConflict(String s) {
		try {
			if (s.contains("<<<<<<<")) {
				String s1 = s.split("<<<<<<<")[1].split("\\|\\|\\|\\|\\|\\|\\|")[0];
				String[] s2 = s1.split("\n");
				int x = s2.length;
				return (x == 1) || s.split("=======")[1].split(">>>>>>>")[0].split("\n").length == 0;
			} else if (s.contains("##FSTMerge##")) {
				String s1 = s.split("##FSTMerge##")[2].split("File path:")[0];
				String[] s2 = s1.split("\n");
				int x = s2.length;
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
