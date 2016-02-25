import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import jxl.CellType;
import jxl.CellView;
import jxl.LabelCell;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.format.UnderlineStyle;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import java.lang.Boolean;

public class Commander {
	private String REPO;
	
	private HashMap<String, HashSet<String>> commitToDiffPlus;
	private HashMap<String, HashSet<String>> commitToDiffMinus;
	private HashMap<String, HashSet<String>> commitToBooleanVariables;
	private HashMap<String, String> commitToCommitMessage;
	private HashMap<String, Boolean> commitToPullRequest;
	private ArrayList<String> goodCommits;
	private ArrayList<Commit> commitList;
	
	//JXL
	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private int noOfSheets;
	private WritableWorkbook workBook;
	
	public Commander(String excelOutputFile) {
		commitToDiffPlus = new HashMap<String, HashSet<String>>();
		commitToDiffMinus = new HashMap<String, HashSet<String>>();
		commitToBooleanVariables = new HashMap<String, HashSet<String>>();
		commitToCommitMessage = new HashMap<String, String>();
		goodCommits = new ArrayList<String>();
		commitList = new ArrayList<Commit>();
		commitToPullRequest = new HashMap<String, Boolean>();
		
		//Jxl
		noOfSheets = 0;
		createWorkBook(excelOutputFile);
		
	}
	
