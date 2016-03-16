package findBooleans;

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
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

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
import utils.ConcurrentHashSet;

public class Commander {
	private String REPO;
	public static ConcurrentHashMap<String, ConcurrentHashSet<String>> commitToDiffPlus;
	public static ConcurrentHashMap<String, ConcurrentHashSet<String>> commitToDiffMinus;
	public static ConcurrentHashMap<String, ConcurrentHashSet<String>> commitToBooleanVariables;
	public static ConcurrentHashMap<String, ConcurrentHashSet<String>> commitToSettingBoolean;
	public static ConcurrentHashMap<String, ConcurrentHashSet<String>> commitToIfBoolean;
	private ConcurrentHashMap<String, String> commitToCommitMessage;
	private ConcurrentHashMap<String, Boolean> commitToPullRequest;
	public static ConcurrentHashSet<String> goodCommits;
	public static ArrayList<Commit> commitList;

	// JXL
	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private int noOfSheets;
	private WritableWorkbook workBook;

	public Commander(String excelOutputFile) {
		commitToDiffPlus = new ConcurrentHashMap<String, ConcurrentHashSet<String>>();
		commitToDiffMinus = new ConcurrentHashMap<String, ConcurrentHashSet<String>>();
		commitToBooleanVariables = new ConcurrentHashMap<String, ConcurrentHashSet<String>>();
		commitToCommitMessage = new ConcurrentHashMap<String, String>();
		synchronized (goodCommits) {
			goodCommits = new ConcurrentHashSet<String>();
		}
		commitList = new ArrayList<Commit>();
		commitToPullRequest = new ConcurrentHashMap<String, Boolean>();
		commitToSettingBoolean = new ConcurrentHashMap<String, ConcurrentHashSet<String>>();
		commitToIfBoolean = new ConcurrentHashMap<String, ConcurrentHashSet<String>>();

		// Jxl
		noOfSheets = 0;
		createWorkBook(excelOutputFile);

	}

