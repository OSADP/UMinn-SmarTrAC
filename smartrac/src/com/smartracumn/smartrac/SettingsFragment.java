package com.smartracumn.smartrac;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.smartracumn.smartrac.util.SmartracDataFormat;

public class SettingsFragment extends Fragment {
	private final String TAG = getClass().getName();

	private ToggleButton toggleButton;

	private Button scheduleStart;

	private Button scheduleStop;

	private Button updateSchedule;

	private CheckBox enableSchedule;

	private boolean serviceOn;

	@Override
	public void onAttach(Activity activity) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onAttach()");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onCreate()");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onCreateView()");
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.settings_fragment, container, false);

		return v;
	}

	/**
	 * Attach call back methods for controls.
	 */
	private void init() {
		serviceOn = getServicePrefs();

		toggleButton = (ToggleButton) getView().findViewById(
				R.id.smartrac_service_switch);

		toggleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean on = ((ToggleButton) v).isChecked();

				if (on != serviceOn) {
					setServicePrefs(on);
				}
			}
		});

		toggleButton.setChecked(serviceOn);

		scheduleStart = (Button) getView().findViewById(R.id.service_start);
		scheduleStop = (Button) getView().findViewById(R.id.service_stop);
		enableSchedule = (CheckBox) getView()
				.findViewById(R.id.enable_schedule);
		updateSchedule = (Button) getView().findViewById(R.id.update_schedule);

		boolean enable = getActivity().getSharedPreferences(
				getResources().getString(R.string.app_domain),
				Context.MODE_PRIVATE).getBoolean(
				getResources().getString(
						R.string.smartrac_service_schedule_enabled), false);

		enableSchedule.setChecked(enable);

		String start = getActivity().getSharedPreferences(
				getResources().getString(R.string.app_domain),
				Context.MODE_PRIVATE).getString(
				getResources().getString(
						R.string.smartrac_service_start_time_of_day),
				"00:00 AM");

		scheduleStart.setText(start);

		String stop = getActivity().getSharedPreferences(
				getResources().getString(R.string.app_domain),
				Context.MODE_PRIVATE)
				.getString(
						getResources().getString(
								R.string.smartrac_service_stop_time_of_day),
						"00:00 AM");

		scheduleStop.setText(stop);

		scheduleStart.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				TimePickerDialog picker = new TimePickerDialog(getActivity(),
						new OnTimeSetListener() {

							@Override
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								Calendar c = Calendar.getInstance();
								c.set(Calendar.HOUR_OF_DAY, hourOfDay);
								c.set(Calendar.MINUTE, minute);

								scheduleStart.setText(SmartracDataFormat
										.getTimeFormat().format(c.getTime()));
							}
						}, 8, 0, false);
				picker.show();
			}
		});

		scheduleStop.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				TimePickerDialog picker = new TimePickerDialog(getActivity(),
						new OnTimeSetListener() {

							@Override
							public void onTimeSet(TimePicker view,
									int hourOfDay, int minute) {
								Calendar c = Calendar.getInstance();
								c.set(Calendar.HOUR_OF_DAY, hourOfDay);
								c.set(Calendar.MINUTE, minute);

								scheduleStop.setText(SmartracDataFormat
										.getTimeFormat().format(c.getTime()));
							}
						}, 20, 0, false);
				picker.show();
			}
		});

		updateSchedule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = !enableSchedule.isChecked() ? "Disable scheduled run?"
						: "Schedule smartrac running from "
								+ scheduleStart.getText() + " to "
								+ scheduleStop.getText() + " everyday?";

				new AlertDialog.Builder(getActivity())
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("Comfirm schedule")
						.setMessage(message)
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										setSchedulePrefs(enableSchedule
												.isChecked(), scheduleStart
												.getText().toString(),
												scheduleStop.getText()
														.toString());
									}

								})
						.setNegativeButton(R.string.no,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										init();
									}
								}).show();

			}
		});
	}

	private void setSchedulePrefs(boolean enableSchedule,
			String schedule_start, String schedule_stop) {
		getActivity()
				.getSharedPreferences(
						getResources().getString(R.string.app_domain),
						Context.MODE_PRIVATE)
				.edit()
				.putBoolean(
						getResources().getString(
								R.string.smartrac_service_schedule_enabled),
						enableSchedule).commit();

		getActivity()
				.getSharedPreferences(
						getResources().getString(R.string.app_domain),
						Context.MODE_PRIVATE)
				.edit()
				.putString(
						getResources().getString(
								R.string.smartrac_service_start_time_of_day),
						schedule_start).commit();

		getActivity()
				.getSharedPreferences(
						getResources().getString(R.string.app_domain),
						Context.MODE_PRIVATE)
				.edit()
				.putString(
						getResources().getString(
								R.string.smartrac_service_stop_time_of_day),
						schedule_stop).commit();

		Intent intent = new Intent(getActivity().getResources().getString(
				R.string.data_service_reschedule_broadcast));

		getActivity().sendBroadcast(intent);
	}

	private void setServicePrefs(boolean value) {
		serviceOn = value;

		getActivity()
				.getSharedPreferences(
						getResources().getString(R.string.app_domain),
						Context.MODE_PRIVATE)
				.edit()
				.putBoolean(
						getResources().getString(
								R.string.smartrac_service_switch), value)
				.commit();

		Intent intent = new Intent(getActivity().getResources().getString(
				R.string.data_service_on_off_broadcast));
		intent.putExtra(
				getActivity().getResources().getString(
						R.string.data_service_state), value);

		getActivity().sendBroadcast(intent);
	}

	private boolean getServicePrefs() {
		return getActivity().getSharedPreferences(
				getResources().getString(R.string.app_domain),
				Context.MODE_PRIVATE).getBoolean(
				getResources().getString(R.string.smartrac_service_switch),
				true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onActivityCreated()");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onStart()");
		super.onStart();
		init();
	}

	@Override
	public void onResume() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onResume()");
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onPause()");
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onStop()");
		super.onStop();
	}

	@Override
	public void onDetach() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onDetach()");
		super.onDetach();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onDestroy()");
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onDestroyView()");
		super.onDestroyView();
	}
}
