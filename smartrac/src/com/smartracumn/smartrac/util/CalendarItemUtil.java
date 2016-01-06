package com.smartracumn.smartrac.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.data.CalendarItemDataSource;
import com.smartracumn.smartrac.data.DwellingSummaryDataSource;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.LocationWrapper;

/**
 * Take care of real time saving of calendar items based on mode and dwelling
 * indicator feeds as well as providing calendar item processing method.
 * 
 * @author kangx385
 * 
 */
public class CalendarItemUtil {
	private final Context context;

	private final String TAG = getClass().getName();
	private final CalendarItemDataSource calendarItemDataSource;
	private final DwellingSummaryDataSource dwellingSummaryDataSource;
	private final ActivityDetector activityDetector;

	/**
	 * Get the instance of dwelling location manager.
	 * 
	 * @return
	 */
	public ActivityDetector getActivityDetector() {
		return this.activityDetector;
	}

	boolean locationNotAvailableReceived = false;

	/**
	 * The class used to draw trips on map.
	 * 
	 * @author kangx385
	 * 
	 */
	public static class TripMapDrawable implements CalendarItem.MapDrawable {
		private List<LocationWrapper> locations;
		private List<LatLng> points;
		private String polyCode;

		private double maxLat = -90;
		private double minLat = 90;
		private double maxLong = -180;
		private double minLong = 180;

		private LatLng northEast;
		private LatLng southWest;

		private LatLng prevActivityPosition;
		private LatLng nextActivityPosition;

		/**
		 * Set previous activity position.
		 * 
		 * @param position
		 */
		public void setPrevActivityPosition(LatLng position) {
			this.prevActivityPosition = position;
		}

		/**
		 * Set next activity position.
		 * 
		 * @param position
		 */
		public void setNextActivityPosition(LatLng position) {
			this.nextActivityPosition = position;
		}

		public TripMapDrawable(String polyCode) {
			this.polyCode = polyCode;
			points = new ArrayList<LatLng>();

			// Do nothing if given polyCode is not valid.
			if (polyCode != null) {
				for (LatLng point : PolyUtil.decode(polyCode)) {
					double latitude = point.latitude;
					double longitude = point.longitude;

					maxLat = Math.max(latitude, maxLat);
					minLat = Math.min(latitude, minLat);
					maxLong = Math.max(longitude, maxLong);
					minLong = Math.min(longitude, minLong);

					points.add(point);
				}

				northEast = new LatLng(maxLat, maxLong);
				southWest = new LatLng(minLat, minLong);
			}
		}

		public TripMapDrawable(List<LocationWrapper> locations) {
			Collections.sort(locations, new Comparator<LocationWrapper>() {

				@Override
				public int compare(LocationWrapper lhs, LocationWrapper rhs) {
					return lhs.getTime().compareTo(rhs.getTime());
				}
			});

			this.locations = locations;
			points = new ArrayList<LatLng>();
			for (LocationWrapper location : locations) {
				double latitude = location.getLocation().getLatitude();
				double longitude = location.getLocation().getLongitude();

				maxLat = Math.max(latitude, maxLat);
				minLat = Math.min(latitude, minLat);
				maxLong = Math.max(longitude, maxLong);
				minLong = Math.min(longitude, minLong);

				points.add(new LatLng(latitude, longitude));
			}

			northEast = new LatLng(maxLat, maxLong);
			southWest = new LatLng(minLat, minLong);
		}

		/**
		 * Get middle point's latitude and longitude.
		 * 
		 * @return middle point's latitude and longitude.
		 */
		public LatLng getMiddleLatLng() {
			if (this.points.size() > 0) {
				return this.points.get(this.points.size() / 2);
			}

			return null;
		}

		/**
		 * Get points from trip map drawable.
		 * 
		 * @return List of LatLng.
		 */
		public List<LatLng> getPoints() {
			return this.points;
		}

		/**
		 * Get a value indicating whether or not trip map drawable contains
		 * given point.
		 * 
		 * @param point
		 * @return True if trip map drawable contains given point.
		 */
		public boolean contains(LatLng point) {
			return PolyUtil.containsLocation(point, this.points, false);
		}

