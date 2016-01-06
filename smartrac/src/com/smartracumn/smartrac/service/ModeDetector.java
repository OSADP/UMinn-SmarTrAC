package com.smartracumn.smartrac.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.model.ModeIndicator;
import com.smartracumn.smartrac.model.MotionData;
import com.smartracumn.smartrac.util.ModeFactory;
import com.smartracumn.smartrac.util.MotionFeatureBuffer;
import com.smartracumn.smartrac.util.SpeedFeatureBuffer;
import com.smartracumn.smartrac.util.SummaryBuffer;

/**
 * Mode detector used to calculate and record travel mode every 30 seconds.
 * 
 * @author kangx385
 * 
 */
public class ModeDetector {
	private final String TAG = getClass().getName();

	private final int LONG_BUFFER_SIZE = 4;

	private final int FULL_BUFFER_SIZE = 8;

	private ModeFactory modeFactory;

	private SpeedFeatureBuffer speedBuffer30s;

	private MotionFeatureBuffer motionBuffer30s;

	private SpeedFeatureBuffer speedBuffer120s;

	private MotionFeatureBuffer motionBuffer120s;

	private SummaryBuffer<CalendarItem.TravelMode> initPredictions;

	private LocationWrapper latestLocation;

	public SpeedFeatureBuffer getSpeedBuffer30s() {
		return speedBuffer30s;
	}

	public MotionFeatureBuffer getMotionBuffer30s() {
		return motionBuffer30s;
	}

	public SpeedFeatureBuffer getSpeedBuffer120s() {
		return speedBuffer120s;
	}

	public MotionFeatureBuffer getMotionBuffer120s() {
		return motionBuffer120s;
	}

	public void reset() {
		speedBuffer30s.clear();
		motionBuffer30s.clear();
		speedBuffer120s.clear();
		motionBuffer120s.clear();
		latestLocation = null;
	}

	/**
	 * Initializes a new instance of the ModeDetector class.
	 * 
	 * @param context
	 * @param databaseWorkerHandler
	 */
	public ModeDetector(Context context) {
		this.modeFactory = ModeFactory.getInstance(context);

		speedBuffer30s = new SpeedFeatureBuffer(1);
		motionBuffer30s = new MotionFeatureBuffer(1);
		motionBuffer120s = new MotionFeatureBuffer(LONG_BUFFER_SIZE);
		speedBuffer120s = new SpeedFeatureBuffer(LONG_BUFFER_SIZE);

		initPredictions = new SummaryBuffer<CalendarItem.TravelMode>(
				FULL_BUFFER_SIZE);
	}

	/**
	 * Add locations to mode detector and start processing if location data and
	 * motion data are in sync.
	 * 
	 * @param locs
	 *            List of location wrappers.
	 */
	public List<LocationWrapper> getLocationWithAccurateSpeed(
			List<LocationWrapper> locs) {
		List<LocationWrapper> result = new ArrayList<LocationWrapper>();

		if (locs.size() == 0) {
			return result;
		}

		// Do pair compare to make sure only location contains trustworthy speed
		// is used.
		for (int i = 0; i < locs.size(); i++) {
			LocationWrapper location = locs.get(i);

			if (latestLocation != null
					&& location.getLocation().getTime()
							- latestLocation.getLocation().getTime() > 0
					&& location.getLocation().getTime()
							- latestLocation.getLocation().getTime() <= 1500) {
				result.add(location);
			}

			latestLocation = location;
		}

		return result;
	}

	public ModeIndicator process(List<MotionData> motionDatas,
			List<LocationWrapper> locations) {
		// locations = getLocationWithAccurateSpeed(locations);
		ModeIndicator result = null;

		if (motionDatas.size() == 0 || locations.size() == 0) {
			return result;
		}

		speedBuffer30s.enqueue(locations);
		speedBuffer120s.enqueue(locations);
		motionBuffer30s.enqueue(motionDatas);
		motionBuffer120s.enqueue(motionDatas);

		Log.i(TAG,
				speedBuffer30s.size() + " || " + speedBuffer120s.size()
						+ " || " + motionBuffer30s.size() + " || "
						+ motionBuffer120s.size());

		// Do mode prediction.
		if (speedBuffer120s.size() == LONG_BUFFER_SIZE
				&& motionBuffer120s.size() == LONG_BUFFER_SIZE) {
			CalendarItem.TravelMode initPredication = this.modeFactory
					.predictMode(speedBuffer30s, speedBuffer120s,
							motionBuffer30s, motionBuffer120s);
			initPredictions.enqueue(initPredication);
			if (initPredictions.size() == FULL_BUFFER_SIZE) {

				CalendarItem.TravelMode finalPrediction = initPredictions
						.getMostFrequentEntity();

				Calendar c = Calendar.getInstance();
				c.add(Calendar.SECOND, -30 * (FULL_BUFFER_SIZE + 1) / 2);
				Date finalTime = c.getTime();

				result = new ModeIndicator(finalTime, finalPrediction);
			}
		}

		return result;
	}
}