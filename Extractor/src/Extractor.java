import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Extractor {
	public final static String TEMP_FOLDER = "/tmp/";
	//public final static String CONFLICT_REPORT_PATH = "/home/isak/Documents/Master/Test/the/Paola/elasticsearch/ResultData/elasticsearch/ConflictsReport.csv";
	public final static String CONFLICT_REPORT_PATH = "/home/isak/Documents/Master/Test/Paola/ResultData/atmosphere/ConflictsReport.csv";
	private String conflictReport;
	
	
	public Extractor() {
		//analyzeConflictReport(CONFLICT_REPORT_PATH, "/home/isak/Documents/Master/projects/hej");
		//analyzeConflictReport("/home/isak/Documents/Master/Test/the/Paola/EventBus/ResultData/EventBus/ConflictsReport.csv", "/home/isak/Documents/Master/projects/EventBus");
		analyzeConflictReport("/home/isak/Documents/Master/Test/the/Paola/elasticsearch/ResultData/elasticsearch/ConflictsReport.csv", "/home/isak/Documents/Master/projects/elasticsearch");
	}
	
	private void analyzeConflictReport(String conflictReportPath, String pathToRepo) {
		Path path = Paths.get(conflictReportPath);
		ArrayList<String> conflictReportLines;
		try {
			conflictReportLines = (ArrayList<String>) Files.readAllLines(path);
			removeLines(conflictReportLines);
			makeString(conflictReportLines);
			ArrayList<String> conflictStrings = splitConflicts();
			filterConflicts(conflictStrings);
			ArrayList<Conflict> conflicts = createConflicts(conflictStrings, pathToRepo);
			analyzeConflicts(conflicts, pathToRepo);
			//writeFile();
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
	
	private void analyzeConflicts(ArrayList<Conflict> conflicts, String pathToRepo) {
		for (Conflict conflict : conflicts) {
			analyzeConflict(conflict, pathToRepo);
		}
	}
	
	private void analyzeConflict(Conflict conflict, String pathToRepo) {
		try {
			Utils.readScriptOutput("analyzeResolution " + pathToRepo + " " + conflict.getMergeCommitSha() + " " + TEMP_FOLDER + "result.java " + pathToRepo + conflict.getFilePath(), false);
			ArrayList<String> resultFile = (ArrayList<String>)Files.readAllLines(Paths.get(TEMP_FOLDER + "result.java"));
			ArrayList<String> resultFunction = FunctionParser.extractFunction(resultFile, conflict.getFunctionName(), conflict.getParameterTypes());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*private void writeFile() {
		File file = new File("bra.csv");
		try {
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
		conflicts.removeIf(s -> !s.contains("Conflict type: SameSignatureCM"));
		conflicts.removeIf(s -> !s.contains("##FSTMerge## ##FSTMerge## "));//den ska inte finnas i anc
		if (conflicts.removeIf(s -> s.contains("~~FSTMerge~~ ##FSTMerge##"))) {//den ska finnas i left
			System.out.println("What the fucc???");
		}
		
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