		@Override
		public LatLngBounds draw(GoogleMap map, boolean highlighted) {
			// Draw trip on google map.
			if (maxLat < minLat || maxLong < minLong) {
				return null;
			}

			int backColor = Color.parseColor("#CF505050");

			int color = highlighted ? Color.parseColor("#FF99FF99") : Color
					.parseColor("#FFB0B0B0");

			float backZ = !highlighted ? 0f : 0.2f;

			float topZ = !highlighted ? 0.1f : 0.3f;

			PolylineOptions polyLine = new PolylineOptions();

			if (prevActivityPosition != null) {
				polyLine.add(prevActivityPosition);
			}

			polyLine.addAll(points);

			if (nextActivityPosition != null) {
				polyLine.add(nextActivityPosition);
			}

			polyLine.width(27);
			polyLine.color(backColor);
			polyLine.zIndex(backZ);
			polyLine.geodesic(true);
			map.addPolyline(polyLine);
			polyLine.color(color);
			polyLine.width(17);
			polyLine.zIndex(topZ);
			map.addPolyline(polyLine);

			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			builder.include(northEast);
			builder.include(southWest);

			// Test mode detection
			// SpeedFeatureBuffer sfb = new SpeedFeatureBuffer(8);
			//
			// List<LocationWrapper> temp = new ArrayList<LocationWrapper>();
			// for (LocationWrapper loc : locations) {
			// temp.add(loc);
			// if (temp.size() == 30) {
			// sfb.enqueue(temp);
			// temp = new ArrayList<LocationWrapper>();
			// Log.i(TAG, "current speed feature buffer: " + sfb);
			// }
			// }
			// Test mode detection

			return builder.build();
		}

		@Override
		public void refresh() {
			List<LatLng> polyLine = new ArrayList<LatLng>(points);
			polyCode = PolyUtil.encode(polyLine);

			for (LatLng point : points) {
				maxLat = Math.max(point.latitude, maxLat);
				minLat = Math.min(point.latitude, minLat);
				maxLong = Math.max(point.longitude, maxLong);
				minLong = Math.min(point.longitude, minLong);
			}

			northEast = new LatLng(maxLat, maxLong);
			southWest = new LatLng(minLat, minLong);
		}

		@Override
		public String getPolyCode() {
			if (polyCode == null) {
				refresh();
			}

			return polyCode;
		}

		public TripMapDrawable getSegment(Date start, Date end) {
			List<LocationWrapper> segmentLocations = new ArrayList<LocationWrapper>();
			for (LocationWrapper location : locations) {
				if (location.getTime().getTime() >= start.getTime()
						&& location.getTime().getTime() < end.getTime()) {
					segmentLocations.add(location);
				}
			}

			if (segmentLocations.size() == 0) {
				return null;
			}

			TripMapDrawable tripSegment = new TripMapDrawable(segmentLocations);

			return tripSegment;
		}

		@Override
		public List<LocationWrapper> getLocations() {
			return locations;
		}
	}

	/**
	 * The class used to draw activities on google map.
	 * 
	 * @author kangx385
	 * 
	 */
	public static class ActivityMapDrawable implements CalendarItem.MapDrawable {
		private String polyCode;
		private List<LocationWrapper> locations;

		private double maxLat = -90;
		private double minLat = 90;
		private double maxLong = -180;
		private double minLong = 180;

		private LatLng weightedCenter;
		private LatLng northEast;
		private LatLng southWest;

		private Location neLoc;
		private Location swLoc;

		public ActivityMapDrawable(String polyCode) {
			this.polyCode = polyCode;

			// Do nothing if given polyCode is invalid.
			if (polyCode != null) {
				List<LatLng> centerAndBounds = PolyUtil.decode(polyCode);

				if (centerAndBounds.size() == 3) {
					this.weightedCenter = centerAndBounds.get(0);
					this.northEast = centerAndBounds.get(1);
					this.southWest = centerAndBounds.get(2);

					neLoc = new Location(this.getClass().getSimpleName());
					neLoc.setLatitude(northEast.latitude);
					neLoc.setLongitude(northEast.longitude);
					swLoc = new Location(this.getClass().getSimpleName());
					swLoc.setLatitude(southWest.latitude);
					swLoc.setLongitude(southWest.longitude);
				}
			}
		}

