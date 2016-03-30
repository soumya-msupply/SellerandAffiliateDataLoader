package com.msupply;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.poi.ss.usermodel.Cell;

public class LoaderUtil {

	private static String getFormattedNumber(Cell cell) {
		if (cell == null) {
			return "";
		}
		DecimalFormat df = new DecimalFormat("#");
		return df.format(cell.getNumericCellValue());
	}

	public static String getTextCellValue(Cell cell) {

		if (cell == null) {
			return "";
		}

		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return getFormattedNumber(cell);
		} 
		
		return cell.getStringCellValue();
	}

	public static int getNumericCellValue(Cell cell) {

		if (cell == null) {
			return 0;
		}
		else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return ((Double)cell.getNumericCellValue()).intValue();
		}
		else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			if (cell.getStringCellValue().length() > 0) {
				return Integer.parseInt(cell.getStringCellValue());
			}
		}
		return 0;
	}

	public static Boolean isValid(Cell cell) {
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return false;
		}
		return true;
	}

	public static Boolean getBoolean(Cell cell) {
		if (!isValid(cell)) {
			return false;
		} else if (cell.getStringCellValue().equalsIgnoreCase("yes")) {
			return Boolean.TRUE;
		}
		return false;
	}
	
	public static Date getCurrentTimestamp(){
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
		return calendar.getTime();
	}

}
