package utils;

import java.util.ArrayList;

import findBooleans.Commit;
import jxl.CellView;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class WorkBookCreator {
	
	// JXL
	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private int noOfSheets;
	private WritableWorkbook workBook;
	private String outputFile;
	private WritableSheet excelSheet;
	private int noOfRows;
	
	public WorkBookCreator(String outputFile) {
		this.outputFile = outputFile;
		noOfSheets = 0;
		noOfRows = 0;
	}
	
	public void createSheet(String sheetName, ArrayList<String> captions) {
		try {
			workBook.createSheet(sheetName, noOfSheets);
			excelSheet = workBook.getSheet(noOfSheets);
			createLabel(excelSheet, captions);

			noOfSheets++;
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createLabel(WritableSheet sheet, ArrayList<String> captions) throws WriteException {
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		times = new WritableCellFormat(times10pt);
		times.setWrap(true);
		WritableFont times10ptBoldUnderline = new WritableFont(
				WritableFont.TIMES, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		timesBoldUnderline.setWrap(true);

		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);
		
		for(int i = 0; i < captions.size(); i++) {
			addCaption(sheet, i, 0, captions.get(i));
		}
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

	public void addRow(String... text)
			throws RowsExceededException, WriteException {
		for(int i = 0; i < text.length; i++) {
			addLabel(excelSheet, i, noOfRows, text[i]);
		}
		noOfRows++;
	}

}
