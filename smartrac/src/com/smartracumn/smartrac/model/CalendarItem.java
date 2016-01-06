package com.smartracumn.smartrac.model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.util.SparseArray;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

/**
 * Calendar item is the place holder for smartrac item.
 * 
 * @author kangx385
 * 
 */
public abstract class CalendarItem implements Comparable<CalendarItem> {
	public static final int UNMODIFIED = 0;
	public static final int SHRINK = 1;
	public static final int EXPAND = 2;

	/**
	 * The enum representing item type.
	 * 
	 * @author kangx385
	 * 
	 */
	public enum Type {
		UNKNOWN_TYPE(0), TRIP(1), ACTIVITY(2), SERVICE_Off(3);

		private static SparseArray<Type> map;

		private static SparseArray<String> stringMap;

		static {
			map = new SparseArray<Type>();
			map.put(0, UNKNOWN_TYPE);
			map.put(1, TRIP);
			map.put(2, ACTIVITY);
			map.put(3, SERVICE_Off);
			stringMap = new SparseArray<String>();
			stringMap.put(0, "Calendar Item");
			stringMap.put(1, "Trip");
			stringMap.put(2, "Activity");
			stringMap.put(3, "SmarTrAC Off");
		}

		private final int value;

		private Type(int value) {
			this.value = value;
		}

		public String toString() {
			return stringMap.get(value);
		}

		/**
		 * Get an integer indicating id of Type.
		 * 
		 * @return The id of Type.
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Get item type by type id.
		 * 
		 * @param value
		 *            The type id.
		 * @return Item type corresponding to given id.
		 */
		public static Type get(int value) {
			return map.get(value);
		}
	}

	/**
	 * The enum representing travel mode.
	 * 
	 * @author kangx385
	 * 
	 */
	public enum TravelMode {
		UNKNOWN_TRAVEL_MODE(0), CAR(1), BUS(2), WALKING(3), BIKE(4), RAIL(5), WAIT(
				6);

		private static SparseArray<TravelMode> map;

		private static SparseArray<String> stringMap;

		static {
			map = new SparseArray<TravelMode>();
			stringMap = new SparseArray<String>();
			map.put(0, UNKNOWN_TRAVEL_MODE);
			map.put(1, CAR);
			map.put(2, BUS);
			map.put(3, WALKING);
			map.put(4, BIKE);
			map.put(5, RAIL);
			map.put(6, WAIT);
			stringMap.put(0, "Trip");
			stringMap.put(1, "Car");
			stringMap.put(2, "Bus");
			stringMap.put(3, "Walking");
			stringMap.put(4, "Bicycle");
			stringMap.put(5, "Rail");
			stringMap.put(6, "Wait");

		}

		private final int value;

		private TravelMode(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public String toString() {
			return stringMap.get(value);
		}

		public static TravelMode get(int value) {
			return map.get(value);
		}
	}

	/**
	 * The enum representing travel mode.
	 * 
	 * @author kangx385
	 * 
	 */
	public enum Activity {
		UNKNOWN_ACTIVITY(0), HOME(1), WORK(2), EDUCATION(3), SHOPPING(4), EAT_OUT(
				5), OTHER_PERSONAL_BUSINESS(6), SOCIAL_RECREATION_COMMUNITY(7);

		private static SparseArray<Activity> map;

		private static SparseArray<String> stringMap;

		static {
			map = new SparseArray<Activity>();
			stringMap = new SparseArray<String>();
			map.put(0, UNKNOWN_ACTIVITY);
			map.put(1, HOME);
			map.put(2, WORK);
			map.put(3, EDUCATION);
			map.put(4, SHOPPING);
			map.put(5, EAT_OUT);
			map.put(6, OTHER_PERSONAL_BUSINESS);
			map.put(7, SOCIAL_RECREATION_COMMUNITY);
			stringMap.put(0, "Activity");
			stringMap.put(1, "Home");
			stringMap.put(2, "Work");
			stringMap.put(3, "Education");
			stringMap.put(4, "Shopping");
			stringMap.put(5, "Eat out");
			stringMap.put(6, "Other personal business");
			stringMap.put(7, "Social, recreation, community");
		}

		private final int value;

		private Activity(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public String toString() {
			return stringMap.get(value);
		}

		public static Activity get(int value) {
			return map.get(value);
		}
	}

	/**
	 * The interface describing how to draw item on google map.
	 * 
	 * @author kangx385
	 * 
	 */
	public interface MapDrawable {
		LatLngBounds draw(GoogleMap map, boolean highlited);

		String getPolyCode();

		List<LocationWrapper> getLocations();

		void refresh();
	}

	protected long id;
	protected Date start;
	protected Date end;
	protected Date originalStart;
	protected Date originalEnd;
	protected Type type;
	protected String description;
	protected boolean isInProgress;
	protected boolean isModified;
	protected boolean isFinalized;
	protected boolean isConfirmed;

	/**
	 * Get a value indicating whether or not current item is newly added.
	 * 
	 * @return
	 */
	public boolean isAdded() {
		return this.id == 0;
	}

	/**
	 * Get a value indicating whether or not current item is modified.
	 * 
	 * @return
	 */
	public boolean isModified() {
		return isModified;
	}

