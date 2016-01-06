package com.smartracumn.smartrac.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.data.SmartracData;
import com.smartracumn.smartrac.model.DwellingIndicator;
import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.model.ModeIndicator;
import com.smartracumn.smartrac.model.MotionData;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * Smartrac Data Service.
 * 
 * @author kangx385
 * 
 */
public class SmartracDataService {
	private final String TAG = getClass().getName();

	public enum State {
		Inaccurate_GPS, Phone_Stationary, Normal, Off;
	}

	private final int RATE;

	private final Context context;

	private final SmartracData data;

	private final DataServiceBroadcastManager broadcastManager;

	private final MotionListener motionListener;

	private final InstantMovementDetector instantMovementDetector;

	private final LocationListener locationListener;

	private final LocationFilter locationFilter;

	private final ActivitySeparator activitySeparator;

	private final ModeDetector modeDetector;

	private final int ALLOWED_NO_DATA_DELAY;

	private final WakeLock wakeLock;

	private final Handler handler = new Handler();

	private final Runnable timer = new Runnable() {

		@Override
		public void run() {
			process();

			if (state != State.Off) {
				handler.postDelayed(this, RATE);
			}
		}
	};

	private State state = State.Off;

	private DwellingIndicator dwelling;

	private ModeIndicator mode;

	private LocationWrapper accurateLocation;

	private LocationInfo cachedInfo;

	private int instantMovementRecovery = 0;

	private Queue<ModeIndicator> cachedModeChange;