	public void createSheets(ConcurrentHashMap<String, String> repos) {
		int progress = 0;
		// for (String repo : repos.keySet()) {
		String repo = "elasticsearch";
		REPO = repos.get(repo);
		print("starting with repo: " + repo + ", progress: " + progress);
		progress++;
		REPO = repos.get(repo);
		// String repo = "elasticsearch";
		print("getting diffs");
		getDiffs();
		print("finding booleans");
		FindVariablesBooleans fvbt = new FindVariablesBooleans(commitToDiffPlus, true);
		FindVariablesBooleans fvbf = new FindVariablesBooleans(commitToDiffMinus, false);
		fvbt.start();
		fvbf.start();
		try {
			fvbt.join();
			fvbf.join();
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		print("finding good booleans");
		FindGoodBooleans fgbt = new FindGoodBooleans(true);
		FindGoodBooleans fgbf = new FindGoodBooleans(false);
		fgbt.start();
		fgbf.start();
		print("finding pull requests");
		findPullRequests();

		print("waiting for threads to finish");
		try {
			fgbt.join();
			fgbf.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		print("creating commits");
		createCommits();
		print("creating excel list");
		createExcelList(repo);

		print("clearing");
		commitToDiffPlus.clear();
		commitToDiffMinus.clear();
		commitToCommitMessage.clear();
		commitToBooleanVariables.clear();
		commitToPullRequest.clear();
		commitToSettingBoolean.clear();
		commitToIfBoolean.clear();
		goodCommits.clear();
		commitList.clear();
		// }

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

		/*
		 * getDiffs(); findVariableBooleans(commitToDiffPlus, true);
		 * findVariableBooleans(commitToDiffMinus, false);
		 * findGoodBooleans(true); findGoodBooleans(false); findPullRequests();
		 * createCommits();
		 * 
		 * createExcelList(repo);
		 * 
		 * commitToDiffPlus.clear(); commitToDiffMinus.clear();
		 * commitToCommitMessage.clear(); commitToBooleanVariables.clear();
		 * commitToPullRequest.clear(); commitToSettingBoolean.clear();
		 * commitToIfBoolean.clear(); goodCommits.clear(); commitList.clear(); }
		 * try { workBook.write(); workBook.close(); } catch (IOException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); } catch
		 * (WriteException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
	}

	private String variablesToString(HashSet<String> variables) {
		StringBuilder sb = new StringBuilder();
		for (String variable : variables) {
			if (sb.length() != 0)
				sb.append(", ");
			sb.append(variable);
		}

		return sb.toString();
	}

	private synchronized void writeToWorkbook() {
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

	private void createCommits() {

		for (String commit : goodCommits) {
			String message = commitToCommitMessage.get(commit);
			boolean isPullRequest = commitToPullRequest.get(commit);
			Commit c = new Commit(commit, message, variablesToString(commitToIfBoolean.get(commit)),
					variablesToString(commitToSettingBoolean.get(commit)), isPullRequest);
			synchronized (commitList) {
				commitList.add(c);
			}
		}

	}

	private void createWorkBook(String excelOutputFile) {
		try {
			File file = new File(excelOutputFile);
			WorkbookSettings wbSettings = new WorkbookSettings();
			wbSettings.setLocale(new Locale("en", "EN"));
			wbSettings.setEncoding("Cp1252");
			workBook = Workbook.createWorkbook(file, wbSettings);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void createExcelList(String sheetName) {
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
		synchronized (commitList) {
			for (int i = 0; i < commitList.size(); i++) {
				Commit c = commitList.get(i);
				if (c.getSha().length() == 0) {
					System.out.println("Här ska vi inte va");
				}
				addLabel(sheet, 0, i + 1, c.getSha());
				addLabel(sheet, 1, i + 1, c.getIfVariables());
				addLabel(sheet, 2, i + 1, c.getSettingVariables());
				addLabel(sheet, 3, i + 1, c.getMessage());
				if (c.isPullRequest())
					addLabel(sheet, 4, i + 1, "X");
			}
		}
	}

	private void createLabel(WritableSheet sheet) throws WriteException {
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		times = new WritableCellFormat(times10pt);
		times.setWrap(true);
		WritableFont times10ptBoldUnderline = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		timesBoldUnderline.setWrap(true);

		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);

		addCaption(sheet, 0, 0, "Commit SHA");
		addCaption(sheet, 1, 0, "Variables in ifs");
		addCaption(sheet, 2, 0, "Variables with setting/property/config");
		addCaption(sheet, 3, 0, "Message");
		addCaption(sheet, 4, 0, "Pull Request");
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label = new Label(column, row, s);
		sheet.addCell(label);
	}

	private void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);
	}

	private void findPullRequests() {
		for (String commit : goodCommits) {
			String message = commitToCommitMessage.get(commit);

			if (message.contains("Merge pull request #"))
				commitToPullRequest.put(commit, true);
			else
				commitToPullRequest.put(commit, false);
		}
	}

	private void printTheGoodCommits() {
		synchronized (goodCommits) {
			for (String commit : goodCommits) {
				System.out.println(commit);
			}
		}
	}

	private void printTheVariables() {
		for (String commit : goodCommits) {
			StringBuilder sb = new StringBuilder();
			for (String str : commitToBooleanVariables.get(commit)) {
				if (sb.length() != 0)
					sb.append(", ");
				sb.append(str);
			}
			System.out.println(sb);
		}

	}

	private void printTheCommitMessages() {
		for (String commit : goodCommits) {
			String msg = commitToCommitMessage.get(commit);
			System.out.println(msg);
		}
	}

	public static boolean isVariable(String str) {
		if (str.contains("(")) {
			if (str.contains("=")) {
				if (str.indexOf("=") < str.indexOf("("))
					return true;
				else
					return false;
			} else
				return false;
		}

		return true;
	}

	public static void print(String text) {
		System.out.println(text);
	}

	private int countMergeCommits() {
		Process commitProcess;
		try {
			commitProcess = Runtime.getRuntime().exec("bash " + "scripts/" + "getNumberOfMergeCommits " + REPO);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(commitProcess.getInputStream()));
			String output = br1.readLine();
			String lines = output.split(" ")[0];
			int nrOfLines = Integer.parseInt(lines);
			return nrOfLines;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private void getDiffs() {
		print("  begin");
		try {
			int nrOfMergeCommits = countMergeCommits();
			int progress = 0;

			print("  executing getCommits");
			Process commitProcess = Runtime.getRuntime().exec("bash " + "scripts/" + "getCommits " + REPO);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(commitProcess.getInputStream()));

			String commitSHA;
			print("  reading commits");
			while ((commitSHA = br1.readLine()) != null) {
				print("processing commit " + progress + " of " + nrOfMergeCommits);
				progress++;
				boolean isJavaFile = false;
				Process diffProcess = Runtime.getRuntime()
						.exec("bash " + "scripts/" + "getDiff " + REPO + " " + commitSHA);

				BufferedReader br2 = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));

				String line;
				ConcurrentHashSet<String> linesPlus = new ConcurrentHashSet<String>();
				ConcurrentHashSet<String> linesMinus = new ConcurrentHashSet<String>();
				// Get all lines that starts with "-" and "+"

				while ((line = br2.readLine()) != null) {
					if (line.startsWith("diff --git ") && line.endsWith(".java"))
						isJavaFile = true;
					else if (line.startsWith("diff --git "))
						isJavaFile = false;

					if (isJavaFile) {
						if (line.startsWith("+"))
							linesPlus.add(line);
						else if (line.startsWith("-"))
							linesMinus.add(line);
					}
				}

				// Get the commit message
				Process msgProcess = Runtime.getRuntime()
						.exec("bash " + "scripts/" + "getCommitMessage " + REPO + " " + commitSHA);
				br2 = new BufferedReader(new InputStreamReader(msgProcess.getInputStream()));
				StringBuilder sb = new StringBuilder();
				while ((line = br2.readLine()) != null) {
					if (sb.length() != 0)
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
