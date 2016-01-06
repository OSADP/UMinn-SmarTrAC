package com.smartracumn.smartrac.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartracumn.smartrac.EditMapFragment.CalendarItemSplitter;
import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.data.SmartracData;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.CalendarItem.Activity;
import com.smartracumn.smartrac.model.CalendarItem.TravelMode;
import com.smartracumn.smartrac.model.DwellingLocation;
import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.model.TripCalendarItem;
import com.smartracumn.smartrac.util.CalendarItemUtil.ActivityMapDrawable;
import com.smartracumn.smartrac.util.CalendarItemUtil.TripMapDrawable;

public class CalendarItemEditor {
	private final String TAG = getClass().getName();

	private final Context context;

	private final SmartracData data;

	public CalendarItemEditor(Context context, SmartracData data) {
		this.context = context;
		this.data = data;
	}

	private void setActivityLocations(ActivityCalendarItem item) {
		item.setLocations(data.getLocationDataSource().getByDateRange(
				item.getStart(), item.getEnd()));
	}

	private void setTripSegmentRoute(TripCalendarItem item) {
		List<LocationWrapper> locs = data.getLocationDataSource()
				.getByDateRange(item.getStart(), item.getEnd());

		Collections.sort(locs);

		item.setMapDrawable(new TripMapDrawable(locs));
	}

	private List<CalendarItem> rearrange(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		if (changedItem.getStartState() == CalendarItem.UNMODIFIED
				&& changedItem.getEndState() == CalendarItem.UNMODIFIED) {
			return toBeDeleted;
		}

		if (changedItem instanceof ActivityCalendarItem) {
			setActivityLocations((ActivityCalendarItem) changedItem);
		} else if (changedItem instanceof TripCalendarItem) {
			setTripSegmentRoute((TripCalendarItem) changedItem);
		}

		if (changedItem.getStartState() == CalendarItem.SHRINK) {
			shrinkStart(changedItem, calendarItems);
		} else if (changedItem.getStartState() == CalendarItem.EXPAND) {
			toBeDeleted.addAll(expandStart(changedItem, calendarItems));
		}

		if (changedItem.getEndState() == CalendarItem.SHRINK) {
			shrinkEnd(changedItem, calendarItems);
		} else if (changedItem.getEndState() == CalendarItem.EXPAND) {
			toBeDeleted.addAll(expandEnd(changedItem, calendarItems));
		}

		return toBeDeleted;
	}

	/**
	 * Expand end time and remove overlapped calendar items.
	 * 
	 * @param changedItem
	 * @param calendarItems
	 */
	private List<CalendarItem> expandEnd(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		int index = calendarItems.indexOf(changedItem);

		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		if (index < 0 || index == calendarItems.size() - 1) {
			return toBeDeleted;
		}

		CalendarItem nextItem = calendarItems.get(calendarItems.size() - 1);
		for (int i = index + 1; i < calendarItems.size(); i++) {
			CalendarItem item = calendarItems.get(i);

			if (item.getEnd().getTime() > changedItem.getEnd().getTime()) {
				nextItem = item;
				break;
			} else {
				toBeDeleted.add(item);
			}
		}

		nextItem.setStart(changedItem.getEnd());

		if (nextItem instanceof ActivityCalendarItem) {
			setActivityLocations((ActivityCalendarItem) nextItem);
		} else if (nextItem instanceof TripCalendarItem) {
			setTripSegmentRoute((TripCalendarItem) nextItem);
		}

		nextItem.setIsModified(true);

		for (CalendarItem item : toBeDeleted) {
			calendarItems.remove(item);
		}

		return toBeDeleted;
	}

	/**
	 * Shrink end time.
	 * 
	 * @param changedItem
	 * @param calendarItems
	 */
	private void shrinkEnd(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		int index = calendarItems.indexOf(changedItem);

		if (index >= 0 && index < calendarItems.size() - 1) {
			CalendarItem nextItem = calendarItems.get(index + 1);

			nextItem.setStart(changedItem.getEnd());

			if (nextItem instanceof ActivityCalendarItem) {
				setActivityLocations((ActivityCalendarItem) nextItem);
			} else if (nextItem instanceof TripCalendarItem) {
				setTripSegmentRoute((TripCalendarItem) nextItem);
			}

			nextItem.setIsModified(true);
		}
	}

	/**
	 * Expand start time and remove overlapped items.
	 * 
	 * @param changedItem
	 * @param calendarItems
	 */
	private List<CalendarItem> expandStart(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		int index = calendarItems.indexOf(changedItem);

		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		if (index <= 0) {
			return toBeDeleted;
		}

		CalendarItem prevItem = calendarItems.get(0);
		for (int i = index - 1; i >= 0; i--) {
			CalendarItem item = calendarItems.get(i);

			if (item.getStart().getTime() < changedItem.getStart().getTime()) {
				prevItem = item;
				break;
			} else {
				toBeDeleted.add(item);
			}
		}

		prevItem.setEnd(changedItem.getStart());

		if (prevItem instanceof ActivityCalendarItem) {
			setActivityLocations((ActivityCalendarItem) prevItem);
		} else if (prevItem instanceof TripCalendarItem) {
			setTripSegmentRoute((TripCalendarItem) prevItem);
		}

		prevItem.setIsModified(true);

		for (CalendarItem item : toBeDeleted) {
			calendarItems.remove(item);
		}

		return toBeDeleted;
	}

