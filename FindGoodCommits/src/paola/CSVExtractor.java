package paola;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import utils.WorkBookCreator;

public class CSVExtractor {
	
	private enum Parameter {
		FILE_NAME,
		MERGE_SHA,
		PARENT1_SHA,
		PARENT2_SHA,
		CONFLICT_TYPE,
		CONFLICT_BODY
	}

	private final String path = "/home/patrik/Documents/Chalmers/5an/MasterThesis/Paola/conflictsAnalyzer/ResultData/Mydalsa";
	private WorkBookCreator wbc;
	private ArrayList<ConflictInfo> conflicts;

	public CSVExtractor() {
		wbc = new WorkBookCreator("conflict_data.xls");
		conflicts = new ArrayList<ConflictInfo>();
		wbc.createSheet("Conflict Information", "Merge SHA-1", "Parent1 SHA-1", "Parent2 SHA-1", "File", "Body", "Conflict Pattern", "Resolution Pattern");
	}

	public void extractResult() {
		extractFromCSV();
		wbc.writeToWorkbook();
	}

	private void extractFromCSV() {
		File csvFile = new File(path + "/ConflictsReport.csv");
		try {
			BufferedReader br = new BufferedReader(new FileReader(csvFile));
			String line = "";
			StringBuilder sb = new StringBuilder();
			
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			String lines = sb.toString();
			String blockPattern = "############## CONFLICT ##############";
			String[] blocks = lines.split(blockPattern);
			
			for(String block : blocks) {
				String conflictPattern = extractValue(block, Parameter.CONFLICT_TYPE);
				String mergeSHA = extractValue(block, Parameter.MERGE_SHA);
				String parent1SHA = extractValue(block, Parameter.PARENT1_SHA);
				String parent2SHA = extractValue(block, Parameter.PARENT2_SHA);
				String body = extractValue(block, Parameter.CONFLICT_BODY);
				String fileName = extractValue(block, Parameter.FILE_NAME);
				
				createConflictInfo(mergeSHA, parent1SHA, parent2SHA, conflictPattern, body, fileName);
				wbc.addRow(mergeSHA, parent1SHA, parent2SHA, fileName, body, conflictPattern, "N/A");				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String extractValue(String block, Parameter p) {
		String splitPattern1;
		String splitPattern2;
		String value = "";
		switch(p) {
		case CONFLICT_TYPE:
			splitPattern1 = "Conflict type: ";
			splitPattern2 = "Merge Commit SHA-1: ";
			break;
		case MERGE_SHA:
			splitPattern1 = "Merge Commit SHA-1: ";
			splitPattern2 = "Parent1 SHA-1: ";
			break;
		case PARENT1_SHA:
			splitPattern1 = "Parent1 SHA-1: ";
			splitPattern2 = "Parent2 SHA-1: ";
			break;
		case PARENT2_SHA:
			splitPattern1 = "Parent2 SHA-1: ";
			splitPattern2 = "Number of Conflicts: ";
			break;
		case CONFLICT_BODY:
			splitPattern1 = "Conflict body: ";
			splitPattern2 = "File path: ";
			break;
		case FILE_NAME:
			splitPattern1 = "File path: ";
			splitPattern2 = "";
			break;
		default:
			splitPattern1 = "";
			splitPattern2 = "";
			break;
		}
		try {
			value = extractFromBlock(block, splitPattern1, splitPattern2);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.print("Invalid block!");
		}
		
		return value;
	}
	
	private String extractFromBlock(String block, String splitPattern1, String splitPattern2) throws ArrayIndexOutOfBoundsException {
		return block.split(splitPattern1)[1].split(splitPattern2)[0];
	}
	
	private void createConflictInfo(String mergeSHA, String parent1sha, String parent2sha, String conflictType,
			String conflictBody, String fileName) {
		ConflictInfo ci = new ConflictInfo(mergeSHA, parent1sha, parent2sha, conflictType, conflictBody, fileName);
		conflicts.add(ci);
	}

}
