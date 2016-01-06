package com.smartracumn.smartrac;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smartracumn.smartrac.data.DwellingLocationDataSource;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.DwellingLocation;
import com.smartracumn.smartrac.service.SmartracService;

/**
 * The main content of search map view and job prompt.
 * 
 * @author kangx385
 * 
 */
public class SearchMapActivity extends FragmentActivity implements
		GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener,
		GoogleMap.OnMapLongClickListener {

	private final String TAG = getClass().getName();

	public static final String HOMELOCATION = "HOME";

	public static final String WORKPLACE = "WORK";

	private Location currentLocation;

	private GoogleMap mMap;

	private SupportMapFragment mapFrag;

	private SearchHeadFragment headFrag;

	private JobPromptFragment jobFrag;

	private RestartFragment restartFrag;

	private Button showCurrentLocation;

	private DwellingLocationDataSource dwellingLocationDataSource;

	private String locationTag = HOMELOCATION;

	private ProgressDialog locatingDialog;

	private boolean showCurr;

	private List<DwellingLocation> dwellingLocations;

	private Handler timerHandler = new Handler();

	/**
	 * The timer used to check if map is initialized in map fragment.
	 */
	private Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			setUpMapIfNeeded();
			timerHandler.postDelayed(this, 500);
		}
	};

	/**
	 * Get the location string indicating whether it is setting home location or
	 * work location.
	 * 
	 * @return The location tag.
	 */
	public String getLocationTag() {
		return locationTag;
	}

	/**
	 * Set location tag.
	 * 
	 * @param s
	 *            The location string.
	 */
	public void setLocationTag(String s) {
		locationTag = s;
	}

	/**
	 * Set up map if the mMap field is not already instantiated.
	 */
	private void setUpMapIfNeeded() {
		if (mMap != null && mMap.equals(mapFrag.getMap())) {
			return;
		}

		if (mapFrag.getMap() != null) {
			mMap = mapFrag.getMap();
		}

		if (mMap != null && mMap.equals(mapFrag.getMap())) {
			configureMap();
			timerHandler.removeCallbacks(timerRunnable);
			showCurr();
		}
	}

	/**
	 * Show current location on the map.
	 */
	private void showCurr() {
		if (showCurr && currentLocation != null) {
			MarkerOptions mOption = new MarkerOptions();
			LatLng p = new LatLng(currentLocation.getLatitude(),
					currentLocation.getLongitude());
			mOption.position(p);
			mOption.title("Current Location");
			getMap().addMarker(mOption);
			getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(p, 12),
					2000, null);
		}
	}

	/**
	 * Get google map instance.
	 * 
	 * @return Google map instance if it is instantiated, null otherwise.
	 */
	protected GoogleMap getMap() {
		setUpMapIfNeeded();
		return mMap;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_map);

		mapFrag = SupportMapFragment.newInstance();

		headFrag = new SearchHeadFragment();

		jobFrag = new JobPromptFragment();

		restartFrag = new RestartFragment();

		dwellingLocations = new ArrayList<DwellingLocation>();

		dwellingLocationDataSource = new DwellingLocationDataSource(this);

		showCurrentLocation = (Button) findViewById(R.id.current_location);

		showCurrentLocation.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				showCurrentLocation();
			}
		});

		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit)
				.replace(R.id.header, headFrag, "search head fragment")
				.commit();

		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit)
				.replace(R.id.main, mapFrag, "map fragment").commit();

		showCurrentLocation.setVisibility(View.VISIBLE);
	}

	/**
	 * Start location service.
	 */
	public void startLocationService() {
		Log.i(TAG, getClass().getSimpleName() + ": startLocationService()");
		Intent service = new Intent(this, SmartracService.class);
		this.startService(service);
	}

	/**
	 * Stop location service.
	 */
	public void stopLocationService() {
		Log.i(TAG, getClass().getSimpleName() + ": stopLocationService()");
		this.stopService(new Intent(this, SmartracService.class));
	}

	protected void saveLocations() {
		new SaveDwellingLocationsTask().execute(dwellingLocations);
	}

	protected void goToSmartracActivity() {
		Intent intent = new Intent(this, SmartracActivity.class);

		startActivity(intent);
	}

	@Override
	public void onStop() {
		if (locationManager != null && locationListener != null) {
			locationManager.removeUpdates(locationListener);
		}

		super.onStop();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	/**
	 * Set Listeners for map.
	 */
	private void configureMap() {
		getMap().setOnMarkerClickListener(this);
		getMap().setOnMarkerDragListener(this);
		getMap().setOnMapLongClickListener(this);
	}

	@Override
	protected void onResume() {
		setUpMapIfNeeded();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_search_map, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_current_location:
			showCurrentLocation();
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	private LocationManager locationManager;

	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network
			// location provider.
			currentLocation = location;
			showCurr = true;
			updateCurrentLocation();
			locationManager.removeUpdates(this);
			locatingDialog.dismiss();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

	/**
	 * Get location manager and fetch current location in order to show on the
	 * map.
	 */
	private void showCurrentLocation() {
		currentLocation = null;
		showCurr = false;
		headFrag.setSearch("");

		// Acquire a reference to the system Location Manager
		if (locationManager == null) {
			locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
		}

		// Register the listener with the Location Manager to receive
		// location updates
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, locationListener);

		locatingDialog = ProgressDialog.show(this, "", "Locating...");

		showCurrentLocation.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (currentLocation == null) {
					locatingDialog.dismiss();
					locationManager.removeUpdates(locationListener);
					Toast.makeText(
							SearchMapActivity.this,
							getResources().getString(
									R.string.no_current_location),
							Toast.LENGTH_LONG).show();
				}
			}

		}, 8000);
	}

	/**
	 * Show new location on google map.
	 * 
	 * @param location
	 *            The location needs tag and zoom.
	 */
	private void updateCurrentLocation() {
		if (currentLocation != null) {
			getMap().clear();
			MarkerOptions mOption = new MarkerOptions();
			LatLng p = new LatLng(currentLocation.getLatitude(),
					currentLocation.getLongitude());
			mOption.position(p);
			mOption.title("Current Location");
			getMap().addMarker(mOption);
			getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(p, 12),
					2000, null);
		}
	}

	/**
	 * Show location on google map for the place with information contained in
	 * cursor.
	 * 
	 * @param c
	 *            The cursor object returned from place provider which contains
	 *            latitude and longitude for location.
	 */
	public void showLocations(Cursor c) {
		MarkerOptions markerOptions = new MarkerOptions();
		LatLng position = null;
		getMap().clear();
		while (c.moveToNext()) {
			markerOptions = new MarkerOptions();
			position = new LatLng(Double.parseDouble(c.getString(1)),
					Double.parseDouble(c.getString(2)));
			markerOptions.position(position);
			markerOptions.title(c.getString(0));
			getMap().addMarker(markerOptions);
		}

		c.close();

		if (position != null) {
			getMap().animateCamera(
					CameraUpdateFactory.newLatLngZoom(position, 12), 2000, null);
		}
	}

	/**
	 * Go to restart page.
	 */
	public void gotoRestart() {
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit)
				.remove(headFrag).commit();

		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit)
				.replace(R.id.main, restartFrag, "restart fragment").commit();

		showCurrentLocation.setVisibility(View.GONE);
	}

	/**
	 * Go to job prompt page.
	 */
	public void gotoJobPrompt() {
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit)
				.remove(headFrag).commit();

		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit)
				.replace(R.id.main, jobFrag, "job fragment").commit();

		showCurrentLocation.setVisibility(View.GONE);
	}

	/**
	 * Go to search map view.
	 */
	public void gotoSearch() {
		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit)
				.replace(R.id.header, headFrag, "search head fragment")
				.commit();

		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.enter, R.anim.exit)
				.replace(R.id.main, mapFrag, "map fragment").commit();

		showCurrentLocation.setVisibility(View.VISIBLE);

		showCurr = currentLocation != null;
		timerHandler.postDelayed(timerRunnable, 0);
	}

	@Override
	public boolean onMarkerClick(final Marker marker) {
		// TODO Auto-generated method stub
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder
				.setTitle("Confirm " + getLocationTag() + " Location");

		// set dialog message
		alertDialogBuilder
				.setMessage(
						"Are you sure you wanna set \"" + marker.getTitle()
								+ "\" as your " + getLocationTag()
								+ " location?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								CalendarItem.Activity activity = getLocationTag()
										.equals(HOMELOCATION) ? CalendarItem.Activity.HOME
										: CalendarItem.Activity.WORK;

								DwellingLocation dummy = new DwellingLocation(
										marker.getPosition(), activity);

								List<DwellingLocation> outDatedLocs = new ArrayList<DwellingLocation>();

								for (DwellingLocation location : dwellingLocations) {
									if (location.getActivity() == activity) {
										outDatedLocs.add(location);
									}
								}

								for (DwellingLocation location : outDatedLocs) {
									dwellingLocations.remove(location);
								}

								dwellingLocations.add(dummy);

								if (!locationTag.equals(WORKPLACE)) {
									gotoJobPrompt();
								} else {
									gotoRestart();
								}
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

		return true;
	}

	@Override
	public void onMarkerDrag(Marker arg0) {
		// TODO Auto-generated method stub
		// pin = arg0.getPosition();
		// showLatLng();
	}

	@Override
	public void onMarkerDragEnd(Marker arg0) {
		// TODO Auto-generated method stub
		// getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(pin, 12),
		// 2000, null);
	}

	@Override
	public void onMarkerDragStart(Marker arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		getMap().clear();
		MarkerOptions mOption = new MarkerOptions().draggable(true);
		mOption.position(arg0);
		mOption.title("Pin");
		getMap().addMarker(mOption);
	}

	private class SaveDwellingLocationsTask extends
			AsyncTask<List<DwellingLocation>, Void, Void> {

		@Override
		protected Void doInBackground(List<DwellingLocation>... params) {
			// Get list of calendar items using SmartracCoreDataProcessor.
			dwellingLocationDataSource.insert(params[0]);
			return null;
		}
	}
}
