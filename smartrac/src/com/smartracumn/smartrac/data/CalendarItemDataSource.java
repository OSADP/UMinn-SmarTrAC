package com.smartracumn.smartrac.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.ServiceOffCalendarItem;
import com.smartracumn.smartrac.model.TripCalendarItem;
import com.smartracumn.smartrac.util.CalendarItemUtil.ActivityMapDrawable;
import com.smartracumn.smartrac.util.CalendarItemUtil.TripMapDrawable;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * Calendar item data source which is used to interact with calendar item tables
 * in SQLiteDatabase.
 * 
 * @author kangx385
 * 
 */
public class CalendarItemDataSource {
	private final String TAG = getClass().getSimpleName();
	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private SmartracSQLiteHelper dbHelper;

	private final static String lhs = "LHS.";

	private final static String rhs = "RHS.";

	private final static int MOOD_DEFAULT = 3;

	public final static String TRIP_CALENDAR_ITEM_TABLE = SmartracSQLiteHelper.TABLE_TRIP_SEGMENTS
			+ " AS LHS LEFT JOIN "
			+ SmartracSQLiteHelper.TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS
			+ " AS M ON LHS."
			+ SmartracSQLiteHelper.COLUMN_ID
			+ " = M."
			+ SmartracSQLiteHelper.COLUMN_TRIP_SEGMENT_ID
			+ " LEFT JOIN "
			+ SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS
			+ " AS RHS ON M."
			+ SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
			+ " = RHS."
			+ SmartracSQLiteHelper.COLUMN_ID;

	public final static String ACTIVITY_CALENDAR_ITEM_TABLE = SmartracSQLiteHelper.TABLE_DWELLING_REGIONS
			+ " AS LHS LEFT JOIN "
			+ SmartracSQLiteHelper.TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS
			+ " AS M ON LHS."
			+ SmartracSQLiteHelper.COLUMN_ID
			+ " = M."
			+ SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID
			+ " LEFT JOIN "
			+ SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS
			+ " AS RHS ON M."
			+ SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
			+ " = RHS."
			+ SmartracSQLiteHelper.COLUMN_ID;

	public final static String[] tripColumns = {
			rhs + SmartracSQLiteHelper.COLUMN_ID,
			rhs + SmartracSQLiteHelper.COLUMN_START_TIME,
			rhs + SmartracSQLiteHelper.COLUMN_END_TIME,
			rhs + SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_TYPE_ID,
			lhs + SmartracSQLiteHelper.COLUMN_TRIP_ID,
			lhs + SmartracSQLiteHelper.COLUMN_PREDICTED_MODE,
			lhs + SmartracSQLiteHelper.COLUMN_USER_CORRECTED_MODE,
			lhs + SmartracSQLiteHelper.COLUMN_TRIP_SEGMENT,
			lhs + SmartracSQLiteHelper.COLUMN_ID };

	public final static String[] activityColumns = {
			rhs + SmartracSQLiteHelper.COLUMN_ID,
			rhs + SmartracSQLiteHelper.COLUMN_START_TIME,
			rhs + SmartracSQLiteHelper.COLUMN_END_TIME,
			rhs + SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_TYPE_ID,
			lhs + SmartracSQLiteHelper.COLUMN_PREDICTED_ACTIVITY,
			lhs + SmartracSQLiteHelper.COLUMN_USER_CORRECTED_ACTIVITY,
			lhs + SmartracSQLiteHelper.COLUMN_DWELLING_REGION,
			lhs + SmartracSQLiteHelper.COLUMN_ID };

	public final static String[] serviceOffColumns = {
			SmartracSQLiteHelper.COLUMN_ID,
			SmartracSQLiteHelper.COLUMN_START_TIME,
			SmartracSQLiteHelper.COLUMN_END_TIME,
			SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_TYPE_ID };

	private final int CALENDAR_ITEM_ID = 0, START_TIME = 1, END_TIME = 2,
			CALENDAR_ITEM_TYPE_ID = 3, TRIP_ID = 4, PREDICTED_ACTIVITY = 4,
			PREDICTED_MODE = 5, USER_CORRECTED_ACTIVITY = 5,
			USER_CORRECTED_MODE = 6, DWELLING_REGION = 6, TRIP_SEGMENT = 7,
			DWELLING_REGION_ID = 7, TRIP_SEGMENT_ID = 8;

