package com.smartracumn.smartrac.service;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.smartracumn.smartrac.model.LocationWrapper;

/**
 * The class represents SmartracLocationListener which listen to location
 * changes and notify observers.
 * 
 * @author kangx385
 * 
 */
public class LocationListener implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {
	private final String TAG = getClass().getName();

	LocationRequest mLocationRequest;

	LocationClient mLocationClient;

	private final int GPS_SAMPLING_RATE = 1000;

	private List<LocationWrapper> locationUpdates = new ArrayList<LocationWrapper>();

	/**
	 * Initializes a new instance of the SmartracLocationListener class.
	 * 
	 * @param context
	 * @param databaseWorker
	 */
	public LocationListener(Context context) {
		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(GPS_SAMPLING_RATE);
		mLocationRequest.setFastestInterval(GPS_SAMPLING_RATE);

		mLocationClient = new LocationClient(context, this, this);
	}

	/**
	 * Get sampled locations, write cached locations to database and then clear
	 * cached location.
	 * 
	 * @return A copy of cached locations.
	 */
	public List<LocationWrapper> getLocations() {
		List<LocationWrapper> locs = new ArrayList<LocationWrapper>(
				locationUpdates);

		locationUpdates.clear();

		return locs;
	}

	/**
	 * Start location data sampling at rate of 1Hz.
	 * 
	 * @return True is start success, false otherwise.
	 */
	public boolean start() {
		if (mLocationClient.isConnected()) {
			return false;
		}

		mLocationClient.connect();
		return true;
	}

	/**
	 * Stop smartrac location listener.
	 */
	public boolean stop() {
		if (mLocationClient.isConnected()) {
			/*
			 * Remove location updates for a listener. The current Activity is
			 * the listener, so the argument is "this".
			 */
			mLocationClient.removeLocationUpdates(this);
		} else {
			return false;
		}
		/*
		 * After disconnect() is called, the client is considered "dead".
		 */
		mLocationClient.disconnect();

		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		locationUpdates.add(new LocationWrapper(location));
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		this.locationUpdates.clear();
	}
}
