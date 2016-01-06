package com.smartracumn.smartrac;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartracumn.smartrac.data.CalendarItemDataSource;
import com.smartracumn.smartrac.data.SmartracSQLiteHelper;
import com.smartracumn.smartrac.data.SummaryDataSource;
import com.smartracumn.smartrac.model.CalendarItem;

public class AggregatedSummary extends Activity {

	private SmartracSQLiteHelper dbHelper;

	private SQLiteDatabase database;

	private List<Integer> walk_ids = new ArrayList<Integer>();
	private List<Integer> bus_ids = new ArrayList<Integer>();
	private List<Integer> car_ids = new ArrayList<Integer>();
	private List<Integer> bike_ids = new ArrayList<Integer>();
	private List<Integer> rail_ids = new ArrayList<Integer>();

	private double walk_duration = 0;
	private double bus_duration = 0;
	private double bike_duration = 0;
	private double rail_duration = 0;
	private double car_duration = 0;

	private double home_duration = 0;
	private double work_duration = 0;
	private double shopping_duration = 0;
	private double education_duration = 0;
	private double opb_duration = 0;
	private double eat_duration = 0;
	private double src_duration = 0;

	private TextView tv_homeDuration;
	private TextView tv_workDuration;
	private TextView tv_educationDuration;
	private TextView tv_srcDuration;
	private TextView tv_eatDuration;
	private TextView tv_opbDuration;
	private TextView tv_shopDuration;

	private TextView tv_walkDuration;
	private TextView tv_carDuration;
	private TextView tv_busDuration;
	private TextView tv_bikeDuration;
	private TextView tv_railDuration;

	private TextView tv_walkDistance;
	private TextView tv_carDistance;
	private TextView tv_busDistance;
	private TextView tv_bikeDistance;
	private TextView tv_railDistacne;

	private LinearLayout ll_summary;

	private final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private MenuItem menu_list;
	private MenuItem menu_map;
	private MenuItem menu_date;

