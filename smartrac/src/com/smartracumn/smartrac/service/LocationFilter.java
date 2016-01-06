package com.smartracumn.smartrac.service;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.service.SmartracDataService.LocationInfo;

public class LocationFilter {
	private final String TAG = getClass().getName();

	private final int ALLOWED_ACCURACY;

	private final double MIN_DISTANCE;

	private final double INSTANT_MOVEMENT_RECOVERY_DISTANCE;

	private final int DROP_LOCATION_CAP;

	private int dropLocationCount = 0;

	private LocationWrapper lastValidLocation;

	public void reset() {
		lastValidLocation = null;
	}

	public LocationFilter(Context context) {
		this.ALLOWED_ACCURACY = context.getResources().getInteger(
				R.integer.allowed_accuracy);
		this.MIN_DISTANCE = ((double) context.getResources().getInteger(
				R.integer.intermediate_location_min_distance_in_cm)) / 100.0;
		this.DROP_LOCATION_CAP = context.getResources().getInteger(
				R.integer.drop_location_cap);
		this.INSTANT_MOVEMENT_RECOVERY_DISTANCE = ((double) context
				.getResources().getInteger(
						R.integer.instant_movement_recovery_distance_in_cm)) / 100.0;
	}

	public List<LocationWrapper> filterByAccuracy(List<LocationWrapper> locs) {
		List<LocationWrapper> locations = new ArrayList<LocationWrapper>();

		for (LocationWrapper loc : locs) {
			if (loc.getLocation().getAccuracy() <= ALLOWED_ACCURACY) {
				locations.add(loc);
			}
		}

		return locations;
	}

	public List<LocationWrapper> filterBySpeedAndAcc(
			LocationWrapper lastAccurateLocation, LocationInfo locInfo,
			List<LocationWrapper> locs) {
		List<LocationWrapper> validLocs = new ArrayList<LocationWrapper>();

		if (dropLocationCount == DROP_LOCATION_CAP) {
			lastAccurateLocation = null;
		}

		for (LocationWrapper loc : locs) {
			if (lastAccurateLocation != null) {
				float timeGap = loc.getTime().getTime()
						- lastAccurateLocation.getTime().getTime();
				timeGap = timeGap < 1000 ? 1000 : timeGap;

				float speed = loc.getLocation().distanceTo(
						lastAccurateLocation.getLocation())
						/ (timeGap / 1000);
				float acc = -1;

				if (locInfo.absSpeed >= 0) {
					acc = Math.abs((speed - locInfo.absSpeed)
							/ (timeGap / 1000));
				}

				Log.i(TAG, "filter by sna: {speed: " + speed + "||acc: " + acc
						+ "}");

				if (speed < 500 && acc < 15) {
					validLocs.add(loc);
					locInfo.absAcc = acc;
					locInfo.absSpeed = speed;
					lastAccurateLocation = loc;
					dropLocationCount = 0;
				} else {
					dropLocationCount++;
				}
			} else {
				validLocs.add(loc);
				lastAccurateLocation = loc;
				dropLocationCount = 0;
			}
		}

		return validLocs;
	}

	public List<LocationWrapper> filterByDistance(
			LocationWrapper lastAccurateLoc, List<LocationWrapper> locs) {
		if (lastAccurateLoc == null) {
			return locs;
		}

		List<LocationWrapper> accLocs = new ArrayList<LocationWrapper>();
		for (LocationWrapper loc : locs) {
			if (loc.getLocation().distanceTo(lastAccurateLoc.getLocation()) < INSTANT_MOVEMENT_RECOVERY_DISTANCE) {
				accLocs.add(loc);
			}
		}

		return accLocs;
	}

	public List<LocationWrapper> getIntermediateLocations(
			List<LocationWrapper> locs) {
		List<LocationWrapper> intermediate = new ArrayList<LocationWrapper>();

		for (LocationWrapper loc : locs) {
			if (lastValidLocation != null) {
				float distance = lastValidLocation.getLocation().distanceTo(
						loc.getLocation());
				if (distance >= MIN_DISTANCE) {
					intermediate.add(loc);
					lastValidLocation = loc;
				}
			} else {
				intermediate.add(loc);
				lastValidLocation = loc;
			}
		}

		return intermediate;
	}
}
