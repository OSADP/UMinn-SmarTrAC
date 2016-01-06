package com.smartracumn.smartrac.model;

import java.util.Date;
import java.util.List;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.util.CalendarItemUtil.ActivityMapDrawable;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * Activity type calendar item which can be drawn on the map as areas.
 * 
 * @author kangx385
 * 
 */
public class ActivityCalendarItem extends CalendarItem implements ClusterItem {

	private Activity activity;

	private Activity userCorrectedActivity;

	private long dwellingRegionId;

	private DwellingLocation associateDwellingLocation;

	private com.smartracumn.smartrac.util.CalendarItemUtil.ActivityMapDrawable mapDrawable;

	private ClusterManager<ClusterItem> clusterManager;

	/**
	 * Initializes a new instance of the ActivityCalendarItem class.
	 * 
	 * @param id
	 *            Calendar item id.
	 * @param start
	 *            The start time.
	 * @param end
	 *            The end time.
	 * @param activity
	 *            The activity type.
	 * @param mapDrawable
	 *            Map drawable.
	 */
	public ActivityCalendarItem(long id, long dwellingRegionId, Date start,
			Date end, Activity activity, ActivityMapDrawable mapDrawable) {
		this(start, end, activity, mapDrawable);
		this.setId(id);
		this.setDwellingRegionId(dwellingRegionId);
	}

	public void setDwellingRegionId(long dwellingRegionId) {
		this.dwellingRegionId = dwellingRegionId;
	}

	/**
	 * Set id.
	 * 
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
		// this.isFinalized = true;
	}

	/**
	 * Initializes a new instance of ActivityCalendarItem class.
	 * 
	 * @param start
	 *            Start time.
	 * @param end
	 *            End time.
	 * @param activity
	 *            Activity type.
	 * @param mapDrawable
	 *            Map drawable.
	 */
	public ActivityCalendarItem(Date start, Date end, Activity activity,
			ActivityMapDrawable mapDrawable) {
		this.type = Type.ACTIVITY;
		this.start = start;
		this.end = end;
		this.activity = activity;
		this.mapDrawable = mapDrawable;
		setActivity(activity);
	}

	/**
	 * Set predicted activity.
	 * 
	 * @param activity
	 */
	public void setActivity(CalendarItem.Activity activity) {
		this.activity = activity;
		this.description = this.activity.toString();
	}

	/**
	 * Get final activity.
	 * 
	 * @return
	 */
	public Activity getActivity() {
		if (userCorrectedActivity != null) {
			return userCorrectedActivity;
		}

		return activity;
	}

	/**
	 * Get user corrected activity.
	 * 
	 * @return User corrected activity.
	 */
	public Activity getUserCorrectedActivity() {
		return this.userCorrectedActivity;
	}

	/**
	 * Set user corrected activity.
	 * 
	 * @param activity
	 *            The user corrected activity.
	 */
	public void setUserCorrectedActivity(Activity activity) {
		this.userCorrectedActivity = activity;
		this.description = this.userCorrectedActivity.toString();
	}

	/**
	 * Get predicted activity.
	 * 
	 * @return Predicted activity.
	 */
	public Activity getPredictedActivity() {
		return this.activity;
	}

	@Override
	public String getDescription() {
		if (isInProgress()) {
			return this.description + " in progress...";
		}

		return this.description;
	}

	@Override
	public String getPolyCode() {
		return mapDrawable.getPolyCode();
	}

	@Override
	public LatLngBounds drawMap(GoogleMap map, boolean highlighted) {
		if (!highlighted) {
			this.clusterManager.addItem(this);
		} else {
			if (description.contains("Home")) {
				map.addMarker(new MarkerOptions()
						.position(getPosition())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.map_selected_homehome)));
			} else if (description.contains("Work")) {
				map.addMarker(new MarkerOptions().position(getPosition()).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.map_selected_work)));
			} else if (description.contains("Social")) {
				map.addMarker(new MarkerOptions().position(getPosition()).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.map_selected_src)));
			} else if (description.contains("Shop")) {
				map.addMarker(new MarkerOptions().position(getPosition()).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.map_selected_shop)));
			} else if (description.contains("Education")) {
				map.addMarker(new MarkerOptions()
						.position(getPosition())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.map_selected_education)));
			} else if (description.contains("Eat")) {
				map.addMarker(new MarkerOptions().position(getPosition()).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.map_selected_eat)));
			} else if (description.contains("Other personal")) {
				map.addMarker(new MarkerOptions().position(getPosition()).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.map_selected_opb)));
			}else{
				map.addMarker(new MarkerOptions().position(getPosition()).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.map_selected_unknown)));

			}
		}

		return mapDrawable.getBounds();
	}

	@Override
	public CalendarItem addMarker(GoogleMap map, MarkerOptions markerOptions) {
		markerOptions.position(this.mapDrawable.getCenterLatLng());
		map.addMarker(markerOptions);

		return this;
	}

	/**
	 * Merge activity calendar with other activity.
	 * 
	 * @param otherItem
	 */
	public void merge(ActivityCalendarItem otherItem) {
		this.mapDrawable.updateBound(otherItem.mapDrawable);

		this.mapDrawable.refresh();
	}

	@Override
	public String toString() {
		return this.description + "\n"
				+ SmartracDataFormat.getTimeFormat().format(this.getStart())
				+ " - "
				+ SmartracDataFormat.getTimeFormat().format(this.getEnd());
	}

	@Override
	public boolean contains(LatLng point) {
		return this.mapDrawable.contains(point);
	}

	/**
	 * Set locations for activity calendar item.
	 * 
	 * @param locations
	 */
	public void setLocations(List<LocationWrapper> locations) {
		this.mapDrawable.setLocations(locations);

		this.mapDrawable.refresh();
	}

	/**
	 * Get the dwelling region id.
	 * 
	 * @return
	 */
	public long getDwellingRegionId() {
		return this.dwellingRegionId;
	}

	/**
	 * Get the weighted center for this activity.
	 * 
	 * @return
	 */
	public LatLng getWeightedCenter() {
		return this.mapDrawable.getCenterLatLng();
	}

	@Override
	public void addLocations(List<LocationWrapper> locations,
			boolean insertToHead) {
		this.mapDrawable.addLocations(locations);

		this.mapDrawable.refresh();
	}

	@Override
	public void removeLocations(List<LocationWrapper> locations,
			boolean removeFromHead) {
		this.mapDrawable.removeLocations(locations);

		this.mapDrawable.refresh();
	}

	@Override
	public List<LocationWrapper> getLocations() {
		return this.mapDrawable.getLocations();
	}

	public DwellingLocation getAssociateDwellingLocation() {
		return associateDwellingLocation;
	}

	public void setAssociateDwellingLocation(DwellingLocation dwellingLocation) {
		Log.i("ActivityCalendarItem", "set associate dwelling location"
				+ dwellingLocation.toString());
		this.associateDwellingLocation = dwellingLocation;
		setActivity(dwellingLocation.getActivity());
	}

	@Override
	public LatLng getPosition() {
		return this.getWeightedCenter();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ClusterItem> void setClusterManager(
			ClusterManager<T> clusterManager) {
		this.clusterManager = (ClusterManager<ClusterItem>) clusterManager;
	}

}
