package com.smartracumn.smartrac;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * The restart phone fragment.
 * 
 * @author kangx385
 * 
 */
public class RestartFragment extends Fragment {
	private final String TAG = getClass().getName();

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
		return inflater.inflate(R.layout.restart, container, false);
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
		getView().findViewById(R.id.restart_ok).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Save user dwelling locations, start location service
						// and go to smartrac activity.
						setPrefs(true);
						((SearchMapActivity) getActivity()).saveLocations();
						((SearchMapActivity) getActivity())
								.startLocationService();
						((SearchMapActivity) getActivity())
								.goToSmartracActivity();
					}
				});

		getView().findViewById(R.id.restart_later).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						setPrefs(false);
					}
				});
	}

	private void setPrefs(boolean value) {
		SharedPreferences prefs = getActivity().getSharedPreferences(
				getResources().getString(R.string.app_domain),
				Context.MODE_PRIVATE);
		prefs.edit()
				.putBoolean(
						getResources().getString(
								R.string.smartrac_service_switch), value)
				.commit();
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