	private SummaryDataSource summaryDataSource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aggregated_summary);

		dbHelper = SmartracSQLiteHelper.getInstance(this);
		database = dbHelper.getReadableDatabase();

		tv_homeDuration = (TextView) findViewById(R.id.home_duration);
		tv_workDuration = (TextView) findViewById(R.id.work_duration);
		tv_educationDuration = (TextView) findViewById(R.id.education_duration);
		tv_srcDuration = (TextView) findViewById(R.id.src_duration);
		tv_eatDuration = (TextView) findViewById(R.id.eat_duration);
		tv_opbDuration = (TextView) findViewById(R.id.opb_duration);
		tv_shopDuration = (TextView) findViewById(R.id.shop_duration);

		tv_walkDuration = (TextView) findViewById(R.id.walk_duration);
		tv_carDuration = (TextView) findViewById(R.id.car_duration);
		tv_busDuration = (TextView) findViewById(R.id.bus_duration);
		tv_bikeDuration = (TextView) findViewById(R.id.bike_duration);
		tv_railDuration = (TextView) findViewById(R.id.rail_duration);

		tv_walkDistance = (TextView) findViewById(R.id.walk_distance);
		tv_carDistance = (TextView) findViewById(R.id.car_distance);
		tv_busDistance = (TextView) findViewById(R.id.bus_distance);
		tv_bikeDistance = (TextView) findViewById(R.id.bike_distance);
		tv_railDistacne = (TextView) findViewById(R.id.rail_distance);

		ll_summary = (LinearLayout) findViewById(R.id.ll_summary);
		ll_summary.setVisibility(View.GONE);

		showDatePicker();

		summaryDataSource = new SummaryDataSource(this);

		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setTitle("Daily Summary");
		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	private String getDuration(double durationInMinutes) {
		int hours = (int) (durationInMinutes / 60);
		int minutes = (int) (durationInMinutes % 60);
		String duration = "" + hours + " hr " + minutes + " min";

		return duration;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_calendar, menu);
		menu_date = (MenuItem) menu.findItem(R.id.action_set_date);
		menu_list = (MenuItem) menu.findItem(R.id.action_calendar);
		menu_map = (MenuItem) menu.findItem(R.id.action_map);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu_list.setVisible(false);
		menu_map.setVisible(false);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_set_date:
			showDatePicker();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void getTripSummary(String date) {
		String whereClause = SmartracSQLiteHelper.COLUMN_START_TIME
				+ " LIKE '%" + date + "%'";

		Cursor cursor = database.query(
				CalendarItemDataSource.TRIP_CALENDAR_ITEM_TABLE,
				CalendarItemDataSource.tripColumns, whereClause, null, null,
				null, null);

		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {

			int mode;
			if (cursor
					.isNull(cursor
							.getColumnIndex(SmartracSQLiteHelper.COLUMN_USER_CORRECTED_MODE))) {
				mode = cursor
						.getInt(cursor
								.getColumnIndex(SmartracSQLiteHelper.COLUMN_PREDICTED_MODE));
			} else {
				mode = cursor
						.getInt(cursor
								.getColumnIndex(SmartracSQLiteHelper.COLUMN_USER_CORRECTED_MODE));
			}
			int id = cursor.getInt(0);

			long diffInMinutes = 0;
			try {
				Date start = ISO8601FORMAT
						.parse(cursor.getString(cursor
								.getColumnIndex(SmartracSQLiteHelper.COLUMN_START_TIME)));
				Date end = ISO8601FORMAT.parse(cursor.getString(cursor
						.getColumnIndex(SmartracSQLiteHelper.COLUMN_END_TIME)));
				long time_duration = end.getTime() - start.getTime();
				diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(time_duration);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			switch (CalendarItem.TravelMode.get(mode)) {
			case WALKING:
				walk_ids.add(id);
				walk_duration = walk_duration + diffInMinutes;
				break;
			case CAR:
				car_ids.add(id);
				car_duration = car_duration + diffInMinutes;
				break;
			case BUS:
				bus_ids.add(id);
				bus_duration = bus_duration + diffInMinutes;
				break;
			case BIKE:
				bike_ids.add(id);
				bike_duration = bike_duration + diffInMinutes;
				break;
			case RAIL:
				rail_ids.add(id);
				rail_duration = rail_duration + diffInMinutes;
				break;
			default:
				break;
			}

			cursor.moveToNext();
		}

		cursor.close();
		// Displaying Distance
		Double distance = getTotalModeDistance(walk_ids);
		Double distance_in_miles = round((0.000621 * distance), 2);
		tv_walkDistance.setText("" + distance + " m" + " (" + distance_in_miles
				+ " miles)");

		distance = getTotalModeDistance(bus_ids);
		distance_in_miles = round((0.000621 * distance), 2);
		tv_busDistance.setText("" + distance + " m" + " (" + distance_in_miles
				+ " miles)");

		distance = getTotalModeDistance(car_ids);
		distance_in_miles = round((0.000621 * distance), 2);
		tv_carDistance.setText("" + distance + " m" + " (" + distance_in_miles
				+ " miles)");

		distance = getTotalModeDistance(bike_ids);
		distance_in_miles = round((0.000621 * distance), 2);
		tv_bikeDistance.setText("" + distance + " m" + " (" + distance_in_miles
				+ " miles)");

		distance = getTotalModeDistance(rail_ids);
		distance_in_miles = round((0.000621 * distance), 2);
		tv_railDistacne.setText("" + distance + " m" + " (" + distance_in_miles
				+ " miles)");

		// Displaying duration
		tv_walkDuration.setText(getDuration(walk_duration));
		tv_busDuration.setText(getDuration(bus_duration));
		tv_carDuration.setText(getDuration(car_duration));
		tv_bikeDuration.setText(getDuration(bike_duration));
		tv_railDuration.setText(getDuration(rail_duration));

	}

	/*
	 * Get the total distance of all the id's passed
	 */
	public double getTotalModeDistance(List<Integer> modeIds) {
		// TODO Auto-generated method stub
		double total_distance = 0;
		for (int id : modeIds) {
			total_distance = total_distance
					+ summaryDataSource.getDistanceFromDB(id);
		}

		return total_distance;
	}

	private void getActivitySummary(String date) {
		String whereClause = SmartracSQLiteHelper.COLUMN_START_TIME
				+ " LIKE '%" + date + "%'";

		Cursor cursor = database.query(
				CalendarItemDataSource.ACTIVITY_CALENDAR_ITEM_TABLE,
				CalendarItemDataSource.activityColumns, whereClause, null,
				null, null, null);

		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {

			int activity;
			if (cursor
					.isNull(cursor
							.getColumnIndex(SmartracSQLiteHelper.COLUMN_USER_CORRECTED_ACTIVITY))) {
				activity = cursor
						.getInt(cursor
								.getColumnIndex(SmartracSQLiteHelper.COLUMN_PREDICTED_ACTIVITY));
			} else {
				activity = cursor
						.getInt(cursor
								.getColumnIndex(SmartracSQLiteHelper.COLUMN_USER_CORRECTED_ACTIVITY));
			}
			int id = cursor.getInt(0);

			long diffInMinutes = 0;
			try {
				Date start = ISO8601FORMAT
						.parse(cursor.getString(cursor
								.getColumnIndex(SmartracSQLiteHelper.COLUMN_START_TIME)));
				Date end = ISO8601FORMAT.parse(cursor.getString(cursor
						.getColumnIndex(SmartracSQLiteHelper.COLUMN_END_TIME)));
				long time_duration = end.getTime() - start.getTime();
				diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(time_duration);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			switch (CalendarItem.Activity.get(activity)) {
			case HOME:
				home_duration = home_duration + diffInMinutes;
				break;
			case WORK:
				work_duration = work_duration + diffInMinutes;
				break;
			case SHOPPING:
				shopping_duration = shopping_duration + diffInMinutes;
				break;
			case EDUCATION:
				education_duration = education_duration + diffInMinutes;
				break;
			case SOCIAL_RECREATION_COMMUNITY:
				src_duration = src_duration + diffInMinutes;
				break;
			case OTHER_PERSONAL_BUSINESS:
				opb_duration = opb_duration + diffInMinutes;
				break;
			case EAT_OUT:
				eat_duration = eat_duration + diffInMinutes;
				break;
			default:
				break;
			}

			cursor.moveToNext();
		}

		cursor.close();

		// Displaying duration of activities
		tv_homeDuration.setText(getDuration(home_duration));
		tv_workDuration.setText(getDuration(work_duration));
		tv_shopDuration.setText(getDuration(shopping_duration));
		tv_educationDuration.setText(getDuration(education_duration));
		tv_srcDuration.setText(getDuration(src_duration));
		tv_opbDuration.setText(getDuration(opb_duration));
		tv_eatDuration.setText(getDuration(eat_duration));

	}

	public void showDatePicker() {

		if (getFragmentManager().findFragmentByTag("DatePicker") != null) {
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.remove(getFragmentManager().findFragmentByTag(
					"DatePicker"));
			transaction.commit();

		}
		DatePickerFragment frag = new DatePickerFragment();
		Bundle args = new Bundle();
		args.putBoolean("Summary", true);
		frag.setArguments(args);
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.add(frag, "DatePicker").commit();

	}

	public void setSelectedDate(Date date) {

		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
		String formatted_date = DATE_FORMAT.format(date);

		ll_summary.setVisibility(View.VISIBLE);
		menu_date.setTitle(formatted_date);
		getTripSummary(formatted_date);
		getActivitySummary(formatted_date);

	}

	public double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
