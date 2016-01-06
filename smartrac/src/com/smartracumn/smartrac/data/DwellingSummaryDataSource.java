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
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.DwellingLocation;

public class DwellingSummaryDataSource {
	private final String TAG = getClass().getSimpleName();

	private SmartracSQLiteHelper dbHelper;

	private final String[] allColumns = { SmartracSQLiteHelper.COLUMN_ID,
			SmartracSQLiteHelper.COLUMN_LOCATION,
			SmartracSQLiteHelper.COLUMN_MOSTLIKELY_ACTIVITY_ID,
			SmartracSQLiteHelper.COLUMN_VISIT_FREQ };

	private final int COLUMN_ID = 0, COLUMN_LOCATION = 1,
			COLUMN_MOSTLIKELY_ACTIVITY_ID = 2, COLUMN_VISIT_FREQ = 3;

	/**
	 * Initializes a new instance of the DwellingLocationDataSource class.
	 * 
	 * @param context
	 *            The application context.
	 */
	public DwellingSummaryDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
	}

	/**
	 * Create dwelling summary based on given activity and its associated
	 * dwelling location.
	 * 
	 * @param activity
	 * @return
	 */
	private long createDwellingSummary(ActivityCalendarItem activity) {
		// Log.i(TAG, "check dwelling summary for activity calendar item: "
		// + activity.getId());

		if (activity.getId() == 0
				|| activity.getAssociateDwellingLocation() == null
				|| activity.getAssociateDwellingLocation().getActivity() == CalendarItem.Activity.UNKNOWN_ACTIVITY) {
			return 0;
		}

		// Log.i(TAG, "create dwelling summary for activity calendar item: "
		// + activity.getId());
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.beginTransaction();

			long dwellingLocationId = activity.getAssociateDwellingLocation()
					.getId();

			if (dwellingLocationId == 0) {
				ContentValues values = new ContentValues();
				values.put(SmartracSQLiteHelper.COLUMN_LOCATION, activity
						.getAssociateDwellingLocation().getCode());
				values.put(SmartracSQLiteHelper.COLUMN_MOSTLIKELY_ACTIVITY_ID,
						activity.getAssociateDwellingLocation().getActivity()
								.getValue());

				dwellingLocationId = database.insert(
						SmartracSQLiteHelper.TABLE_DWELLING_LOCATIONS, null,
						values);
			}

			ContentValues values = new ContentValues();
			values.put(SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID,
					activity.getDwellingRegionId());
			values.put(SmartracSQLiteHelper.COLUMN_DWELLING_LOCATION_ID,
					dwellingLocationId);

			String[] columns = { SmartracSQLiteHelper.COLUMN_ID };

			Cursor cursor = database.query(
					SmartracSQLiteHelper.TABLE_DWELLING_SUMMARY, columns,
					SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID + " = "
							+ activity.getDwellingRegionId(), null, null, null,
					null);

			if (cursor.moveToFirst()) {
				database.delete(SmartracSQLiteHelper.TABLE_DWELLING_SUMMARY,
						SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID + " = "
								+ cursor.getLong(0), null);
			}

			cursor.close();

			long summaryId = database.insert(
					SmartracSQLiteHelper.TABLE_DWELLING_SUMMARY, null, values);

			database.setTransactionSuccessful();
			return summaryId;
		} catch (SQLException e) {
			return 0;
		} finally {
			database.endTransaction();
		}
	}

	/**
	 * Created dwelling summaries for activity calendar items presented in given
	 * calendar item.
	 * 
	 * @param items
	 * @return
	 */
	public boolean createDwellingSummaries(List<CalendarItem> items) {
		List<Long> insertedIds = new ArrayList<Long>();

		for (CalendarItem item : items) {
			if (item instanceof ActivityCalendarItem) {
				insertedIds.add(this
						.createDwellingSummary((ActivityCalendarItem) item));
			}
		}

		return insertedIds.size() > 0;
	}

	/**
	 * Insert a list of dwelling locations to dwelling locations table.
	 * 
	 * @param locations
	 */
	private long insert(long dwellingRegionId, long dwellingLocationId) {
		Log.i(TAG, "insert dwelling summary.");

		SQLiteDatabase database = dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID,
				dwellingRegionId);
		values.put(SmartracSQLiteHelper.COLUMN_DWELLING_LOCATION_ID,
				dwellingLocationId);

		return database.insert(SmartracSQLiteHelper.TABLE_DWELLING_SUMMARY,
				null, values);
	}

	/**
	 * Delete dwelling location from dwelling locations table.
	 * 
	 * @param location
	 */
	private void delete(long dwellingRegionId) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		Log.i(TAG, "Dwelling summary deleted for region: " + dwellingRegionId);
		database.delete(SmartracSQLiteHelper.TABLE_DWELLING_SUMMARY,
				SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID + " = "
						+ dwellingRegionId, null);
	}

	public void delete(ActivityCalendarItem activity) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		String[] columns = { SmartracSQLiteHelper.COLUMN_ID };

		Cursor cursor = database.query(
				SmartracSQLiteHelper.TABLE_DWELLING_SUMMARY, columns,
				SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID + " = "
						+ activity.getDwellingRegionId(), null, null, null,
				null);

		if (cursor.moveToFirst()) {
			database.delete(SmartracSQLiteHelper.TABLE_DWELLING_SUMMARY,
					SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID + " = "
							+ cursor.getLong(0), null);
		}

		cursor.close();
	}

	public void update(long dwellingRegionId, long dwellingLocationId) {
		delete(dwellingRegionId);
		insert(dwellingRegionId, dwellingLocationId);
	}

	/**
	 * Delete all dwelling locations.
	 */
	public void deleteAll() {
		Log.i(TAG, "All dwelling summaries deleted");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(SmartracSQLiteHelper.TABLE_DWELLING_SUMMARY, null, null);
	}

	/**
	 * Get all dwelling locations in table.
	 * 
	 * @return
	 */
	public List<DwellingLocation> getAll() {
		Log.i(TAG, "Get all dwelling locations");
		List<DwellingLocation> dls = new ArrayList<DwellingLocation>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(
				SmartracSQLiteHelper.VIEW_DWELLING_SUMMARY, allColumns, null,
				null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			DwellingLocation dl = cursorToDwellingLocationWithFrequency(cursor);
			dls.add(dl);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return dls;
	}

	private DwellingLocation cursorToDwellingLocationWithFrequency(Cursor cursor) {
		long id = cursor.getLong(COLUMN_ID);
		String locationCode = cursor.getString(COLUMN_LOCATION);
		LatLng location = PolyUtil.decode(locationCode).get(0);
		CalendarItem.Activity activity = CalendarItem.Activity.get(cursor
				.getInt(COLUMN_MOSTLIKELY_ACTIVITY_ID));
		int visitFreq = cursor.getInt(COLUMN_VISIT_FREQ);

		DwellingLocation dl = new DwellingLocation(id, location, activity);
		dl.setVisitFrequency(visitFreq);
		dl.setCode(locationCode);

		return dl;
	}
}
