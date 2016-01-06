package com.smartracumn.smartrac.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.smartracumn.smartrac.CalendarListFragment.CalendarItemParentView;
import com.smartracumn.smartrac.EditMapFragment.CalendarItemSplitter;
import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.data.CalendarItemDataSource;
import com.smartracumn.smartrac.data.DwellingSummaryDataSource;
import com.smartracumn.smartrac.data.LocationDataSource;
import com.smartracumn.smartrac.data.MotionDataSource;
import com.smartracumn.smartrac.data.SummaryDataSource;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.DwellingLocation;
import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.model.TripCalendarItem;

/**
 * The class used to process smartrac data and generate object used in the UI.
 * 
 * @author kangx385
 * 
 */
public class SmartracCoreDataProcessor {
	private final String TAG = getClass().getSimpleName();

	private final MotionDataSource motionDataSource;

	private final LocationDataSource locationDataSource;

	private final DwellingSummaryDataSource dwellingSummaryDataSource;

	private final CalendarItemDataSource calendarItemDataSource;

	private final Context context;

	private final ActivityDetector activityDetector;

	private SummaryDataSource summaryDataSource;

	private List<CalendarItem> itemsToBeDeleted = new ArrayList<CalendarItem>();

	/**
	 * Get the instance of dwelling location manager associated with core data
	 * processor.
	 * 
	 * @return
	 */
	public ActivityDetector getActivityDetector() {
		return this.activityDetector;
	}

	/**
	 * Instantiate a new instance of the SmartracCoreDataProcessor class.
	 * 
	 * @param context
	 *            The application context.
	 */
	public SmartracCoreDataProcessor(Context context) {
		this.context = context;
		this.locationDataSource = new LocationDataSource(this.context);
		this.calendarItemDataSource = new CalendarItemDataSource(this.context);
		this.motionDataSource = new MotionDataSource(this.context);
		this.dwellingSummaryDataSource = new DwellingSummaryDataSource(
				this.context);
		this.activityDetector = new ActivityDetector(this.context,
				new Handler());
		this.summaryDataSource = new SummaryDataSource(this.context);
	}

	/**
	 * Initializes items to be deleted.
	 */
	public void initializes() {
		this.itemsToBeDeleted.clear();
	}

	private void rearrange(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		if (changedItem.getStartState() == CalendarItem.SHRINK) {
			shrinkStart(changedItem, calendarItems);
		} else if (changedItem.getStartState() == CalendarItem.EXPAND) {
			expandStart(changedItem, calendarItems);
		}

		if (changedItem.getEndState() == CalendarItem.SHRINK) {
			shrinkEnd(changedItem, calendarItems);
		} else if (changedItem.getEndState() == CalendarItem.EXPAND) {
			expandEnd(changedItem, calendarItems);
		}
	}

	private void setActivityLocations(ActivityCalendarItem item) {
		item.setLocations(locationDataSource.getByDateRange(item.getStart(),
				item.getEnd()));
	}

	/**
	 * Expand end time and remove overlapped calendar items.
	 * 
	 * @param changedItem
	 * @param calendarItems
	 */
	private void expandEnd(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		int index = calendarItems.indexOf(changedItem);

		if (index == calendarItems.size() - 1) {
			return;
		}

		int nextIndex = calendarItems.size() - 1;
		for (int i = index + 1; i < calendarItems.size() - 1; i++) {
			if (calendarItems.get(i).getEnd().getTime() > changedItem.getEnd()
					.getTime()) {
				nextIndex = i;
				break;
			}
		}

		Date locationStart = calendarItems.get(index + 1).getStart();
		Date locationEnd = changedItem.getEnd();

		List<LocationWrapper> locs = locationDataSource.getByDateRange(
				locationStart, locationEnd);

		Collections.sort(locs);

		if (changedItem instanceof ActivityCalendarItem) {
			setActivityLocations((ActivityCalendarItem) changedItem);
		} else {
			changedItem.addLocations(locs, false);
		}

		List<LocationWrapper> locsInNext = new ArrayList<LocationWrapper>();

		for (int i = locs.size() - 1; i >= 0
				&& locs.get(i).getTime().getTime() >= calendarItems
						.get(nextIndex).getStart().getTime(); i--) {
			locsInNext.add(0, locs.get(i));
		}

		calendarItems.get(nextIndex).setStart(changedItem.getEnd());
		if (calendarItems.get(nextIndex) instanceof ActivityCalendarItem) {
			setActivityLocations((ActivityCalendarItem) calendarItems
					.get(nextIndex));
		} else {
			calendarItems.get(nextIndex).removeLocations(locsInNext, true);
		}

		calendarItems.get(nextIndex).setIsModified(true);

		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		for (int i = nextIndex - 1; i > index; i--) {
			toBeDeleted.add(0, calendarItems.get(i));
		}

		for (CalendarItem item : toBeDeleted) {
			calendarItems.remove(item);
			itemsToBeDeleted.add(item);
		}
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

		if (index < calendarItems.size() - 1) {
			Date locationStart = changedItem.getEnd();
			Date locationEnd = calendarItems.get(index + 1).getStart();

			List<LocationWrapper> locs = locationDataSource.getByDateRange(
					locationStart, locationEnd);

			Collections.sort(locs);

			if (changedItem instanceof ActivityCalendarItem) {
				setActivityLocations((ActivityCalendarItem) changedItem);
			} else {
				changedItem.removeLocations(locs, false);
			}

			calendarItems.get(index + 1).setStart(changedItem.getEnd());
			if (calendarItems.get(index + 1) instanceof ActivityCalendarItem) {
				setActivityLocations((ActivityCalendarItem) calendarItems
						.get(index + 1));
			} else {
				calendarItems.get(index + 1).addLocations(locs, true);
			}

			calendarItems.get(index + 1).setIsModified(true);
		}
	}

