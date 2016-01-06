package com.smartracumn.smartrac.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.DwellingIndicator;
import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.model.ModeIndicator;
import com.smartracumn.smartrac.model.TripCalendarItem;
import com.smartracumn.smartrac.util.ActivityDetector;
import com.smartracumn.smartrac.util.CalendarItemUtil.ActivityMapDrawable;
import com.smartracumn.smartrac.util.CalendarItemUtil.TripMapDrawable;

public class CalendarItemFactory {
	private final ActivityDetector activityDetector;

	private final long INACCURATE_GPS_THRESHOLD;

	public CalendarItemFactory(Context context,
			ActivityDetector activityDetector) {
		this.activityDetector = activityDetector;
		this.INACCURATE_GPS_THRESHOLD = context.getResources().getInteger(
				com.smartracumn.smartrac.R.integer.lost_of_data_measure_time);
	}

	public synchronized List<CalendarItem> getCalendarItems(
			List<DwellingIndicator> dis, List<ModeIndicator> mis,
			List<LocationWrapper> locs) {
		List<CalendarItem> items = new ArrayList<CalendarItem>();

		if (dis.size() < 2) {
			return items;
		}

		Date start = dis.get(0).getAdjustedTime();
		Date end = dis.get(dis.size() - 1).getAdjustedTime();

		int modeIndex = 0;
		int locationIndex = 0;
		int dwellingIndex = 0;
		Date segmentStart = start;
		Date segmentEnd = end;

		List<DwellingIndicator> dss = getDwellingSegments(dis);

		for (; dwellingIndex < dss.size(); dwellingIndex++) {
			segmentEnd = dwellingIndex == dss.size() - 1 ? end : dss.get(
					dwellingIndex + 1).getAdjustedTime();

			if (!segmentStart.before(segmentEnd)) {
				continue;
			}

			// Select location fall into dwelling segment
			List<LocationWrapper> locations = new ArrayList<LocationWrapper>();
			for (; locationIndex < locs.size(); locationIndex++) {
				if (locs.get(locationIndex).getTime().getTime() >= segmentStart
						.getTime()
						&& locs.get(locationIndex).getTime().before(segmentEnd)) {
					locations.add(locs.get(locationIndex));
				} else if (!locs.get(locationIndex).getTime()
						.before(segmentEnd)) {
					break;
				}
			}

			// Select mode fall into dwelling segment
			List<ModeIndicator> modeIndicators = new ArrayList<ModeIndicator>();
			for (; modeIndex < mis.size(); modeIndex++) {
				if (mis.get(modeIndex).getTime().getTime() >= segmentStart
						.getTime()
						&& mis.get(modeIndex).getTime().before(segmentEnd)) {
					modeIndicators.add(mis.get(modeIndex));
				} else if (!mis.get(modeIndex).getTime().before(segmentEnd)) {
					break;
				}
			}

			DwellingIndicator ds = dss.get(dwellingIndex);

			// Negative dwelling indicator id indicates inaccurate GPS segment.
			if (ds.getId() >= 0) {
				if (ds.isDwelling()) {
					ActivityCalendarItem item = generateActivityCalendarItem(
							ds, segmentStart, segmentEnd, locations);

					if (item != null) {
						items.add(item);
					}
				} else {
					items.addAll(generateTripSegments(modeIndicators,
							segmentStart, segmentEnd, locations));
				}
			}

			segmentStart = segmentEnd;
		}

		return items;
	}

	private ActivityCalendarItem generateActivityCalendarItem(
			DwellingIndicator di, Date start, Date end,
			List<LocationWrapper> locs) {
		if (locs.size() == 0) {
			return null;
		}

		ActivityCalendarItem item = new ActivityCalendarItem(start, end,
				CalendarItem.Activity.UNKNOWN_ACTIVITY,
				new ActivityMapDrawable(locs));
		if (activityDetector != null) {
			activityDetector.predict(item);
		}

		return item;
	}

