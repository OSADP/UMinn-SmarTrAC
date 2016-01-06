package com.smartracumn.smartrac.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.smartracumn.smartrac.model.MotionData;

/**
 * The class represent instant movement detector which checks instant
 * accelerations to determine whether or not to turn the gps off.
 * 
 * @author kangx385
 * 
 */
public class InstantMovementDetector {
	private final String TAG = getClass().getName();

	private final double INSTANT_MOTION_THRESHOLD = 0.15;

	private final int STABLE_DURATION_THRESHOLD = 5 * 60 * 1000;

	private Date lastStableTime = null;

	/**
	 * Check 30 seconds instant movement indicators.
	 * 
	 * @param movements
	 */
	public boolean checkInstantMovements(List<MotionData> movements) {
		double maxMag = getMaxPerSecMagnitude(movements);

		if (maxMag >= INSTANT_MOTION_THRESHOLD) {
			Log.i(TAG, "phone moved from put");
			lastStableTime = null;
			return false;
		} else {
			if (lastStableTime == null) {
				lastStableTime = Calendar.getInstance().getTime();
				Log.i(TAG, "motion stop start");

				return false;
			} else if (Calendar.getInstance().getTime().getTime()
					- lastStableTime.getTime() >= STABLE_DURATION_THRESHOLD) {
				Log.i(TAG, "motion stopped for a while");
				return true;
			} else {
				return false;
			}
		}
	}

	private double getMaxPerSecMagnitude(List<MotionData> movements) {
		double maxMag = 0;
		double sumMag = 0;
		int count = 0;
		MotionData prev = null;

		for (MotionData movement : movements) {
			if (prev == null
					|| prev.getTime().getTime() / 1000 == movement.getTime()
							.getTime() / 1000) {
				sumMag += movement.getLinearMag();
				count++;
			} else {
				maxMag = Math.max(maxMag, sumMag / count);

				sumMag = movement.getLinearMag();
				count = 1;
			}
		}

		maxMag = Math.max(maxMag, sumMag / count);

		return maxMag;
	}
}
