package com.smartracumn.smartrac;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class DataCleaningReminder {

	protected static final String TAG = "DataCleaningReminder";

	private Context context;

	private SharedPreferences sharedPref;
	
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	
	private static int WARNING_PERIOD=14;
	
	public DataCleaningReminder(Context context) {
		this.context = context;
		
		sharedPref = context.getSharedPreferences(
				"com.smartracumn.smartrac", Context.MODE_PRIVATE);
		String startDate_str = sharedPref.getString("firstnewday", "2013-10-21");
		
		try {
			Date firstNewDay = df.parse(startDate_str);
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, -WARNING_PERIOD);
			
			String warningDate = df.format(cal.getTime());
			if(firstNewDay.before(df.parse(warningDate))){
				showMessage();
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	private void showMessage(){
		
		new AlertDialog.Builder(context)
	    .setTitle("Data Upload Reminder")
	    .setMessage("You haven't uploaded the data for more than two weeks. To Upload, Go to Left Slider Menu -> Upload")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        }
	     })
	    .setIcon(android.R.drawable.ic_dialog_alert)
	     .show();
	}

}
