package com.smartracumn.smartrac.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

/**
 * Calendar item data source which is used to interact with calendar item tables
 * in SQLiteDatabase.
 * 
 * @author kangx385
 * 
 */
public class SummaryDataSource {

	private SmartracSQLiteHelper dbHelper;

	private SQLiteDatabase database;

	public SummaryDataSource(Context context) {
		dbHelper = SmartracSQLiteHelper.getInstance(context);
		database = dbHelper.getReadableDatabase();

	}

	/*
	 * Set the distance in the database
	 */
	public void setDistance(long newId, double segmentDistance) {

		String whereId = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID + " = "
				+ newId;

		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_DISTANCE,
				Math.round(segmentDistance));

		database.update(SmartracSQLiteHelper.TABLE_TRIP_USER_SUMMARY, values,
				whereId, null);
	}

	/*
	 * Get total distance across all the trips that belong to the same segment
	 */
	public float getTotalTripDistance(long id) {

		float total_distance = 0;
		String query = "select "
				+ SmartracSQLiteHelper.TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS
				+ "."
				+ SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
				+ " from "
				+ SmartracSQLiteHelper.TABLE_TRIP_SEGMENTS
				+ " JOIN "
				+ SmartracSQLiteHelper.TABLE_CAL_ITEM_TRIP_SEG_RELATIONSHIPS
				+ " ON "
				+ "table_trip_segments._id = table_cal_item_trip_seg_relationships.trip_segment_id "
				+ "and table_trip_segments.trip_id = "
				+ "(select trip_id from table_trip_segments JOIN table_cal_item_trip_seg_relationships "
				+ "ON table_trip_segments._id = table_cal_item_trip_seg_relationships.trip_segment_id and "
				+ "table_cal_item_trip_seg_relationships.calendar_item_id ="
				+ id + ")";

		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		while (cursor.isAfterLast() == false) {
			int newId = cursor
					.getInt(cursor
							.getColumnIndex(SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID));
			double segmentDistance = calculateDistance(newId);
			setDistance(newId, segmentDistance);
			total_distance = total_distance + calculateDistance(newId);
			cursor.moveToNext();
		}
		cursor.close();
		return total_distance;

	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	/*
	 * Calculate the distance if not already done using the encoded string
	 */
	public float calculateDistance(long id) {

//		if (getDistanceFromDB(id) != -1) {
//			return (float) getDistanceFromDB(id);
//		}
		String query = "select table_trip_segments.trip_segment from table_cal_item_trip_seg_relationships "
				+ "JOIN table_trip_segments ON table_cal_item_trip_seg_relationships.trip_segment_id = "
				+ "table_trip_segments._id where table_cal_item_trip_seg_relationships.calendar_item_id ="
				+ id;

		List<LatLng> path = new ArrayList<LatLng>();

		Cursor cursor = database.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			String trip_seg = cursor.getString(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_TRIP_SEGMENT));
			path = PolyUtil.decode(trip_seg);
		}
		float distance = 0;
		if (path.size() > 0) {
			LatLng initial = new LatLng(path.get(0).latitude,
					path.get(0).longitude);
			float[] results = new float[3];
			for (LatLng point : path) {

				Location.distanceBetween(initial.latitude, initial.longitude,
						point.latitude, point.longitude, results);

				distance = distance + results[0];

				initial = point;

			}
		}
		cursor.close();
		return distance;
	}

	/*
	 * Get distance stored in the database
	 */
	public double getDistanceFromDB(long newId) {

		double distance = 0;
		String whereId = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID + " = "
				+ newId;

		Cursor cursor = database.query(
				SmartracSQLiteHelper.TABLE_TRIP_USER_SUMMARY, null, whereId,
				null, null, null, null);

		if (cursor.moveToFirst()) {
			if (cursor.isNull(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_DISTANCE))) {
				return -1;
			}
			distance = cursor.getDouble(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_DISTANCE));
		}
		cursor.close();
		return distance;
	}

	/*
	 * Check if the item has been confirmed by the user
	 * 
	 * 0: Not confirmed 1: Confirmed
	 */
	public boolean isConfirmed(long id, boolean isActivity) {

		String whereId = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID + " = "
				+ id;
		Cursor cursor;
		if (isActivity) {
			cursor = database.query(
					SmartracSQLiteHelper.TABLE_ACTIVITY_USER_SUMMARY, null,
					whereId, null, null, null, null);
		} else {
			cursor = database.query(
					SmartracSQLiteHelper.TABLE_TRIP_USER_SUMMARY, null,
					whereId, null, null, null, null);

		}
		if (cursor.moveToFirst()) {
			int value = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_CONFIRMED));
			if (value == 1)
				return true;
		}

		cursor.close();
		return false;
	}

	
	public float updateDistance(long id) {

		String query = "select table_trip_segments.trip_segment from table_cal_item_trip_seg_relationships "
				+ "JOIN table_trip_segments ON table_cal_item_trip_seg_relationships.trip_segment_id = "
				+ "table_trip_segments._id where table_cal_item_trip_seg_relationships.calendar_item_id ="
				+ id;

		List<LatLng> path = new ArrayList<LatLng>();

		Cursor cursor = database.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			String trip_seg = cursor.getString(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_TRIP_SEGMENT));
			path = PolyUtil.decode(trip_seg);
		}
		float distance = 0;
		if (path.size() > 0) {
			LatLng initial = new LatLng(path.get(0).latitude,
					path.get(0).longitude);
			float[] results = new float[3];
			for (LatLng point : path) {

				Location.distanceBetween(initial.latitude, initial.longitude,
						point.latitude, point.longitude, results);

				distance = distance + results[0];

				initial = point;

			}
		}
		cursor.close();
		return distance;
	}

	public void setConfirm(long id) {
		 SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		Calendar c = Calendar.getInstance();
		String date_format = ISO8601FORMAT.format(c.getTime());

		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_CONFIRMED, 1);
		values.put(SmartracSQLiteHelper.COLUMN_CONFIRMED_DATE,
				date_format);
		
		 String whereClause = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
				+ " = " + id;

		database.update(
				SmartracSQLiteHelper.TABLE_ACTIVITY_USER_SUMMARY,
				values, whereClause, null);
		
	}

}
