package com.smartracumn.smartrac.model;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

/**
 * The object mapping for dwelling location.
 * 
 * @author kangx385
 * 
 */
public class DwellingLocation {
	private long id;
	private LatLng location;
	private Location loc;
	private CalendarItem.Activity activity;
	private int visitFreq;
	private String code;

	public DwellingLocation(DwellingLocation dwellingLocation) {
		this.id = dwellingLocation.id;
		this.loc = dwellingLocation.loc;
		this.location = dwellingLocation.location;
		this.activity = dwellingLocation.activity;
		this.visitFreq = dwellingLocation.visitFreq;
		this.code = dwellingLocation.code;
	}

	/**
	 * Initializes a new instance of the DwellingLocation class.
	 * 
	 * @param id
	 * @param location
	 * @param activity
	 */
	public DwellingLocation(long id, LatLng location,
			CalendarItem.Activity activity) {
		this.id = id;
		this.location = location;
		this.activity = activity;
	}

	/**
	 * Initializes a new instance of the DwellingLocation class with id = 0.
	 * 
	 * @param location
	 * @param activity
	 */
	public DwellingLocation(LatLng location, CalendarItem.Activity activity) {
		this(0, location, activity);
	}

	/**
	 * Get id.
	 * 
	 * @return
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Get LatLng.
	 * 
	 * @return
	 */
	public LatLng getLatLng() {
		return this.location;
	}

	/**
	 * Get location.
	 * 
	 * @return
	 */
	public Location getLocation() {
		if (this.loc == null) {
			this.loc = new Location(getClass().getSimpleName());
			this.loc.setLatitude(this.location.latitude);
			this.loc.setLongitude(this.location.longitude);
		}

		return this.loc;
	}

	/**
	 * Get most likely activity.
	 * 
	 * @return
	 */
	public CalendarItem.Activity getActivity() {
		return this.activity;
	}

	/**
	 * Set the visit frequency.
	 * 
	 * @param visitFrequency
	 */
	public void setVisitFrequency(int visitFrequency) {
		this.visitFreq = visitFrequency;
	}

	/**
	 * Get the visit frequency.
	 * 
	 * @return
	 */
	public int getVisitFrequency() {
		return this.visitFreq;
	}

	/**
	 * Get the distance between this dwelling location and other location.
	 * 
	 * @param other
	 * @return
	 */
	public double distanceTo(Location other) {
		return this.getLocation().distanceTo(other);
	}

	/**
	 * Get poly code representing location.
	 * 
	 * @return
	 */
	public String getCode() {
		if (this.code == null) {
			List<LatLng> locs = new ArrayList<LatLng>();
			locs.add(this.location);
			this.code = PolyUtil.encode(locs);
		}

		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
