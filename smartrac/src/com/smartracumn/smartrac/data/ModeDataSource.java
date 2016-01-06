package com.smartracumn.smartrac.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.ModeIndicator;

/**
 * Mode data source which is used to interact with Modes table in smartrac
 * database.
 * 
 * @author kangx385
 * 
 */
public class ModeDataSource {
	private final String TAG = getClass().getSimpleName();
	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private SmartracSQLiteHelper dbHelper;

	private final String[] allColumns = { SmartracSQLiteHelper.COLUMN_ID,
			SmartracSQLiteHelper.COLUMN_TIME, SmartracSQLiteHelper.COLUMN_MODE };

	private final int COLUMN_ID = 0, COLUMN_TIME = 1, COLUMN_MODE = 2;

	public ModeDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
	}

	/**
	 * Create mode indicator and insert into modes table.
	 * 
	 * @param time
	 *            The time stamp for inserted mode.
	 * @param mode
	 *            The travel mode.
	 * @return The mode indicator inserted into table.
	 */
	public synchronized ModeIndicator createModeIndicator(Date time,
			CalendarItem.TravelMode mode) {
		Log.i(TAG, getClass().getSimpleName() + ": create mode indicator "
				+ ISO8601FORMAT.format(time) + " " + mode);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_MODE, mode.getValue());
		values.put(SmartracSQLiteHelper.COLUMN_TIME, ISO8601FORMAT.format(time));

		long insertId = database.insert(SmartracSQLiteHelper.TABLE_MODES, null,
				values);

		return new ModeIndicator(insertId, time, mode);
	}

	/**
	 * Create mode indicator and insert into modes table.
	 * 
	 * @param time
	 *            The time stamp for inserted mode.
	 * @param mode
	 *            The travel mode.
	 * @return The mode indicator inserted into table.
	 */
	public synchronized void insertInstantMovement(Date time, boolean gpsService) {
		Log.i(TAG,
				getClass().getSimpleName()
						+ ": create instant movement indicator "
						+ ISO8601FORMAT.format(time) + " " + gpsService);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_GPS_SERVICE, gpsService ? 1 : 0);
		values.put(SmartracSQLiteHelper.COLUMN_TIME, ISO8601FORMAT.format(time));

		database.insert(SmartracSQLiteHelper.TABLE_INSTANT_MOVEMENTS, null,
				values);
	}

	/**
	 * Delete given mode indicator from modes table.
	 * 
	 * @param mode
	 *            The mode indicator to be deleted.
	 */
	public synchronized void deleteRecord(ModeIndicator mode) {
		long id = mode.getId();
		Log.i(TAG, "Mode deleted with id: " + id);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_MODES,
				SmartracSQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	/**
	 * Delete all records from modes table.
	 */
	public synchronized void deleteAll() {
		Log.i(TAG, "Records deleted");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_MODES, null, null);
	}

	/**
	 * Get mode indicator fall into given time span.
	 * 
	 * @param start
	 *            The start time.
	 * @param end
	 *            The end time.
	 * @return Mode indicators within given time span.
	 */
	public synchronized List<ModeIndicator> getRecordsForDateRange(
			final Date start, final Date end) {

		// Not returning data.
		Log.i(TAG, "Get mode indicator for " + ISO8601FORMAT.format(start)
				+ " - " + ISO8601FORMAT.format(end));

		List<ModeIndicator> mis = new ArrayList<ModeIndicator>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(SmartracSQLiteHelper.TABLE_MODES,
				allColumns, SmartracSQLiteHelper.COLUMN_TIME + " BETWEEN \""
						+ ISO8601FORMAT.format(start) + "\" AND \""
						+ ISO8601FORMAT.format(end) + "\";", null, null, null,
				null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ModeIndicator mi = cursorToModeIndicator(cursor);
			mis.add(mi);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		return mis;
	}

	/**
	 * Get all mode indicator records.
	 * 
	 * @return All existing mode indicator.
	 */
	public synchronized List<ModeIndicator> getAllRecords() {
		Log.i(TAG, "Get all records()");

		List<ModeIndicator> mis = new ArrayList<ModeIndicator>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(SmartracSQLiteHelper.TABLE_MODES,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ModeIndicator mi = cursorToModeIndicator(cursor);
			mis.add(mi);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return mis;
	}

	/**
	 * Map cursor to mode indicator.
	 * 
	 * @param cursor
	 *            The cursor object.
	 * @return Mode indicator mapped from given cursor.
	 */
	private ModeIndicator cursorToModeIndicator(Cursor cursor) {
		int id = cursor.getInt(COLUMN_ID);
		int modeId = cursor.getInt(COLUMN_MODE);
		String timeString = cursor.getString(COLUMN_TIME);
		Date time = null;
		try {
			time = ISO8601FORMAT.parse(timeString);
		} catch (ParseException e) {
			time = null;
		}

		CalendarItem.TravelMode mode = CalendarItem.TravelMode.get(modeId);

		ModeIndicator indicator = new ModeIndicator(id, time, mode);

		return indicator;
	}
}
