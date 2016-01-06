package com.smartracumn.smartrac.model;

import java.util.Date;

/**
 * The type that maps modes data to id, time and travel mode.
 * 
 * @author kangx385
 * 
 */
public class ModeIndicator implements Comparable<ModeIndicator> {
	private long id;
	private Date time;
	private CalendarItem.TravelMode mode;

	public ModeIndicator(long id, Date time, CalendarItem.TravelMode mode) {
		this.id = id;
		this.time = time;
		this.mode = mode;
	}

	public ModeIndicator(Date time, CalendarItem.TravelMode mode) {
		this(0, time, mode);
	}

	public long getId() {
		return id;
	}

	public Date getTime() {
		return time;
	}

	public CalendarItem.TravelMode getMode() {
		return mode;
	}

	@Override
	public int compareTo(ModeIndicator another) {
		return this.getTime().compareTo(another.getTime());
	}
}
