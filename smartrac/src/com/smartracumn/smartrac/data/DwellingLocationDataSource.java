package com.smartracumn.smartrac.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.DwellingLocation;

/**
 * The class represents dwelling location data source.
 * 
 * @author kangx385
 * 
 */
public class DwellingLocationDataSource {
	private final String TAG = getClass().getSimpleName();

	private SmartracSQLiteHelper dbHelper;
	private final String[] allColumns = { SmartracSQLiteHelper.COLUMN_ID,
			SmartracSQLiteHelper.COLUMN_LOCATION,
			SmartracSQLiteHelper.COLUMN_MOSTLIKELY_ACTIVITY_ID };

	private final int COLUMN_ID = 0, COLUMN_LOCATION = 1,
			COLUMN_MOSTLIKELY_ACTIVITY_ID = 2;

	/**
	 * Initializes a new instance of the DwellingLocationDataSource class.
	 * 
	 * @param context
	 *            The application context.
	 */
	public DwellingLocationDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
	}

	/**
	 * Insert a list of dwelling locations to dwelling locations table.
	 * 
	 * @param locations
	 */
	public boolean insert(List<DwellingLocation> locations) {
		Log.i(TAG, "insert " + locations.size() + " dwelling locations.");

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.beginTransaction();
			for (DwellingLocation loc : locations) {
				String[] selectionArgs = { loc.getCode(),
						String.valueOf(loc.getActivity().getValue()) };
				Cursor cursor = database
						.query(SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS,
								allColumns,
								SmartracSQLiteHelper.COLUMN_LOCATION
										+ " = ? AND "
										+ SmartracSQLiteHelper.COLUMN_MOSTLIKELY_ACTIVITY_ID
										+ " = ?", selectionArgs, null, null,
								null);

				if (cursor.moveToFirst()) {
					// DwellingLocation existing =
					// cursorToDwellingLocation(cursor);
					// ContentValues values = new ContentValues();
					// values.put(SmartracSQLiteHelper.COLUMN_VISIT_FREQ,
					// existing.getVisitFrequency() + 1);
					//
					// database.update(
					// SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS,
					// values, SmartracSQLiteHelper.COLUMN_ID + " = "
					// + existing.getId(), null);
				} else {
					database.insert(
							SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS,
							null, dwellingLocationToContentValues(loc));
				}

				cursor.close();
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
	 * Create dwelling location in dwelling locations table.
	 * 
	 * @param location
	 *            The location latitude and longitude.
	 * @param activity
	 *            Most likely activity.
	 * @return The Dwelling location inserted into dwelling locations table.
	 */
	public DwellingLocation createDwellingLocation(LatLng location,
			CalendarItem.Activity activity) {
		Log.i(TAG, ": create dwelling location");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		List<LatLng> locs = new ArrayList<LatLng>();
		locs.add(location);
		String locationCode = PolyUtil.encode(locs);

		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_LOCATION, locationCode);
		values.put(SmartracSQLiteHelper.COLUMN_MOSTLIKELY_ACTIVITY_ID,
				activity.getValue());

		long insertId = database.insert(
				SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS, null, values);

		return new DwellingLocation(insertId, location, activity);
	}

	/**
	 * Delete dwelling location from dwelling locations table.
	 * 
	 * @param location
	 */
	public void delete(DwellingLocation location) {
		long id = location.getId();
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		if (id != 0) {
			Log.i(TAG, "Dwelling location deleted with id: " + id);
			database.delete(SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS,
					SmartracSQLiteHelper.COLUMN_ID + " = " + id, null);
		} else {
			Log.i(TAG, "Dwelling location deleted with location and activity: "
					+ location.getActivity());
			String[] selectionArgs = { location.getCode(),
					String.valueOf(location.getActivity().getValue()) };
			Cursor cursor = database
					.query(SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS,
							allColumns,
							SmartracSQLiteHelper.COLUMN_LOCATION
									+ " = ? AND "
									+ SmartracSQLiteHelper.COLUMN_MOSTLIKELY_ACTIVITY_ID
									+ " = ?", selectionArgs, null, null, null);

			if (cursor.moveToFirst()) {
				DwellingLocation existing = cursorToDwellingLocation(cursor);
				// if (existing.getVisitFrequency() > 0) {
				// ContentValues values = new ContentValues();
				// values.put(SmartracSQLiteHelper.COLUMN_VISIT_FREQ,
				// existing.getVisitFrequency() - 1);
				//
				// database.update(
				// SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS,
				// values, SmartracSQLiteHelper.COLUMN_ID + " = "
				// + existing.getId(), null);
				// }
				database.delete(
						SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS,
						SmartracSQLiteHelper.COLUMN_ID + " = "
								+ existing.getId(), null);
			}

			cursor.close();
		}
	}

	/**
	 * Delete a list of dwelling locations.
	 * 
	 * @param dwellingLocations
	 */
	public boolean delete(List<DwellingLocation> dwellingLocations) {
		for (DwellingLocation location : dwellingLocations) {
			delete(location);
		}

		return true;
	}

	/**
	 * Delete all dwelling locations.
	 */
	public void deleteAll() {
		Log.i(TAG, "All dwelling locations deleted");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS, null,
				null);
	}

	/**
	 * Get dwelling locations by activity type.
	 * 
	 * @param activity
	 * @return
	 */
	public List<DwellingLocation> getByActivityType(
			CalendarItem.Activity activity) {

		// Not returning data.
		Log.i(TAG,
				"Get dwelling locations with activity " + activity.toString());

		List<DwellingLocation> dls = new ArrayList<DwellingLocation>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(SmartracSQLiteHelper.TABLE_DWELLINGS,
				allColumns, SmartracSQLiteHelper.COLUMN_MOSTLIKELY_ACTIVITY_ID
						+ " = " + activity.getValue(), null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			DwellingLocation dl = cursorToDwellingLocation(cursor);
			dls.add(dl);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		return dls;
	}

	/**
	 * Get all dwelling locations in table.
	 * 
	 * @return
	 */
	public List<DwellingLocation> getAll() {
		List<DwellingLocation> dls = new ArrayList<DwellingLocation>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(
				SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS, allColumns,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			DwellingLocation dl = cursorToDwellingLocation(cursor);
			dls.add(dl);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return dls;
	}

	private DwellingLocation cursorToDwellingLocation(Cursor cursor) {
		long id = cursor.getLong(COLUMN_ID);
		String locationCode = cursor.getString(COLUMN_LOCATION);
		LatLng location = PolyUtil.decode(locationCode).get(0);
		CalendarItem.Activity activity = CalendarItem.Activity.get(cursor
				.getInt(COLUMN_MOSTLIKELY_ACTIVITY_ID));

		DwellingLocation dl = new DwellingLocation(id, location, activity);
		dl.setCode(locationCode);

		return dl;
	}

	private ContentValues dwellingLocationToContentValues(
			DwellingLocation location) {
		ContentValues values = new ContentValues();

		if (location.getId() != 0) {
			values.put(SmartracSQLiteHelper.COLUMN_ID, location.getId());
		}

		values.put(SmartracSQLiteHelper.COLUMN_LOCATION, location.getCode());
		values.put(SmartracSQLiteHelper.COLUMN_MOSTLIKELY_ACTIVITY_ID, location
				.getActivity().getValue());

		return values;
	}
}
