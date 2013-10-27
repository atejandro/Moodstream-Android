package com.moodstream.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import com.moodstream.R;

public class DateTimePickerDialog extends AlertDialog{
	
	private static final String TAG="DateTimePickerDialog";
	
	private Date date=null;
	private DatePicker dp;
	private TimePicker tp;

	//______________________________________________________________________________
	//CONSTRUCTOR___________________________________________________________________
	@SuppressLint("NewApi")
	public DateTimePickerDialog(Context context) {
		super(context);
		// Inflate a view to assign to the AlertDialog
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_datetimepicker, null);

		// Assign control variables to the view inflated
		tp = (TimePicker) view.findViewById(R.id.timepicker);
		dp = (DatePicker) view.findViewById(R.id.datepicker);
		
		// Create dialog builder
				AlertDialog.Builder dialog = new AlertDialog.Builder(context);
				dialog.setTitle("Choose a date");
				dialog.setCancelable(true);
				dialog.setView(view);
				
				dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Calendar cal=Calendar.getInstance();
						cal.set(dp.getYear(), dp.getMonth(), dp.getDayOfMonth(), tp.getCurrentHour(), tp.getCurrentMinute());
						date=cal.getTime();
						Log.d(TAG, "Date saved");
						dialog.dismiss();
					}
				});
				
				dialog.create().show();
	}
	
	//______________________________________________________________________________
	//METHODS_______________________________________________________________________
	
	public Date getDate()
	{
		if(date==null)
		{
			Calendar cal=Calendar.getInstance();
			date=cal.getTime();
		}
		return date;
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getDateFormatString(String format)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format);
	    String dateString = formatter.format(date);
	    return dateString;
	}



}
