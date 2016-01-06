package com.smartracumn.smartrac;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smartracumn.smartrac.data.SmartracSQLiteHelper;

public class DataCleaning {

	protected static final String TAG = "DataCleaning";

	private SmartracSQLiteHelper dbHelper;

	private SQLiteDatabase database;

	private String startDate;

	private String endDate;

	private Context context;

	public DataCleaning(Context context) {
		this.context = context;
		dbHelper = SmartracSQLiteHelper.getInstance(context);
		database = dbHelper.getReadableDatabase();
	}

	public void cleanData(String startDate, String endDate) {

		this.startDate = startDate;
		this.endDate = endDate;
		Thread dataClenaingThread = new Thread(runnable);
		dataClenaingThread.start();
	}

	Runnable runnable = new Runnable() {
		public void run() {

			String condition = "time > '" + startDate + "' and time < '"
					+ endDate + "'";

			int count = database.delete("table_locations", condition, null);
			Log.i(TAG, "Rows deleted from locations table: " + count);

			count = database.delete("table_motions", condition, null);
			Log.i(TAG, "Rows deleted from motions table: " + count);

			count = database.delete("table_intermediate_locations", condition,
					null);
			Log.i(TAG, "Rows deleted from intermediate locations table: "
					+ count);

//			if (count > 0) {
//				Toast.makeText(context, "Data cleaning successful!",
//						Toast.LENGTH_LONG).show();
//			}

		}
	};

}
