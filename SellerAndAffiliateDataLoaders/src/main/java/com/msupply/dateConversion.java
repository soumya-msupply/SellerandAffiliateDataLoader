package com.msupply;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class dateConversion {
	
	public static void main(String[] args) {
		try {
			Date date = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("IST"));
			cal.setTime(dateFormat.parse("2016-03-10T18:15:00.000Z"));
			date = cal.getTime();
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("d MMM'`'yy 'at' HH:mm aaa");
			String formattedDate = dateFormat1.format(date);
			System.out.println(formattedDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