	public SmartracDataService(Context context, int sampling_rate,
			SmartracData data) {
		this.context = context;
		this.RATE = sampling_rate;
		this.data = data;
		this.cachedModeChange = new LinkedList<ModeIndicator>();
		this.broadcastManager = new DataServiceBroadcastManager(context);
		this.motionListener = new MotionListener(context);
		this.instantMovementDetector = new InstantMovementDetector();
		this.locationListener = new LocationListener(context);
		this.locationFilter = new LocationFilter(context);
		this.activitySeparator = new ActivitySeparator(context);
		this.modeDetector = new ModeDetector(context);
		this.ALLOWED_NO_DATA_DELAY = context.getResources().getInteger(
				R.integer.allowed_no_data_delay);

		PowerManager powerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);

		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"MyWakeTag");

		this.cachedInfo = new LocationInfo();
	}

	private void setState(State state) {
		this.state = state;

		broadcastManager.dataServiceState(state);
	}

	public boolean startDataService() {
		if (state == State.Off) {
			motionListener.start();
			locationListener.start();
			handler.removeCallbacks(timer);
			handler.post(timer);
			setState(State.Normal);
			broadcastManager.dataServiceStart(true);
			wakeLock.acquire();

			return true;
		}

		return false;
	}

	public boolean stopDataService() {
		if (state != State.Off) {
			motionListener.stop();
			locationListener.stop();
			handler.removeCallbacks(timer);

			activitySeparator.reset();
			modeDetector.reset();
			locationFilter.reset();
			cachedModeChange.clear();
			accurateLocation = null;
			cachedInfo.invalidate();
			dwelling = null;
			mode = null;

			setState(State.Off);
			wakeLock.release();

			return true;
		}

		return false;
	}

	private boolean checkAccurateLocation(List<LocationWrapper> location) {
		if (location.size() > 0) {
			if (state == State.Inaccurate_GPS) {
				setState(State.Normal);
			}

			accurateLocation = location.get(location.size() - 1);
			return false;
		}

		if (state == State.Normal) {
			if (accurateLocation == null) {
				return true;
			}

			long timeGap = Calendar.getInstance().getTimeInMillis()
					- accurateLocation.getTime().getTime();

			if (timeGap > ALLOWED_NO_DATA_DELAY) {
				return true;
			} else {
				Location loc = new Location(accurateLocation.getLocation());
				loc.setProvider(context.getResources().getString(
						R.string.no_data_impute));
				loc.setTime(Calendar.getInstance().getTimeInMillis());

				location.add(new LocationWrapper(loc));
			}
		} else if (state == State.Phone_Stationary && accurateLocation != null) {
			Date currentTime = Calendar.getInstance().getTime();

			accurateLocation.setTime(currentTime);
			accurateLocation.getLocation().setTime(currentTime.getTime());
			accurateLocation.getLocation().setProvider(
					context.getResources().getString(R.string.gps_off_impute));

			location.add(accurateLocation);
		}

		return false;
	}

	private boolean checkInstantMovement(boolean phonePut) {
		if (phonePut) {
			if (state == State.Phone_Stationary) {
				return false;
			} else if (state == State.Inaccurate_GPS || state == State.Normal) {

				return true;
			}
		} else {
			if (state == State.Phone_Stationary) {

				return true;
			} else if (state == State.Inaccurate_GPS || state == State.Normal) {
				return false;
			}
		}

		return false;
	}

	private void process() {
		// Get and save locations.
		List<LocationWrapper> location = locationListener.getLocations();
		List<LocationWrapper> acc = locationFilter.filterByAccuracy(location);
		acc = locationFilter.filterBySpeedAndAcc(accurateLocation, cachedInfo,
				acc);
		// Do additional distance check if instant movement is triggered.
		// Because android could feed out dated location data when GPS is
		// started.
		// Run for at most instant movement recovery times.
		if (instantMovementRecovery > 0) {
			acc = locationFilter.filterByDistance(accurateLocation, acc);
			instantMovementRecovery--;
		}

		List<LocationWrapper> intermediate = locationFilter
				.getIntermediateLocations(acc);

		boolean broadcastInaccurateGPS = checkAccurateLocation(acc);

		if (acc.size() == 1
				&& (acc.get(0)
						.getLocation()
						.getProvider()
						.equals(context.getResources().getString(
								R.string.no_data_impute)) || acc
						.get(0)
						.getLocation()
						.getProvider()
						.equals(context.getResources().getString(
								R.string.gps_off_impute)))) {
			location.add(acc.get(0));
		}

		Log.i(TAG, "get locations " + location.size()
				+ " || accurate locations: " + acc.size()
				+ " || intermediate locations: " + intermediate.size());

		data.getLocationDataSource().insert(location);
		data.getLocationDataSource().insertIntermediate(intermediate);

		// Get and save motions
		List<MotionData> motion = motionListener.getMotionDatas();

		data.getMotionDataSource().insert(motion);

		// Get and save phone put indicators
		boolean phonePut = instantMovementDetector
				.checkInstantMovements(motion);
		Log.i(TAG, "get motion datas " + motion.size() + " || phone is put: "
				+ phonePut);

		if (state == State.Phone_Stationary && !phonePut) {
			data.getModeDataSource().insertInstantMovement(
					Calendar.getInstance().getTime(), !phonePut);
		} else if (state != State.Phone_Stationary && phonePut) {
			data.getModeDataSource().insertInstantMovement(
					Calendar.getInstance().getTime(), !phonePut);
		}

		boolean broadcastInstantMovement = checkInstantMovement(phonePut);

		// Generate dwelling indicator using all acc locations, no data impute
		// generated dwelling will be deleted when inaccurate GPS detected.
		DwellingIndicator di = activitySeparator.process(acc);
		if (di != null) {
			Log.i(TAG,
					"get dwelling indicator "
							+ di.isDwelling()
							+ " || "
							+ SmartracDataFormat.getDateTimeFormat().format(
									di.getTime()));

			data.getDwellingDataSource().createDwellingIndicator(di.getTime(),
					di.isDwelling(), di.getAdjustment());
		} else {
			Log.i(TAG, "get dwelling indicator failed");
		}

		// generate mode indicator using non-gps_off_imp locations.
		List<LocationWrapper> miLocs = new ArrayList<LocationWrapper>();

		for (LocationWrapper loc : acc) {
			if (!loc.getLocation()
					.getProvider()
					.equals(context.getResources().getString(
							R.string.gps_off_impute))) {
				miLocs.add(loc);
			}
		}

		ModeIndicator mi = modeDetector.process(motion, miLocs);
		if (mi != null) {
			Log.i(TAG,
					"get mode indicator "
							+ mi.getMode()
							+ " || "
							+ SmartracDataFormat.getDateTimeFormat().format(
									mi.getTime()));

			data.getModeDataSource().createModeIndicator(mi.getTime(),
					mi.getMode());

			data.getModeFeaturesDataSource().writeMotionBuffer(mi.getTime(),
					modeDetector.getMotionBuffer30s(),
					modeDetector.getMotionBuffer120s());
			data.getModeFeaturesDataSource().writeSpeedBuffer(mi.getTime(),
					modeDetector.getSpeedBuffer30s(),
					modeDetector.getSpeedBuffer120s());
		} else {
			Log.i(TAG, "get mode indicator failed");
		}

		Log.i(TAG, String.format(
				"phonePut-%s, state-%s, acc_size-%d, di_created-%s", phonePut,
				state.name(), acc.size(), di != null));

		if (broadcastInstantMovement) {
			if (phonePut) {
				broadcastManager.phoneMovement(false);
				locationFilter.reset();
				locationListener.stop();
				setState(State.Phone_Stationary);
			} else {
				broadcastManager.phoneMovement(true);
				locationFilter.reset();
				locationListener.start();
				instantMovementRecovery = 2;
				setState(State.Normal);
			}
		}

		if (broadcastInaccurateGPS) {
			Calendar actualTimeLostLocation = Calendar.getInstance();
			actualTimeLostLocation.add(Calendar.MILLISECOND,
					-ALLOWED_NO_DATA_DELAY);

			broadcastManager.inaccurateGPS(actualTimeLostLocation.getTime());
			activitySeparator.reset();
			modeDetector.reset();
			locationFilter.reset();
			cachedModeChange.clear();
			accurateLocation = null;
			cachedInfo.invalidate();
			dwelling = null;
			mode = null;
			setState(State.Inaccurate_GPS);
		}

		if (di != null) {
			// If previous dwelling is null, indicating that current dwelling is
			// the first valid dwelling indicator, reset filter to guaratee next
			// intermediate location get save.
			if (dwelling == null) {
				locationFilter.reset();
			}

			boolean activityChanged = false;

			if (dwelling != null && dwelling.isDwelling() != di.isDwelling()) {
				broadcastManager.activityChanged(di);
				activityChanged = true;
			}

			// pop mode change indicator until cached mode change is empty or
			// the first mode indicator has time greater than dwelling indicator
			// time.
			while (cachedModeChange.size() > 0
					&& ((di.hasAdjustment() && cachedModeChange.peek()
							.getTime().getTime() <= di.getAdjustment()
							.getTime()) || (!di.hasAdjustment() && cachedModeChange
							.peek().getTime().getTime() <= di.getTime()
							.getTime() - 180000))) {
				ModeIndicator mChange = cachedModeChange.poll();

				// If dwelling status regarding popped mode indicator indicating
				// trip, broadcast corresponding mode change.
				if (!di.isDwelling() && !activityChanged) {
					broadcastManager.modeChanged(mChange);
				}
			}

			dwelling = di;
		}

		if (mi != null) {
			// if mode change is detected, cache mode change.
			if (mode != null && mode.getMode() != mi.getMode()) {
				cachedModeChange.offer(mi);
			}

			mode = mi;
		}

		// cached mode change cannot be more than 3 minutes, the adjustment
		// upper bound.
		while (cachedModeChange.size() > 12) {
			cachedModeChange.poll();
		}
	}

	public class LocationInfo {
		public float absSpeed = -1;
		public float absAcc = -1;

		public void invalidate() {
			absSpeed = -1;
			absAcc = -1;
		}
	}
}