		public ActivityMapDrawable(List<LocationWrapper> locations) {
			this.locations = locations;
			updateBound();
		}

		/**
		 * Set locations belong to map drawable.
		 * 
		 * @param locations
		 */
		public void setLocations(List<LocationWrapper> locations) {
			this.locations = locations;
			updateBound();
		}

		/**
		 * Add locations to map drawable.
		 * 
		 * @param locations
		 */
		public void addLocations(List<LocationWrapper> locations) {
			if (this.locations == null) {
				return;
			}

			this.locations.addAll(locations);
			updateBound();
		}

		/**
		 * Remove locations from map drawable.
		 * 
		 * @param locations
		 */
		public void removeLocations(List<LocationWrapper> locations) {
			if (this.locations == null || this.locations.size() <= 2) {
				return;
			}

			this.locations.removeAll(locations);
			updateBound();
		}

		private void updateBound() {
			double sumLat = 0, sumLng = 0;

			long countTime = 0;
			for (int i = 0; i < locations.size(); i++) {
				LocationWrapper location = locations.get(i);
				double latitude = location.getLocation().getLatitude();
				double longitude = location.getLocation().getLongitude();

				maxLat = Math.max(latitude, maxLat);
				minLat = Math.min(latitude, minLat);
				maxLong = Math.max(longitude, maxLong);
				minLong = Math.min(longitude, minLong);

				long timeGap = 1000;

				if (i < locations.size() - 1) {
					LocationWrapper next = locations.get(i + 1);
					long temp = next.getTime().getTime()
							- location.getTime().getTime();
					timeGap = temp > 1000 ? temp : 1000;
				}

				long step = timeGap / 1000;

				sumLat += latitude * step;
				sumLng += longitude * step;

				countTime += step;
			}

			weightedCenter = new LatLng(sumLat / countTime, sumLng / countTime);
			northEast = new LatLng(maxLat, maxLong);
			southWest = new LatLng(minLat, minLong);

			neLoc = new Location(this.getClass().getSimpleName());
			neLoc.setLatitude(northEast.latitude);
			neLoc.setLongitude(northEast.longitude);
			swLoc = new Location(this.getClass().getSimpleName());
			swLoc.setLatitude(southWest.latitude);
			swLoc.setLongitude(southWest.longitude);

			refresh();
		}

		/**
		 * Update boundary based on current and given activity map drawable.
		 * 
		 * @param otherDrawable
		 */
		public void updateBound(ActivityMapDrawable otherDrawable) {
			this.northEast = new LatLng(Math.max(northEast.latitude,
					otherDrawable.northEast.latitude), Math.max(
					northEast.longitude, otherDrawable.northEast.longitude));
			this.southWest = new LatLng(Math.min(southWest.latitude,
					otherDrawable.southWest.latitude), Math.min(
					southWest.longitude, otherDrawable.southWest.longitude));
			this.neLoc.setLatitude(northEast.latitude);
			this.neLoc.setLongitude(northEast.longitude);
			this.swLoc.setLatitude(southWest.latitude);
			this.swLoc.setLongitude(southWest.longitude);

			this.weightedCenter = new LatLng(
					(weightedCenter.latitude + otherDrawable.weightedCenter.latitude) / 2,
					(weightedCenter.longitude + otherDrawable.weightedCenter.longitude) / 2);

			refresh();
		}

		/**
		 * Get the latitude longitude of weighted center.
		 * 
		 * @return
		 */
		public LatLng getCenterLatLng() {
			return this.weightedCenter;
		}