	/**
	 * Expand start time and remove overlapped items.
	 * 
	 * @param changedItem
	 * @param calendarItems
	 */
	private void expandStart(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		int index = calendarItems.indexOf(changedItem);

		if (index == 0) {
			return;
		}

		int prevIndex = 0;
		for (int i = index - 1; i >= 0; i--) {
			if (calendarItems.get(i).getStart().getTime() < changedItem
					.getStart().getTime()) {
				prevIndex = i;
				break;
			}
		}

		Date locationStart = changedItem.getStart();
		Date locationEnd = calendarItems.get(index - 1).getEnd();

		List<LocationWrapper> locs = locationDataSource.getByDateRange(
				locationStart, locationEnd);

		Collections.sort(locs);

		// Log.i(TAG, "locations loaded: " + locs.size());

		if (changedItem instanceof ActivityCalendarItem) {
			setActivityLocations((ActivityCalendarItem) changedItem);
		} else {
			changedItem.addLocations(locs, true);
		}

		List<LocationWrapper> locsInPrev = new ArrayList<LocationWrapper>();

		for (LocationWrapper loc : locs) {
			if (loc.getTime().getTime() < calendarItems.get(prevIndex).getEnd()
					.getTime()) {
				locsInPrev.add(loc);
			} else {
				break;
			}
		}

		// Log.i(TAG, "locations belongs to new prev: " + locsInPrev.size());

		calendarItems.get(prevIndex).setEnd(changedItem.getStart());
		if (calendarItems.get(prevIndex) instanceof ActivityCalendarItem) {
			setActivityLocations((ActivityCalendarItem) calendarItems
					.get(prevIndex));
		} else {
			calendarItems.get(prevIndex).removeLocations(locsInPrev, false);
		}

		calendarItems.get(prevIndex).setIsModified(true);

		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		for (int i = prevIndex + 1; i < index; i++) {
			Log.i(TAG, calendarItems.get(i).toString() + " will be removed");
			toBeDeleted.add(calendarItems.get(i));
		}

		for (CalendarItem item : toBeDeleted) {
			calendarItems.remove(item);
			itemsToBeDeleted.add(item);
		}
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

		if (index != 0) {
			Date locationStart = calendarItems.get(index - 1).getEnd();
			Date locationEnd = changedItem.getStart();

			List<LocationWrapper> locs = locationDataSource.getByDateRange(
					locationStart, locationEnd);

			Collections.sort(locs);

			if (calendarItems.get(index - 1) instanceof ActivityCalendarItem) {
				setActivityLocations((ActivityCalendarItem) calendarItems
						.get(index - 1));
			} else {
				calendarItems.get(index - 1).addLocations(locs, false);
			}

			calendarItems.get(index - 1).setEnd(changedItem.getStart());
			if (changedItem instanceof ActivityCalendarItem) {
				setActivityLocations((ActivityCalendarItem) changedItem);
			} else {
				changedItem.removeLocations(locs, true);
			}
			calendarItems.get(index - 1).setIsModified(true);
		}
	}