	/**
	 * Shrink start time.
	 * 
	 * @param changedItem
	 * @param calendarItems
	 */
	private void shrinkStart(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		int index = calendarItems.indexOf(changedItem);

		if (index > 0) {
			CalendarItem prevItem = calendarItems.get(index - 1);

			prevItem.setEnd(changedItem.getStart());

			if (prevItem instanceof ActivityCalendarItem) {
				setActivityLocations((ActivityCalendarItem) prevItem);
			} else if (prevItem instanceof TripCalendarItem) {
				setTripSegmentRoute((TripCalendarItem) prevItem);
			}

			prevItem.setIsModified(true);
		}
	}

	private List<CalendarItem> merge(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		int i = calendarItems.indexOf(changedItem);

		if (i < 0) {
			return toBeDeleted;
		}

		boolean mergePrev = false;
		boolean mergeNext = false;

		if (i > 0
				&& calendarItems.get(i - 1).getDescription()
						.equals(changedItem.getDescription())) {
			changedItem.setStart(calendarItems.get(i - 1).getStart());
			mergePrev = true;

			toBeDeleted.add(calendarItems.get(i - 1));
		}

		if (i < calendarItems.size() - 1
				&& calendarItems.get(i + 1).getDescription()
						.equals(changedItem.getDescription())) {
			changedItem.setEnd(calendarItems.get(i + 1).getEnd());
			mergeNext = true;

			toBeDeleted.add(calendarItems.get(i + 1));
		}

		if (mergePrev || mergeNext) {
			if (changedItem instanceof TripCalendarItem) {
				setTripSegmentRoute((TripCalendarItem) changedItem);
			} else if (changedItem instanceof ActivityCalendarItem) {
				setActivityLocations((ActivityCalendarItem) changedItem);
			}

			for (CalendarItem item : toBeDeleted) {
				calendarItems.remove(item);
			}
		}

		if (changedItem instanceof TripCalendarItem) {
			data.getSummaryDataSource().setDistance(
					changedItem.getId(),
					data.getSummaryDataSource().updateDistance(
							changedItem.getId()));
		}

		return toBeDeleted;
	}