	private List<TripCalendarItem> generateTripSegments(
			List<ModeIndicator> modeIndicators, Date segmentStart,
			Date segmentEnd, List<LocationWrapper> locations) {
		List<TripCalendarItem> tripSegments = new ArrayList<TripCalendarItem>();

		// generate and add trip segments to items
		List<ModeIndicator> mss = getTripSegments(modeIndicators);
		int locIndex = 0;
		int mIndex = 0;
		Date sStart = segmentStart;
		Date sEnd = segmentEnd;

		for (; mIndex < mss.size(); mIndex++) {
			sEnd = mIndex == mss.size() - 1 ? segmentEnd : mss.get(mIndex + 1)
					.getTime();

			if (sStart.getTime() >= sEnd.getTime()) {
				continue;
			}

			// Select location fall into mode segment
			List<LocationWrapper> mLocs = new ArrayList<LocationWrapper>();

			for (; locIndex < locations.size(); locIndex++) {
				if (locations.get(locIndex).getTime().getTime() >= sStart
						.getTime()
						&& locations.get(locIndex).getTime().before(sEnd)) {
					mLocs.add(locations.get(locIndex));
				} else if (!locations.get(locIndex).getTime().before(sEnd)) {
					break;
				}
			}

			ModeIndicator ms = mss.get(mIndex);

			if (mLocs.size() > 0) {
				tripSegments.add(new TripCalendarItem(sStart, sEnd, ms
						.getMode(), new TripMapDrawable(mLocs)));
			}

			sStart = sEnd;
		}

		return tripSegments;
	}

	private List<DwellingIndicator> getDwellingSegments(
			List<DwellingIndicator> dis) {
		List<DwellingIndicator> dwellingSegments = new ArrayList<DwellingIndicator>();
		DwellingIndicator temp = null;

		for (DwellingIndicator di : dis) {
			if (dwellingSegments.size() == 0) {
				dwellingSegments.add(di);

				temp = di;
				continue;
			}

			if (di.isDwelling() == dwellingSegments.get(
					dwellingSegments.size() - 1).isDwelling()) {
				// If there is a gap more than inaccurate GPS in between. Insert
				// indication of inaccurate GPS in dwelling segment, which is a
				// dwelling indicator with id -1.
				if (temp != null
						&& di.getTime().getTime() - temp.getTime().getTime() >= INACCURATE_GPS_THRESHOLD) {
					DwellingIndicator inaccurateGPSInsert = new DwellingIndicator(
							-1, temp.getTime(), temp.isDwelling());
					inaccurateGPSInsert.setAdjustment(temp.getAdjustment());

					dwellingSegments.add(inaccurateGPSInsert);
					dwellingSegments.add(di);
				}

				temp = di;
				continue;
			}

			// Popout all dwelling segments lasts longer than current
			// dwelling indicator if current dwelling indicator has adjustment.
			// Otherwise, ignore current dwelling indicator.
			if (di.hasAdjustment()) {
				while (dwellingSegments.size() > 0
						&& dwellingSegments.get(dwellingSegments.size() - 1)
								.getAdjustedTime().getTime() >= di
								.getAdjustedTime().getTime()) {
					dwellingSegments.remove(dwellingSegments.size() - 1);
				}

				dwellingSegments.add(di);
			} else if (di.getAdjustedTime().getTime() > dwellingSegments
					.get(dwellingSegments.size() - 1).getTime().getTime()) {
				dwellingSegments.add(di);
			}

			temp = di;
		}

		return dwellingSegments;
	}

	private List<ModeIndicator> getTripSegments(List<ModeIndicator> mis) {
		List<ModeIndicator> modeSegments = new ArrayList<ModeIndicator>();

		for (ModeIndicator mi : mis) {
			if (modeSegments.size() == 0) {
				modeSegments.add(mi);
				continue;
			}

			if (mi.getMode() == modeSegments.get(modeSegments.size() - 1)
					.getMode()) {
				continue;
			}

			modeSegments.add(mi);
		}

		return modeSegments;
	}
}
