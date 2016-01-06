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

import com.smartracumn.smartrac.model.DwellingIndicator;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * Dwelling data source which is used to interact with dwelling tables in
 * SQLiteDatabase.
 * 
 * @author Jie
 * 
 */
public class DwellingDataSource {
	private final String TAG = getClass().getSimpleName();
	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private SmartracSQLiteHelper dbHelper;
	private final String[] allColumns = { SmartracSQLiteHelper.COLUMN_ID,
			SmartracSQLiteHelper.COLUMN_TIME,
			SmartracSQLiteHelper.COLUMN_DWELLING_NONDWELLING,
			SmartracSQLiteHelper.COLUMN_ADJUSTMENT };

	private final int COLUMN_ID = 0, COLUMN_TIME = 1,
			COLUMN_DWELLING_NONDWELLING = 2, COLUMN_ADJUSTMENT = 3;

	public DwellingDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
	}

	public synchronized DwellingIndicator createDwellingIndicator(Date time,
			boolean isDwelling, Date adjustment) {
		Log.i(TAG, getClass().getSimpleName() + ": create dwelling indicator");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_DWELLING_NONDWELLING,
				isDwelling ? 1 : 0);
		values.put(SmartracSQLiteHelper.COLUMN_TIME, ISO8601FORMAT.format(time));
		if (adjustment != null) {
			values.put(SmartracSQLiteHelper.COLUMN_ADJUSTMENT,
					ISO8601FORMAT.format(adjustment));
		}

		long insertId = database.insert(SmartracSQLiteHelper.TABLE_DWELLINGS,
				null, values);

		return new DwellingIndicator(insertId, time, isDwelling, adjustment);
	}

	public synchronized void deleteFrom(Date time) {
		Log.i(TAG, "delete dwellings from "
				+ SmartracDataFormat.getIso8601Format().format(time));

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		database.delete(SmartracSQLiteHelper.TABLE_DWELLINGS,
				SmartracSQLiteHelper.COLUMN_TIME + " > \""
						+ SmartracDataFormat.getIso8601Format().format(time)
						+ "\"", null);
	}

	public synchronized void deleteRecord(DwellingIndicator dwelling) {
		long id = dwelling.getId();
		Log.i(TAG, "Dwelling deleted with id: " + id);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_DWELLINGS,
				SmartracSQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	public synchronized void deleteAll() {
		Log.i(TAG, "Records deleted");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_DWELLINGS, null, null);
	}

	public synchronized List<DwellingIndicator> getRecordsForDateRange(
			final Date start, final Date end) {

		// Not returning data.
		Log.i(TAG, "Get dwelling indicator for " + ISO8601FORMAT.format(start)
				+ " - " + ISO8601FORMAT.format(end));

		List<DwellingIndicator> dis = new ArrayList<DwellingIndicator>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(SmartracSQLiteHelper.TABLE_DWELLINGS,
				allColumns, SmartracSQLiteHelper.COLUMN_TIME + " BETWEEN \""
						+ ISO8601FORMAT.format(start) + "\" AND \""
						+ ISO8601FORMAT.format(end) + "\";", null, null, null,
				null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			DwellingIndicator di = cursorToDwellingIndicator(cursor);
			dis.add(di);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		return dis;
	}

	public synchronized List<DwellingIndicator> getAllRecords() {
		List<DwellingIndicator> dis = new ArrayList<DwellingIndicator>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(SmartracSQLiteHelper.TABLE_DWELLINGS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			DwellingIndicator di = cursorToDwellingIndicator(cursor);
			dis.add(di);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return dis;
	}

	private DwellingIndicator cursorToDwellingIndicator(Cursor cursor) {
		int id = cursor.getInt(COLUMN_ID);
		String timeString = cursor.getString(COLUMN_TIME);
		Date time = null;
		Date adj = null;
		try {
			time = ISO8601FORMAT.parse(timeString);
		} catch (ParseException e) {
			time = null;
		}

		boolean dwelling = cursor.getInt(COLUMN_DWELLING_NONDWELLING) == 0 ? false
				: true;

		if (!cursor.isNull(COLUMN_ADJUSTMENT)) {
			String adjString = cursor.getString(COLUMN_ADJUSTMENT);
			try {
				adj = ISO8601FORMAT.parse(adjString);
			} catch (ParseException e) {
				adj = null;
			}
		}

		DwellingIndicator indicator = new DwellingIndicator(id, time, dwelling,
				adj);

		return indicator;
	}
}