	private void connectTripsAndActivities(List<CalendarItem> items) {
		Collections.sort(items);
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

	public synchronized CalendarItem mergeTrip(List<CalendarItem> tripSegments,
			List<CalendarItem> calendarItems) {
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		if (tripSegments.size() <= 1) {
			return null;
		}

		Collections.sort(tripSegments);

		CalendarItem firstItem = tripSegments.get(0);
		CalendarItem lastItem = tripSegments.get(tripSegments.size() - 1);

		lastItem.setStart(firstItem.getStart());
		setTripSegmentRoute((TripCalendarItem) lastItem);

		for (int i = tripSegments.size() - 2; i >= 0; i--) {
			TripCalendarItem currentTripSegment = (TripCalendarItem) tripSegments
					.get(i);
			calendarItems.remove(currentTripSegment);
			toBeDeleted.add(currentTripSegment);
		}

		updateCalendarItems(calendarItems, toBeDeleted);

		connectTripsAndActivities(calendarItems);

		return lastItem;
	}

	/**
	 * Do post editing processing on calendar item splitter.
	 * 
	 * @param splitter
	 *            The splitter which holds the source calendar item as well as
	 *            the way to split it.
	 * @param calendarItems
	 *            List of daily calendar items.
	 * @return The modified calendar item.
	 */
	public synchronized CalendarItem split(CalendarItemSplitter splitter,
			List<CalendarItem> calendarItems) {
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		List<LocationWrapper> locs = data.getLocationDataSource()
				.getByDateRange(splitter.getSourceItem().getStart(),
						splitter.getSourceItem().getEnd());

		Collections.sort(locs);

		CalendarItem[] items = splitter.getCalendarItems(locs);

		int insertIndex = calendarItems.indexOf(splitter.getSourceItem());
		if (insertIndex >= 0) {
			toBeDeleted.add(calendarItems.remove(insertIndex));

			calendarItems.add(insertIndex, items[1]);

			calendarItems.add(insertIndex, items[0]);
		}

		for (CalendarItem item : calendarItems) {
			item.reset();
		}

		toBeDeleted.addAll(merge(items[0], calendarItems));

		toBeDeleted.addAll(merge(items[1], calendarItems));

		updateCalendarItems(calendarItems, toBeDeleted);

		connectTripsAndActivities(calendarItems);

		return items[0];
	}

	/**
	 * Processing calendar items on user modification.
	 * 
	 * @param changedItem
	 * @param calendarItems
	 * @return
	 */
	public synchronized CalendarItem change(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		// Process list of calendar items on user modification.
		// Actions like combine/separate/delete items should be taken here.

		if (!calendarItems.contains(changedItem)) {
			return null;
		}

		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		toBeDeleted.addAll(rearrange(changedItem, calendarItems));

		toBeDeleted.addAll(merge(changedItem, calendarItems));

		if (changedItem instanceof ActivityCalendarItem) {
			DwellingLocation associateDwellingLocation = new DwellingLocation(
					((ActivityCalendarItem) changedItem).getWeightedCenter(),
					((ActivityCalendarItem) changedItem).getActivity());

			((ActivityCalendarItem) changedItem)
					.setAssociateDwellingLocation(associateDwellingLocation);
		}

		for (CalendarItem item : calendarItems) {
			item.reset();
		}

		if (changedItem instanceof TripCalendarItem) {
			data.getSummaryDataSource().setDistance(
					changedItem.getId(),
					data.getSummaryDataSource().updateDistance(
							changedItem.getId()));
		}

		updateCalendarItems(calendarItems, toBeDeleted);

		connectTripsAndActivities(calendarItems);

		return changedItem;
	}

	public synchronized CalendarItem changeActivityToTrip(
			CalendarItem changedItem, List<CalendarItem> calendarItems) {
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		List<LocationWrapper> locs = data.getLocationDataSource()
				.getByDateRange(changedItem.getStart(), changedItem.getEnd());
		Collections.sort(locs);

		TripCalendarItem waitItem = new TripCalendarItem(
				changedItem.getStart(), changedItem.getEnd(), TravelMode.WAIT,
				new TripMapDrawable(locs));

		toBeDeleted.add(changedItem);

		int itemIndex = calendarItems.indexOf(changedItem);
		if (itemIndex >= 0) {
			calendarItems.remove(itemIndex);
			calendarItems.add(itemIndex, waitItem);
		}

		updateCalendarItems(calendarItems, toBeDeleted);

		connectTripsAndActivities(calendarItems);

		return waitItem;
	}

	public synchronized CalendarItem changeTripToActivity(
			CalendarItem changedItem, List<CalendarItem> calendarItems) {
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		List<LocationWrapper> locs = data.getLocationDataSource()
				.getByDateRange(changedItem.getStart(), changedItem.getEnd());
		Collections.sort(locs);

		ActivityCalendarItem unknownActivityItem = new ActivityCalendarItem(
				changedItem.getStart(), changedItem.getEnd(), Activity.UNKNOWN_ACTIVITY,
				new ActivityMapDrawable(locs));

		toBeDeleted.add(changedItem);

		int itemIndex = calendarItems.indexOf(changedItem);
		if (itemIndex >= 0) {
			calendarItems.remove(itemIndex);
			calendarItems.add(itemIndex, unknownActivityItem);
		}

		updateCalendarItems(calendarItems, toBeDeleted);

		connectTripsAndActivities(calendarItems);

		return unknownActivityItem;
	}
	/**
	 * Finalize calendar items and save them to smartrac database.
	 * 
	 * @param calendarItems
	 *            Calendar items.
	 * @return True if saved successfully.
	 */
	public synchronized boolean updateCalendarItems(
			List<CalendarItem> calendarItems, List<CalendarItem> toBeDeleted) {
		List<CalendarItem> calendarItemsToBeSaved = new ArrayList<CalendarItem>();
		List<CalendarItem> modifiedCalendarItems = new ArrayList<CalendarItem>();

		for (CalendarItem item : calendarItems) {
			if (item.isInProgress()) {
				continue;
			}

			if (item.isAdded()) {
				calendarItemsToBeSaved.add(item);
			} else if (item.isModified()) {
				modifiedCalendarItems.add(item);
			}

			item.setIsModified(false);
			item.reset();
		}

		boolean insertSuccess = data.getCalendarItemDataSource().insert(
				calendarItemsToBeSaved);
		boolean updateSuccess = data.getCalendarItemDataSource()
				.updateCalendarItems(modifiedCalendarItems);
		boolean deleteSuccess = data.getCalendarItemDataSource().delete(
				toBeDeleted);
		boolean updateSummaryForNew = data.getDwellingSummaryDataSource()
				.createDwellingSummaries(calendarItemsToBeSaved);
		boolean updateSummaryForModified = data.getDwellingSummaryDataSource()
				.createDwellingSummaries(modifiedCalendarItems);

		if (updateSummaryForNew || updateSummaryForModified) {
			Log.i(TAG, "broadcast dwelling summary updated");
			context.sendBroadcast(new Intent(context.getResources().getString(
					R.string.dwelling_summary_updated_broadcast)));
		}

		return insertSuccess && updateSuccess && deleteSuccess;
	}
}