		/**
		 * Check if map polygon contains given point.
		 * 
		 * @param point
		 * @return
		 */
		public boolean contains(LatLng point) {
			Location centerLocation = new Location(getClass().getName());
			centerLocation
					.setLatitude((northEast.latitude + southWest.latitude) / 2);
			centerLocation
					.setLongitude((northEast.longitude + southWest.longitude) / 2);

			Location pointLocation = new Location(getClass().getName());
			pointLocation.setLatitude(point.latitude);
			pointLocation.setLongitude(point.longitude);

			return centerLocation.distanceTo(pointLocation) <= neLoc
					.distanceTo(swLoc) / 2;
		}

		@Override
		public LatLngBounds draw(GoogleMap map, boolean highlighted) {
			// Draw activity on google map.
			int backColor = Color.parseColor("#CF505050");

			int color = highlighted ? Color.parseColor("#7F99FF99") : Color
					.parseColor("#7FA0A0A0");

			map.addCircle(
					new CircleOptions()
							.center(new LatLng(
									(northEast.latitude + southWest.latitude) / 2,
									(northEast.longitude + southWest.longitude) / 2))
							.radius(swLoc.distanceTo(neLoc) / 2).strokeWidth(5)
							.fillColor(color)).setStrokeColor(backColor);

			return getBounds();
		}

		public LatLngBounds getBounds() {
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			builder.include(northEast);
			builder.include(southWest);

			return builder.build();
		}

		@Override
		public String getPolyCode() {
			if (polyCode == null) {
				refresh();
			}

			return polyCode;
		}

		@Override
		public void refresh() {
			List<LatLng> centerAndBounds = new ArrayList<LatLng>();
			centerAndBounds.add(weightedCenter);
			centerAndBounds.add(northEast);
			centerAndBounds.add(southWest);

			polyCode = PolyUtil.encode(centerAndBounds);
		}

		@Override
		public List<LocationWrapper> getLocations() {
			return locations;
		}
	}

	private final Handler handler;

	/**
	 * Initializes a new instance of the CalendarItemUtil class.
	 * 
	 * @param context
	 * @param databaseWorkerHandler
	 */
	public CalendarItemUtil(Context context, Handler databaseWorkerHandler) {
		this.context = context;
		this.handler = databaseWorkerHandler;
		this.calendarItemDataSource = new CalendarItemDataSource(this.context);
		this.dwellingSummaryDataSource = new DwellingSummaryDataSource(
				this.context);
		this.activityDetector = new ActivityDetector(this.context, this.handler);
	}

	/**
	 * Save calendar items real time.
	 * 
	 * @param calendarItems
	 *            Calendar items.
	 * @return True if saved successfully.
	 */
	public boolean saveCalendarItems(List<CalendarItem> calendarItems) {
		if (calendarItems == null || calendarItems.size() == 0) {
			return false;
		}

		List<CalendarItem> calendarItemsToBeSaved = new ArrayList<CalendarItem>();

		// List<DwellingLocation> dwellingLocations = new
		// ArrayList<DwellingLocation>();

		for (CalendarItem item : calendarItems) {
			if (item.isInProgress() || item.isFinalized())
				continue;
			calendarItemsToBeSaved.add(item);

			// if (item instanceof ActivityCalendarItem) {
			// ActivityCalendarItem activity = (ActivityCalendarItem) item;
			// if (activity.getActivity() !=
			// CalendarItem.Activity.UNKNOWN_ACTIVITY) {
			// dwellingLocations.add(new DwellingLocation(activity
			// .getWeightedCenter(), activity.getActivity()));
			// }
			// }
		}

		boolean insertSuccess = calendarItemDataSource
				.insert(calendarItemsToBeSaved);
		boolean summaryUpdated = dwellingSummaryDataSource
				.createDwellingSummaries(calendarItemsToBeSaved);

		if (summaryUpdated) {
			sendDwellingSummaryUpdatedBroadcast();
		}

		return insertSuccess;
	}

	private void sendDwellingSummaryUpdatedBroadcast() {
		Log.i(TAG, "Broadcast dwelling summary updated");
		context.sendBroadcast(new Intent(context.getResources().getString(
				R.string.dwelling_summary_updated_broadcast)));
	}
}
