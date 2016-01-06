package com.smartracumn.smartrac.model;

import java.util.ArrayList;
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
import com.smartracumn.smartrac.util.CalendarItemUtil.TripMapDrawable;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * Trip type calendar item which can be drawn on the map as poly lines, and
 * being able to be split by time.
 * 
 * @author kangx385
 * 
 */
public class TripCalendarItem extends CalendarItem {
	private final String TAG = getClass().getName();

	private TravelMode mode;

	private TravelMode userCorrectedMode;

	private long tripId;

	private long tripSegmentId;

	private com.smartracumn.smartrac.util.CalendarItemUtil.TripMapDrawable mapDrawable;

	/**
	 * Initializes a new instance of the TripCalendarItem class.
	 * 
	 * @param id
	 *            The calendar item id.
	 * @param start
	 *            The start date.
	 * @param end
	 *            The end date.
	 * @param mode
	 *            The travel mode.
	 * @param mapDrawable
	 *            Map drawable.
	 */
	public TripCalendarItem(long id, long tripSegmentId, Date start, Date end,
			TravelMode mode, TripMapDrawable mapDrawable) {
		this(start, end, mode, mapDrawable);
		this.setId(id);
		this.setTripSegmentId(tripSegmentId);
	}

	public long getTripSegmentId() {
		return this.tripSegmentId;
	}

	public void setTripSegmentId(long tripSegmentId) {
		this.tripSegmentId = tripSegmentId;
	}

	/**
	 * Initializes a new instance of the TripCalendarItem class.
	 * 
	 * @param start
	 *            The start time.
	 * @param end
	 *            The end time.
	 * @param mode
	 *            The travel mode.
	 * @param mapDrawable
	 *            Map drawable.
	 */
	public TripCalendarItem(Date start, Date end, TravelMode mode,
			TripMapDrawable mapDrawable) {
		this.type = Type.TRIP;
		this.start = start;
		this.end = end;
		this.mode = mode;
		this.mapDrawable = mapDrawable;
		this.description = this.mode.toString();
	}

	public TravelMode getMode() {
		if (userCorrectedMode != null) {
			return userCorrectedMode;
		}
		return mode;
	}

	public void setMode(TravelMode mode) {
		this.mode = mode;
	}

	/**
	 * Get user corrected travel mode.
	 * 
	 * @return User corrected travel mode.
	 */
	public TravelMode getUserCorrectedMode() {
		return this.userCorrectedMode;
	}

	/**
	 * Set user corrected travel mode.
	 * 
	 * @param mode
	 *            The user corrected travel mode.
	 */
	public void setUserCorrectedMode(TravelMode mode) {
		this.userCorrectedMode = mode;
		this.description = this.userCorrectedMode.toString();
	}

	/**
	 * Get predicted travel mode.
	 * 
	 * @return Predicted travel mode.
	 */
	public TravelMode getPredictedMode() {
		return this.mode;
	}

	/**
	 * Get trip id.
	 * 
	 * @return trip id this trip segment belongs to.
	 */
	public long getTripId() {
		return this.tripId;
	}

	/**
	 * Set trip id.
	 * 
	 * @param tripId
	 *            The trip id assigned to current segment.
	 */
	public void setTripId(long tripId) {
		this.tripId = tripId;
	}

	/**
	 * Set trip calendar id.
	 * 
	 * @param id
	 */
	public void setId(long id) {
		this.id = id;
		// this.isFinalized = true;
	}

	@Override
	public String getDescription() {
		if (isInProgress()) {
			return this.description + " in progress... ";
		}

		if (isFinalized()) {
			return this.description + " (" + tripId + ")";
		}

		return description;
	}

	/**
	 * Get trip segment from trip.
	 * 
	 * @param start
	 *            The start time of segment.
	 * @param end
	 *            The end time of segment.
	 * @param mode
	 *            The travel mode assigned to trip segment.
	 * @return Return the trip calendar item representing trip segment.
	 */
	public TripCalendarItem getTripSegment(Date start, Date end, TravelMode mode) {
		TripCalendarItem tripSegment = new TripCalendarItem(start, end, mode,
				null);
		tripSegment.mapDrawable = this.mapDrawable.getSegment(start, end);

		if (tripSegment.mapDrawable == null) {
			return null;
		}

		return tripSegment;
	}

