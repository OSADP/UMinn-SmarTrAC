package com.smartracumn.smartrac.util;


import android.content.Context;
import android.os.Handler;

import com.smartracumn.smartrac.model.ActivityCalendarItem;

/**
 * Mode detector used to calculate and record travel mode every 30 seconds.
 * 
 * @author kangx385
 * 
 */
public class ActivityDetector {

	private final Context context;

	private final Handler handler;

	private ActivityFactory activityFactory;
	
	private DwellingLocationManager locationManager;

	/**
	 * Initializes a new instance of the ModeDetector class.
	 * 
	 * @param context
	 * @param databaseWorkerHandler
	 */
	public ActivityDetector(Context context, Handler databaseWorkerHandler) {
		this.context = context;
		this.handler = databaseWorkerHandler;
		this.activityFactory = ActivityFactory.getInstance(this.context);
		this.locationManager = new DwellingLocationManager(context,handler);
	}

	public void predict(ActivityCalendarItem item) {
		
		//Checking if the dwelling region is in the database
		locationManager.predict(item);
				
	}
	
	public DwellingLocationManager getDwellingLocationManager(){
		return locationManager;
	}

}
