package com.smartracumn.smartrac.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.model.MotionData;

public class SmartracDataFormat {
	private static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private static final SimpleDateFormat MY_FORMAT = new SimpleDateFormat(
			"yyyy/MM/dd, HH:mm:ss");

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd");

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(
			"hh:mm a");

	private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(
			" MM/dd hh:mm a");

	private final String SPLITER = ", ";

	private final String FILE_SPLITER = "&";

	public static SimpleDateFormat getSimpleDateTimeFormat() {
		return DATE_TIME_FORMAT;
	}

	public static SimpleDateFormat getTimeFormat() {
		return TIME_FORMAT;
	}

	public static SimpleDateFormat getIso8601Format() {
		return ISO8601FORMAT;
	}

	public static SimpleDateFormat getDateTimeFormat() {
		return MY_FORMAT;
	}

	public static SimpleDateFormat getDateFormat() {
		return DATE_FORMAT;
	}

	public String getModeHeader() {
		return "Date, Time, Mode" + SPLITER + "Note";
	}

	public String getFileName(String imei, Date time, String fileName) {
		return imei + FILE_SPLITER + DATE_FORMAT.format(time) + FILE_SPLITER
				+ fileName;
	}

	public String formatModeAndNote(Date time, String mode, String note) {
		StringBuilder data = new StringBuilder();
		data.append(MY_FORMAT.format(time));
		data.append(SPLITER);
		data.append(mode);
		if (note != null && !note.equals("")) {
			data.append(SPLITER);
			data.append(note);
		}

		return data.toString();
	}

	public String formatLocation(LocationWrapper loc) {
		StringBuilder data = new StringBuilder();
		data.append(MY_FORMAT.format(loc.getTime()));
		data.append(SPLITER);
		data.append(loc.getLocation().getLatitude());
		data.append(SPLITER);
		data.append(loc.getLocation().getLongitude());
		data.append(SPLITER);
		data.append(loc.getLocation().getSpeed());
		data.append(SPLITER);
		data.append(loc.getLocation().getProvider());
		data.append(SPLITER);
		data.append(loc.getLocation().getAccuracy());
		data.append(SPLITER);
		data.append(loc.getLocation().getAltitude());
		data.append(SPLITER);
		data.append(loc.getLocation().getBearing());

		return data.toString();
	}

	public String getGpsHeader() {
		StringBuilder data = new StringBuilder();
		data.append("Date, Time");
		data.append(SPLITER);
		data.append("Latitude");
		data.append(SPLITER);
		data.append("Longitude");
		data.append(SPLITER);
		data.append("Speed");
		data.append(SPLITER);
		data.append("Provider");
		data.append(SPLITER);
		data.append("Accuracy");
		data.append(SPLITER);
		data.append("Altitude");
		data.append(SPLITER);
		data.append("Bearing");

		return data.toString();
	}

	public String formatMotionData(MotionData motionData) {
		StringBuilder data = new StringBuilder();

		data.append(MY_FORMAT.format(motionData.getTime()));
		data.append(SPLITER);
		data.append(motionData.getLinearX());
		data.append(SPLITER);
		data.append(motionData.getLinearY());
		data.append(SPLITER);
		data.append(motionData.getLinearZ());
		data.append(SPLITER);
		data.append(motionData.getLinearMag());
		data.append(SPLITER);
		data.append(motionData.getTrueX());
		data.append(SPLITER);
		data.append(motionData.getTrueY());
		data.append(SPLITER);
		data.append(motionData.getTrueZ());
		data.append(SPLITER);
		data.append(motionData.getTrueMag());

		return data.toString();
	}

	public String getAccHeader() {
		StringBuilder data = new StringBuilder();

		data.append("Date, Time");
		data.append(SPLITER);
		data.append("LinearX");
		data.append(SPLITER);
		data.append("LinearY");
		data.append(SPLITER);
		data.append("LinearZ");
		data.append(SPLITER);
		data.append("LinearMagnitude");
		data.append(SPLITER);
		data.append("TrueX");
		data.append(SPLITER);
		data.append("TrueY");
		data.append(SPLITER);
		data.append("TrueZ");
		data.append(SPLITER);
		data.append("TrueMagnitude");

		return data.toString();
	}
}
