package com.smartracumn.smartrac.util;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.data.DwellingSummaryDataSource;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.DwellingLocation;

/**
 * Activity type predictor used to predict activity types based on user tagged
 * locations.
 * 
 * @author kangx385
 * 
 */
public class DwellingLocationManager {
	private final String TAG = getClass().getName();

	private final Context context;

	private final Handler handler;

	private final DwellingSummaryDataSource dwellingSummaryDataSource;

	private List<DwellingLocation> dwellingLocations;

	private boolean locationsLoaded;

	private boolean requestRefresh;

	private DwellingSummaryUpdatedBroadcastReceiver receiver;

	private Runnable getDwellingLocationRunnable = new Runnable() {

		@Override
		public void run() {
			dwellingLocations = dwellingSummaryDataSource.getAll();
			locationsLoaded = true;

			if (requestRefresh) {
				broadcastDwellingLocationsLoaded();
				requestRefresh = false;
			}
		}

	};

	private void broadcastDwellingLocationsLoaded() {
		Intent intent = new Intent(context.getResources().getString(
				R.string.dwelling_locations_loaded_broadcast));
		context.sendBroadcast(intent);
	}

	/**
	 * Initializes a new instance of the ActivityTypePredictor class.
	 * 
	 * @param context
	 * @param databaseWorkerHandler
	 */
	public DwellingLocationManager(Context context,
			Handler databaseWorkerHandler) {
		this.context = context;
		this.handler = databaseWorkerHandler;
		this.dwellingSummaryDataSource = new DwellingSummaryDataSource(
				this.context);
		this.handler.post(getDwellingLocationRunnable);
		this.receiver = new DwellingSummaryUpdatedBroadcastReceiver();
	}

	public DwellingSummaryUpdatedBroadcastReceiver getReceiver() {
		return this.receiver;
	}

	/**
	 * Reload dwelling locations.
	 */
	private void reloadDwellingLocations() {
		this.handler.post(getDwellingLocationRunnable);
	}

	public void forceReload() {
		dwellingLocations = dwellingSummaryDataSource.getAll();
	}

	/**
	 * Predict activity type for given activity.
	 * 
	 * @param activity
	 */
	public void predict(ActivityCalendarItem activity) {
		if (locationsLoaded && dwellingLocations != null) {
			LatLng activityCenter = activity.getWeightedCenter();

			Location centerLoc = new Location(TAG);
			centerLoc.setLatitude(activityCenter.latitude);
			centerLoc.setLongitude(activityCenter.longitude);

			int visitFreq = 0;

			for (DwellingLocation loc : dwellingLocations) {
				double distance = loc.distanceTo(centerLoc);
				if (distance <= 50 && loc.getVisitFrequency() > visitFreq) {
					activity.setAssociateDwellingLocation(new DwellingLocation(
							loc));
					visitFreq = loc.getVisitFrequency();
				}
			}
		} else if (!locationsLoaded) {
			requestRefresh = true;
		}
	}

	/**
	 * Dwelling summary updated broadcast receiver used to receive dwelling
	 * summary updated broadcast and reload dwelling locations accordingly.
	 * 
	 * @author Jie
	 * 
	 */
	public class DwellingSummaryUpdatedBroadcastReceiver extends
			BroadcastReceiver {
		private final String TAG = getClass().getName();

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.i(TAG, "received dwelling summary updated broadcast");
			reloadDwellingLocations();
		}
	}
}
