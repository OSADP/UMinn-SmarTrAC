package com.smartracumn.smartrac.model;

import java.text.ParseException;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * The class representing dwelling indicator.
 * 
 * @author kangx385
 * 
 */
public class DwellingIndicator implements Parcelable,
		Comparable<DwellingIndicator> {
	private long id;
	private Date time;
	private boolean dwelling;
	private Date adjustment;

	/**
	 * Initializes a new instance of the DwellingIndicator class.
	 * 
	 * @param id
	 * @param time
	 * @param dwelling
	 */
	public DwellingIndicator(long id, Date time, boolean dwelling) {
		this.id = id;
		this.time = time;
		this.dwelling = dwelling;
	}

	/**
	 * Initializes a new instance of the dwellingIndicator class by given
	 * parcel.
	 * 
	 * @param in
	 *            in parcel.
	 */
	public DwellingIndicator(Parcel in) {
		this.id = in.readLong();
		this.time = null;
		try {
			this.time = SmartracDataFormat.getIso8601Format().parse(
					in.readString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.dwelling = in.readInt() > 0;
		String adjustmentString = in.readString();
		if (adjustmentString != null) {
			try {
				this.adjustment = SmartracDataFormat.getIso8601Format().parse(
						adjustmentString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initializes a new instance of the DwellingIndicator class with
	 * adjustment.
	 * 
	 * @param id
	 * @param time
	 * @param dwelling
	 * @param adjustment
	 */
	public DwellingIndicator(long id, Date time, boolean dwelling,
			Date adjustment) {
		this(id, time, dwelling);
		this.adjustment = adjustment;
	}

	/**
	 * Get time.
	 * 
	 * @return The time stamp.
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * Check if current indicator indicating dwelling region.
	 * 
	 * @return True if it is dwelling.
	 */
	public boolean isDwelling() {
		return dwelling;
	}

	/**
	 * Set adjustment time.
	 * 
	 * @param adjustment
	 *            The adjustment.
	 */
	public void setAdjustment(Date adjustment) {
		this.adjustment = adjustment;
	}

	/**
	 * Get the adjustment time.
	 * 
	 * @return The adjustment time.
	 */
	public Date getAdjustment() {
		return adjustment;
	}

	/**
	 * Get id.
	 * 
	 * @return The id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Check if current dwelling indicator has adjustment.
	 * 
	 * @return True if adjustment exists.
	 */
	public boolean hasAdjustment() {
		return this.adjustment != null;
	}

	/**
	 * Get the actual change time.
	 * 
	 * @return The adjustment time if it exists.
	 */
	public Date getAdjustedTime() {
		return this.adjustment == null ? this.time : this.adjustment;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(SmartracDataFormat.getIso8601Format().format(time));
		dest.writeInt(dwelling ? 1 : 0);
		dest.writeString(hasAdjustment() ? SmartracDataFormat
				.getIso8601Format().format(time) : null);
	}

	/**
	 * The dwelling indicator parcel creator.
	 */
	public static final Parcelable.Creator<DwellingIndicator> CREATOR = new Parcelable.Creator<DwellingIndicator>() {
		public DwellingIndicator createFromParcel(Parcel in) {
			return new DwellingIndicator(in);
		}

		public DwellingIndicator[] newArray(int size) {
			return new DwellingIndicator[size];
		}
	};

	@Override
	public int compareTo(DwellingIndicator another) {
		return this.getTime().compareTo(another.getTime());
	}
}