	public CalendarItemDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
	}

	/**
	 * Insert calendar items into calendar item tables.
	 * 
	 * @param calendarItems
	 *            List of calendar items needs to be saved.
	 * @return True if insert successfully.
	 */
	public boolean insert(List<CalendarItem> calendarItems) {
		List<TripCalendarItem> trips = new ArrayList<TripCalendarItem>();
		List<ActivityCalendarItem> activities = new ArrayList<ActivityCalendarItem>();
		List<ServiceOffCalendarItem> noDatas = new ArrayList<ServiceOffCalendarItem>();

		for (CalendarItem calendarItem : calendarItems) {
			if (calendarItem.getType() == CalendarItem.Type.TRIP) {
				trips.add((TripCalendarItem) calendarItem);
			} else if (calendarItem.getType() == CalendarItem.Type.ACTIVITY) {
				activities.add((ActivityCalendarItem) calendarItem);
			} else if (calendarItem.getType() == CalendarItem.Type.SERVICE_Off) {
				noDatas.add((ServiceOffCalendarItem) calendarItem);
			}
		}

		// Have to insert activity first to make sure trip ids are updated
		// correctly.
		boolean activitiesInsertSuccess = insertActivityCalendarItem(activities);
		boolean tripsInsertSuccess = insertTripCalendarItem(trips);
		boolean noDatasInsertSuccess = insertNoDataCalendarItem(noDatas);

		return tripsInsertSuccess && activitiesInsertSuccess
				&& noDatasInsertSuccess;
	}

	private boolean insertActivityCalendarItem(
			List<ActivityCalendarItem> activities) {
		Log.i(TAG, "insert " + activities.size() + " activity calendar items.");
		for (ActivityCalendarItem item : activities) {
			Log.i(TAG,
					"insert "
							+ SmartracDataFormat.getDateTimeFormat().format(
									item.getStart())
							+ " - "
							+ SmartracDataFormat.getDateTimeFormat().format(
									item.getEnd()));
		}

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.beginTransaction();
			for (ActivityCalendarItem activity : activities) {
				ContentValues[] values = activityCalendarItemToContentValues(activity);
				long calendarItemId = database.insert(
						SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS, null,
						values[0]);

				long dwellingRegionId = database.insert(
						SmartracSQLiteHelper.TABLE_DWELLING_REGIONS, null,
						values[1]);
				database.insert(
						SmartracSQLiteHelper.TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS,
						null,
						getActivityCalendarItemRelationshipContentValues(
								calendarItemId, dwellingRegionId));

				database.insert(
						SmartracSQLiteHelper.TABLE_ACTIVITY_USER_SUMMARY, null,
						getUserSummaryContentValues(calendarItemId));

				activity.setDwellingRegionId(dwellingRegionId);
				activity.setId(calendarItemId);
			}
			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			database.endTransaction();
		}
	}

	public boolean insertTripCalendarItem(List<TripCalendarItem> trips) {
		Log.i(TAG, "insert " + trips.size() + " trip calendar items.");
		for (TripCalendarItem item : trips) {
			Log.i(TAG,
					"insert "
							+ SmartracDataFormat.getDateTimeFormat().format(
									item.getStart())
							+ " - "
							+ SmartracDataFormat.getDateTimeFormat().format(
									item.getEnd()));
		}

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.beginTransaction();

			for (TripCalendarItem trip : trips) {
				ContentValues[] values = tripCalendarItemToContentValues(trip);
				long calendarItemId = database.insert(
						SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS, null,
						values[0]);

				long tripSegmentId = database.insert(
						SmartracSQLiteHelper.TABLE_TRIP_SEGMENTS, null,
						values[1]);

				database.insert(
						SmartracSQLiteHelper.TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS,
						null,
						getTripCalendarItemRelationshipContentValues(
								calendarItemId, tripSegmentId));

				database.insert(SmartracSQLiteHelper.TABLE_TRIP_USER_SUMMARY,
						null, getUserSummaryContentValues(calendarItemId));

				trip.setId(calendarItemId);
				trip.setTripSegmentId(tripSegmentId);

				updateTripId(tripSegmentId, trip, database);
			}

			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			database.endTransaction();
		}
	}

	private boolean insertNoDataCalendarItem(
			List<ServiceOffCalendarItem> noDatas) {
		Log.i(TAG, "insert " + noDatas.size() + " no data calendar items.");

		if (noDatas.size() == 0) {
			return true;
		}

		for (ServiceOffCalendarItem item : noDatas) {
			Log.i(TAG,
					"insert "
							+ SmartracDataFormat.getDateTimeFormat().format(
									item.getStart())
							+ " - "
							+ SmartracDataFormat.getDateTimeFormat().format(
									item.getEnd()));
		}

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		try {
			database.beginTransaction();

			for (ServiceOffCalendarItem noData : noDatas) {
				ContentValues value = noDataCalendarItemToContentValues(noData);
				long calendarItemId = database.insert(
						SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS, null, value);

				noData.setId(calendarItemId);
			}

			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			database.endTransaction();
		}
	}

	private List<ServiceOffCalendarItem> getNoDatasByDateRange(Date start,
			Date end) {
		Log.i(TAG, "Get no datas for "
				+ SmartracDataFormat.getIso8601Format().format(start) + " - "
				+ SmartracDataFormat.getIso8601Format().format(end));

		List<ServiceOffCalendarItem> noDatas = new ArrayList<ServiceOffCalendarItem>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(
				SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS, serviceOffColumns,
				SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_TYPE_ID
						+ " >= 3 AND ("
						+ SmartracSQLiteHelper.COLUMN_START_TIME
						+ " BETWEEN \""
						+ SmartracDataFormat.getIso8601Format().format(start)
						+ "\" AND \""
						+ SmartracDataFormat.getIso8601Format().format(end)
						+ "\" OR " + SmartracSQLiteHelper.COLUMN_END_TIME
						+ " BETWEEN \""
						+ SmartracDataFormat.getIso8601Format().format(start)
						+ "\" AND \""
						+ SmartracDataFormat.getIso8601Format().format(end)
						+ "\");", null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ServiceOffCalendarItem noData = cursorToNoDataCalendarItem(cursor);
			noDatas.add(noData);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		Log.i(TAG, " get " + noDatas.size() + " no data calendar items.");

		return noDatas;
	}

	private void updateTripId(long tripSegmentId, TripCalendarItem trip,
			SQLiteDatabase db) {
		String checkExistance = "SELECT "
				+ SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_TYPE_ID + " FROM "
				+ SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS + " WHERE "
				+ SmartracSQLiteHelper.COLUMN_END_TIME + " <= " + "\""
				+ SmartracDataFormat.getIso8601Format().format(trip.getStart())
				+ "\"" + " ORDER BY " + SmartracSQLiteHelper.COLUMN_END_TIME
				+ " DESC LIMIT 1;";

		String getTripId = "SELECT " + lhs
				+ SmartracSQLiteHelper.COLUMN_TRIP_ID + " FROM "
				+ TRIP_CALENDAR_ITEM_TABLE + " WHERE " + rhs
				+ SmartracSQLiteHelper.COLUMN_END_TIME + " <= \""
				+ SmartracDataFormat.getIso8601Format().format(trip.getStart())
				+ "\"" + " ORDER BY " + SmartracSQLiteHelper.COLUMN_END_TIME
				+ " DESC LIMIT 1;";

		Cursor cursor = db.rawQuery(checkExistance, null);

		ContentValues values = new ContentValues();

		if (cursor.moveToFirst()
				&& CalendarItem.Type.get(cursor.getInt(0)) == CalendarItem.Type.TRIP) {
			Cursor cursor2 = db.rawQuery(getTripId, null);
			if (cursor2.moveToFirst()) {
				values.put(SmartracSQLiteHelper.COLUMN_TRIP_ID,
						cursor2.getLong(0));
			} else {
				values.put(SmartracSQLiteHelper.COLUMN_TRIP_ID, tripSegmentId);
			}

			cursor2.close();
		} else {
			values.put(SmartracSQLiteHelper.COLUMN_TRIP_ID, tripSegmentId);
		}

		cursor.close();

		db.update(SmartracSQLiteHelper.TABLE_TRIP_SEGMENTS, values,
				SmartracSQLiteHelper.COLUMN_ID + " = " + tripSegmentId, null);
	}

	/**
	 * Delete all calendar items from database.
	 */
	public void deleteAll() {
		Log.i(TAG, "All calendar items deleted");
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(
				SmartracSQLiteHelper.TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS,
				null, null);
		database.delete(
				SmartracSQLiteHelper.TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS,
				null, null);
		database.delete(SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS, null, null);
		database.delete(SmartracSQLiteHelper.TABLE_DWELLING_REGIONS, null, null);
		database.delete(SmartracSQLiteHelper.TABLE_TRIP_SEGMENTS, null, null);
	}

	/**
	 * Delete given calendar items.
	 * 
	 * @param calendarItems
	 * @return true if deleted successfully.
	 */
	public boolean delete(List<CalendarItem> calendarItems) {
		List<TripCalendarItem> trips = new ArrayList<TripCalendarItem>();
		List<ActivityCalendarItem> activities = new ArrayList<ActivityCalendarItem>();
		List<ServiceOffCalendarItem> noDatas = new ArrayList<ServiceOffCalendarItem>();

		for (CalendarItem item : calendarItems) {
			if (item instanceof TripCalendarItem) {
				trips.add((TripCalendarItem) item);
			}

			if (item instanceof ActivityCalendarItem) {
				activities.add((ActivityCalendarItem) item);
			}

			if (item instanceof ServiceOffCalendarItem) {
				noDatas.add((ServiceOffCalendarItem) item);
			}
		}

		Log.i(TAG, "delete " + trips.size() + " trips, " + activities.size()
				+ " activities, " + noDatas.size() + " no datas.");

		return deleteTripCalendarItem(trips)
				&& deleteActivityCalendarItem(activities)
				&& deleteServiceOffCalendarItem(noDatas);
	}

	public boolean deleteTripCalendarItem(List<TripCalendarItem> trips) {
		if (trips.size() == 0) {
			return true;
		}

		Log.i(TAG, "delete trip calendar items.");
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		try {
			database.beginTransaction();
			for (TripCalendarItem trip : trips) {
				if (trip.isAdded()) {
					continue;
				}

				Cursor cursor = database
						.rawQuery(
								"SELECT "
										+ SmartracSQLiteHelper.COLUMN_ID
										+ ", "
										+ SmartracSQLiteHelper.COLUMN_TRIP_SEGMENT_ID
										+ " FROM "
										+ SmartracSQLiteHelper.TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS
										+ " WHERE "
										+ SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
										+ " = " + String.valueOf(trip.getId()),
								null);

				if (cursor.moveToFirst()) {
					String[] relationshipId = { String
							.valueOf(cursor.getInt(0)) };
					String[] tripSegmentId = { String.valueOf(cursor.getInt(1)) };
					String[] calendarItemId = { String.valueOf(trip.getId()) };

					database.delete(SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS,
							SmartracSQLiteHelper.COLUMN_ID + " = ?",
							calendarItemId);

					database.delete(SmartracSQLiteHelper.TABLE_TRIP_SEGMENTS,
							SmartracSQLiteHelper.COLUMN_ID + " = ?",
							tripSegmentId);

					database.delete(
							SmartracSQLiteHelper.TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS,
							SmartracSQLiteHelper.COLUMN_ID + " = ?",
							relationshipId);
				} else {
					throw new SQLException(
							"No Trip Relationship Exists for Calendar Item: "
									+ trip.getId());
				}
			}
			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			database.endTransaction();
		}
	}

	private boolean deleteServiceOffCalendarItem(
			List<ServiceOffCalendarItem> noDatas) {
		if (noDatas.size() == 0) {
			return true;
		}

		Log.i(TAG, "delete service off calendar items.");
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		try {
			database.beginTransaction();
			for (ServiceOffCalendarItem noData : noDatas) {
				if (noData.isAdded()) {
					continue;
				}

				String[] noDataId = { String.valueOf(noData.getId()) };

				database.delete(SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS,
						SmartracSQLiteHelper.COLUMN_ID + " = ?", noDataId);
			}
			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			database.endTransaction();
		}
	}

	public boolean deleteActivityCalendarItem(
			List<ActivityCalendarItem> activities) {
		if (activities.size() == 0) {
			return true;
		}

		Log.i(TAG, "delete activity calendar items.");
		SQLiteDatabase database = dbHelper.getWritableDatabase();

		try {
			database.beginTransaction();
			for (ActivityCalendarItem activity : activities) {
				if (activity.isAdded()) {
					continue;
				}

				Cursor cursor = database
						.rawQuery(
								"SELECT "
										+ SmartracSQLiteHelper.COLUMN_ID
										+ ", "
										+ SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID
										+ " FROM "
										+ SmartracSQLiteHelper.TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS
										+ " WHERE "
										+ SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
										+ " = "
										+ String.valueOf(activity.getId()),
								null);

				if (cursor.moveToFirst()) {
					String[] relationshipId = { String
							.valueOf(cursor.getInt(0)) };
					String[] dwellingRegionId = { String.valueOf(cursor
							.getInt(1)) };
					String[] calendarItemId = { String
							.valueOf(activity.getId()) };

					database.delete(SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS,
							SmartracSQLiteHelper.COLUMN_ID + " = ?",
							calendarItemId);

					database.delete(
							SmartracSQLiteHelper.TABLE_DWELLING_REGIONS,
							SmartracSQLiteHelper.COLUMN_ID + " = ?",
							dwellingRegionId);

					database.delete(
							SmartracSQLiteHelper.TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS,
							SmartracSQLiteHelper.COLUMN_ID + " = ?",
							relationshipId);
				} else {
					throw new SQLException(
							"No Trip Relationship Exists for Calendar Item: "
									+ activity.getId());
				}
			}
			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			database.endTransaction();
		}
	}

	/**
	 * Get calendar items by date range.
	 * 
	 * @param start
	 *            The start time.
	 * @param end
	 *            The end time.
	 * @return List of calendar items which fall into given date range.
	 */
	public List<CalendarItem> getByDateRange(Date start, Date end) {
		List<CalendarItem> calendarItems = new ArrayList<CalendarItem>();
		calendarItems.addAll(getTripsByDateRange(start, end));
		calendarItems.addAll(getActivitiesByDateRange(start, end));
		calendarItems.addAll(getNoDatasByDateRange(start, end));

		Collections.sort(calendarItems);

		return calendarItems;
	}

	public Date getLatestEndBefore(Date start) {
		Log.i(TAG, "Get latest calendar Item end before "
				+ SmartracDataFormat.getDateTimeFormat().format(start));

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.rawQuery("SELECT "
				+ SmartracSQLiteHelper.COLUMN_END_TIME + " FROM "
				+ SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS + " WHERE "
				+ SmartracSQLiteHelper.COLUMN_END_TIME + " <= \""
				+ SmartracDataFormat.getIso8601Format().format(start)
				+ "\" ORDER BY " + SmartracSQLiteHelper.COLUMN_END_TIME
				+ " DESC LIMIT 1;", null);

		Calendar c = Calendar.getInstance();
		c.setTime(start);
		Date end = c.getTime();

		if (cursor.moveToFirst()) {
			try {
				end = ISO8601FORMAT.parse(cursor.getString(0));
			} catch (ParseException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}

		return end;
	}

	public Date getEarliestStartAfter(Date end) {
		Log.i(TAG, "Get earliest calendar Item start after "
				+ SmartracDataFormat.getDateTimeFormat().format(end));

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.rawQuery("SELECT "
				+ SmartracSQLiteHelper.COLUMN_START_TIME + " FROM "
				+ SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS + " WHERE "
				+ SmartracSQLiteHelper.COLUMN_START_TIME + " >= \""
				+ SmartracDataFormat.getIso8601Format().format(end)
				+ "\" ORDER BY " + SmartracSQLiteHelper.COLUMN_START_TIME
				+ " LIMIT 1;", null);

		Calendar c = Calendar.getInstance();
		c.setTime(end);
		Date start = c.getTime();

		if (cursor.moveToFirst()) {
			try {
				start = ISO8601FORMAT.parse(cursor.getString(0));
			} catch (ParseException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}

		return start;
	}

	/**
	 * Get the end time of the latest calendar item.
	 * 
	 * @return The end time of the latest calendar item.
	 */
	public Date getLatestEnd() {
		Log.i(TAG, "Get latest calendar Item end.");

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database
				.rawQuery("SELECT " + SmartracSQLiteHelper.COLUMN_END_TIME
						+ " FROM " + SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS
						+ " ORDER BY " + SmartracSQLiteHelper.COLUMN_END_TIME
						+ " DESC LIMIT 1;", null);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2014);
		c.set(Calendar.DAY_OF_YEAR, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		Date end = c.getTime();

		if (cursor.moveToFirst()) {
			try {
				end = ISO8601FORMAT.parse(cursor.getString(0));
			} catch (ParseException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}

		return end;
	}

	public TripCalendarItem getLastTrip(long l) {

		TripCalendarItem item = null;

		Log.i(TAG, "Get latest calendar Item end.");

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		String whereClause = "RHS." + SmartracSQLiteHelper.COLUMN_ID + "<" + l;

		Cursor cursor = database.query(TRIP_CALENDAR_ITEM_TABLE, tripColumns,
				whereClause, null, null, null, null, null);

		if (cursor.moveToLast()) {
			item = cursorToTripCalendarItem(cursor);
		}

		return item;

	}

	public ActivityCalendarItem getLastActivity(long l) {

		ActivityCalendarItem item = null;

		Log.i(TAG, "Get latest calendar Item end.");

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		String whereClause = "RHS." + SmartracSQLiteHelper.COLUMN_ID + "<" + l;

		Cursor cursor = database.query(ACTIVITY_CALENDAR_ITEM_TABLE,
				activityColumns, whereClause, null, null, null, null, null);

		if (cursor.moveToLast()) {
			item = cursorToActivityCalendarItem(cursor);
		}

		return item;

	}

	private List<TripCalendarItem> getTripsByDateRange(Date start, Date end) {
		Log.i(TAG, "Get trips for "
				+ SmartracDataFormat.getIso8601Format().format(start) + " - "
				+ SmartracDataFormat.getIso8601Format().format(end));

		List<TripCalendarItem> trips = new ArrayList<TripCalendarItem>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(TRIP_CALENDAR_ITEM_TABLE, tripColumns,
				SmartracSQLiteHelper.COLUMN_START_TIME + " BETWEEN \""
						+ SmartracDataFormat.getIso8601Format().format(start)
						+ "\" AND \""
						+ SmartracDataFormat.getIso8601Format().format(end)
						+ "\" OR " + SmartracSQLiteHelper.COLUMN_END_TIME
						+ " BETWEEN \""
						+ SmartracDataFormat.getIso8601Format().format(start)
						+ "\" AND \""
						+ SmartracDataFormat.getIso8601Format().format(end)
						+ "\";", null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			TripCalendarItem trip = cursorToTripCalendarItem(cursor);
			trips.add(trip);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		Log.i(TAG, " get " + trips.size() + " trip calendar items.");

		return trips;
	}

	private List<ActivityCalendarItem> getActivitiesByDateRange(Date start,
			Date end) {
		Log.i(TAG, "Get activities for "
				+ SmartracDataFormat.getIso8601Format().format(start) + " - "
				+ SmartracDataFormat.getIso8601Format().format(end));

		List<ActivityCalendarItem> activities = new ArrayList<ActivityCalendarItem>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = database.query(ACTIVITY_CALENDAR_ITEM_TABLE,
				activityColumns, SmartracSQLiteHelper.COLUMN_START_TIME
						+ " BETWEEN \""
						+ SmartracDataFormat.getIso8601Format().format(start)
						+ "\" AND \""
						+ SmartracDataFormat.getIso8601Format().format(end)
						+ "\" OR " + SmartracSQLiteHelper.COLUMN_END_TIME
						+ " BETWEEN \""
						+ SmartracDataFormat.getIso8601Format().format(start)
						+ "\" AND \""
						+ SmartracDataFormat.getIso8601Format().format(end)
						+ "\";", null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			ActivityCalendarItem activity = cursorToActivityCalendarItem(cursor);
			activities.add(activity);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		Log.i(TAG, " get " + activities.size() + " activity calendar items.");

		return activities;
	}

	/**
	 * Update calendar items.
	 * 
	 * @param calendarItems
	 * @return true if updated successfully.
	 */
	public boolean updateCalendarItems(List<CalendarItem> calendarItems) {
		List<TripCalendarItem> trips = new ArrayList<TripCalendarItem>();
		List<ActivityCalendarItem> activities = new ArrayList<ActivityCalendarItem>();

		for (CalendarItem item : calendarItems) {
			if (item instanceof TripCalendarItem) {
				trips.add((TripCalendarItem) item);
			}

			if (item instanceof ActivityCalendarItem) {
				activities.add((ActivityCalendarItem) item);
			}
		}

		return updateTripCalendarItems(trips)
				&& updateActivityCalendarItems(activities);
	}

	private boolean updateTripCalendarItems(List<TripCalendarItem> trips) {
		if (trips.size() == 0) {
			return true;
		}

		Log.i(TAG, "update trip calendar items.");
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		try {
			database.beginTransaction();
			for (TripCalendarItem trip : trips) {
				if (trip.isAdded()) {
					continue;
				}

				ContentValues[] values = tripCalendarItemToContentValues(trip);
				String[] whereArgs = { String.valueOf(trip.getId()) };

				database.update(SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS,
						values[0], SmartracSQLiteHelper.COLUMN_ID + " = ?",
						whereArgs);

				Cursor cursor = database
						.rawQuery(
								"SELECT "
										+ SmartracSQLiteHelper.COLUMN_TRIP_SEGMENT_ID
										+ " FROM "
										+ SmartracSQLiteHelper.TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS
										+ " WHERE "
										+ SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
										+ " = " + String.valueOf(trip.getId()),
								null);

				if (cursor.moveToFirst()) {
					int tripSegmentId = cursor.getInt(0);

					String[] tripSegmentsWhereArgs = { String
							.valueOf(tripSegmentId) };

					database.update(SmartracSQLiteHelper.TABLE_TRIP_SEGMENTS,
							values[1], SmartracSQLiteHelper.COLUMN_ID + " = ?",
							tripSegmentsWhereArgs);
				}

				cursor.close();
			}
			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			database.endTransaction();
		}
	}

	public List<TripCalendarItem> getLastTrip() {
		List<TripCalendarItem> trips = new ArrayList<TripCalendarItem>();

		SQLiteDatabase database = dbHelper.getReadableDatabase();

		Cursor cursor = database.query(TRIP_CALENDAR_ITEM_TABLE, tripColumns,
				SmartracSQLiteHelper.COLUMN_TRIP_ID + " in (select max("
						+ SmartracSQLiteHelper.COLUMN_TRIP_ID + ") from "
						+ SmartracSQLiteHelper.TABLE_TRIP_SEGMENTS + ")", null,
				null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			TripCalendarItem trip = cursorToTripCalendarItem(cursor);
			trips.add(trip);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();

		Log.i(TAG, " get " + trips.size() + " trip calendar items.");

		return trips;
	}

	private boolean updateActivityCalendarItems(
			List<ActivityCalendarItem> activities) {
		if (activities.size() == 0) {
			return true;
		}

		Log.i(TAG, "update activity calendar items.");
		SQLiteDatabase database = dbHelper.getReadableDatabase();

		try {
			database.beginTransaction();
			for (ActivityCalendarItem activity : activities) {
				if (activity.isAdded()) {
					continue;
				}

				ContentValues[] values = activityCalendarItemToContentValues(activity);
				String[] whereArgs = { String.valueOf(activity.getId()) };

				database.update(SmartracSQLiteHelper.TABLE_CALENDAR_ITEMS,
						values[0], SmartracSQLiteHelper.COLUMN_ID + " = ?",
						whereArgs);

				Cursor cursor = database
						.rawQuery(
								"SELECT "
										+ SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID
										+ " FROM "
										+ SmartracSQLiteHelper.TABLE_CAL_ITEM_DWELLING_REG_RELATIONSHIPS
										+ " WHERE "
										+ SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
										+ " = "
										+ String.valueOf(activity.getId()),
								null);

				if (cursor.moveToFirst()) {
					int dwellingRegionId = cursor.getInt(0);

					String[] dwellingRegionsWhereArgs = { String
							.valueOf(dwellingRegionId) };

					database.update(
							SmartracSQLiteHelper.TABLE_DWELLING_REGIONS,
							values[1], SmartracSQLiteHelper.COLUMN_ID + " = ?",
							dwellingRegionsWhereArgs);
				}

				cursor.close();
			}
			database.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			database.endTransaction();
		}
	}

	private TripCalendarItem cursorToTripCalendarItem(Cursor cursor) {

		long id = cursor.getInt(CALENDAR_ITEM_ID);
		long tripSegmentId = cursor.getInt(TRIP_SEGMENT_ID);
		Date start = null;
		Date end = null;
		try {
			start = ISO8601FORMAT.parse(cursor.getString(START_TIME));
			end = ISO8601FORMAT.parse(cursor.getString(END_TIME));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		CalendarItem.TravelMode mode = CalendarItem.TravelMode.get(cursor
				.getInt(PREDICTED_MODE));

		TripMapDrawable tmd = new TripMapDrawable(
				cursor.getString(TRIP_SEGMENT));
		TripCalendarItem trip = new TripCalendarItem(id, tripSegmentId, start,
				end, mode, tmd);
		trip.setTripId(cursor.getInt(TRIP_ID));
		if (!cursor.isNull(USER_CORRECTED_MODE)) {
			trip.setUserCorrectedMode(CalendarItem.TravelMode.get(cursor
					.getInt(USER_CORRECTED_MODE)));
		}

		return trip;
	}

	private ActivityCalendarItem cursorToActivityCalendarItem(Cursor cursor) {
		long id = cursor.getInt(CALENDAR_ITEM_ID);
		long dwellingRegionId = cursor.getInt(DWELLING_REGION_ID);
		Date start = null;
		Date end = null;
		try {
			start = ISO8601FORMAT.parse(cursor.getString(START_TIME));
			end = ISO8601FORMAT.parse(cursor.getString(END_TIME));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		CalendarItem.Activity activityType = CalendarItem.Activity.get(cursor
				.getInt(PREDICTED_ACTIVITY));
		ActivityMapDrawable amd = new ActivityMapDrawable(
				cursor.getString(DWELLING_REGION));
		ActivityCalendarItem activity = new ActivityCalendarItem(id,
				dwellingRegionId, start, end, activityType, amd);
		if (!cursor.isNull(USER_CORRECTED_ACTIVITY)) {
			activity.setUserCorrectedActivity(CalendarItem.Activity.get(cursor
					.getInt(USER_CORRECTED_ACTIVITY)));
		}

		return activity;
	}

	private ServiceOffCalendarItem cursorToNoDataCalendarItem(Cursor cursor) {
		long id = cursor.getInt(CALENDAR_ITEM_ID);
		int typeId = cursor.getInt(CALENDAR_ITEM_TYPE_ID);

		Date start = null;
		Date end = null;
		try {
			start = ISO8601FORMAT.parse(cursor.getString(START_TIME));
			end = ISO8601FORMAT.parse(cursor.getString(END_TIME));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		ServiceOffCalendarItem noData = new ServiceOffCalendarItem(id, start,
				end, CalendarItem.Type.get(typeId));

		return noData;
	}

	private ContentValues[] tripCalendarItemToContentValues(
			TripCalendarItem trip) {
		ContentValues calendarItemValues = new ContentValues();
		ContentValues tripSegmentValues = new ContentValues();

		calendarItemValues.put(SmartracSQLiteHelper.COLUMN_START_TIME,
				SmartracDataFormat.getIso8601Format().format(trip.getStart()));
		calendarItemValues.put(SmartracSQLiteHelper.COLUMN_END_TIME,
				SmartracDataFormat.getIso8601Format().format(trip.getEnd()));
		calendarItemValues.put(
				SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_TYPE_ID, trip
						.getType().getValue());

		tripSegmentValues.put(SmartracSQLiteHelper.COLUMN_PREDICTED_MODE, trip
				.getPredictedMode().getValue());
		if (trip.getUserCorrectedMode() != null) {
			tripSegmentValues.put(
					SmartracSQLiteHelper.COLUMN_USER_CORRECTED_MODE, trip
							.getUserCorrectedMode().getValue());
		}

		String polygon = trip.getPolyCode();

		tripSegmentValues
				.put(SmartracSQLiteHelper.COLUMN_TRIP_SEGMENT, polygon);

		ContentValues[] contentValues = { calendarItemValues, tripSegmentValues };
		return contentValues;
	}

	private ContentValues getTripCalendarItemRelationshipContentValues(
			long calendarItemId, long tripSegmentId) {
		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID, calendarItemId);
		values.put(SmartracSQLiteHelper.COLUMN_TRIP_SEGMENT_ID, tripSegmentId);

		return values;
	}

	private ContentValues[] activityCalendarItemToContentValues(
			ActivityCalendarItem activity) {
		ContentValues calendarItemValues = new ContentValues();
		ContentValues dwellingRegionValues = new ContentValues();

		calendarItemValues.put(
				SmartracSQLiteHelper.COLUMN_START_TIME,
				SmartracDataFormat.getIso8601Format().format(
						activity.getStart()));
		calendarItemValues
				.put(SmartracSQLiteHelper.COLUMN_END_TIME, SmartracDataFormat
						.getIso8601Format().format(activity.getEnd()));
		calendarItemValues.put(
				SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_TYPE_ID, activity
						.getType().getValue());

		dwellingRegionValues.put(
				SmartracSQLiteHelper.COLUMN_PREDICTED_ACTIVITY, activity
						.getPredictedActivity().getValue());
		if (activity.getUserCorrectedActivity() != null) {
			dwellingRegionValues.put(
					SmartracSQLiteHelper.COLUMN_USER_CORRECTED_ACTIVITY,
					activity.getUserCorrectedActivity().getValue());
		}

		String polygon = activity.getPolyCode();

		dwellingRegionValues.put(SmartracSQLiteHelper.COLUMN_DWELLING_REGION,
				polygon);

		ContentValues[] contentValues = { calendarItemValues,
				dwellingRegionValues };

		return contentValues;
	}

	private ContentValues getActivityCalendarItemRelationshipContentValues(
			long calendarItemId, long dwellingRegionId) {
		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID, calendarItemId);
		values.put(SmartracSQLiteHelper.COLUMN_DWELLING_REGION_ID,
				dwellingRegionId);

		return values;
	}

	private ContentValues noDataCalendarItemToContentValues(
			ServiceOffCalendarItem noData) {
		ContentValues calendarItemValues = new ContentValues();

		calendarItemValues
				.put(SmartracSQLiteHelper.COLUMN_START_TIME, SmartracDataFormat
						.getIso8601Format().format(noData.getStart()));
		calendarItemValues.put(SmartracSQLiteHelper.COLUMN_END_TIME,
				SmartracDataFormat.getIso8601Format().format(noData.getEnd()));
		calendarItemValues.put(
				SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_TYPE_ID, noData
						.getType().getValue());

		return calendarItemValues;
	}

	private ContentValues getUserSummaryContentValues(long calendarItemId) {
		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID, calendarItemId);
		values.put(SmartracSQLiteHelper.COLUMN_HAPPY, MOOD_DEFAULT);
		values.put(SmartracSQLiteHelper.COLUMN_TIRED, MOOD_DEFAULT);
		values.put(SmartracSQLiteHelper.COLUMN_STRESS, MOOD_DEFAULT);
		values.put(SmartracSQLiteHelper.COLUMN_SAD, MOOD_DEFAULT);
		values.put(SmartracSQLiteHelper.COLUMN_PAIN, MOOD_DEFAULT);
		values.put(SmartracSQLiteHelper.COLUMN_MEANINGFUL, MOOD_DEFAULT);

		return values;
	}

}
