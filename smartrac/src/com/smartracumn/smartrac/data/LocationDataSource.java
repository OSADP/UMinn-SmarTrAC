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
import android.location.Location;
import android.util.Log;

import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * Location data source which is used to interact with locations table in
 * SQLiteDatabase.
 * 
 * @author kangx385
 * 
 */
public class LocationDataSource {
	private final String TAG = getClass().getSimpleName();

	private SmartracSQLiteHelper dbHelper;

	private final String[] allColumns = { SmartracSQLiteHelper.COLUMN_ID,
			SmartracSQLiteHelper.COLUMN_TIME,
			SmartracSQLiteHelper.COLUMN_LATITUDE,
			SmartracSQLiteHelper.COLUMN_LONGITUDE,
			SmartracSQLiteHelper.COLUMN_SPEED,
			SmartracSQLiteHelper.COLUMN_PROVIDER,
			SmartracSQLiteHelper.COLUMN_ACCURACY,
			SmartracSQLiteHelper.COLUMN_ALTITUDE,
			SmartracSQLiteHelper.COLUMN_BEARING };

	private final int COLUMN_ID = 0, COLUMN_TIME = 1, COLUMN_LATITUDE = 2,
			COLUMN_LONGITUDE = 3, COLUMN_SPEED = 4, COLUMN_PROVIDER = 5,
			COLUMN_ACCURACY = 6, COLUMN_ALTITUDE = 7, COLUMN_BEARING = 8;

	/**
	 * Initializes a new instance of the LocationDataSource class.
	 * 
	 * @param context
	 */
	public LocationDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
	}

	/**
	 * Insert a list of location wrappers into locations table.
	 * 
	 * @param locs
	 *            Locations wrappers.
	 * @return True if insert successfully.
	 */
	public synchronized boolean insert(List<LocationWrapper> locs) {
		Log.i(TAG, "insert " + locs.size() + " location records.");
		List<Long> newIds = new ArrayList<Long>();
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.beginTransaction();
			for (LocationWrapper loc : locs) {
				ContentValues values = LocationWrapperToContentValues(loc);
				long insertId = database.insert(
						SmartracSQLiteHelper.TABLE_LOCATIONS, null, values);
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
	 * Insert a list of location wrappers into locations table.
	 * 
	 * @param locs
	 *            Locations wrappers.
	 * @return True if insert successfully.
	 */
	public synchronized boolean insertIntermediate(List<LocationWrapper> locs) {
		Log.i(TAG, "insert " + locs.size()
				+ " location records to intermediate locations.");
		List<Long> newIds = new ArrayList<Long>();
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.beginTransaction();
			for (LocationWrapper loc : locs) {
				ContentValues values = LocationWrapperToContentValues(loc);
				long insertId = database.insert(
						SmartracSQLiteHelper.TABLE_INTERMEDIATE_LOCATIONS,
						null, values);
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
	 * Delete given location wrapper from locations table.
	 * 
	 * @param loc
	 *            The location Wrapper.
	 */
	public synchronized void delete(LocationWrapper loc) {
		long id = loc.getId();
		Log.i(TAG, "Location deleted with id: " + id);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_LOCATIONS,
				SmartracSQLiteHelper.COLUMN_ID + " = " + id, null);
	}

	/**
	 * Delete all data from locations table.
	 */
	public synchronized void deleteAll() {
		Log.i(TAG, "Records deleted");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_LOCATIONS, null, null);
	}

	/**
	 * Get location wrappers that match given time range.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public synchronized List<LocationWrapper> getByDateRange(final Date start,
			final Date end) {

		List<LocationWrapper> locs = new ArrayList<LocationWrapper>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(
				SmartracSQLiteHelper.TABLE_INTERMEDIATE_LOCATIONS, allColumns,
				SmartracSQLiteHelper.COLUMN_TIME + " BETWEEN \""
						+ SmartracDataFormat.getIso8601Format().format(start)
						+ "\" AND \""
						+ SmartracDataFormat.getIso8601Format().format(end)
						+ "\";", null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocationWrapper loc = cursorToLocationWrapper(cursor);
			locs.add(loc);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		Log.i(TAG, "Get locations for "
				+ SmartracDataFormat.getIso8601Format().format(start) + " - "
				+ SmartracDataFormat.getIso8601Format().format(end)
				+ ". Location size: " + locs.size());

		// Log.i(TAG,
		// "locations time "
		// + SmartracDataFormat.getIso8601Format().format(
		// locs.get(0).getTime())
		// + " "
		// + SmartracDataFormat.getIso8601Format().format(
		// locs.get(locs.size() - 1).getTime()));

		return locs;
	}

	/**
	 * Get all location wrappers presented in locations table.
	 * 
	 * @return
	 */
	public synchronized List<LocationWrapper> getAll() {
		List<LocationWrapper> locs = new ArrayList<LocationWrapper>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(SmartracSQLiteHelper.TABLE_LOCATIONS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			LocationWrapper loc = cursorToLocationWrapper(cursor);
			locs.add(loc);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return locs;
	}

	private LocationWrapper cursorToLocationWrapper(Cursor cursor) {
		int id = cursor.getInt(COLUMN_ID);
		String timeString = cursor.getString(COLUMN_TIME);
		Date time = null;
		try {
			time = SmartracDataFormat.getIso8601Format().parse(timeString);
		} catch (ParseException e) {
			time = null;
		}
		double latitude = cursor.getDouble(COLUMN_LATITUDE);
		double longitude = cursor.getDouble(COLUMN_LONGITUDE);
		double altitude = cursor.getDouble(COLUMN_ALTITUDE);
		String provider = cursor.getString(COLUMN_PROVIDER);
		float speed = cursor.getFloat(COLUMN_SPEED);
		float accuracy = cursor.getFloat(COLUMN_ACCURACY);
		float bearing = cursor.getFloat(COLUMN_BEARING);

		Location loc = new Location(provider);
		loc.setTime(time.getTime());
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		loc.setAltitude(altitude);
		loc.setSpeed(speed);
		loc.setAccuracy(accuracy);
		loc.setBearing(bearing);

		return new LocationWrapper(id, loc);
	}

	private ContentValues LocationWrapperToContentValues(LocationWrapper loc) {
		ContentValues values = new ContentValues();

		values.put(SmartracSQLiteHelper.COLUMN_TIME, SmartracDataFormat
				.getIso8601Format().format(loc.getTime()));
		values.put(SmartracSQLiteHelper.COLUMN_LATITUDE, loc.getLocation()
				.getLatitude());
		values.put(SmartracSQLiteHelper.COLUMN_LONGITUDE, loc.getLocation()
				.getLongitude());
		values.put(SmartracSQLiteHelper.COLUMN_SPEED, loc.getLocation()
				.getSpeed());
		values.put(SmartracSQLiteHelper.COLUMN_PROVIDER, loc.getLocation()
				.getProvider());
		values.put(SmartracSQLiteHelper.COLUMN_ACCURACY, loc.getLocation()
				.getAccuracy());
		values.put(SmartracSQLiteHelper.COLUMN_ALTITUDE, loc.getLocation()
				.getAltitude());
		values.put(SmartracSQLiteHelper.COLUMN_BEARING, loc.getLocation()
				.getBearing());

		return values;
	}
}
