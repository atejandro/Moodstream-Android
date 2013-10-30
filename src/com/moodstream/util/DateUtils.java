package com.moodstream.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.api.client.util.DateTime;

public class DateUtils {

	public static final String TAG = "DateUtils";

	public static final String DATE_FORMAT = "dd/MM/yyyy-h:mm a";

	public static final String HOUR_FORMAT = "h:mm a";

	public static final String TODAY_HOUR_FORMAT = "h:mm a";

	public static final int MILISECONDS_IN_SECOND = 1000;

	public static final int SECONDS_IN_MINUTE = 60;

	public static final int MILISECONDS_IN_MINUTE = SECONDS_IN_MINUTE
			* MILISECONDS_IN_SECOND;

	public static final int MILISECONDS_IN_HOUR = 60 * SECONDS_IN_MINUTE
			* MILISECONDS_IN_SECOND;

	public static final int HOURS_IN_A_DAY = 24;

	public static final int DAYS_IN_A_WEEK = 7;

	@SuppressLint("SimpleDateFormat")
	public static String getDateFormatString(String format, DateTime d) {
		Date date = new Date(d.getValue());
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String dateString = formatter.format(date);
		if (format == TODAY_HOUR_FORMAT)
			dateString = "Today at " + dateString;
		return dateString;
	}

	public static String getTimeDifference(Long date) {
		Calendar cal = Calendar.getInstance();
		long deltaTime = cal.getTimeInMillis() - date;

		if (deltaTime < MILISECONDS_IN_SECOND
				|| deltaTime < MILISECONDS_IN_MINUTE)
			return "Now";
		else if (deltaTime < MILISECONDS_IN_HOUR)
			return (deltaTime / MILISECONDS_IN_MINUTE) + "m";
		else if (deltaTime < MILISECONDS_IN_HOUR * HOURS_IN_A_DAY)
			return (deltaTime / MILISECONDS_IN_HOUR) + "h";
		else if (deltaTime < DAYS_IN_A_WEEK * HOURS_IN_A_DAY
				* MILISECONDS_IN_HOUR)
			return (deltaTime / (HOURS_IN_A_DAY * MILISECONDS_IN_HOUR)) + "d";
		else return (deltaTime/(DAYS_IN_A_WEEK*HOURS_IN_A_DAY*MILISECONDS_IN_HOUR))+"w";	
	}

}