	/**
	 * Set the value indicating whether or not current item is modified.
	 * 
	 * @param isModified
	 */
	public void setIsModified(boolean isModified) {
		this.isModified = isModified;
	}

	/**
	 * Gets a value indicating whether or not this calendar item is finalized.
	 * 
	 * @return True if it is finalized and already saved to the database.
	 */
	public boolean isFinalized() {
		return isFinalized;
	}

	/**
	 * Gets a value indicating whether or not this calendar item is saved.
	 * 
	 * @return True if it is already saved in the database.
	 */
	public boolean isSaved() {
		return this.id != 0;
	}

	/**
	 * Get calendar item id.
	 * 
	 * @return the calendar item id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set start time.
	 * 
	 * @param start
	 */
	public void setStart(Date start) {
		if (originalStart == null) {
			this.originalStart = this.start;
		}

		this.start = start;
	}

	/**
	 * Get start time.
	 * 
	 * @return Start time.
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Get the state of start time.
	 * 
	 * @return
	 */
	public int getStartState() {
		if (this.originalStart == null
				|| this.originalStart.getTime() == this.getStart().getTime()) {
			return UNMODIFIED;
		}

		return this.originalStart.getTime() < this.start.getTime() ? SHRINK
				: EXPAND;
	}

	/**
	 * Set end time.
	 * 
	 * @param end
	 */
	public void setEnd(Date end) {
		if (originalEnd == null) {
			this.originalEnd = this.end;
		}

		this.end = end;
	}

	/**
	 * Get end time.
	 * 
	 * @return End time.
	 */
	public Date getEnd() {
		return end;
	}

	/**
	 * Get the state of end time.
	 * 
	 * @return
	 */
	public int getEndState() {
		if (this.originalEnd == null
				|| this.originalEnd.getTime() == this.getEnd().getTime()) {
			return UNMODIFIED;
		}

		return this.originalEnd.getTime() < this.end.getTime() ? EXPAND
				: SHRINK;
	}

	/**
	 * Get the time span in milliseconds for current calendar item.
	 * 
	 * @return
	 */
	public long getTimeSpanInMillis() {
		return this.end.getTime() - this.start.getTime();
	}

	/**
	 * Get item type.
	 * 
	 * @return Item type.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set the value indicating whether or not this calendar item is currently
	 * in progress.
	 * 
	 * @param value
	 */
	public void setIsInProgress(boolean value) {
		this.isInProgress = value;
	}

	/**
	 * Get a value indicating whether or not this calendar item is currently in
	 * progress.
	 * 
	 * @return True if current item is in progress.
	 */
	public boolean isInProgress() {
		return this.isInProgress;
	}

	/**
	 * Reset calendar item be setting original start and end to null.
	 */
	public void reset() {
		this.originalEnd = null;
		this.originalStart = null;
	}

	/**
	 * Get a value indicating whether or not this item span across date.
	 * 
	 * @return
	 */
	public boolean isCrossDay() {
		Calendar start = Calendar.getInstance();
		start.setTime(this.start);
		Calendar end = Calendar.getInstance();
		end.setTime(this.end);

		return start.get(Calendar.DATE) != end.get(Calendar.DATE);
	}

	/**
	 * Get description.
	 * 
	 * @return Item description.
	 */
	public abstract String getDescription();

	/**
	 * Get encoded polygon string.
	 * 
	 * @return The encoded polygon string.
	 */
	public abstract String getPolyCode();

	/**
	 * Draw calendar item on google map.
	 * 
	 * @param map
	 *            The google map instance.
	 * @return Latitude/Longitude bounds for calendar item on google map.
	 */
	public abstract LatLngBounds drawMap(GoogleMap map, boolean highlighted);

	/**
	 * Add marker to calendar item map view.
	 * 
	 * @param map
	 * @param marker
	 */
	public abstract CalendarItem addMarker(GoogleMap map, MarkerOptions marker);

	/**
	 * Determine whether or not given point is contained by calendar item.
	 * 
	 * @param point
	 * @return
	 */
	public abstract boolean contains(LatLng point);

	/**
	 * Add locations to calendar item.
	 * 
	 * @param locations
	 * @param insertToHead
	 */
	public abstract void addLocations(List<LocationWrapper> locations,
			boolean insertToHead);

	/**
	 * Remove locations from calendar item.
	 * 
	 * @param locations
	 * @param removeFromHead
	 */
	public abstract void removeLocations(List<LocationWrapper> locations,
			boolean removeFromHead);

	/**
	 * Get locations contained in calendar item if if has any.
	 * 
	 * @return Location wrappers if current calendar item. Location wrapper data
	 *         will be lost when calendar item is saved and polyline code
	 *         generated.
	 */
	public abstract List<LocationWrapper> getLocations();

	public abstract <T extends ClusterItem> void setClusterManager(
			ClusterManager<T> clusterManager);

	@Override
	public int compareTo(CalendarItem another) {
		return this.getStart().compareTo(another.getStart());
	}

	public void setconfirmed(boolean b) {
		isConfirmed = b;
	}

	public boolean getConfirmed() {
		return isConfirmed;
	}
}