	public void createSheets(HashMap<String, String> repos) {
		for(String repo : repos.keySet()) {
			REPO = repos.get(repo);
			
			getDiffs();
			findVariableBooleans(commitToDiffPlus, true);
			findVariableBooleans(commitToDiffMinus, false);
			findIfsWithBooleans();
			findPullRequests();
			createCommits();
			
			createExcelList(repo);
			
			commitToDiffPlus.clear();
			commitToDiffMinus.clear();
			commitToCommitMessage.clear();
			commitToBooleanVariables.clear();
			commitToPullRequest.clear();
			goodCommits.clear();
			commitList.clear();
		}
		try {
			workBook.write();
			workBook.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String variablesToString(String commit) {
		StringBuilder sb = new StringBuilder();
		for(String str : commitToBooleanVariables.get(commit)) {
			if(sb.length() != 0)
				sb.append(", ");
			sb.append(str);
		}
		
		return sb.toString();
	}
	
	private void createCommits() {
		for(String commit : goodCommits) {
			String message = commitToCommitMessage.get(commit);
			boolean isPullRequest = commitToPullRequest.get(commit);
			Commit c = new Commit(commit, message, variablesToString(commit), isPullRequest);
			commitList.add(c);
		}
	}
	
	private void createWorkBook(String excelOutputFile) {
		try {
			File file = new File(excelOutputFile);
			WorkbookSettings wbSettings = new WorkbookSettings();
			wbSettings.setLocale(new Locale("en", "EN"));
			workBook = Workbook.createWorkbook(file, wbSettings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createExcelList(String sheetName) {
		try {
			workBook.createSheet(sheetName, noOfSheets);
			WritableSheet excelSheet = workBook.getSheet(noOfSheets);
			createLabel(excelSheet);
			
			fillExcelDocument(excelSheet);
			noOfSheets++;
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void fillExcelDocument(WritableSheet sheet) throws RowsExceededException, WriteException {
		for(int i = 0; i < commitList.size(); i++) {
			Commit c = commitList.get(i);
			addLabel(sheet, 0, i+1, c.getSha());
			addLabel(sheet, 1, i+1, c.getVariables());
			addLabel(sheet, 2, i+1, c.getMessage());
			if(c.isPullRequest())
				addLabel(sheet, 3, i+1, "X");
		}
	}
	
	private void createLabel(WritableSheet sheet) throws WriteException {
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		times = new WritableCellFormat(times10pt);
		times.setWrap(true);
		WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false, UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		timesBoldUnderline.setWrap(true);
		
		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);
		
		addCaption(sheet, 0, 0, "Commit SHA");
		addCaption(sheet, 1, 0, "Variables");
		addCaption(sheet, 2, 0, "Message");
		addCaption(sheet, 3, 0, "Pull Request");
	}
	
	private void addLabel(WritableSheet sheet, int column, int row, String s) throws RowsExceededException, WriteException {
		Label label = new Label(column, row, s);
		sheet.addCell(label);
	}
	
	private void addCaption(WritableSheet sheet, int column, int row, String s) throws RowsExceededException, WriteException {
		Label label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);
	}
	
	private void findVariableBooleans(HashMap<String, HashSet<String>> list, boolean plus) {
		for(String key : list.keySet()) {
			HashSet<String> diff = list.get(key);
			
			String variableName;
			HashSet<String> variables = new HashSet<String>();
			for(String line : diff) {
				if(line.contains("boolean ") && line.endsWith(";")) {
					int startIndex = line.indexOf("boolean") + 8;
					String fromBoolean = line.substring(startIndex);
					
					int endIndex = -1;
					if(fromBoolean.indexOf(" ") != -1)
						endIndex = fromBoolean.indexOf(" ");
					else if(fromBoolean.indexOf("=") != -1)
						endIndex = fromBoolean.indexOf("=");
					else if(fromBoolean.indexOf(";") != -1)
						endIndex = fromBoolean.indexOf(";");
					
					variableName = fromBoolean.substring(0, endIndex);
					if(!variableName.contains(",") && !variableName.contains(")") && isVariable(fromBoolean)) {
						variables.add(variableName);
					}
				}
				if(plus)
					commitToBooleanVariables.put(key, variables);
				else {
					HashSet<String> set = commitToBooleanVariables.get(key);
					if(set != null) {
						for(String name : variables)
							set.remove(name);
					}
						
				}
					
			}
			
		}
		
	}
	
	private void findIfsWithBooleans() {
		for(String key : commitToBooleanVariables.keySet()) {
			HashSet<String> variables = commitToBooleanVariables.get(key);
			HashSet<String> lines = commitToDiffPlus.get(key);
			for(String variable : variables) {
				for(String line : lines) {
					if(line.contains(variable) && line.contains("if"))
						if(!goodCommits.contains(key))
							goodCommits.add(key);
				}
			}
			
		}
	}
	
	private void findPullRequests() {
		for(String commit : goodCommits) {
			String message = commitToCommitMessage.get(commit);
			
			if(message.contains("Merge pull request #"))
				commitToPullRequest.put(commit, true);
			else
				commitToPullRequest.put(commit, false);
		}
	}
	
	private void printTheGoodCommits() {
		for(String commit : goodCommits) {
			System.out.println(commit);
		}
	}
	
	private void printTheVariables() {
		for(String commit : goodCommits) {
			StringBuilder sb = new StringBuilder();
			for(String str : commitToBooleanVariables.get(commit)) {
				if(sb.length() != 0)
					sb.append(", ");
				sb.append(str);
			}
			System.out.println(sb);
		}
		
	}
	
	private void printTheCommitMessages() {
		for(String commit : goodCommits) {
			String msg = commitToCommitMessage.get(commit);
			System.out.println(msg);
		}
	}
	
	private boolean isVariable(String str) {
		if(str.contains("(")) {
			if(str.contains("=")) {
				if(str.indexOf("=") < str.indexOf("("))
					return true;
				else
					return false;
			}
			else
				return false;
		}
		
		return true;
	}	
	
	private void getDiffs() {
		try {
			Process commitProcess = Runtime.getRuntime().exec("bash " + "scripts/" + "getCommits " + REPO);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(commitProcess.getInputStream()));
			
			String commitSHA;
			while((commitSHA = br1.readLine()) != null) {
				boolean isJavaFile = false;
				Process diffProcess = Runtime.getRuntime().exec("bash " + "scripts/" + "getDiff " + REPO + " " + commitSHA);
				
				BufferedReader br2 = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
				
				String line;
				HashSet<String> linesPlus = new HashSet<String>();
				HashSet<String> linesMinus = new HashSet<String>();
				//Get all lines that starts with "-" and "+"
				while((line = br2.readLine()) != null) {
					if(line.startsWith("diff --git ") && line.endsWith(".java"))
						isJavaFile = true;
					else if(line.startsWith("diff --git "))
						isJavaFile = false;
							
					if(isJavaFile) {
						if(line.startsWith("+"))
							linesPlus.add(line);
						else if(line.startsWith("-"))
							linesMinus.add(line);
					}
				}
				
				//Get the commit message
				Process msgProcess = Runtime.getRuntime().exec("bash " + "scripts/" + "getCommitMessage " + REPO + " " + commitSHA);
				br2 = new BufferedReader(new InputStreamReader(msgProcess.getInputStream()));
				StringBuilder sb = new StringBuilder();
				while((line = br2.readLine()) != null) {
					if(sb.length() != 0)
						sb.append("\n");
					sb.append(line);
				}
				
				commitToDiffPlus.put(commitSHA, linesPlus);
				commitToDiffMinus.put(commitSHA, linesMinus);
				commitToCommitMessage.put(commitSHA, sb.toString());
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
