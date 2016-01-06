package com.smartracumn.smartrac.data;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smartracumn.smartrac.model.MotionData;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * Motion data source which is used to interact with motions table in
 * SQLiteDatabase.
 * 
 * @author kangx385
 * 
 */
public class MotionDataSource {
	private final String TAG = getClass().getSimpleName();

	private SmartracSQLiteHelper dbHelper;

	private final String[] allColumns = { SmartracSQLiteHelper.COLUMN_ID,
			SmartracSQLiteHelper.COLUMN_TIME,
			SmartracSQLiteHelper.COLUMN_LINEAR_X,
			SmartracSQLiteHelper.COLUMN_LINEAR_Y,
			SmartracSQLiteHelper.COLUMN_LINEAR_Z,
			SmartracSQLiteHelper.COLUMN_LINEAR_MAGNITUDE,
			SmartracSQLiteHelper.COLUMN_TRUE_X,
			SmartracSQLiteHelper.COLUMN_TRUE_Y,
			SmartracSQLiteHelper.COLUMN_TRUE_Z,
			SmartracSQLiteHelper.COLUMN_TRUE_MAGNITUDE };

	private final int COLUMN_ID = 0, COLUMN_TIME = 1, COLUMN_LINEAR_X = 2,
			COLUMN_LINEAR_Y = 3, COLUMN_LINEAR_Z = 4,
			COLUMN_LINEAR_MAGNITUDE = 5, COLUMN_TRUE_X = 6, COLUMN_TRUE_Y = 7,
			COLUMN_TRUE_Z = 8, COLUMN_TRUE_MAGNITUDE = 9;

	/**
	 * Initializes a new instance of the MotionDataSource class.
	 * 
	 * @param context
	 */
	public MotionDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
	}

	/**
	 * Insert a list of motion datas into motions table.
	 * 
	 * @param motionDatas
	 * @return True is insert executed successfully.
	 */
	public synchronized boolean insert(List<MotionData> motionDatas) {
		Log.i(TAG, "insert " + motionDatas.size() + " motion records.");
		List<Long> newIds = new ArrayList<Long>();
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.beginTransaction();
			for (MotionData motionData : motionDatas) {
				ContentValues values = MotionDataToContentValues(motionData);
				long insertId = database.insert(
						SmartracSQLiteHelper.TABLE_MOTIONS, null, values);
				newIds.add(insertId);
			}
			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			database.endTransaction();
		}
	}

	/**
	 * Delete given motion data from motions table.
	 * 
	 * @param motionData
	 */
	public synchronized void delete(MotionData motionData) {
		long id = motionData.getId();
		Log.i(TAG, "Motion data deleted with id: " + id);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_MOTIONS,
				SmartracSQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	/**
	 * Delete all records from motions table.
	 */
	public synchronized void deleteAll() {
		Log.i(TAG, "delete all motion datas");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_MOTIONS, null, null);
	}

	/**
	 * Get a list of motions datas that match given time range.
	 * 
	 * @param start
	 * @param end
	 * @return List of motion data.
	 */
	public synchronized List<MotionData> getByDateRange(final Date start,
			final Date end) {

		Log.i(TAG, "Get motion datas for "
				+ SmartracDataFormat.getIso8601Format().format(start) + " - "
				+ SmartracDataFormat.getIso8601Format().format(end));

		List<MotionData> mDatas = new ArrayList<MotionData>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(SmartracSQLiteHelper.TABLE_MOTIONS,
				allColumns, SmartracSQLiteHelper.COLUMN_TIME + " BETWEEN \""
						+ SmartracDataFormat.getIso8601Format().format(start)
						+ "\" AND \""
						+ SmartracDataFormat.getIso8601Format().format(end)
						+ "\";", null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MotionData mData = cursorToMotionData(cursor);
			mDatas.add(mData);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		return mDatas;
	}

	/**
	 * Get all motion datas from motions table.
	 * 
	 * @return
	 */
	public synchronized List<MotionData> getAll() {
		List<MotionData> mDatas = new ArrayList<MotionData>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(SmartracSQLiteHelper.TABLE_MOTIONS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			MotionData mData = cursorToMotionData(cursor);
			mDatas.add(mData);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return mDatas;
	}

	private MotionData cursorToMotionData(Cursor cursor) {
		int id = cursor.getInt(COLUMN_ID);
		String timeString = cursor.getString(COLUMN_TIME);
		Date time = null;
		try {
			time = SmartracDataFormat.getIso8601Format().parse(timeString);
		} catch (ParseException e) {
			time = null;
		}

		float linearX = cursor.getFloat(COLUMN_LINEAR_X);
		float linearY = cursor.getFloat(COLUMN_LINEAR_Y);
		float linearZ = cursor.getFloat(COLUMN_LINEAR_Z);
		float linearMag = cursor.getFloat(COLUMN_LINEAR_MAGNITUDE);

		float trueX = cursor.getFloat(COLUMN_TRUE_X);
		float trueY = cursor.getFloat(COLUMN_TRUE_Y);
		float trueZ = cursor.getFloat(COLUMN_TRUE_Z);
		float trueMag = cursor.getFloat(COLUMN_TRUE_MAGNITUDE);

		float[] linearAcc = { linearX, linearY, linearZ, linearMag };
		float[] trueAcc = { trueX, trueY, trueZ, trueMag };

		MotionData data = new MotionData(id, time, linearAcc, trueAcc);

		return data;
	}

	private ContentValues MotionDataToContentValues(MotionData mData) {
		ContentValues values = new ContentValues();

		values.put(SmartracSQLiteHelper.COLUMN_TIME, SmartracDataFormat
				.getIso8601Format().format(mData.getTime()));
		values.put(SmartracSQLiteHelper.COLUMN_LINEAR_X, mData.getLinearX());
		values.put(SmartracSQLiteHelper.COLUMN_LINEAR_Y, mData.getLinearY());
		values.put(SmartracSQLiteHelper.COLUMN_LINEAR_Z, mData.getLinearZ());
		values.put(SmartracSQLiteHelper.COLUMN_LINEAR_MAGNITUDE,
				mData.getLinearMag());
		values.put(SmartracSQLiteHelper.COLUMN_TRUE_X, mData.getTrueX());
		values.put(SmartracSQLiteHelper.COLUMN_TRUE_Y, mData.getTrueY());
		values.put(SmartracSQLiteHelper.COLUMN_TRUE_Z, mData.getTrueZ());
		values.put(SmartracSQLiteHelper.COLUMN_TRUE_MAGNITUDE,
				mData.getTrueMag());

		return values;
	}
}
