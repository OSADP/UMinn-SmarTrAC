package com.smartracumn.smartrac;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {

	// Variable to determine if thew fragment is called by Zipfiles(Upload)
	boolean fromUpload;

	// Variable to determine if the fragment is called by AggregatedSummary
	// class
	boolean fromSummary;

	// Variable to stop the execution of setDate twice
	boolean fired = false;

	public static final String YEAR = "year";
	public static final String MONTH = "month";
	public static final String DAY = "day";

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker

		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);

		Bundle args = getArguments();
		if (args != null && args.containsKey(YEAR)) {
			mYear = args.getInt(YEAR);
			mMonth = args.getInt(MONTH);
			mDay = args.getInt(DAY);
		}
		final int date_year = c.get(Calendar.YEAR);
		final int date_month = c.get(Calendar.MONTH);
		final int date_day = c.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, mYear,
				mMonth, mDay) {
			@Override
			public void onDateChanged(DatePicker view, int newYear,
					int newMonth, int newDay) {
				if (newYear > date_year)
					view.updateDate(date_year, date_month, date_day);

				if (newMonth > date_month && newYear == date_year)
					view.updateDate(date_year, date_month, date_day);

				if (newDay > date_day && newYear == date_year
						&& newMonth == date_month)
					view.updateDate(date_year, date_month, date_day);

			}
		};

		dpd.setTitle("Choose a date");

		// if (args != null && args.getBoolean("Upload", false)) {
		// fromUpload = true;
		//
		// dpd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// if (which == DialogInterface.BUTTON_NEGATIVE) {
		// if (fromUpload) {
		// ZipFiles zip_act = (ZipFiles) getActivity();
		// // zip_act.finish_activity();
		// }
		// }
		// }
		// });
		// }
		if (args != null && args.getBoolean("Summary", false)) {
			fromSummary = true;
		}

		return dpd;
	}

	public void onDateSet(DatePicker view, int year, int month, int day) {

		if (fired)
			return;

		Calendar now = Calendar.getInstance();
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		if (c.after(now)) {
			System.out.println("Invalid date");
			return;
		}
		// if (fromUpload) {
		// ZipFiles zip_act = (ZipFiles) getActivity();
		// // zip_act.setDate(c.getTime());
		// }
		// else
		if (fromSummary) {
			AggregatedSummary act = (AggregatedSummary) getActivity();
			act.setSelectedDate(c.getTime());
		} else {
			SmartracActivity act = (SmartracActivity) getActivity();
			act.setSelectedDate(c.getTime());
		}

		fired = true;
	}
}