package com.smartracumn.smartrac.service;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;

import com.smartracumn.smartrac.R;
import com.smartracumn.smartrac.model.DwellingIndicator;
import com.smartracumn.smartrac.model.ModeIndicator;
import com.smartracumn.smartrac.service.SmartracDataService.State;
import com.smartracumn.smartrac.util.SmartracDataFormat;

public class DataServiceBroadcastManager {
	private final Context context;

	public DataServiceBroadcastManager(Context context) {
		this.context = context;
	}

	public void activityChanged(DwellingIndicator di) {
		Intent intent = new Intent(context.getResources().getString(
				R.string.dwelling_changed_broadcast));
		intent.putExtra(
				context.getResources().getString(R.string.dwelling_indicator),
				di);
		// intent.putExtra(
		// context.getResources()
		// .getString(R.string.dwelling_change_point),
		// findLocation(lastChange.getAdjustedTime()));
		context.sendBroadcast(intent);
	}

	public void modeChanged(ModeIndicator mi) {
		Intent intent = new Intent(context.getResources().getString(
				R.string.mode_changed_broadcast));
		intent.putExtra(context.getResources().getString(R.string.time_tag),
				SmartracDataFormat.getDateTimeFormat().format(mi.getTime()));
		context.sendBroadcast(intent);
	}

	public void inaccurateGPS(Date time) {
		Intent intent = new Intent(context.getResources().getString(
				R.string.location_not_available_broadcast));
		intent.putExtra(
				context.getResources().getString(
						R.string.location_not_available_time),
				SmartracDataFormat.getDateTimeFormat().format(time));
		context.sendBroadcast(intent);
	}

	public void phoneMovement(boolean moving) {
		if (moving) {
			Intent intent = new Intent(context.getResources().getString(
					R.string.phone_pick_up_broadcast));
			context.sendBroadcast(intent);

		} else {
			Intent intent = new Intent(context.getResources().getString(
					R.string.phone_put_broadcast));
			context.sendBroadcast(intent);
		}
	}

	public void dataServiceStart(boolean recording) {
		if (recording) {
			Intent intent = new Intent(context.getResources().getString(
					R.string.service_started_broadcast));

			intent.putExtra(
					context.getResources().getString(
							R.string.service_start_time),
					SmartracDataFormat.getDateTimeFormat().format(
							Calendar.getInstance().getTime()));

			context.sendBroadcast(intent);
		} else {

		}
	}

	public void dataServiceState(State state) {
		Intent intent = new Intent(context.getResources().getString(
				R.string.data_service_state_change_broadcast));

		intent.putExtra(
				context.getResources().getString(R.string.data_service_state),
				state.name());

		context.sendBroadcast(intent);
	}
}
