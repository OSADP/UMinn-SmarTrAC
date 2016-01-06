package com.smartracumn.smartrac;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartracumn.smartrac.data.SmartracSQLiteHelper;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.TripCalendarItem;
import com.smartracumn.smartrac.util.SmartracDataFormat;

public class ConfirmScreenFragment extends DialogFragment {

	private SmartracSQLiteHelper dbHelper;

	private SQLiteDatabase database;

	private CalendarItem item;

	private View view;

	private TextView tv_title;
	private TextView tv_startTime;
	private TextView tv_endTime;
	private TextView tv_description;
	private TextView tv_companion;
	private TextView tv_mood;

	private String description;

	private int netEffect;

	private Cursor cursor;

	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public interface onCofirmButtonClicked{
		public void confirmClicked();
	}
	
	onCofirmButtonClicked mCallback;
	
	public ConfirmScreenFragment(CalendarItem item) {
		this.item = item;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try{
			mCallback = (onCofirmButtonClicked)activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString()
                    + " must implement onCofirmButtonClicked");
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		dbHelper = SmartracSQLiteHelper.getInstance(getActivity());
		database = dbHelper.getReadableDatabase();
		description = getArguments().getString("Description");
		view = inflater.inflate(R.layout.confirmation_screen, container);
		tv_startTime = (TextView) view.findViewById(R.id.startTime);
		tv_endTime = (TextView) view.findViewById(R.id.endTime);
		tv_description = (TextView) view.findViewById(R.id.description);
		tv_companion = (TextView) view.findViewById(R.id.companion);
		tv_title = (TextView) view.findViewById(R.id.title);
		tv_mood = (TextView) view.findViewById(R.id.mood);

		getDialog().setTitle("Review and Confirm: ");

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		String endTime = SmartracDataFormat.getTimeFormat().format(
				item.getEnd());
		String startTime = SmartracDataFormat.getTimeFormat().format(
				item.getStart());

		tv_title.setText(description);

		final String whereClause = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
				+ " = " + item.getId();

		if (item instanceof ActivityCalendarItem) {
			cursor = database.query(
					SmartracSQLiteHelper.TABLE_ACTIVITY_USER_SUMMARY, null,
					whereClause, null, null, null, null);
		} else if (item instanceof TripCalendarItem) {
			cursor = database.query(
					SmartracSQLiteHelper.TABLE_TRIP_USER_SUMMARY, null,
					whereClause, null, null, null, null);

		}
		if (cursor.moveToFirst()) {

			// set description
			String desc = cursor.getString(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_DESC));
			tv_description.setText(desc);

			// Set Companions
			StringBuilder sb = new StringBuilder();
			if (cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_ALONE)) == 1)
				sb.append("Alone\n");
			if (cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_SPOUSE)) == 1)
				sb.append("Spouse\n");
			if (cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_CHILDREN)) == 1)
				sb.append("Own Children\n");
			if (cursor
					.getInt(cursor
							.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_OTHER_FAMILY)) == 1)
				sb.append("Other Family Members\n");
			if (cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_FRIENDS)) == 1)
				sb.append("Friends, Neighbors, Acquaintances\n");
			if (cursor
					.getInt(cursor
							.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_COWORKERS)) == 1)
				sb.append("Co-workers, Customers, people from work");

			tv_companion.setText(sb.toString());

			// Set mood
			int happy_measure = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_HAPPY));
			int stress_measure = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_STRESS));
			int sad_measure = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_SAD));
			int pain_measure = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_PAIN));

			cursor.close();

			netEffect = happy_measure
					- ((stress_measure + sad_measure + pain_measure) / 3);
			tv_mood.setText(" " + netEffect);
		}

		tv_startTime.setText(startTime);
		tv_endTime.setText(endTime);

		ImageView icon = (ImageView) view.findViewById(R.id.item_icon);

		seticon(icon, item.getDescription());

		Button confirm = (Button) view.findViewById(R.id.confirm);
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Calendar c = Calendar.getInstance();
				String date_format = ISO8601FORMAT.format(c.getTime());

				ContentValues values = new ContentValues();
				values.put(SmartracSQLiteHelper.COLUMN_CONFIRMED, 1);
				values.put(SmartracSQLiteHelper.COLUMN_CONFIRMED_DATE,
						date_format);

				if (item instanceof TripCalendarItem) {

					//Multi-mode trip
					String query = "select table_cal_item_trip_seg_relationships.calendar_item_id from "
							+ "table_trip_segments JOIN table_cal_item_trip_seg_relationships ON "
							+ "table_trip_segments._id = table_cal_item_trip_seg_relationships.trip_segment_id "
							+ "and table_trip_segments.trip_id = "
							+ "(select trip_id from table_trip_segments JOIN table_cal_item_trip_seg_relationships "
							+ "ON table_trip_segments._id = table_cal_item_trip_seg_relationships.trip_segment_id and "
							+ "table_cal_item_trip_seg_relationships.calendar_item_id ="
							+ item.getId() + ")";
					
					Cursor cursor = database.rawQuery(query, null);
					cursor.moveToFirst();
					//Confirming all the trips if its an multi-mode trip
					while (cursor.isAfterLast() == false) {
						int newId = cursor.getInt(cursor
								.getColumnIndex(SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID));
						String whereId = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
								+ " = " + newId;

						int count = database.update(
								SmartracSQLiteHelper.TABLE_TRIP_USER_SUMMARY,
								values, whereId, null);
						cursor.moveToNext();
					}

				}
				if (item instanceof ActivityCalendarItem) {
					database.update(
							SmartracSQLiteHelper.TABLE_ACTIVITY_USER_SUMMARY,
							values, whereClause, null);
				}
				
//				SmartracActivity act = (SmartracActivity) getActivity();
//				act.setSelectedDate(act.getSelectedDate());
//
				mCallback.confirmClicked();
				getDialog().dismiss();
				
				
			}
		});
	}

	private void seticon(ImageView icon, String desc) {
		icon.setVisibility(View.VISIBLE);

		if (desc.contains("Home"))
			icon.setImageResource(R.drawable.home);
		else if (desc.contains("Work"))
			icon.setImageResource(R.drawable.work);
		else if (desc.contains("Shopping"))
			icon.setImageResource(R.drawable.shopping);
		else if (desc.contains("Education"))
			icon.setImageResource(R.drawable.education);
		else if (desc.contains("Eat out"))
			icon.setImageResource(R.drawable.eat_out);
		else if (desc.contains("Other personal business"))
			icon.setImageResource(R.drawable.personal_business);
		else if (desc.contains("Social"))
			icon.setImageResource(R.drawable.social);
		else if (desc.contains("Activity"))
			icon.setImageResource(R.drawable.question);
		else if (desc.contains("Walk"))
			icon.setImageResource(R.drawable.walk);
		else if (desc.contains("Bus"))
			icon.setImageResource(R.drawable.bus);
		else if (desc.contains("Car"))
			icon.setImageResource(R.drawable.car);
		else if (desc.contains("Rail"))
			icon.setImageResource(R.drawable.rail);
		else if (desc.contains("Bicycle"))
			icon.setImageResource(R.drawable.bike);
		else if (desc.contains("Wait"))
			icon.setImageResource(R.drawable.wait);
		else
			icon.setImageResource(R.drawable.no_data);

	}

}