	/**
	 * Connect trip calendar items together.
	 * 
	 * @param otherItem
	 */
	public void connect(TripCalendarItem otherItem) {
		if (this.mapDrawable.getPoints().size() > 0
				&& otherItem.mapDrawable.getPoints().size() > 0
				&& !this.mapDrawable.getPoints()
						.get(this.mapDrawable.getPoints().size() - 1)
						.equals(otherItem.mapDrawable.getPoints().get(0))) {
			this.mapDrawable.getPoints().add(
					otherItem.mapDrawable.getPoints().get(0));

			this.mapDrawable.refresh();
		}
	}

	/**
	 * Merge trip calendar item with other trip calendar item.
	 * 
	 * @param otherItem
	 * @param isPreviousItem
	 */
	public void merge(TripCalendarItem otherItem, boolean isPreviousItem) {
		if (this.mapDrawable.getPoints().size() > 0
				&& otherItem.mapDrawable.getPoints().size() > 0) {
			if (isPreviousItem) {
				this.mapDrawable.getPoints().addAll(0,
						otherItem.mapDrawable.getPoints());
			} else {
				this.mapDrawable.getPoints().addAll(
						otherItem.mapDrawable.getPoints());
			}
		}

		this.mapDrawable.refresh();
	}

	@Override
	public String getPolyCode() {
		return mapDrawable.getPolyCode();
	}

	@Override
	public LatLngBounds drawMap(GoogleMap map, boolean highlighted) {
		return mapDrawable.draw(map, highlighted);
	}

	@Override
	public CalendarItem addMarker(GoogleMap map, MarkerOptions marker) {
		if (this.mapDrawable.getMiddleLatLng() != null) {
			marker.position(this.mapDrawable.getMiddleLatLng());
			marker.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
			map.addMarker(marker);
		}

		return this;
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
	 * Connect trip route with activity weighted center.
	 * 
	 * @param activityPosition
	 * @param isPrev
	 */
	public void connectActivity(LatLng activityPosition, boolean isPrev) {
		if (activityPosition == null || Double.isNaN(activityPosition.latitude)
				|| Double.isNaN(activityPosition.longitude)) {
			return;
		}

		if (isPrev) {
			this.mapDrawable.setPrevActivityPosition(activityPosition);
		} else {
			this.mapDrawable.setNextActivityPosition(activityPosition);
		}
	}

	@Override
	public void addLocations(List<LocationWrapper> locations,
			boolean insertToHead) {
		Log.i(TAG, "add " + locations.size() + " to " + this.toString());
		List<LocationWrapper> validLocations = new ArrayList<LocationWrapper>();
		for (LocationWrapper location : locations) {
			validLocations.add(location);
		}

		if (insertToHead) {
			for (int i = validLocations.size() - 1; i >= 0; i--) {
				LatLng point = new LatLng(validLocations.get(i).getLocation()
						.getLatitude(), validLocations.get(i).getLocation()
						.getLongitude());

				this.mapDrawable.getPoints().add(0, point);
			}
		} else {
			for (int i = 0; i < validLocations.size(); i++) {
				LatLng point = new LatLng(validLocations.get(i).getLocation()
						.getLatitude(), validLocations.get(i).getLocation()
						.getLongitude());

				this.mapDrawable.getPoints().add(point);
			}
		}

		this.mapDrawable.refresh();
	}

	public void setMapDrawable(TripMapDrawable mapDrawable) {
		this.mapDrawable = mapDrawable;
	}

	@Override
	public void removeLocations(List<LocationWrapper> locations,
			boolean removeFromHead) {
		Log.i(TAG, "remove " + locations.size() + " from " + this.toString());

		int count = locations.size();

		while (count > 0 && this.mapDrawable.getPoints().size() > 0) {
			if (removeFromHead) {
				this.mapDrawable.getPoints().remove(0);
			} else {
				this.mapDrawable.getPoints().remove(
						this.mapDrawable.getPoints().size() - 1);
			}

			count--;
		}

		this.mapDrawable.refresh();
	}

	@Override
	public List<LocationWrapper> getLocations() {
		return this.mapDrawable.getLocations();
	}

	@Override
	public <T extends ClusterItem> void setClusterManager(
			ClusterManager<T> clusterManager) {
		// TODO Auto-generated method stub

	}
}
