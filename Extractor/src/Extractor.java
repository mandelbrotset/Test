import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class Extractor {
	public final static String TEMP_FOLDER = "/tmp/";
	//public final static String CONFLICT_REPORT_PATH = "/home/isak/Documents/Master/Test/the/Paola/elasticsearch/ResultData/elasticsearch/ConflictsReport.csv";
	//public final static String CONFLICT_REPORT_PATH = "/home/isak/Documents/Master/Test/Paola/ResultData/atmosphere/ConflictsReport.csv";
	private String conflictReport;
	private WorkBookCreator wbc;
	
	public Extractor() {
		//analyzeConflictReport(CONFLICT_REPORT_PATH, "/home/isak/Documents/Master/projects/hej");
		wbc = new WorkBookCreator("ExtractorHactorResult.xls");
		wbc.createSheet("Conflicts", "Project", "Merge Commit SHA", "Result Body", "Left SHA", "Left Body", "Left Date", "Right SHA", "Right Body", "Right Date");
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
		analyzeConflictReport("atmosphere");
		
		wbc.writeToWorkbook();
	}
	
	private String getConflictReport(String project) {
		File file = new File("/home/isak");
		if(file.exists())
			return "/home/isak/Documents/Master/braresults/paolaboarba/" + project + "/ResultData/" + project + "/ConflictsReport.csv";
		
		return "/home/patrik/Documents/Chalmers/5an/MasterThesis/braresults/paolaboarba/" + project + "/ResultData/" + project + "/ConflictsReport.csv";
	}
	
	private String getPathToRepo(String project) {
		File file = new File("/home/isak");
		if(file.exists())
			return "/home/isak/Documents/Master/projects/" + project;
		
		return "/home/patrik/Documents/Chalmers/5an/MasterThesis/GHProject/" + project;
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
		for (Conflict conflict : conflicts) {
			System.out.println("\t analyzing " + conflict.getMergeCommitSha());
			analyzeConflict(project, conflict, pathToRepo);
		}
	}
	
	private void analyzeConflict(String project, Conflict conflict, String pathToRepo) {
		try {
			//System.out.println("1");
			Utils.readScriptOutput("analyzeResolution " + pathToRepo + " " + conflict.getMergeCommitSha() + " " + TEMP_FOLDER + "result.java " + pathToRepo + "/" + conflict.getFilePath(), false);
			//System.out.println("2");
			ArrayList<String> resultFile = (ArrayList<String>)Files.readAllLines(Paths.get(TEMP_FOLDER + "result.java"));
			//System.out.println("3");
			ArrayList<String> resultFunction = FunctionParser.extractFunction(resultFile, conflict.getFunctionName(), conflict.getParameterTypes());
			//System.out.println("4");
			if (resultFunction == null) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			//System.out.println("5");
			for(String line : resultFunction) {
				sb.append(line + "\n");
			}
			//System.out.println("6");
			String resultBody = sb.toString();
			//System.out.println("7");
			wbc.addRow(project, conflict.getMergeCommitSha(), resultBody, conflict.getLeftSha(), conflict.getLeftBody(), conflict.getLeftDate(), conflict.getRightSha(), conflict.getRightBody(), conflict.getRightDate());
			
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

	/*private void writeFile() {
		File file = new File("bra.csv");
		try {analyzeConflictReport("/home/isak/Documents/Master/braresults/paolaboarba/EventBus/ResultData/EventBus/ConflictsReport.csv", "/home/isak/Documents/Master/projects/EventBus");
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			StringBuilder sb = new StringBuilder();
			for (String conflict : conflicts) {
				sb.append(conflict);
			}
			bw.write(sb.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}*/
	
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
		//TODO: fix
		//~~FSTMerge~~ ##FSTMerge## ##FSTMerge## 
		conflicts.removeIf(s -> !s.contains("Conflict type: SameSignatureCM") && !s.contains("Conflict type: EditSameMC"));
		//conflicts.removeIf(s -> s.contains("##FSTMerge##"));
		//conflicts.removeIf(s -> !s.contains("<<<<<"));
		//if (conflicts.removeIf(s -> s.contains("~~FSTMerge~~ ##FSTMerge##"))) {//den ska finnas i left
			
		//System.out.println("What the fucc???");
		//}
		
	}
	
	private void removeLines(ArrayList<String> lines) {
		lines.removeIf(s -> s.startsWith("Revision: "));
		lines.removeIf(s -> s.startsWith("===================="));
	}

	public static int countNumberOf(String body, String word) {
		if (!body.contains(word)) return 0; 
		int count = 0;
		for (int i = 0; i < body.length()-word.length(); i++) {
			if (body.substring(i, i+word.length()).equals(word))
				count++;
		}
		return count;
	}
	
	
	
}
