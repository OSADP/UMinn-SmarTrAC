package com.smartracumn.smartrac.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.location.Location;

import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.model.DwellingIndicator;
import com.smartracumn.smartrac.model.LocationWrapper;

/**
 * Dwelling/Non-dwelling separator takes care of doing real time separation of
 * trips and activities.
 * 
 * @author kangx385
 * 
 */
public class ActivitySeparator {
	private final String TAG = getClass().getName();

	private DwellingIndicator lastDwelling;

	private final int PROCESS_CACHE_LENGTH;

	private final double MAX_TOLERANCE;

	private final float ADJUST_THRESHOLD;

	private List<LocationWrapper> smoothedLocs = new ArrayList<LocationWrapper>();

	private List<Float> maxDistanceAfter = new ArrayList<Float>();

	private List<Float> maxDistanceBefore = new ArrayList<Float>();

	public void reset() {
		lastDwelling = null;
		smoothedLocs.clear();
		maxDistanceAfter.clear();
	}

	/**
	 * Initializes a new instance of the DwellingNonDwellingSeparator class.
	 * 
	 * @param context
	 * @param databaseWorkerHandler
	 */
	public ActivitySeparator(Context context) {
		this.PROCESS_CACHE_LENGTH = context.getResources().getInteger(
				R.integer.process_cache_length);
		this.MAX_TOLERANCE = context.getResources().getInteger(
				R.integer.activity_max_tolerance);
		this.ADJUST_THRESHOLD = context.getResources().getInteger(
				R.integer.adjust_threshold);
	}

	private LocationWrapper getSmoothedLocation(List<LocationWrapper> locs) {
		int count = 0;
		double sumLat = 0, sumLng = 0;

		for (LocationWrapper loc : locs) {
			if (loc != null && loc.getLocation() != null) {
				count++;
				sumLat += loc.getLocation().getLatitude();
				sumLng += loc.getLocation().getLongitude();
			}
		}

		if (count > 0) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, -15);

			Location smoothedLoc = new Location(getClass().getSimpleName());
			smoothedLoc.setLatitude(sumLat / count);
			smoothedLoc.setLongitude(sumLng / count);
			smoothedLoc.setTime(c.getTimeInMillis());
			return new LocationWrapper(0, smoothedLoc);
		} else {
			return null;
		}
	}

	public DwellingIndicator process(List<LocationWrapper> locs) {
		DwellingIndicator result = null;

		if (locs == null || locs.size() == 0) {
			return result;
		}

		smoothedLocs.add(getSmoothedLocation(locs));
		maxDistanceAfter.add(0f);

		updateMaxDistancAfters();

		int index = smoothedLocs.size() - 6;

		if (index >= 0) {
			float diameter = getDiameter();

			boolean isDwelling = diameter <= MAX_TOLERANCE;

			// Calendar c = Calendar.getInstance();
			// c.setTime(smoothedLocs.get(smoothedLocs.size() - 1).getTime());
			// c.add(Calendar.SECOND, -(PROCESS_CACHE_LENGTH + 1) / 2 * 30);
			// Date time = c.getTime();

			Date time = smoothedLocs.get(index).getTime();

			result = new DwellingIndicator(0, time, isDwelling);

			updateAdjustment(result, diameter);

			lastDwelling = result;
		}

		if (smoothedLocs.size() >= PROCESS_CACHE_LENGTH) {
			smoothedLocs.remove(0);
			maxDistanceAfter.remove(0);
		}

		return result;
	}

	private void updateMaxDistancAfters() {
		for (int i = smoothedLocs.size() - 2; i >= 0; i--) {
			float distance = smoothedLocs
					.get(i)
					.getLocation()
					.distanceTo(
							smoothedLocs.get(smoothedLocs.size() - 1)
									.getLocation());

			maxDistanceAfter.set(
					i,
					Math.max(maxDistanceAfter.get(i + 1),
							Math.max(distance, maxDistanceAfter.get(i))));
		}
	}

	private void updateMaxDistanceBefores() {
		maxDistanceBefore.clear();

		for (int i = 0; i < smoothedLocs.size(); i++) {
			maxDistanceBefore.add(0f);
		}

		for (int i = 1; i < smoothedLocs.size(); i++) {
			float distance = 0f;
			for (int j = 0; j < i; j++) {
				distance = Math.max(distance, smoothedLocs.get(i).getLocation()
						.distanceTo(smoothedLocs.get(j).getLocation()));
			}

			maxDistanceBefore.set(i,
					Math.max(distance, maxDistanceBefore.get(i - 1)));
		}
	}

	private void updateAdjustment(DwellingIndicator dwellingIndicator,
			float originalDiameter) {
		// Update adjustment only when there are previous dwelling indicator and
		// the previous one is different from current one.
		if (lastDwelling == null
				|| lastDwelling.isDwelling() == dwellingIndicator.isDwelling()) {
			return;
		}

		// iterate from head until the locations given dwelling indicator refers
		// to.
		if (dwellingIndicator.isDwelling()) {
			adjustActivityStart(dwellingIndicator, originalDiameter);
		} else {
			adjustTripStart(dwellingIndicator, originalDiameter);
		}
	}

	private void adjustTripStart(DwellingIndicator dwellingIndicator,
			float originalDiameter) {
		updateMaxDistanceBefores();

		for (int i = 5; i < smoothedLocs.size() - 1; i++) {
			float iDistance = maxDistanceBefore.get(i);
			float iPlusOneDistance = maxDistanceBefore.get(i + 1);

			if (Math.abs(iPlusOneDistance - iDistance) >= ADJUST_THRESHOLD) {
				dwellingIndicator.setAdjustment(smoothedLocs.get(i).getTime());
				break;
			}
		}

		if (!dwellingIndicator.hasAdjustment()) {
			dwellingIndicator.setAdjustment(smoothedLocs.get(
					smoothedLocs.size() - 1).getTime());
		}

		maxDistanceBefore.clear();
	}

	private void adjustActivityStart(DwellingIndicator dwellingIndicator,
			float originalDiameter) {
		for (int i = smoothedLocs.size() - 6; i > 0; i--) {
			float iDistance = maxDistanceAfter.get(i);
			float iMinusOneDistance = maxDistanceAfter.get(i - 1);

			if (Math.abs(iMinusOneDistance - iDistance) >= ADJUST_THRESHOLD) {
				dwellingIndicator.setAdjustment(smoothedLocs.get(i).getTime());
				break;
			}
		}

		if (!dwellingIndicator.hasAdjustment()) {
			dwellingIndicator.setAdjustment(smoothedLocs.get(0).getTime());
		}
	}

	private float getDiameter() {
		return maxDistanceAfter.get(0);
	}
}
