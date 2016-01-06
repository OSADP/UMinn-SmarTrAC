package com.smartracumn.smartrac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterInfoWindowClickListener;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemInfoWindowClickListener;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.smartracumn.smartrac.CalendarListFragment.CalendarItemParentView;
import com.smartracumn.smartrac.ConfirmScreenFragment.onCofirmButtonClicked;
import com.smartracumn.smartrac.EditMapFragment.CalendarItemSplitter;
import com.smartracumn.smartrac.EditMapFragment.OnCalendarItemModifiedListener;
import com.smartracumn.smartrac.data.CalendarItemDataSource;
import com.smartracumn.smartrac.data.SmartracSQLiteHelper;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.service.CalendarItemUtility.CalendarItemService;
import com.smartracumn.smartrac.service.SmartracService;
import com.smartracumn.smartrac.service.SmartracService.SmartracBinder;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * The class representing smartrac main access activity.
 * 
 * @author kangx385
 * 
 */
public class SmartracActivity extends FragmentActivity implements
		onCofirmButtonClicked {
	private final String TAG = getClass().getName();

	private final String MAP_VIEW = "map_view";

	private final String CALENDAR_VIEW = "calendar_view";

	private final String SETTINGS = "settings";

	private final String EDIT_VIEW = "edit_view";

	private final int DAILY_DETAIL_MENU = R.id.daily_detail_menu_group;

	private final int MAIN_MENU = R.id.main_menu_group;

	private CalendarListFragment calendarListFrag = new CalendarListFragment();

	private SupportMapFragment mapFrag = SupportMapFragment.newInstance();

	private EditMapFragment editMapFrag = new EditMapFragment();

	private SettingsFragment settingsFrag = new SettingsFragment();

	private boolean editting = false;

	private GoogleMap map;

	private String[] mNavigationTitles;

	private DrawerLayout mDrawerLayout;

	private ListView mDrawerList;

	private ActionBarDrawerToggle mDrawerToggle;

	private int selectedDrawerPosition = 0;

	private ClusterManager<ActivityCalendarItem> clusterManager;

	private CalendarItemParentView selectedParentView;

	private CalendarItem selectedItem;

	private Date selectedDate;

	private IconGenerator iconGenerator;

	private List<CalendarItem> selectedCalendarItems;

	private AlertDialog alertDialog;

	private CalendarItemDataSource calendarItemDataSource;

	private CalendarItemService calendarItemService;

	boolean isBound = false;

	private ProgressDialog bindingDialog;

	private ServiceConnection myConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			SmartracBinder binder = (SmartracBinder) service;
			calendarItemService = binder.getService();
			isBound = true;

			if (bindingDialog != null) {
				bindingDialog.dismiss();
				bindingDialog = null;
			}

			if (selectedDate == null) {
				initializeSelectedDate();
			}

			setSelectedDate(selectedDate);
		}

		public void onServiceDisconnected(ComponentName arg0) {
			isBound = false;
			calendarItemService = null;
		}
	};

	private Map<String, CalendarItem> markerItemMap = new HashMap<String, CalendarItem>();

	private Map<CalendarItem.Activity, Integer> calendarItemPinIconMap;

	private void initializePinIconMap() {
		calendarItemPinIconMap = new HashMap<CalendarItem.Activity, Integer>();
		calendarItemPinIconMap.put(CalendarItem.Activity.UNKNOWN_ACTIVITY,
				R.drawable.map_unknown);
		calendarItemPinIconMap.put(CalendarItem.Activity.EAT_OUT,
				R.drawable.map_eat);
		calendarItemPinIconMap.put(CalendarItem.Activity.HOME,
				R.drawable.map_home);
		calendarItemPinIconMap.put(CalendarItem.Activity.EDUCATION,
				R.drawable.map_education);
		calendarItemPinIconMap.put(
				CalendarItem.Activity.SOCIAL_RECREATION_COMMUNITY,
				R.drawable.map_src);
		calendarItemPinIconMap.put(CalendarItem.Activity.SHOPPING,
				R.drawable.map_shop);
		calendarItemPinIconMap.put(
				CalendarItem.Activity.OTHER_PERSONAL_BUSINESS,
				R.drawable.map_opb);
		calendarItemPinIconMap.put(CalendarItem.Activity.WORK,
				R.drawable.map_work);
	}

	/**
	 * Set selected date and load calendar items according to selected date.
	 * 
	 * @param date
	 *            The date.
	 */
	public void setSelectedDate(Date date) {
		this.selectedDate = date;
		try {
			new GetCalendarItemTask().execute()
					.get(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Date getSelectedDate() {
		return selectedDate;
	}

	private Menu menu;

	private List<CalendarItem> currentItems = new ArrayList<CalendarItem>();

	private Handler timerHandler = new Handler();

	private Marker marker;

	private OnCalendarItemModifiedListener onItemModifiedListener = new OnCalendarItemModifiedListener() {

		@Override
		public void onCalendarItemModified(CalendarItem item,
				CalendarItemSplitter splitter, CalendarItemParentView parentView) {
			if (splitter != null) {
				new PostSplitterProcessingTask().execute(splitter);
			} else if (item != null) {
				new PostProcessingTask().execute(item);
			} else if (parentView != null) {
				new PostProcessingParentViewTask().execute(parentView);
			}
		}
	};

	private OnMapClickListener onMapClickListener = new OnMapClickListener() {

		@Override
		public void onMapClick(LatLng point) {
			selectedCalendarItems = new ArrayList<CalendarItem>();

			for (CalendarItem item : currentItems) {
				if (!(item instanceof ActivityCalendarItem)
						&& item.contains(point)) {
					selectedCalendarItems.add(item);
				}
			}

			if (marker != null) {
				marker.remove();
			}

			if (selectedCalendarItems.size() > 0) {
				MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.icon(BitmapDescriptorFactory
						.fromBitmap(iconGenerator
								.makeIcon(makeList(selectedCalendarItems))));
				markerOptions.position(point);
				markerOptions.anchor(iconGenerator.getAnchorU(),
						iconGenerator.getAnchorV());
				marker = map.addMarker(markerOptions);
				// selectedItem = selectedItems.get(0);
				// enterEditMode();
			}
		}
	};

	private void removeTripsMarker() {
		if (marker != null) {
			marker.remove();
		}

		selectedCalendarItems = null;
	}

	private void setupClusterManager() {
		this.clusterManager = new ClusterManager<ActivityCalendarItem>(this,
				map);
		this.clusterManager.setRenderer(new ActivityCalendarItemRenderer());

		this.clusterManager
				.setOnClusterInfoWindowClickListener(new OnClusterInfoWindowClickListener<ActivityCalendarItem>() {

					@Override
					public void onClusterInfoWindowClick(
							Cluster<ActivityCalendarItem> cluster) {
						confirmSelection(new ArrayList<CalendarItem>(cluster
								.getItems()));
					}
				});
		this.clusterManager
				.setOnClusterItemInfoWindowClickListener(new OnClusterItemInfoWindowClickListener<ActivityCalendarItem>() {

					@Override
					public void onClusterItemInfoWindowClick(
							ActivityCalendarItem item) {
						selectedItem = item;
						enterEditMode();
					}
				});
	}

	private String makeList(List<CalendarItem> items) {
		StringBuilder sb = new StringBuilder();

		for (CalendarItem item : items) {
			sb.append(item.toString());
			sb.append("\n");
		}

		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * The timer used to check if map is initialized in map fragment.
	 */
	Runnable drawItemRunnable = new Runnable() {

		@Override
		public void run() {
			if (mapFrag.getMap() == null) {
				timerHandler.postDelayed(this, 500);
			} else {
				map = mapFrag.getMap();
				setupClusterManager();
				map.setOnCameraChangeListener(clusterManager);
				map.setOnInfoWindowClickListener(clusterManager);
				map.getUiSettings().setZoomControlsEnabled(false);
				timerHandler.removeCallbacks(this);

				map.clear();
				markerItemMap.clear();
				for (CalendarItem item : currentItems) {
					item.setClusterManager(clusterManager);
					if (item != selectedItem) {
						item.drawMap(map, false);

						// MarkerOptions marker = new MarkerOptions();
						// marker.title(item.toString());
						// markerItemMap.put(marker.getTitle(),
						// item.addMarker(mapFrag.getMap(), marker));
					}

					markerItemMap.put(item.toString(), item);
				}

				attachemapClickHandler();
				attachMarkerClickHandler();

				LatLngBounds bounds = selectedItem.drawMap(map, true);

				if (bounds != null) {
					map.animateCamera(CameraUpdateFactory.newLatLngBounds(
							bounds, 800, 800, 50), 1000, null);
				}
			}
		}
	};

	Runnable drawItemsRunnable = new Runnable() {
		@Override
		public void run() {
			if (mapFrag.getMap() == null) {
				timerHandler.postDelayed(this, 500);
			} else {
				map = mapFrag.getMap();
				setupClusterManager();
				map.setOnCameraChangeListener(clusterManager);
				map.setOnInfoWindowClickListener(clusterManager);
				map.getUiSettings().setZoomControlsEnabled(true);
				timerHandler.removeCallbacks(this);
				refreshMapView();
			}
		}
	};

	Runnable drawTripRunnable = new Runnable() {
		@Override
		public void run() {
			if (mapFrag.getMap() == null) {
				timerHandler.postDelayed(this, 500);
			} else {
				map = mapFrag.getMap();
				setupClusterManager();
				map.setOnCameraChangeListener(clusterManager);
				map.setOnInfoWindowClickListener(clusterManager);
				map.getUiSettings().setZoomControlsEnabled(true);
				timerHandler.removeCallbacks(this);
				drawTrip();
			}
		}
	};

	/**
	 * ClusterRenderer used to render activity calendar item and cluster.
	 * 
	 * @author Jie
	 * 
	 */
	private class ActivityCalendarItemRenderer extends
			DefaultClusterRenderer<ActivityCalendarItem> {
		public ActivityCalendarItemRenderer() {
			super(getApplicationContext(), map, clusterManager);
		}

		@Override
		protected void onBeforeClusterItemRendered(
				ActivityCalendarItem activity, MarkerOptions markerOptions) {
			// mImageView.setImageResource(person.profilePhoto);
			// Bitmap icon = mIconGenerator.makeIcon();
			BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(
					calendarItemPinIconMap.get(activity.getActivity()));
			Bitmap b = bd.getBitmap();
			Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 2,
					b.getHeight() / 2, false);
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bhalfsize));

			markerOptions.title(activity.toString());
		}

		@Override
		protected void onBeforeClusterRendered(
				Cluster<ActivityCalendarItem> cluster,
				MarkerOptions markerOptions) {
			super.onBeforeClusterRendered(cluster, markerOptions);

			markerOptions.title(getResources()
					.getString(R.string.cluster_title));
		}

		@Override
		protected boolean shouldRenderAsCluster(
				Cluster<ActivityCalendarItem> cluster) {
			// Always render clusters.
			return cluster.getSize() > 1;
		}
	}

	private void attachMarkerClickHandler() {
		map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker.getTitle() != null) {
					return false;
				} else if (selectedCalendarItems != null) {
					int selectedItemCount = selectedCalendarItems.size();
					if (selectedItemCount == 1) {
						selectedItem = selectedCalendarItems.get(0);
						enterEditMode();
					} else if (selectedItemCount > 1) {
						confirmSelection(selectedCalendarItems);
					}
				}

				return true;
			}
		});
	}

	private void attachemapClickHandler() {
		map.setOnMapClickListener(this.onMapClickListener);
	}

	private void drawTrip() {
		List<LatLngBounds> bounds = new ArrayList<LatLngBounds>();
		map.clear();
		markerItemMap.clear();
		for (CalendarItem item : currentItems) {
			item.setClusterManager(clusterManager);
			LatLngBounds bound = null;

			if (selectedParentView.getTrip().contains(item)) {
				bound = item.drawMap(map, true);
			} else {
				item.drawMap(map, false);
			}

			if (bound != null) {
				bounds.add(bound);
			}

			markerItemMap.put(item.toString(), item);
		}

		attachemapClickHandler();
		attachMarkerClickHandler();

		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		for (LatLngBounds bound : bounds) {
			builder.include(bound.northeast);
			builder.include(bound.southwest);
		}

		if (bounds.size() > 0) {
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 800, 800, 50), 1000, null);
		}
	}

	private void refreshMapView() {
		List<LatLngBounds> bounds = new ArrayList<LatLngBounds>();
		map.clear();
		markerItemMap.clear();
		for (CalendarItem item : currentItems) {
			item.setClusterManager(clusterManager);
			LatLngBounds bound = item.drawMap(map, false);
			if (bound != null) {
				bounds.add(bound);
			}

			// MarkerOptions marker = new MarkerOptions();
			// marker.title(item.toString());
			// markerItemMap.put(marker.getTitle(),
			// item.addMarker(mapFrag.getMap(), marker));
			markerItemMap.put(item.toString(), item);
		}

		attachemapClickHandler();
		attachMarkerClickHandler();

		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		for (LatLngBounds bound : bounds) {
			builder.include(bound.northeast);
			builder.include(bound.southwest);
		}

		if (bounds.size() > 0) {
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(
					builder.build(), 900, 900, 50), 1000, null);
		}
	}

	private CalendarItemLoadedListener currentItemsLoadedListener = new CalendarItemLoadedListener() {

		@Override
		public void onCalendarItemLoaded(List<CalendarItem> items) {
			// Draw items on google map after data loaded.
			timerHandler.post(drawItemsRunnable);
		}
	};

	@Override
	public void onBackPressed() {
		if (!calendarListFrag.isVisible()) {
			mDrawerList.setItemChecked(0, true);
			gotoCalendarView();
			return;
		}

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private Set<CalendarItemLoadedListener> itemLoadedListeners = new HashSet<CalendarItemLoadedListener>();

	/**
	 * Listen to calendar item loaded event.
	 * 
	 * @author kangx385
	 * 
	 */
	public interface CalendarItemLoadedListener {
		void onCalendarItemLoaded(List<CalendarItem> items);
	}

	/**
	 * Set current calendar items and notify observers of calendar item changes.
	 * 
	 * @param items
	 *            Calendar items.
	 */
	private void setCurrentItems(List<CalendarItem> items) {
		currentItems = items;
		if (this.menu != null) {
			MenuItem dateButton = this.menu.findItem(R.id.action_set_date);
			dateButton.setTitle(SmartracDataFormat.getDateFormat().format(
					selectedDate));
		}

		notifyCalendarItemLoaded(items);
		if (currentItems.size() == 0) {
			Toast.makeText(SmartracActivity.this, "No Calendar Item...",
					Toast.LENGTH_LONG).show();
		}
	}

	private void notifyCalendarItemLoaded(List<CalendarItem> items) {
		for (CalendarItemLoadedListener listener : itemLoadedListeners) {
			listener.onCalendarItemLoaded(items);
		}

		if (editMapFrag.isVisible()) {
			if (selectedItem != null) {
				setSelectedItem(selectedItem);
			}
		}
	}

	/**
	 * Get current calendar items.
	 * 
	 * @return List of calendar items needs to be shown in fragment.
	 */
	public List<CalendarItem> getCurrentItems() {
		return currentItems;
	}

	/**
	 * Register calendar item loaded call back method.
	 * 
	 * @param listener
	 *            The calendar item loaded listener.
	 */
	public void registerItemLoadedListener(CalendarItemLoadedListener listener) {
		itemLoadedListeners.add(listener);
	}

	/**
	 * Unregister calendar item loaded call back method.
	 * 
	 * @param listener
	 *            The calendar item loaded listener.
	 */
	public void unregisterItemLoadedListener(CalendarItemLoadedListener listener) {
		itemLoadedListeners.remove(listener);
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
				long id) {
			selectDrawerItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectDrawerItem(int position) {
		// Create a new fragment and specify the planet to show based on
		// position
		selectedDrawerPosition = position;
		mDrawerLayout.closeDrawers();

		switch (position) {
		case 0:
			gotoCalendarView();
			break;
		case 1:
			Intent summary = new Intent(SmartracActivity.this,
					AggregatedSummary.class);
			startActivity(summary);
			break;
		case 2:
			gotoSettings();
			break;
		case 3:
			// Intent aws = new Intent(SmartracActivity.this, AWSCognito.class);
			// startActivity(aws);
			new UploadTask().execute();
			break;
		case 4:
			Intent bugReport = new Intent(SmartracActivity.this,
					BugReport.class);
			startActivity(bugReport);
			break;
		case 5:
			Intent about = new Intent(SmartracActivity.this, About.class);
			startActivity(about);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, getClass().getSimpleName() + ": onCreate");

		setContentView(R.layout.smartrac_activity);

		mNavigationTitles = getResources().getStringArray(R.array.navigation);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mNavigationTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerList.setItemChecked(selectedDrawerPosition, true);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(SmartracActivity.this,
				mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close);

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		new PrepareTask().execute();

		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.smartrac_activity_content, calendarListFrag,
						CALENDAR_VIEW)
				.setCustomAnimations(R.anim.enter, R.anim.exit).commit();

		iconGenerator = new IconGenerator(this);
		iconGenerator.setStyle(IconGenerator.STYLE_GREEN);

		editMapFrag
				.registerOnCalendarItemModifiedListener(onItemModifiedListener);

		initializePinIconMap();

		initializeSelectedDate();

		calendarItemDataSource = new CalendarItemDataSource(this);

	}

	private void gotoSettings() {
		if (editMapFrag.isVisible()) {
			exitEditMode();
		}

		showMenu(DAILY_DETAIL_MENU, false);
		showMenu(MAIN_MENU, false);

		if (!settingsFrag.isVisible()) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.smartrac_activity_content, settingsFrag,
							SETTINGS)
					.setCustomAnimations(R.anim.enter, R.anim.exit).commit();

			unregisterItemLoadedListener(currentItemsLoadedListener);
		}
	}

	private void gotoCalendarView() {
		if (editMapFrag.isVisible()) {
			exitEditMode();
		}

		showMenu(DAILY_DETAIL_MENU, true);
		showMenu(MAIN_MENU, true);

		if (!calendarListFrag.isVisible()) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.smartrac_activity_content, calendarListFrag,
							CALENDAR_VIEW)
					.setCustomAnimations(R.anim.enter, R.anim.exit).commit();

			unregisterItemLoadedListener(currentItemsLoadedListener);
		} else {
			notifyCalendarItemLoaded(currentItems);
		}
		invalidateOptionsMenu();

	}

	private void gotoMapView() {

		if (editMapFrag.isVisible()) {
			exitEditMode();
		}

		showMenu(DAILY_DETAIL_MENU, true);
		showMenu(MAIN_MENU, true);

		if (!mapFrag.isVisible() || editMapFrag.isVisible()) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.smartrac_activity_content, mapFrag, MAP_VIEW)
					.setCustomAnimations(R.anim.enter, R.anim.exit).commit();

			timerHandler.post(drawItemsRunnable);
			registerItemLoadedListener(currentItemsLoadedListener);
		}
		invalidateOptionsMenu();
	}

	private void promptDateSelect() {
		DatePickerFragment datePicker = new DatePickerFragment();

		if (selectedDate != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(selectedDate);
			Bundle args = new Bundle();
			args.putInt(DatePickerFragment.YEAR, c.get(Calendar.YEAR));
			args.putInt(DatePickerFragment.MONTH, c.get(Calendar.MONTH));
			args.putInt(DatePickerFragment.DAY, c.get(Calendar.DAY_OF_MONTH));
			datePicker.setArguments(args);
		}

		datePicker.show(SmartracActivity.this.getFragmentManager(),
				"datePicker");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_calendar, this.menu);
		boolean result = super.onCreateOptionsMenu(this.menu);

		// If the menu is created after selected date is set, initialize time.
		if (result && selectedDate != null) {
			MenuItem dateButton = this.menu.findItem(R.id.action_set_date);
			if (dateButton.getTitle().toString()
					.equals(getResources().getString(R.string.date))) {
				dateButton.setTitle(SmartracDataFormat.getDateFormat().format(
						selectedDate));
			}
		}

		if (mapFrag.isVisible()) {
			MenuItem calendar_item = menu.findItem(R.id.action_calendar);
			Drawable calIcon = getResources().getDrawable(
					R.drawable.action_calendar_list);
			calIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
			calendar_item.setIcon(calIcon);
		}

		// MenuItem screenShotButton =
		// this.menu.findItem(R.id.action_screenShot);
		// SharedPreferences sharedPref = this.getSharedPreferences(
		// "com.smartracumn.smartrac", Context.MODE_PRIVATE);
		// boolean bugMode = sharedPref.getBoolean("BugMode", false);
		// if (bugMode) {
		// screenShotButton.setVisible(true);
		// } else {
		// screenShotButton.setVisible(false);
		// }

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		// case R.id.action_finalize_current_items:
		// new SaveCalendarItemTask().execute(currentItems);
		// return true;
		// case R.id.action_delete_all_calendar_items:
		// new DeleteCalendarItemTask().execute();
		// return true;
		case R.id.action_reset_calendar_items_for_day:
			// If selected date is more than 14 days ahead of current time. Does
			// not allow clear changes.
			if (Calendar.getInstance().getTimeInMillis()
					- getSelectedDate().getTime() > 14 * 24 * 60 * 60 * 1000) {
				new AlertDialog.Builder(this)
						.setTitle("Clear Changes")
						.setMessage(
								"Not able to do clear changes for a day 14 days ago.")
						.setPositiveButton("OK", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.cancel();
							}
						}).setCancelable(false).show();
			} else {
				new ClearChangesTask().execute();
			}

			return true;
		case R.id.action_export_database:
			new ExportDatabaseFileTask().execute();
			return true;
			// case R.id.action_delete_all_raw_data:
			// new DeleteRawDataTask().execute();
			// return true;
		case R.id.action_calendar:
			gotoCalendarView();
			return true;
		case R.id.action_map:
			gotoMapView();
			return true;
		case R.id.action_refresh:
			setSelectedDate(selectedDate);
			return true;
		case R.id.action_set_date:
			promptDateSelect();
			return true;
			// case R.id.action_screenShot:
			// Bitmap bitmap = takeScreenshot();
			// if (bitmap != null) {
			// saveBitmap(bitmap);
			// }
			// return true;
			// case R.id.action_clear_changes:
			// setSelectedDate(selectedDate);
			// return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// private Bitmap takeScreenshot() {
	// View rootView;
	// if (editMapFrag.isVisible()) {
	// rootView = findViewById(R.id.RelativeLayout1);
	// } else {
	// rootView = findViewById(R.id.smartrac_activity_content);
	// }
	// rootView.setDrawingCacheEnabled(true);
	// return rootView.getDrawingCache();
	// }
	//
	// public void saveBitmap(Bitmap bitmap) {
	//
	// Calendar c = Calendar.getInstance();
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// String strDate = sdf.format(c.getTime());
	// File imagePath = new File(Environment.getExternalStorageDirectory()
	// + "/screenshot " + strDate + ".png");
	// FileOutputStream fos;
	// try {
	// fos = new FileOutputStream(imagePath);
	// if (bitmap.compress(CompressFormat.JPEG, 100, fos)) {
	// Toast.makeText(this, "Screenshot successful",
	// Toast.LENGTH_SHORT).show();
	// }
	// fos.flush();
	// fos.close();
	// } catch (FileNotFoundException e) {
	// Log.e("GREC", e.getMessage(), e);
	// } catch (IOException e) {
	// Log.e("GREC", e.getMessage(), e);
	// }
	// }

	private void showMenu(int menuGroup, boolean showMenu) {
		if (menu == null)
			return;

		menu.setGroupVisible(menuGroup, showMenu);
	}

	private void initializeSelectedDate() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.MINUTE, 0);

		selectedDate = c.getTime();
	}

	/**
	 * Get a value indicating whether or not current list contains unsaved
	 * changes.
	 * 
	 * @return
	 */
	public boolean containsUnsavedChanges() {
		if (getCurrentItems() == null) {
			return false;
		} else {
			for (CalendarItem item : getCurrentItems()) {
				if (item.isInProgress()) {
					continue;
				}

				if (item.isModified() || item.isAdded()) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public void onStop() {
		Log.i(TAG, ": onStop");
		super.onStop();
	}

	@Override
	public void onStart() {
		Log.i(TAG, ": onStart");

		super.onStart();
	}

	@Override
	public void onPause() {
		Log.i(TAG, ": onPause");

		if (!editting) {
			initializeSelectedDate();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.i(TAG, ": onResume");

		super.onResume();

		if (!isBound) {
			bindingDialog = new ProgressDialog(SmartracActivity.this);

			bindingDialog.setMessage("Aquiring Smartrac Service...");
			bindingDialog.show();
		} else if (!editting) {
			if (selectedDate == null) {
				initializeSelectedDate();
			}

			setSelectedDate(selectedDate);
		}
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, ": onDestroy");

		editMapFrag
				.unregisterOnCalendarItemModifiedListener(onItemModifiedListener);

		unbindService(myConnection);

		super.onDestroy();
	}

	/**
	 * Set selected calendar item.
	 * 
	 * @param item
	 *            The calendar item user want to interact with.
	 */
	public void setSelectedItem(CalendarItem item) {
		if (item == null) {
			return;
		}

		for (CalendarItem c : getCurrentItems()) {
			if (c.getId() == item.getId()) {
				selectedItem = c;
			}
		}

		invalidateOptionsMenu();
		enterEditMode();
	}

	/**
	 * Get selected calendar item.
	 * 
	 * @return
	 */
	public CalendarItem getSelectedItem() {
		return selectedItem;
	}

	public CalendarItemParentView getSelectedParentView() {
		return selectedParentView;
	}

	private void enterEditMode() {
		selectedParentView = null;
		drawItem(selectedItem);
		showMenu(DAILY_DETAIL_MENU, true);
		showMenu(MAIN_MENU, false);

		for (CalendarItem item : currentItems) {
			item.reset();
		}

		if (!editMapFrag.isVisible()) {
			getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(R.anim.enter_from_bottom,
							R.anim.exit_to_bottom)
					.replace(R.id.smartrac_activity_foot, editMapFrag,
							EDIT_VIEW).commit();
		} else {
			editMapFrag.setCalendarItem(selectedItem);
		}

		removeTripsMarker();
		unregisterItemLoadedListener(currentItemsLoadedListener);
		editting = true;
	}

	private void enterEditTripMode() {
		selectedItem = null;
		drawTripInSelectedParent(selectedParentView);

		showMenu(DAILY_DETAIL_MENU, true);
		showMenu(MAIN_MENU, false);

		for (CalendarItem item : currentItems) {
			item.reset();
		}

		if (!editMapFrag.isVisible()) {
			getSupportFragmentManager()
					.beginTransaction()
					.setCustomAnimations(R.anim.enter_from_bottom,
							R.anim.exit_to_bottom)
					.replace(R.id.smartrac_activity_foot, editMapFrag,
							EDIT_VIEW).commit();
		} else {
			editMapFrag.setCalendarItemParent(selectedParentView);
		}

		removeTripsMarker();
		unregisterItemLoadedListener(currentItemsLoadedListener);
		editting = true;
	}

	public void editWholeTrip(CalendarItemParentView parent) {
		this.selectedParentView = parent;
		invalidateOptionsMenu();
		enterEditTripMode();
	}

	private void exitEditMode() {
		getSupportFragmentManager().beginTransaction().remove(editMapFrag)
				.commit();
		editting = false;
	}

	/**
	 * Draw calendar item on google map.
	 * 
	 * @param item
	 *            Calendar item to be drawn.
	 */
	public void drawItem(CalendarItem item) {
		if (item != null) {
			if (!mapFrag.isVisible()) {
				getSupportFragmentManager()
						.beginTransaction()
						.setCustomAnimations(R.anim.enter, R.anim.exit)
						.replace(R.id.smartrac_activity_content, mapFrag,
								MAP_VIEW).commit();
			}

			timerHandler.post(drawItemRunnable);
		}
	}

	/**
	 * Draw trip segments within given parent view to form trip.
	 * 
	 * @param parentView
	 */
	public void drawTripInSelectedParent(CalendarItemParentView parentView) {
		if (parentView != null && parentView.getTrip().size() > 0) {
			if (!mapFrag.isVisible()) {
				getSupportFragmentManager()
						.beginTransaction()
						.setCustomAnimations(R.anim.enter, R.anim.exit)
						.replace(R.id.smartrac_activity_content, mapFrag,
								MAP_VIEW).commit();
			}

			timerHandler.post(drawTripRunnable);
		}
	}

	private void confirmSelection(List<CalendarItem> items) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Calendar Item");

		ListView itemList = new ListView(this);
		String[] stringArray = toStringArray(items);
		ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				stringArray);
		itemList.setAdapter(itemAdapter);

		itemList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				selectedItem = markerItemMap.get((String) adapterView
						.getAdapter().getItem(position));
				enterEditMode();
				alertDialog.dismiss();
			}
		});

		builder.setView(itemList);
		alertDialog = builder.create();

		removeTripsMarker();
		alertDialog.show();
	}

	private static String[] toStringArray(List<CalendarItem> items) {
		String[] array = new String[items.size()];

		for (int i = 0; i < items.size(); i++) {
			array[i] = items.get(i).toString();
		}

		return array;
	}

	private class GetCalendarItemTask extends
			AsyncTask<Void, Void, List<CalendarItem>> {

		private ProgressDialog dialog;

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Loading location data...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected List<CalendarItem> doInBackground(Void... params) {
			// Get list of calendar items using SmartracCoreDataProcessor.
			Calendar c = Calendar.getInstance();
			c.setTime(selectedDate);
			c.set(Calendar.HOUR, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			c.add(Calendar.DAY_OF_MONTH, 1);
			Date endDate = c.getTime();

			if (isBound) {
				return calendarItemService.getCalendarItems(selectedDate,
						endDate);
			}

			return new ArrayList<CalendarItem>();
		}

		@Override
		protected void onPostExecute(List<CalendarItem> params) {
			setCurrentItems(params);

			try {
				if (this.dialog != null && this.dialog.isShowing()) {
					this.dialog.dismiss();
				}
			} catch (final IllegalArgumentException e) {
				// Handle or log or ignore
			} catch (final Exception e) {
				// Handle or log or ignore
			} finally {
				this.dialog = null;
			}
		}
	}

	private class ClearChangesTask extends AsyncTask<Void, Void, Boolean> {

		private ProgressDialog dialog;

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Clear changes in calendar items ...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return calendarItemService.clearChanges(currentItems);
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (this.dialog != null && this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			if (success) {
				// Refresh current view after calendar item changes cleared.
				new GetCalendarItemTask().execute();
				Toast.makeText(SmartracActivity.this, "Calendar Items Reset",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(SmartracActivity.this, "Clear Changes Fail",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	// Used for test purpose, start service if it is not already running.
	private class PrepareTask extends AsyncTask<Void, Void, Void> {
		private boolean isMyServiceRunning(Class<?> serviceClass) {
			ActivityManager manager = (ActivityManager) SmartracActivity.this
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (RunningServiceInfo service : manager
					.getRunningServices(Integer.MAX_VALUE)) {
				if (serviceClass.getName().equals(
						service.service.getClassName())) {
					return true;
				}
			}

			return false;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Intent service = new Intent(SmartracActivity.this,
					SmartracService.class);

			if (!isMyServiceRunning(SmartracService.class)) {
				SmartracActivity.this.startService(service);
			}

			bindService(service, myConnection, Context.BIND_AUTO_CREATE);

			return null;
		}
	}

	private class PostProcessingTask extends
			AsyncTask<CalendarItem, Void, CalendarItem> {

		private ProgressDialog dialog;

		// can use UI thread here
		@Override
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Saving Changes...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected CalendarItem doInBackground(CalendarItem... items) {
			return calendarItemService.edit().change(items[0],
					getCurrentItems());
		}

		@Override
		protected void onPostExecute(CalendarItem item) {
			if (this.dialog != null && this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			selectedItem = item;

			setCurrentItems(getCurrentItems());
		}
	}

	private class PostProcessingParentViewTask extends
			AsyncTask<CalendarItemParentView, Void, CalendarItem> {

		private ProgressDialog dialog;

		// can use UI thread here
		@Override
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Saving Changes...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected CalendarItem doInBackground(CalendarItemParentView... parent) {
			return calendarItemService.edit().mergeTrip(
					parent[0].getChildren(), getCurrentItems());
		}

		@Override
		protected void onPostExecute(CalendarItem item) {
			if (this.dialog != null && this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			selectedParentView = null;
			selectedItem = item;

			setCurrentItems(getCurrentItems());
		}
	}

	private class PostSplitterProcessingTask extends
			AsyncTask<CalendarItemSplitter, Void, CalendarItem> {

		private ProgressDialog dialog;

		// can use UI thread here
		@Override
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Saving Changes...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected CalendarItem doInBackground(CalendarItemSplitter... splitter) {
			return calendarItemService.edit().split(splitter[0],
					getCurrentItems());
		}

		@Override
		protected void onPostExecute(CalendarItem item) {
			if (this.dialog != null && this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			selectedItem = item;

			setCurrentItems(getCurrentItems());
		}
	}

	private class PostProcessingActivityToWaitTask extends
			AsyncTask<CalendarItem, Void, CalendarItem> {

		private ProgressDialog dialog;

		// can use UI thread here
		@Override
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Saving Changes...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected CalendarItem doInBackground(CalendarItem... items) {
			return calendarItemService.edit().changeActivityToTrip(items[0],
					getCurrentItems());
		}

		@Override
		protected void onPostExecute(CalendarItem item) {
			if (this.dialog != null && this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			selectedItem = item;

			setCurrentItems(getCurrentItems());
		}
	}

	private class PostProcessingTripToActivityTask extends
			AsyncTask<CalendarItem, Void, CalendarItem> {

		private ProgressDialog dialog;

		// can use UI thread here
		@Override
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Saving Changes...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		@Override
		protected CalendarItem doInBackground(CalendarItem... items) {
			return calendarItemService.edit().changeTripToActivity(items[0],
					getCurrentItems());
		}

		@Override
		protected void onPostExecute(CalendarItem item) {
			if (this.dialog != null && this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			selectedItem = item;

			setCurrentItems(getCurrentItems());
		}
	}

	private class ExportDatabaseFileTask extends
			AsyncTask<String, Void, Boolean> {
		private ProgressDialog dialog;

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Exporting database...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		// automatically done on worker thread (separate from UI thread)
		protected Boolean doInBackground(final String... args) {

			File dbFile = SmartracActivity.this
					.getDatabasePath(SmartracSQLiteHelper.DATABASE_NAME);

			File exportDir = new File(
					Environment.getExternalStorageDirectory(), "");
			if (!exportDir.exists()) {
				exportDir.mkdirs();
			}
			File file = new File(exportDir, dbFile.getName());

			try {
				file.createNewFile();
				this.copyFile(dbFile, file);
				return true;
			} catch (IOException e) {
				Log.e("mypck", e.getMessage(), e);
				return false;
			}
		}

		// can use UI thread here
		protected void onPostExecute(final Boolean success) {
			if (this.dialog != null && this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
			if (success) {
				Toast.makeText(SmartracActivity.this, "Export successful!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(SmartracActivity.this, "Export failed",
						Toast.LENGTH_SHORT).show();
			}
		}

		void copyFile(File src, File dst) throws IOException {
			FileChannel inChannel = new FileInputStream(src).getChannel();
			FileChannel outChannel = new FileOutputStream(dst).getChannel();
			try {
				inChannel.transferTo(0, inChannel.size(), outChannel);
			} finally {
				if (inChannel != null)
					inChannel.close();
				if (outChannel != null)
					outChannel.close();
			}
		}

	}

	private class UploadTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog dialog;

		protected void onPreExecute() {
			this.dialog = new ProgressDialog(SmartracActivity.this);
			this.dialog.setMessage("Preparing for upload...");
			this.dialog.setCancelable(false);
			this.dialog.show();
		}

		protected Boolean doInBackground(Void... params) {
			ConnectivityManager cm = (ConnectivityManager) SmartracActivity.this
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null
					&& activeNetwork.isConnected();

			boolean isWiFi = isConnected
					&& activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

			if (isWiFi) {
				return true;
			}

			return false;
		}

		protected void onPostExecute(Boolean uploadStarted) {
			if (this.dialog != null && this.dialog.isShowing()) {
				this.dialog.dismiss();
			}

			selectedDrawerPosition = 0;
			mDrawerList.setItemChecked(selectedDrawerPosition, true);

			if (!uploadStarted) {
				new AlertDialog.Builder(SmartracActivity.this)
						.setTitle("Data Upload NOT Started")
						.setMessage(
								"Expect high data usage. Please connect to WIFI to proceed.")
						.setCancelable(false)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// continue with delete
										dialog.dismiss();
									}
								}).setIcon(android.R.drawable.ic_dialog_info)
						.show();
			} else {

				File f = getDatabasePath(SmartracSQLiteHelper.DATABASE_NAME);
				// Getting database size in MB
				long dbSize = f.length() / (1024 * 1024);

				SharedPreferences sharedPref = getSharedPreferences(
						"com.smartracumn.smartrac", Context.MODE_PRIVATE);

				String lastUploadDate = sharedPref.getString("LastUploadDate",
						"FIRST_TIME");

				String message = "Total size: "
						+ dbSize
						+ " MB\nOnce your data is uploaded, all SmarTrAC researchers will have access to your data. This may raise privacy concerns. Please contact the researchers at smartrac@umn.edu to enable this function.";
				if (!lastUploadDate.equals("FIRST_TIME")) {
					message = "Last Uploaded: " + lastUploadDate + "\n"
							+ message;
				}

				new AlertDialog.Builder(SmartracActivity.this)
						.setTitle("Data Upload: ")
						.setMessage(message)
						.setCancelable(true)
						.setPositiveButton(android.R.string.yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										// continue with delete
										dialog.dismiss();
									}
								})
						.setNegativeButton(android.R.string.no,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).setIcon(android.R.drawable.ic_dialog_info)
						.show();
			}
		}
	}

	public void showAdditionalDetailsFragment(long id, boolean isActivity) {
		AdditionalUserDetailsFragment frag = new AdditionalUserDetailsFragment(
				id, isActivity);
		FragmentManager fm = getFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.add(frag, "AdditionalDetails").commit();

	}

	public void showConfirmFragment(CalendarItem item, String description) {
		ConfirmScreenFragment frag = new ConfirmScreenFragment(item);
		FragmentManager fm = getFragmentManager();
		Bundle args = new Bundle();
		args.putString("Description", description);
		frag.setArguments(args);
		FragmentTransaction transaction = fm.beginTransaction();
		transaction.add(frag, "ConfirmScreen").commit();
	}

	/*
	 * Change activity to a trip(wait)
	 */
	public void changeActivityToTrip(CalendarItem item) {
		new PostProcessingActivityToWaitTask().execute(item);
	}

	/*
	 * Change Trip to Activity
	 */
	public void changeTripToActivity(CalendarItem item) {
		new PostProcessingTripToActivityTask().execute(item);
	}

	@Override
	public void confirmClicked() {
		CalendarListFragment frag = (CalendarListFragment) getSupportFragmentManager()
				.findFragmentByTag(CALENDAR_VIEW);

		if (frag != null) {
			frag.changeDropDownColor();
		}

	}
}