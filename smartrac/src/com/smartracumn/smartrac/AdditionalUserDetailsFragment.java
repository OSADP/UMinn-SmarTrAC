package com.smartracumn.smartrac;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.smartracumn.smartrac.data.SmartracSQLiteHelper;

public class AdditionalUserDetailsFragment extends DialogFragment implements
		OnSeekBarChangeListener, OnCheckedChangeListener {

	private SeekBar happy;
	// private SeekBar tired;
	private SeekBar sad;
	private SeekBar stress;
	private SeekBar pain;
	// private SeekBar meaningful;

	private int happy_measure = 0;
	private int tired_measure = 0;
	private int sad_measure = 0;
	private int stress_measure = 0;
	private int pain_measure = 0;
	private int meaningful_measure = 0;

	// private CheckBox alone;
	private CheckBox spouse;
	private CheckBox children;
	private CheckBox otherFamily;
	private CheckBox friends;
	private CheckBox coWorkers;

	private int with_alone = 0;
	private int with_spouse = 0;
	private int with_children = 0;
	private int with_otherFamily = 0;
	private int with_friends = 0;
	private int with_coWorkers = 0;

	private TextView happy_index;
	private TextView stress_index;
	private TextView sad_index;
	private TextView pain_index;

	private View view;

	private long id;
	private boolean isActivity;

	private SmartracSQLiteHelper dbHelper;

	private SQLiteDatabase database;

	private String desc;

	private EditText et_desc;

	private Button save;
	private Button prev;
	private Button next;

	private Cursor cursor;

	public AdditionalUserDetailsFragment(long id, boolean isActivity) {
		this.id = id;
		this.isActivity = isActivity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		dbHelper = SmartracSQLiteHelper.getInstance(getActivity());
		database = dbHelper.getReadableDatabase();

		view = inflater.inflate(R.layout.additional_user_info, container);
		et_desc = (EditText) view.findViewById(R.id.et_desc);
		save = (Button) view.findViewById(R.id.bSave);
		// prev = (Button) view.findViewById(R.id.bPrev);
		// next = (Button) view.findViewById(R.id.bNext);
		// flipper = (ViewFlipper) view.findViewById(R.id.view_flipper);

		happy = (SeekBar) view.findViewById(R.id.happy);
		// tired = (SeekBar) view.findViewById(R.id.tired);
		sad = (SeekBar) view.findViewById(R.id.sad);
		stress = (SeekBar) view.findViewById(R.id.stress);
		pain = (SeekBar) view.findViewById(R.id.pain);
		// meaningful = (SeekBar) view.findViewById(R.id.meaningful);

		happy.setOnSeekBarChangeListener(this);
		// tired.setOnSeekBarChangeListener(this);
		sad.setOnSeekBarChangeListener(this);
		stress.setOnSeekBarChangeListener(this);
		pain.setOnSeekBarChangeListener(this);
		// meaningful.setOnSeekBarChangeListener(this);

		// alone = (CheckBox) view.findViewById(R.id.alone);
		spouse = (CheckBox) view.findViewById(R.id.spouse);
		children = (CheckBox) view.findViewById(R.id.children);
		otherFamily = (CheckBox) view.findViewById(R.id.otherFamily);
		friends = (CheckBox) view.findViewById(R.id.friends);
		coWorkers = (CheckBox) view.findViewById(R.id.coWorkers);

		happy_index = (TextView) view.findViewById(R.id.happy_index);
		stress_index = (TextView) view.findViewById(R.id.stress_index);
		sad_index = (TextView) view.findViewById(R.id.sad_index);
		pain_index = (TextView) view.findViewById(R.id.pain_index);

		// alone.setOnCheckedChangeListener(this);
		spouse.setOnCheckedChangeListener(this);
		children.setOnCheckedChangeListener(this);
		otherFamily.setOnCheckedChangeListener(this);
		friends.setOnCheckedChangeListener(this);
		coWorkers.setOnCheckedChangeListener(this);

		getDialog().setTitle("Enter additional details: ");

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		final String whereClause = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
				+ " = " + id;
		if (isActivity) {
			cursor = database.query(
					SmartracSQLiteHelper.TABLE_ACTIVITY_USER_SUMMARY, null,
					whereClause, null, null, null, null);
		} else {
			cursor = database.query(
					SmartracSQLiteHelper.TABLE_TRIP_USER_SUMMARY, null,
					whereClause, null, null, null, null);

		}
		if (cursor.moveToFirst()) {
			desc = cursor.getString(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_DESC));
			if (desc != null)
				et_desc.setText(desc);

			// Mood
			int happy_db = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_HAPPY));
			happy.setProgress(happy_db);
			happy_index.setText("" + happy_db);
			int stress_db = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_STRESS));
			stress.setProgress(stress_db);
			stress_index.setText("" + stress_db);
			// int tired_db = cursor.getInt(cursor
			// .getColumnIndex(SmartracSQLiteHelper.COLUMN_TIRED));
			// tired.setProgress(tired_db);
			int sad_db = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_SAD));
			sad.setProgress(sad_db);
			sad_index.setText("" + sad_db);
			int pain_db = cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_PAIN));
			pain.setProgress(pain_db);
			pain_index.setText("" + pain_db);
			// int meaningful_db = cursor.getInt(cursor
			// .getColumnIndex(SmartracSQLiteHelper.COLUMN_MEANINGFUL));
			// meaningful.setProgress(meaningful_db);

			// Companion
			// if (cursor.getInt(cursor
			// .getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_ALONE)) == 1)
			// alone.setChecked(true);
			if (cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_SPOUSE)) == 1)
				spouse.setChecked(true);
			if (cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_CHILDREN)) == 1)
				children.setChecked(true);
			if (cursor
					.getInt(cursor
							.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_OTHER_FAMILY)) == 1)
				otherFamily.setChecked(true);
			if (cursor.getInt(cursor
					.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_FRIENDS)) == 1)
				friends.setChecked(true);
			if (cursor
					.getInt(cursor
							.getColumnIndex(SmartracSQLiteHelper.COLUMN_WITH_COWORKERS)) == 1)
				coWorkers.setChecked(true);

		}

		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getDialog().dismiss();

				if (!isActivity) {
					String query = "select table_cal_item_trip_seg_relationships.calendar_item_id from "
							+ "table_trip_segments JOIN table_cal_item_trip_seg_relationships ON "
							+ "table_trip_segments._id = table_cal_item_trip_seg_relationships.trip_segment_id "
							+ "and table_trip_segments.trip_id = "
							+ "(select trip_id from table_trip_segments JOIN table_cal_item_trip_seg_relationships "
							+ "ON table_trip_segments._id = table_cal_item_trip_seg_relationships.trip_segment_id and "
							+ "table_cal_item_trip_seg_relationships.calendar_item_id ="
							+ id + ")";

					Cursor cursor = database.rawQuery(query, null);
					cursor.moveToFirst();
					while (cursor.isAfterLast() == false) {
						int newId = cursor.getInt(cursor
								.getColumnIndex(SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID));
						String whereId = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
								+ " = " + newId;

						int count = database.update(
								SmartracSQLiteHelper.TABLE_TRIP_USER_SUMMARY,
								getUserSummaryContentValues(), whereId, null);

						cursor.moveToNext();
					}

				}

				if (isActivity) {
					String whereClause = SmartracSQLiteHelper.COLUMN_CALENDAR_ITEM_ID
							+ " = " + id;

					int count = database.update(
							SmartracSQLiteHelper.TABLE_ACTIVITY_USER_SUMMARY,
							getUserSummaryContentValues(), whereClause, null);

				}
			}
		});
		// prev.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // flipper.showPrevious();
		// }
		// });
		//
		// next.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // flipper.showNext();
		// // if (flipper.getDisplayedChild() == 1) {
		// // next.setVisibility(View.GONE);
		// // }
		// }
		// });

	}

	private ContentValues getUserSummaryContentValues() {
		ContentValues values = new ContentValues();
		values.put(SmartracSQLiteHelper.COLUMN_HAPPY, happy_measure);
		values.put(SmartracSQLiteHelper.COLUMN_STRESS, stress_measure);
		values.put(SmartracSQLiteHelper.COLUMN_SAD, sad_measure);
		values.put(SmartracSQLiteHelper.COLUMN_TIRED, tired_measure);
		values.put(SmartracSQLiteHelper.COLUMN_PAIN, pain_measure);
		values.put(SmartracSQLiteHelper.COLUMN_MEANINGFUL, meaningful_measure);
		values.put(SmartracSQLiteHelper.COLUMN_DESC, et_desc.getText()
				.toString());
		values.put(SmartracSQLiteHelper.COLUMN_WITH_ALONE, with_alone);
		values.put(SmartracSQLiteHelper.COLUMN_WITH_SPOUSE, with_spouse);
		values.put(SmartracSQLiteHelper.COLUMN_WITH_CHILDREN, with_children);
		values.put(SmartracSQLiteHelper.COLUMN_WITH_OTHER_FAMILY,
				with_otherFamily);
		values.put(SmartracSQLiteHelper.COLUMN_WITH_FRIENDS, with_friends);
		values.put(SmartracSQLiteHelper.COLUMN_WITH_COWORKERS, with_coWorkers);

		return values;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		switch (seekBar.getId()) {
		case R.id.happy:
			happy_index.setText("" + progress);
			happy_measure = progress;
			break;
		case R.id.stress:
			stress_measure = progress;
			stress_index.setText("" + progress);
			break;
		case R.id.sad:
			sad_measure = progress;
			sad_index.setText("" + progress);
			break;
		case R.id.pain:
			pain_measure = progress;
			pain_index.setText("" + progress);
			break;
		default:
			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		// case R.id.alone:
		// if (isChecked)
		// with_alone = 1;
		// else
		// with_alone = 0;
		//
		// break;
		case R.id.spouse:
			if (isChecked)
				with_spouse = 1;
			else
				with_spouse = 0;
			break;
		case R.id.children:
			if (isChecked)
				with_children = 1;
			else
				with_children = 0;
			break;
		case R.id.otherFamily:
			if (isChecked)
				with_otherFamily = 1;
			else
				with_otherFamily = 0;
			break;
		case R.id.friends:
			if (isChecked)
				with_friends = 1;
			else
				with_friends = 0;
			break;
		case R.id.coWorkers:
			if (isChecked)
				with_coWorkers = 1;
			else
				with_coWorkers = 0;
			break;

		default:
			break;
		}

	}

}
