package com.moodstream.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import com.google.api.client.util.DateTime;

public class DateUtils {
	
	public static final String TAG="DateUtils";
	
	public static final String DATE_FORMAT="dd/MM/yyyy-h:mm a";
	
	public static final String HOUR_FORMAT="h:mm a";
	
	public static final String TODAY_HOUR_FORMAT="h:mm a";
	
	@SuppressLint("SimpleDateFormat")
	public static String getDateFormatString(String format, DateTime d) {
		Date date = new Date(d.getValue());
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String dateString = formatter.format(date);
		if(format==TODAY_HOUR_FORMAT) dateString="Today at "+dateString;
		return dateString;
	}

}
