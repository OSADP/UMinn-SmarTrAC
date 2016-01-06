package com.smartracumn.smartrac.model;

import java.util.Date;

import android.location.Location;

/**
 * Location wrapper used to wrap location and capture the time wrapper is
 * created.
 * 
 * @author kangx385
 * 
 */
public class LocationWrapper implements Comparable<LocationWrapper> {
	private long id;
	private Date time;
	private Location location;

	/**
	 * Instantiate a new instance of location wrapper.
	 * 
	 * @param location
	 *            The location.
	 */
	public LocationWrapper(Location location) {
		this.time = new Date();
		this.location = location;
	}

	/**
	 * Initializes a new instance of the LocationWrapper class.
	 * 
	 * @param id
	 * @param location
	 */
	public LocationWrapper(long id, Location location) {
		this.id = id;
		this.location = location;
		this.time = new Date(location.getTime());
	}

	/**
	 * Get location contained.
	 * 
	 * @return
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Get the id.
	 * 
	 * @return
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the time stamp.
	 * 
	 * @param time
	 */
	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * Gets time stamp.
	 * 
	 * @return
	 */
	public Date getTime() {
		return time;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof LocationWrapper) {
			return ((LocationWrapper) other).getTime().getTime() == getTime()
					.getTime();
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.getTime().hashCode();
	}

	@Override
	public int compareTo(LocationWrapper another) {
		return this.getTime().compareTo(another.getTime());
	}
}
