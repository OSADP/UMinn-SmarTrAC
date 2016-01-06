package com.smartracumn.smartrac.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.data.SmartracData;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.CalendarItem.TravelMode;
import com.smartracumn.smartrac.model.CalendarItem.Type;
import com.smartracumn.smartrac.model.DummyCalendarItem;
import com.smartracumn.smartrac.model.DwellingIndicator;
import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.model.ModeIndicator;
import com.smartracumn.smartrac.model.ServiceOffCalendarItem;
import com.smartracumn.smartrac.model.TripCalendarItem;
import com.smartracumn.smartrac.util.ActivityDetector;
import com.smartracumn.smartrac.util.SmartracDataFormat;

public class CalendarItemUtility {
	private final String TAG = getClass().getName();

	private final Context context;

	private final SmartracData data;

	private final ActivityDetector activityDetector;

	private final CalendarItemFactory calendarItemFactory;

	private Date validStartTime;

	private Set<CalendarItem.TravelMode> vehicleMode;

	private CalendarItemEditor editor;

	private final BroadcastReceiver dataServiceStartReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "service started received.");
			Bundle extra = intent.getExtras();
			String timeString = extra.getString(context.getResources()
					.getString(R.string.service_start_time));
			Date startTime = null;
			try {
				startTime = SmartracDataFormat.getDateTimeFormat().parse(
						timeString);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (startTime != null) {
				saveCalendarItemsOnServiceStart(startTime);
			}

		}
	};

	private final BroadcastReceiver inaccurateGPSReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Location not available received.");
			Bundle extra = intent.getExtras();
			String timeString = extra.getString(context.getResources()
					.getString(R.string.location_not_available_time));
			Date time = null;
			try {
				time = SmartracDataFormat.getDateTimeFormat().parse(timeString);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (time != null) {
				// Delete dwellings created by imput location data.
				data.getDwellingDataSource().deleteFrom(time);
				saveCalendarItemsOnInaccurateGPS(time);
			}
		}
	};

	private final BroadcastReceiver activityChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "dwelling indicator changed received");
			Bundle extra = intent.getExtras();

			DwellingIndicator di = extra.getParcelable(context.getResources()
					.getString(R.string.dwelling_indicator));

			if (di != null) {
				saveCalendarItems(di);
			}
		}
	};

	private final BroadcastReceiver modeChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "mode changed received");
			Bundle extra = intent.getExtras();
			String timeString = extra.getString(context.getResources()
					.getString(R.string.time_tag));
			Date changeTime = null;
			try {
				changeTime = SmartracDataFormat.getDateTimeFormat().parse(
						timeString);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (changeTime != null) {
				saveCalendarItems(new ModeIndicator(changeTime,
						CalendarItem.TravelMode.UNKNOWN_TRAVEL_MODE));
			}
		}
	};

	public CalendarItemUtility(Context context, SmartracData data,
			ActivityDetector activityDetector) {
		this.data = data;
		this.context = context;
		this.activityDetector = activityDetector;
		this.calendarItemFactory = new CalendarItemFactory(context,
				activityDetector);
		this.editor = new CalendarItemEditor(context, data);

		vehicleMode = new HashSet<CalendarItem.TravelMode>();
		vehicleMode.add(CalendarItem.TravelMode.CAR);
		vehicleMode.add(CalendarItem.TravelMode.BUS);
		vehicleMode.add(CalendarItem.TravelMode.RAIL);
	}

	private synchronized List<CalendarItem> generateCalendarItems(Date start,
			Date end) {
		if (!end.after(start)) {
			return new ArrayList<CalendarItem>();
		}

		end = new Date(end.getTime() + 1000);

		List<DwellingIndicator> dwellingInds = data.getDwellingDataSource()
				.getRecordsForDateRange(start, end);

		List<LocationWrapper> locs = data.getLocationDataSource()
				.getByDateRange(start, end);

		List<ModeIndicator> modeInds = data.getModeDataSource()
				.getRecordsForDateRange(start, end);

		Collections.sort(locs);
		Collections.sort(modeInds);
		Collections.sort(dwellingInds);

		List<CalendarItem> items = calendarItemFactory.getCalendarItems(
				dwellingInds, modeInds, locs);

		return items;
	}

	private synchronized boolean saveCalendarItems(Date time,
			boolean allowTripMerge) {
		Date start = validStartTime;

		// Start time should be exclusive in order not to remove inaccurate GPS
		// chunk.
		if (start == null) {
			start = new Date(data.getCalendarItemDataSource().getLatestEnd()
					.getTime() + 1000);
		}

		if (start.getTime() >= time.getTime()) {
			validStartTime = null;
			return false;
		}

		List<CalendarItem> items = generateCalendarItems(start, time);

		boolean success = saveCalendarItems(items);

		if (items.size() > 0) {
			validStartTime = items.get(items.size() - 1).getEnd();

			if (allowTripMerge) {
				if (items.get(items.size() - 1).getType() == CalendarItem.Type.TRIP) {
					List<TripCalendarItem> lastTrip = data
							.getCalendarItemDataSource().getLastTrip();

					mergeTrip(lastTrip, true);
				}
			}
		}

		return success;
	}

	private synchronized boolean saveCalendarItems(ModeIndicator mi) {
		Date time = mi.getTime();

		return saveCalendarItems(time, false);
	}

	private synchronized boolean saveCalendarItems(DwellingIndicator di) {
		Date time = di.getAdjustedTime();

		return saveCalendarItems(time, di.isDwelling());
	}

	private synchronized boolean saveCalendarItemsOnServiceStart(Date startTime) {
		Date start = validStartTime;

		// Start time should be exclusive in order not to remove inaccurate GPS
		// chunk.
		if (start == null) {
			start = new Date(data.getCalendarItemDataSource().getLatestEnd()
					.getTime() + 1000);
		}

		if (start.getTime() >= startTime.getTime()) {
			return false;
		}

		List<CalendarItem> items = generateCalendarItems(start, startTime);

		if (items.size() == 0) {
			items.add(new ServiceOffCalendarItem(0, start, startTime,
					CalendarItem.Type.SERVICE_Off));
		} else {
			CalendarItem lastItem = items.get(items.size() - 1);

			items.add(new ServiceOffCalendarItem(0, lastItem.getEnd(),
					startTime, CalendarItem.Type.SERVICE_Off));
		}

		validStartTime = startTime;
		boolean sucess = saveCalendarItems(items);

		if (items.size() > 1
				&& items.get(items.size() - 2).getType() == CalendarItem.Type.TRIP) {
			List<TripCalendarItem> lastTrip = data.getCalendarItemDataSource()
					.getLastTrip();

			mergeTrip(lastTrip, true);
		}

		return sucess;
	}

	private synchronized boolean saveCalendarItemsOnInaccurateGPS(Date time) {
		boolean success = saveCalendarItems(time, true);
		validStartTime = null;

		return success;
	}

	private synchronized boolean saveCalendarItems(
			List<CalendarItem> calendarItems) {
		if (calendarItems == null || calendarItems.size() == 0) {
			return false;
		}

		List<CalendarItem> calendarItemsToBeSaved = new ArrayList<CalendarItem>();

		for (CalendarItem item : calendarItems) {
			if (item.isInProgress() || item.isFinalized())
				continue;
			calendarItemsToBeSaved.add(item);
		}

		boolean insertSuccess = data.getCalendarItemDataSource().insert(
				calendarItemsToBeSaved);
		boolean summaryUpdated = data.getDwellingSummaryDataSource()
				.createDwellingSummaries(calendarItemsToBeSaved);

		if (summaryUpdated) {
			Log.i(TAG, "Broadcast dwelling summary updated");
			context.sendBroadcast(new Intent(context.getResources().getString(
					R.string.dwelling_summary_updated_broadcast)));
		}

		return insertSuccess;
	}

	private boolean mergeTrip(List<TripCalendarItem> trip,
			boolean updateDatabase) {
		Log.i(TAG, "Merge Trip");

		if (trip.size() < 2) {
			return false;
		}

		Collections.sort(trip);

		List<CalendarItem> toBeUpdated = new ArrayList<CalendarItem>();
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		Map<CalendarItem.TravelMode, Long> count = new HashMap<CalendarItem.TravelMode, Long>();
		List<TripCalendarItem> temp = new ArrayList<TripCalendarItem>();

		int vehicleCount = 0;
		for (TripCalendarItem item : trip) {
			if (vehicleMode.contains(item.getMode())) {
				long prevVal = 0;
				if (count.containsKey(item.getMode())) {
					prevVal = count.get(item.getMode());
				}

				count.put(item.getMode(), prevVal
						+ (item.getTimeSpanInMillis() / 1000));
				temp.add(item);
				vehicleCount++;
			} else {
				if (vehicleCount > 1) {
					// Find the vehicle mode last the longest time
					TravelMode majorMode = TravelMode.UNKNOWN_TRAVEL_MODE;
					long max = 0;
					for (TravelMode mode : count.keySet()) {
						if (count.get(mode) > max) {
							max = count.get(mode);
							majorMode = mode;
						}
					}

					TripCalendarItem tripStart = temp.remove(0);
					tripStart.setMode(majorMode);
					for (TripCalendarItem other : temp) {
						tripStart.setEnd(other.getEnd());
						tripStart.merge(other, false);
					}

					toBeUpdated.add(tripStart);
					toBeDeleted.addAll(temp);
				}

				count.clear();
				temp.clear();
				vehicleCount = 0;
			}
		}

		if (vehicleCount > 1) {
			TravelMode majorMode = TravelMode.UNKNOWN_TRAVEL_MODE;
			long max = 0;
			for (TravelMode mode : count.keySet()) {
				if (count.get(mode) > max) {
					max = count.get(mode);
					majorMode = mode;
				}
			}

			TripCalendarItem tripStart = temp.remove(0);
			tripStart.setMode(majorMode);
			for (TripCalendarItem other : temp) {
				tripStart.setEnd(other.getEnd());
				tripStart.merge(other, false);
			}

			toBeUpdated.add(tripStart);
			toBeDeleted.addAll(temp);
		}

		if (updateDatabase) {
			boolean updateSuccess = data.getCalendarItemDataSource()
					.updateCalendarItems(toBeUpdated);
			boolean deleteSuccess = data.getCalendarItemDataSource().delete(
					toBeDeleted);

			return updateSuccess && deleteSuccess;
		}

		for (CalendarItem tripSeg : toBeDeleted) {
			trip.remove((TripCalendarItem) tripSeg);
		}

		return false;
	}

	public BroadcastReceiver getDataServiceStartReceiver() {
		return dataServiceStartReceiver;
	}

	public BroadcastReceiver getInaccurateGPSReceiver() {
		return inaccurateGPSReceiver;
	}

	public BroadcastReceiver getActivityChangeReceiver() {
		return activityChangeReceiver;
	}

	public BroadcastReceiver getModeChangeReceiver() {
		return modeChangeReceiver;
	}

	public synchronized List<CalendarItem> getCalendarItems(Date start, Date end) {
		List<CalendarItem> items = new ArrayList<CalendarItem>();

		Date queryStart = start;
		Date queryEnd = end;

		// Make time exclusive while query saved calendar items.
		start = data.getCalendarItemDataSource().getLatestEndBefore(start);
		start = new Date(start.getTime() + 1000);
		end = data.getCalendarItemDataSource().getEarliestStartAfter(end);
		end = new Date(end.getTime() - 1000);

		// Get saved calendar items in given time range.
		items.addAll(data.getCalendarItemDataSource()
				.getByDateRange(start, end));

		// Determine time range of unsaved items to be generated.
		Date genStart = validStartTime;
		Date genEnd = end;

		// Start time should be exclusive in order not to remove inaccurate GPS
		// chunk.
		if (genStart == null) {
			genStart = new Date(data.getCalendarItemDataSource().getLatestEnd()
					.getTime() + 1000);
		}

		// Only generate calendar items when there there exists unsaved item
		// before end time.
		if (genStart.before(genEnd)) {
			genEnd = Calendar.getInstance().getTime();

			Log.i(TAG, "genearte calendar item for: "
					+ SmartracDataFormat.getTimeFormat().format(genStart) + "-"
					+ SmartracDataFormat.getTimeFormat().format(genEnd));

			// Generate unsaved calendar items using new time range.
			List<CalendarItem> generatedItems = generateCalendarItems(genStart,
					genEnd);

			items.addAll(generatedItems);
		}

		preclientProcessing(start, end, items);

		// Remove items that are totally out of the query time range.
		List<CalendarItem> outOfSceneItems = new ArrayList<CalendarItem>();
		for (CalendarItem item : items) {
			if (item.getEnd().before(queryStart)
					|| item.getStart().after(queryEnd)) {
				outOfSceneItems.add(item);
			}
		}
		for (CalendarItem item : outOfSceneItems) {
			items.remove(item);
		}

		return items;
	}

	private void mergeAdjacentActivities(List<CalendarItem> items) {
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();
		List<CalendarItem> toBeUpdated = new ArrayList<CalendarItem>();

		for (int i = 1; i < items.size() - 1; i++) {
			CalendarItem current = items.get(i);

			if (current instanceof DummyCalendarItem) {
				CalendarItem prev = items.get(i - 1);
				CalendarItem next = items.get(i + 1);

				if (prev instanceof ActivityCalendarItem
						&& next instanceof ActivityCalendarItem
						&& prev.getDescription().equals(next.getDescription())) {

					LatLng item_center = ((ActivityCalendarItem) prev)
							.getWeightedCenter();
					LatLng next_center = ((ActivityCalendarItem) next)
							.getWeightedCenter();
					float[] results = new float[1];
					Location.distanceBetween(item_center.latitude,
							item_center.longitude, next_center.latitude,
							next_center.longitude, results);

					if (results[0] <= context.getResources().getInteger(
							R.integer.adjacent_activity_threshold)
							&& !next.isInProgress()) {

						// update items and record changes to be made to
						// database.
						prev.setEnd(next.getEnd());
						toBeUpdated.add(prev);
						toBeDeleted.add(current);
						toBeDeleted.add(next);
						items.remove(current);
						items.remove(next);

						i -= 1;
					}
				}
			}
		}

		// Database changes.
		data.getCalendarItemDataSource().updateCalendarItems(toBeUpdated);
		data.getCalendarItemDataSource().delete(toBeDeleted);
	}

	private void preclientProcessing(Date start, Date end,
			List<CalendarItem> items) {
		// Set in progress item
		if (items.size() > 0) {
			CalendarItem last = items.get(items.size() - 1);

			if (Calendar.getInstance().getTimeInMillis()
					- last.getEnd().getTime() <= context.getResources()
					.getInteger(R.integer.lost_of_data_measure_time)) {
				last.setIsInProgress(true);
			}
		}

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.setTime(end);
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		today.add(Calendar.MILLISECOND, -1000);
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.setTime(today.getTime());
		tomorrow.add(Calendar.DAY_OF_MONTH, 1);

		// Insert inaccurate GPS items between gaps.
		if (items.size() > 0) {
			List<CalendarItem> inaccurateGPSItems = new ArrayList<CalendarItem>();

			// If first item is cross day, do not insert inaccurate GPS.
			if (!items.get(0).isCrossDay()
					&& items.get(0).getStart().getTime() - start.getTime() >= context
							.getResources().getInteger(
									R.integer.lost_of_data_measure_time)) {
				inaccurateGPSItems.add(new DummyCalendarItem(start, items
						.get(0).getStart(), context.getResources().getString(
						R.string.no_data_description)));
			}

			for (int i = 0; i < items.size() - 1; i++) {
				CalendarItem item = items.get(i);
				CalendarItem next = items.get(i + 1);

				if (next.getStart().getTime() - item.getEnd().getTime() >= context
						.getResources().getInteger(
								R.integer.lost_of_data_measure_time)) {
					inaccurateGPSItems.add(new DummyCalendarItem(item.getEnd(),
							next.getStart(), context.getResources().getString(
									R.string.no_data_description)));
				}
			}

			// Append tail if front end is querying for previous date's data.
			// If last item is cross day, do not append inaccurate GPS.
			// XXX: Do not insert inaccurate gps when query for yesterday and
			// today while last item is the last saved item
			if (!items.get(items.size() - 1).isCrossDay()
					&& !(endCalendar.getTimeInMillis() == today
							.getTimeInMillis() || endCalendar.getTimeInMillis() == tomorrow
							.getTimeInMillis())
					&& end.getTime() < Calendar.getInstance().getTimeInMillis()
					&& end.getTime()
							- items.get(items.size() - 1).getEnd().getTime() >= context
							.getResources().getInteger(
									R.integer.lost_of_data_measure_time)) {
				inaccurateGPSItems.add(new DummyCalendarItem(items.get(
						items.size() - 1).getEnd(), end, context.getResources()
						.getString(R.string.no_data_description)));
			}

			items.addAll(inaccurateGPSItems);
			Collections.sort(items);
		}

		// Merge adjacent activities splitted by inaccurateGPS
		mergeAdjacentActivities(items);

		// Insert abnormal in progress item
		if (items.size() > 0) {
			CalendarItem last = items.get(items.size() - 1);
			Calendar itemEnd = Calendar.getInstance();
			itemEnd.setTime(last.getEnd());
			Calendar now = Calendar.getInstance();

			// XXX: if last in progress item is service off item. Set it to
			// non-in-progress and add another in progress dummy item.
			// In order to show prediction in progress. Which could later being
			// changed to inaccurate GPS in progress.
			if (last.isInProgress() && last instanceof ServiceOffCalendarItem) {
				last.setIsInProgress(false);
				CalendarItem temp = new DummyCalendarItem(last.getEnd(),
						Calendar.getInstance().getTime(), "Prediction");
				temp.setIsInProgress(true);
				items.add(temp);
				// XXX: Show abnormal in progress only when end time equals
				// today (query yesterday while no item happen after last item
				// from yesterday)
				// ,or end time is equal to tomorrow (query for today)
			} else if (!last.isInProgress()
					&& (endCalendar.getTimeInMillis() == today
							.getTimeInMillis() || endCalendar.getTimeInMillis() == tomorrow
							.getTimeInMillis())) {

				boolean dataServiceOn = context.getSharedPreferences(
						context.getResources().getString(R.string.app_domain),
						Context.MODE_PRIVATE).getBoolean(
						context.getResources().getString(
								R.string.smartrac_service_switch), true);

				CalendarItem ipItem = null;
				if (dataServiceOn) {
					ipItem = new DummyCalendarItem(last.getEnd(), Calendar
							.getInstance().getTime(), context.getResources()
							.getString(R.string.no_data_description));
				} else {
					ipItem = new ServiceOffCalendarItem(0, last.getEnd(),
							Calendar.getInstance().getTime(), Type.SERVICE_Off);
				}

				ipItem.setIsInProgress(true);
				items.add(ipItem);
			}
		} else if (endCalendar.getTimeInMillis() == today.getTimeInMillis()
				|| endCalendar.getTimeInMillis() == tomorrow.getTimeInMillis()) {
			boolean dataServiceOn = context.getSharedPreferences(
					context.getResources().getString(R.string.app_domain),
					Context.MODE_PRIVATE).getBoolean(
					context.getResources().getString(
							R.string.smartrac_service_switch), true);

			CalendarItem ipItem = null;
			if (dataServiceOn) {
				ipItem = new DummyCalendarItem(start, Calendar.getInstance()
						.getTime(), context.getResources().getString(
						R.string.no_data_description));
			} else {
				ipItem = new ServiceOffCalendarItem(0, start, Calendar
						.getInstance().getTime(), Type.SERVICE_Off);
			}

			ipItem.setIsInProgress(true);
			items.add(ipItem);
		}

		// Connect continuous trip segments
		for (int i = 0; i < items.size() - 1; i++) {
			CalendarItem item = items.get(i);
			CalendarItem next = items.get(i + 1);

			if (item instanceof TripCalendarItem
					&& next instanceof TripCalendarItem) {
				((TripCalendarItem) item).connect((TripCalendarItem) next);
			}
		}

		// connect trips with adjacent activity weighted center
		for (int i = 0; i < items.size(); i++) {
			CalendarItem item = items.get(i);

			if (item instanceof ActivityCalendarItem) {
				ActivityCalendarItem activity = (ActivityCalendarItem) item;

				if (i - 1 >= 0 && items.get(i - 1) instanceof TripCalendarItem) {
					((TripCalendarItem) items.get(i - 1)).connectActivity(
							activity.getPosition(), false);
				}

				if (i + 1 < items.size()
						&& items.get(i + 1) instanceof TripCalendarItem) {
					((TripCalendarItem) items.get(i + 1)).connectActivity(
							activity.getPosition(), true);
				}
			}
		}
	}

	public synchronized boolean clearChanges(List<CalendarItem> items) {
		if (items == null || items.size() == 0) {
			return true;
		}

		Collections.sort(items);

		Date start = null;
		Date end = null;

		for (CalendarItem item : items) {
			if (!item.isInProgress()) {
				if (start == null) {
					start = item.getStart();
				}

				end = item.getEnd();
			}
		}

		if (start == null || end == null || !start.before(end)) {
			return true;
		}

		if (data.getCalendarItemDataSource().delete(items)) {
			// Update dwelling summary
			for (CalendarItem item : items) {
				if (item instanceof ActivityCalendarItem) {
					data.getDwellingSummaryDataSource().delete(
							(ActivityCalendarItem) item);
				}
			}
			activityDetector.getDwellingLocationManager().forceReload();

			// Regenerate calendar items.
			List<CalendarItem> generatedCalendarItems = new ArrayList<CalendarItem>();
			Date temp = null;

			for (CalendarItem item : items) {
				if (item instanceof ServiceOffCalendarItem) {
					if (generatedCalendarItems.size() > 0
							&& generatedCalendarItems
									.get(generatedCalendarItems.size() - 1)
									.getStart().getTime() == item.getStart()
									.getTime()) {
						continue;
					}

					temp = item.getStart();
					generatedCalendarItems.addAll(generateCalendarItems(start,
							temp));
					((ServiceOffCalendarItem) item).setId(0);
					generatedCalendarItems.add(item);
					start = item.getEnd();
				}
			}

			generatedCalendarItems.addAll(generateCalendarItems(start, end));

			// Merge vehicle trip segments presented in generated calendar
			// items.
			// Fill gaps.
			List<CalendarItem> inaccurateGPSItems = new ArrayList<CalendarItem>();
			for (int i = 0; i < generatedCalendarItems.size() - 1; i++) {
				CalendarItem item = generatedCalendarItems.get(i);
				CalendarItem next = generatedCalendarItems.get(i + 1);

				if (next.getStart().getTime() - item.getEnd().getTime() >= context
						.getResources().getInteger(
								R.integer.lost_of_data_measure_time)) {
					inaccurateGPSItems.add(new DummyCalendarItem(item.getEnd(),
							next.getStart(), context.getResources().getString(
									R.string.no_data_description)));
				}
			}

			generatedCalendarItems.addAll(inaccurateGPSItems);
			Collections.sort(generatedCalendarItems);

			// Generate merged trip segments.
			List<CalendarItem> mergedTripSegments = new ArrayList<CalendarItem>();
			List<CalendarItem> originalTripSegments = new ArrayList<CalendarItem>();
			List<TripCalendarItem> trip = new ArrayList<TripCalendarItem>();
			for (CalendarItem item : generatedCalendarItems) {
				if (item instanceof TripCalendarItem) {
					trip.add((TripCalendarItem) item);
					originalTripSegments.add(item);
				} else if (trip.size() > 0) {
					mergeTrip(trip, false);
					mergedTripSegments.addAll(trip);
					trip.clear();
				}
			}

			if (trip.size() > 0) {
				mergeTrip(trip, false);
				mergedTripSegments.addAll(trip);
				trip.clear();
			}

			// remove original trip segments and insert merged trip segments.
			for (CalendarItem item : originalTripSegments) {
				generatedCalendarItems.remove(item);
			}
			generatedCalendarItems.addAll(mergedTripSegments);

			return data.getCalendarItemDataSource().insert(
					generatedCalendarItems);
		}

		return false;
	}

	public class CalendarItemService {
		public List<CalendarItem> getCalendarItems(Date start, Date end) {
			return CalendarItemUtility.this.getCalendarItems(start, end);
		}

		public boolean clearChanges(List<CalendarItem> items) {
			return CalendarItemUtility.this.clearChanges(items);
		}

		public CalendarItemEditor edit() {
			return editor;
		}
	}

	public CalendarItemService getService() {
		return new CalendarItemService();
	}
}
