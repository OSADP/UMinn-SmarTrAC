package com.smartracumn.smartrac.data;

import android.content.Context;

public class SmartracData {
	private final String TAG = getClass().getName();

	private ActivityFeaturesDataSource activityFeatures;
	private CalendarItemDataSource calendarItem;
	private DwellingDataSource dwelling;
	private DwellingLocationDataSource dwellingLocation;
	private DwellingSummaryDataSource dwellingSummary;
	private LocationDataSource location;
	private ModeDataSource mode;
	private ModeFeaturesDataSource modeFeatures;
	private MotionDataSource motion;
	private SummaryDataSource summary;

	public SmartracData(Context context) {
		activityFeatures = new ActivityFeaturesDataSource(context);
		calendarItem = new CalendarItemDataSource(context);
		dwelling = new DwellingDataSource(context);
		dwellingLocation = new DwellingLocationDataSource(context);
		dwellingSummary = new DwellingSummaryDataSource(context);
		location = new LocationDataSource(context);
		mode = new ModeDataSource(context);
		modeFeatures = new ModeFeaturesDataSource(context);
		motion = new MotionDataSource(context);
		summary = new SummaryDataSource(context);
	}

	public ActivityFeaturesDataSource getActivityFeaturesDataSource() {
		return activityFeatures;
	}

	public CalendarItemDataSource getCalendarItemDataSource() {
		return calendarItem;
	}

	public DwellingDataSource getDwellingDataSource() {
		return dwelling;
	}

	public DwellingLocationDataSource getDwellingLocationDataSource() {
		return dwellingLocation;
	}

	public DwellingSummaryDataSource getDwellingSummaryDataSource() {
		return dwellingSummary;
	}

	public LocationDataSource getLocationDataSource() {
		return location;
	}

	public ModeDataSource getModeDataSource() {
		return mode;
	}

	public ModeFeaturesDataSource getModeFeaturesDataSource() {
		return modeFeatures;
	}

	public MotionDataSource getMotionDataSource() {
		return motion;
	}

	public SummaryDataSource getSummaryDataSource() {
		return summary;
	}
}