	public CalendarItem postProcessingParentView(CalendarItemParentView parent,
			List<CalendarItem> items) {
		initializes();

		List<CalendarItem> tripSegments = parent.getChildren();

		if (tripSegments.size() <= 1) {
			return null;
		}

		Collections.sort(tripSegments);

		CalendarItem lastItem = tripSegments.get(tripSegments.size() - 1);

		// Date locationStart = tripSegments.get(0).getStart();
		// Date locationEnd = tripSegments.get(tripSegments.size() -
		// 2).getEnd();
		//
		// List<LocationWrapper> locs = locationDataSource.getByDateRange(
		// locationStart, locationEnd);
		//
		// Collections.sort(locs);
		//
		// ((TripCalendarItem) lastItem).addLocations(locs, true);
		// lastItem.setStart(tripSegments.get(0).getStart());

		for (int i = tripSegments.size() - 2; i >= 0; i--) {
			TripCalendarItem currentTripSegment = (TripCalendarItem) tripSegments
					.get(i);

			((TripCalendarItem) lastItem).merge(currentTripSegment, true);
			lastItem.setStart(currentTripSegment.getStart());
			items.remove(currentTripSegment);
			itemsToBeDeleted.add(currentTripSegment);
		}

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
	public CalendarItem postProcessingSplitter(CalendarItemSplitter splitter,
			List<CalendarItem> calendarItems) {
		initializes();

		List<LocationWrapper> locs = locationDataSource.getByDateRange(splitter
				.getSourceItem().getStart(), splitter.getSourceItem().getEnd());

		Collections.sort(locs);

		CalendarItem[] items = splitter.getCalendarItems(locs);

		int insertIndex = calendarItems.indexOf(splitter.getSourceItem());

		itemsToBeDeleted.add(calendarItems.remove(insertIndex));

		calendarItems.add(insertIndex, items[1]);

		calendarItems.add(insertIndex, items[0]);

		for (CalendarItem item : calendarItems) {
			item.reset();
		}

		merge(items[0], calendarItems);

		merge(items[1], calendarItems);

		// connectTripsAndActivities(calendarItems);

		return items[0];
	}

	/**
	 * Processing calendar items on user modification.
	 * 
	 * @param changedItem
	 * @param calendarItems
	 * @return
	 */
	public CalendarItem postProcessing(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		// Process list of calendar items on user modification.
		// Actions like combine/separate/delete items should be taken here.

		if (!calendarItems.contains(changedItem)) {
			return null;
		}

		initializes();

		rearrange(changedItem, calendarItems);

		merge(changedItem, calendarItems);

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

		// connectTripsAndActivities(calendarItems);

		if (changedItem instanceof TripCalendarItem) {
			summaryDataSource.setDistance(changedItem.getId(),
					summaryDataSource.updateDistance(changedItem.getId()));
		}
		return changedItem;
	}

	private void merge(CalendarItem changedItem,
			List<CalendarItem> calendarItems) {
		List<CalendarItem> toBeDeleted = new ArrayList<CalendarItem>();

		int i = calendarItems.indexOf(changedItem);

		if (i < 0) {
			return;
		}

		if (i > 0
				&& calendarItems.get(i - 1).getDescription()
						.equals(changedItem.getDescription())) {
			if (changedItem instanceof TripCalendarItem) {
				((TripCalendarItem) changedItem).merge(
						(TripCalendarItem) calendarItems.get(i - 1), true);
			}

			if (changedItem instanceof ActivityCalendarItem) {
				((ActivityCalendarItem) changedItem)
						.merge((ActivityCalendarItem) calendarItems.get(i - 1));
			}

			changedItem.setStart(calendarItems.get(i - 1).getStart());
			toBeDeleted.add(calendarItems.get(i - 1));
		}

		if (i < calendarItems.size() - 1
				&& calendarItems.get(i + 1).getDescription()
						.equals(changedItem.getDescription())) {
			if (changedItem instanceof TripCalendarItem) {
				((TripCalendarItem) changedItem).merge(
						(TripCalendarItem) calendarItems.get(i + 1), false);
			}

			if (changedItem instanceof ActivityCalendarItem) {
				((ActivityCalendarItem) changedItem)
						.merge((ActivityCalendarItem) calendarItems.get(i + 1));
			}

			changedItem.setEnd(calendarItems.get(i + 1).getEnd());
			toBeDeleted.add(calendarItems.get(i + 1));
		}

		for (CalendarItem item : toBeDeleted) {
			calendarItems.remove(item);
			itemsToBeDeleted.add(item);
		}

		if (changedItem instanceof TripCalendarItem) {
			summaryDataSource.setDistance(changedItem.getId(),
					summaryDataSource.updateDistance(changedItem.getId()));
		}
	}

	/**
	 * Finalize calendar items and save them to smartrac database.
	 * 
	 * @param calendarItems
	 *            Calendar items.
	 * @return True if saved successfully.
	 */
	public synchronized boolean saveCalendarItems(
			List<CalendarItem> calendarItems) {
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
		}

		boolean insertSuccess = calendarItemDataSource
				.insert(calendarItemsToBeSaved);
		boolean updateSuccess = calendarItemDataSource
				.updateCalendarItems(modifiedCalendarItems);
		boolean deleteSuccess = calendarItemDataSource.delete(itemsToBeDeleted);
		boolean updateSummaryForNew = dwellingSummaryDataSource
				.createDwellingSummaries(calendarItemsToBeSaved);
		boolean updateSummaryForModified = dwellingSummaryDataSource
				.createDwellingSummaries(modifiedCalendarItems);

		if (updateSummaryForNew || updateSummaryForModified) {
			sendDwellingSummaryUpdatedBroadcast();
		}

		return insertSuccess && updateSuccess && deleteSuccess;
	}

	private void sendDwellingSummaryUpdatedBroadcast() {
		Log.i(TAG, "broadcast dwelling summary updated");
		context.sendBroadcast(new Intent(context.getResources().getString(
				R.string.dwelling_summary_updated_broadcast)));
	}

	/**
	 * Delete all saved calendar items from database.
	 */
	public void deleteAllCalendarItems() {
		calendarItemDataSource.deleteAll();
	}

	/**
	 * Delete all raw data.
	 */
	public void deleteAllRawData() {
		locationDataSource.deleteAll();
		motionDataSource.deleteAll();
		// Delete dwelling locations for testing purpose.
		// dwellingLocationDataSource.deleteAll();
		// dwellingSummaryDataSource.deleteAll();
	}
}
