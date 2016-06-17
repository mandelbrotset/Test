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
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
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
	private WritableSheet excelSheet;
	private int noOfRows;
	
	public WorkBookCreator(String outputFile) {
		noOfSheets = 0;
		noOfRows = 1;
		
		try {
			File file = new File(outputFile);
			WorkbookSettings wbSettings = new WorkbookSettings();
			wbSettings.setLocale(new Locale("en", "EN"));
			wbSettings.setEncoding("Cp1252");
			workBook = Workbook.createWorkbook(file, wbSettings);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createSheet(String sheetName, String... captions) {
		try {
			workBook.createSheet(sheetName, noOfSheets);
			excelSheet = workBook.getSheet(noOfSheets);
			createLabel(excelSheet, captions);

			noOfSheets++;
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	private void createLabel(WritableSheet sheet, String... captions) throws WriteException {
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
		
		for(int i = 0; i < captions.length; i++) {
			addCaption(sheet, i, 0, captions[i]);
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
	
	public void writeToWorkbook() {
		try {
			workBook.write();
			workBook.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
}
