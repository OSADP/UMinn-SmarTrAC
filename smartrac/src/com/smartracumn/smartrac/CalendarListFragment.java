package com.smartracumn.smartrac;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartracumn.smartrac.SmartracActivity.CalendarItemLoadedListener;
import com.smartracumn.smartrac.data.SummaryDataSource;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.DummyCalendarItem;
import com.smartracumn.smartrac.model.ServiceOffCalendarItem;
import com.smartracumn.smartrac.model.TripCalendarItem;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * The fragment used to render calendar view of calendar items.
 * 
 * @author kangx385
 * 
 */
public class CalendarListFragment extends Fragment implements
		OnGroupClickListener, OnChildClickListener {
	private final String TAG = getClass().getName();

	private CalendarItemExpandableListAdapter expandableAdapter;

	private ExpandableListView expandableList;

	private List<CalendarItemParentView> parents;

	private LinearLayout last_opened_layout;

	private ImageView last_opened_dropdown;

	private static final int CONFIRMED_COLOR = Color.rgb(0, 84, 165);

	private SummaryDataSource summaryDataSource;

	private boolean noModificationAllowed = false;

	private DataCleaningReminder reminder;

	private ImageView selectedDropdown;

	private LinearLayout dropdown_ll;

	private CalendarItemLoadedListener listener = new CalendarItemLoadedListener() {

		@Override
		public void onCalendarItemLoaded(List<CalendarItem> items) {
			// Update calendar items on calendar item loaded.
			updateCalendarItems(getActivity(), items);
		}
	};

	private void selectItem(CalendarItem item) {
		((SmartracActivity) getActivity()).setSelectedItem(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ": onCreateView");
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.calendar_list_fragment, container,
				false);
		expandableList = (ExpandableListView) v
				.findViewById(R.id.expandable_list_view);
		expandableList.setGroupIndicator(null);
		expandableList.setOnGroupClickListener(this);
		expandableList.setOnChildClickListener(this);

		summaryDataSource = new SummaryDataSource(getActivity());

		// Register swipe event.
		expandableList.setOnTouchListener(new OnSwipeTouchListener(
				getActivity()) {

			@Override
			public void onSwipeRight() {
				SmartracActivity activity = (SmartracActivity) getActivity();
				Date selectedDate = activity.getSelectedDate();

				Calendar tomorrow = Calendar.getInstance();
				tomorrow.setTime(selectedDate);
				tomorrow.add(Calendar.DAY_OF_MONTH, -1);

				activity.setSelectedDate(tomorrow.getTime());
			}

			@Override
			public void onSwipeLeft() {
				SmartracActivity activity = (SmartracActivity) getActivity();
				Date selectedDate = activity.getSelectedDate();
				Calendar now = Calendar.getInstance();
				Calendar selected = Calendar.getInstance();
				selected.setTime(selectedDate);

				if (selected.get(Calendar.DAY_OF_MONTH) != now
						.get(Calendar.DAY_OF_MONTH)) {
					Calendar tomorrow = Calendar.getInstance();
					tomorrow.setTime(selectedDate);
					tomorrow.add(Calendar.DAY_OF_MONTH, 1);

					activity.setSelectedDate(tomorrow.getTime());
				}
			}
		});

		// Display reminder
		// reminder = new DataCleaningReminder(getActivity());
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_calendar, menu);

		// If the menu is created after selected date is set, initialize time.
		if (((SmartracActivity) getActivity()).getSelectedDate() != null) {
			MenuItem dateButton = menu.findItem(R.id.action_set_date);
			if (dateButton.getTitle().toString()
					.equals(getResources().getString(R.string.date))) {
				dateButton.setTitle(SmartracDataFormat.getDateFormat().format(
						((SmartracActivity) getActivity()).getSelectedDate()));
			}
		}

		MenuItem map_item = menu.findItem(R.id.action_map);
		Drawable mapIcon = getResources().getDrawable(R.drawable.action_map);
		mapIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
		map_item.setIcon(mapIcon);

		// MenuItem screenShotButton = menu.findItem(R.id.action_screenShot);
		//
		// SharedPreferences sharedPref = getActivity().getSharedPreferences(
		// "com.smartracumn.smartrac", Context.MODE_PRIVATE);
		//
		// boolean bugMode = sharedPref.getBoolean("BugMode", false);
		// if (bugMode) {
		// screenShotButton.setVisible(true);
		// } else {
		// screenShotButton.setVisible(false);
		// }

	}

	private void updateCalendarItems(Context context, List<CalendarItem> items) {
		groupCalendarItems(items);

		expandableAdapter = new CalendarItemExpandableListAdapter(context);
		expandableList.setAdapter(expandableAdapter);
	}

	private void groupCalendarItems(List<CalendarItem> items) {
		this.parents = new ArrayList<CalendarItemParentView>();

		List<CalendarItem> tempGroup = null;

		for (CalendarItem item : items) {
			if ((item instanceof ActivityCalendarItem)
					|| (item instanceof DummyCalendarItem)
					|| (item instanceof ServiceOffCalendarItem)) {
				if (tempGroup != null) {
					if (tempGroup.size() == 1) {
						this.parents.add(new CalendarItemParentView(tempGroup
								.get(0)));
					} else {
						this.parents.add(new CalendarItemParentView(tempGroup));
					}

					tempGroup = null;
				}

				this.parents.add(new CalendarItemParentView(item));
			} else if (item instanceof TripCalendarItem) {
				if (tempGroup == null) {
					tempGroup = new ArrayList<CalendarItem>();
				}

				tempGroup.add(item);
			}
		}

		if (tempGroup != null) {
			if (tempGroup.size() == 1) {
				this.parents.add(new CalendarItemParentView(tempGroup.get(0)));
			} else {
				this.parents.add(new CalendarItemParentView(tempGroup));
			}
			tempGroup = null;
		}
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
		updateCalendarItems(getActivity(),
				((SmartracActivity) getActivity()).getCurrentItems());
		((SmartracActivity) getActivity())
				.registerItemLoadedListener(this.listener);
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
		((SmartracActivity) getActivity())
				.unregisterItemLoadedListener(this.listener);
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

	public void changeDropDownColor() {
		selectedDropdown.setBackgroundColor(CONFIRMED_COLOR);
		dropdown_ll.setVisibility(View.GONE);
		last_opened_dropdown.setImageResource(R.drawable.down_arrow);

	}

	/**
	 * Calendar item expandable list adapter.
	 * 
	 * @author kangx385
	 * 
	 */
	private class CalendarItemExpandableListAdapter extends
			BaseExpandableListAdapter {
		private Context context;

		private LayoutInflater inflater;

		public CalendarItemExpandableListAdapter(Context context) {
			super();

			this.context = context;
			this.inflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			noModificationAllowed = false;

			Calendar cal = Calendar.getInstance();

			// Date two weeks ago
			cal.add(Calendar.DAY_OF_MONTH, -14);

			if (((SmartracActivity) getActivity()).getSelectedDate() != null
					&& ((SmartracActivity) getActivity()).getSelectedDate()
							.before(cal.getTime())) {
				noModificationAllowed = true;
			}

		}

		// This Function used to inflate parent rows view
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parentView) {
			// Inflate grouprow.xml file for parent rows
			convertView = inflater.inflate(R.layout.calendar_item, parentView,
					false);
			final int position = groupPosition;
			final LinearLayout ll = (LinearLayout) convertView
					.findViewById(R.id.summary);

			final CalendarItemParentView parent = parents.get(groupPosition);

			final ImageView icon = (ImageView) convertView
					.findViewById(R.id.icon);
			seticon(icon, parent.getDescription().toString());

			TextView descriptionView = (TextView) convertView
					.findViewById(R.id.description);
			TextView startView = (TextView) convertView
					.findViewById(R.id.calendar_item_start);
			TextView endView = (TextView) convertView
					.findViewById(R.id.calendar_item_end);
			final ImageView dropDown = (ImageView) convertView
					.findViewById(R.id.dropdown);
			TextView tv_duration = (TextView) convertView
					.findViewById(R.id.tv_duration);

			Button edit = (Button) convertView.findViewById(R.id.edit);
			Button addDetails = (Button) convertView
					.findViewById(R.id.add_details);
			Button confirm = (Button) convertView.findViewById(R.id.confirm);

			descriptionView.setText(parent.getDescription());

			boolean isActivity = false;

			if (!(parents.get(position).getSingleItem() instanceof DummyCalendarItem || parents
					.get(position).getSingleItem() instanceof ServiceOffCalendarItem)) {

				// Hide the distance and speed as they are not relevant for
				// activities
				if (parent.getSingleItem() instanceof ActivityCalendarItem) {
					LinearLayout ll_distance = (LinearLayout) convertView
							.findViewById(R.id.ll_distance);
					LinearLayout ll_avgSpeed = (LinearLayout) convertView
							.findViewById(R.id.ll_avgSpeed);

					ll_distance.setVisibility(View.GONE);
					ll_avgSpeed.setVisibility(View.GONE);

					isActivity = true;

					if (parent.getDescription().contains("Home")
							|| parent.getDescription().contains("Work")) {
						addDetails.setVisibility(View.GONE);

					}

				}

				dropDown.setVisibility(View.VISIBLE);

				// Checking if the item is confirmed by the user
				if (parent.getSingleItem() != null
						&& summaryDataSource.isConfirmed(parent.getSingleItem()
								.getId(), isActivity)) {
					dropDown.setBackgroundColor(CONFIRMED_COLOR);
				} else if (parent.getChildren() != null
						&& parent.getChildren().size() > 0) {
					if (summaryDataSource.isConfirmed(
							parent.getChildren().get(0).getId(), isActivity)) {
						dropDown.setBackgroundColor(CONFIRMED_COLOR);

					}
				}

				if ((parent.getSingleItem() != null)
						&& parent.getSingleItem().isInProgress()) {
					dropDown.setVisibility(View.GONE);
				}
				if (parent.getChildren() != null
						&& parent.getChildren()
								.get(parent.getChildren().size() - 1)
								.isInProgress()) {
					dropDown.setVisibility(View.GONE);
				}
				dropDown.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						if (last_opened_layout != null
								&& last_opened_layout != ll) {
							last_opened_layout.setVisibility(View.GONE);
							last_opened_dropdown
									.setImageResource(R.drawable.down_arrow);
						}

						if (ll.getVisibility() == View.GONE) {
							last_opened_layout = ll;
							last_opened_dropdown = dropDown;
							dropDown.setImageResource(R.drawable.up_arrow);
							ll.setVisibility(View.VISIBLE);
						} else if (ll.getVisibility() == View.VISIBLE) {
							dropDown.setImageResource(R.drawable.down_arrow);
							ll.setVisibility(View.GONE);
						}

					}
				});

				edit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						if (parents.get(position).getSingleItem() == null) {
							((SmartracActivity) getActivity())
									.editWholeTrip(parents.get(position));
						} else {
							selectItem(parents.get(position).getSingleItem());
						}
					}
				});

				addDetails.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// Activity item
						if (parent.getSingleItem() instanceof ActivityCalendarItem) {
							((SmartracActivity) getActivity())
									.showAdditionalDetailsFragment(parent
											.getSingleItem().getId(), true);
						} else if (parent.getChildren() != null
								&& parent.getChildren().size() > 0) {
							// Trip item with children
							((SmartracActivity) getActivity())
									.showAdditionalDetailsFragment(parent
											.getChildren().get(0).getId(),
											false);
						} else {
							// Trip item without children
							((SmartracActivity) getActivity())
									.showAdditionalDetailsFragment(parent
											.getSingleItem().getId(), false);

						}
					}
				});

			}

			confirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					selectedDropdown = dropDown;
					dropdown_ll = ll;

					if (parent.getChildren() != null
							&& parent.getChildren().size() > 0) {
						// Trip item with children
						CalendarItem newItem = parent.getChildren().get(0);
						// Setting the end time same as the end time of the last
						// trip
						newItem.setEnd(parent.getChildren()
								.get(parent.getChildren().size() - 1).getEnd());
						((SmartracActivity) getActivity()).showConfirmFragment(
								newItem, parent.getDescription());
					} else {
						// item without children

						if (parent.getDescription().contains("Home")
								|| parent.getDescription().contains("Work")) {
							summaryDataSource.setConfirm(parent.getSingleItem()
									.getId());
							Toast.makeText(getActivity(), "Activity Confirmed",
									Toast.LENGTH_SHORT).show();
							selectedDropdown
									.setBackgroundColor(CONFIRMED_COLOR);
							dropdown_ll.setVisibility(View.GONE);
							last_opened_dropdown
									.setImageResource(R.drawable.down_arrow);
						} else {
							((SmartracActivity) getActivity())
									.showConfirmFragment(
											parent.getSingleItem(),
											parent.getDescription());
						}

					}

				}
			});

			if (noModificationAllowed == true) {
				addDetails.setEnabled(false);
				confirm.setEnabled(false);
				addDetails.setBackgroundColor(Color.GRAY);
				confirm.setBackgroundColor(Color.GRAY);
			}
			float total_distance = 0;

			// Calculating the distance of all the children
			if (parent.getChildren() != null && parent.getChildren().size() > 0) {

				// Using the calendar item id of one children, we get the id of
				// all the children that have the same trip_id and calculate the
				// total distance

				total_distance = summaryDataSource.getTotalTripDistance(parent
						.getChildren().get(0).getId());

			} else if (parent.getSingleItem() instanceof TripCalendarItem) {

				total_distance = total_distance
						+ summaryDataSource.calculateDistance(parent
								.getSingleItem().getId());
				summaryDataSource.setDistance(parent.getSingleItem().getId(),
						total_distance);
			}

			TextView tv_distance = (TextView) convertView
					.findViewById(R.id.distance);
			float distance_miles = (float) (0.000621 * total_distance);
			tv_distance.setText("" + round(total_distance, 2) + " m" + " ("
					+ round(distance_miles, 2) + " miles)");

			Date startDate = parent.getStart();
			Date endDate = parent.getEnd();

			long time_duration = endDate.getTime() - startDate.getTime();
			long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(time_duration);

			// Duration
			int hours = (int) (diffInMinutes / 60);
			int minutes = (int) (diffInMinutes % 60);
			String duration = "" + hours + " hr " + minutes + " mins";
			tv_duration.setText(duration);

			// Avg Speed
			long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(time_duration);
			double avgSpeed = total_distance / diffInSeconds;
			TextView tv_avgSpeed = (TextView) convertView
					.findViewById(R.id.avgSpeed);
			double avgSpeed_in_miles = avgSpeed * 2.23;
			tv_avgSpeed.setText(" " + round(avgSpeed, 2) + " m/s" + " ("
					+ round(avgSpeed_in_miles, 2) + " miles/hr)");

			String startText = SmartracDataFormat.getTimeFormat().format(
					startDate);
			String endText = SmartracDataFormat.getTimeFormat().format(endDate);

			if (parent.isCrossDay()) {
				if (groupPosition == 0) {
					startText = SmartracDataFormat.getSimpleDateTimeFormat()
							.format(parent.getStart());
				} else {
					endText = SmartracDataFormat.getSimpleDateTimeFormat()
							.format(parent.getEnd());
				}
			}

			if (parent.isInProgress()) {
				endView.setText("");
			} else {
				endView.setText(endText);
			}

			startView.setText(startText);

			if (parent.getSingleItem() != null) {
				if (parent.getSingleItem() instanceof ActivityCalendarItem) {
					convertView.setBackgroundColor(getResources().getColor(
							R.color.row_shadow));
				} else if (parent.getSingleItem() instanceof DummyCalendarItem
						|| parent.getSingleItem() instanceof ServiceOffCalendarItem) {
					convertView.setBackgroundColor(getResources().getColor(
							R.color.grey));
				}
			}

			return convertView;
		}

		private void seticon(ImageView icon, String desc) {
			icon.setVisibility(View.VISIBLE);
			if (desc.contains("in progress..."))
				icon.setImageResource(R.drawable.progress);
			else if (desc.contains("Home"))
				icon.setImageResource(R.drawable.home);
			else if (desc.contains("Work"))
				icon.setImageResource(R.drawable.work);
			else if (desc.contains("Shopping"))
				icon.setImageResource(R.drawable.shopping);
			else if (desc.contains("Education"))
				icon.setImageResource(R.drawable.education);
			else if (desc.contains("Eat out"))
				icon.setImageResource(R.drawable.eat_out);
			else if (desc.contains("Other personal business"))
				icon.setImageResource(R.drawable.personal_business);
			else if (desc.contains("Social"))
				icon.setImageResource(R.drawable.social);
			else if (desc.contains("Activity"))
				icon.setImageResource(R.drawable.question);
			else if (desc.contains("Walk"))
				icon.setImageResource(R.drawable.walk);
			else if (desc.contains("Bus"))
				icon.setImageResource(R.drawable.bus);
			else if (desc.contains("Car"))
				icon.setImageResource(R.drawable.car);
			else if (desc.contains("Rail"))
				icon.setImageResource(R.drawable.rail);
			else if (desc.contains("Bicycle"))
				icon.setImageResource(R.drawable.bike);
			else if (desc.contains("Wait"))
				icon.setImageResource(R.drawable.wait);
			else if (desc.contains("Processing"))
				icon.setImageResource(R.drawable.processing);
			else
				icon.setImageResource(R.drawable.no_data);

		}

		// This Function used to inflate child rows view
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parentView) {
			final CalendarItemParentView parent = parents.get(groupPosition);
			final CalendarItem child = parent.getChildren().get(childPosition);

			// Inflate childrow.xml file for child rows
			convertView = inflater.inflate(R.layout.calendar_item, parentView,
					false);

			final LinearLayout ll = (LinearLayout) convertView
					.findViewById(R.id.summary);

			TextView descriptionView = (TextView) convertView
					.findViewById(R.id.description);
			TextView startView = (TextView) convertView
					.findViewById(R.id.calendar_item_start);
			TextView endView = (TextView) convertView
					.findViewById(R.id.calendar_item_end);
			descriptionView.setText(child.getDescription());

			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			seticon(icon, child.getDescription().toString());

			final ImageView dropDown = (ImageView) convertView
					.findViewById(R.id.dropdown);
			dropDown.setVisibility(View.VISIBLE);

			dropDown.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (last_opened_layout != null && last_opened_layout != ll) {
						last_opened_layout.setVisibility(View.GONE);
						last_opened_dropdown
								.setImageResource(R.drawable.down_arrow);
					}
					if (ll.getVisibility() == View.GONE) {
						last_opened_layout = ll;
						last_opened_dropdown = dropDown;
						dropDown.setImageResource(R.drawable.up_arrow);
						ll.setVisibility(View.VISIBLE);
					} else if (ll.getVisibility() == View.VISIBLE) {
						ll.setVisibility(View.GONE);
						dropDown.setImageResource(R.drawable.down_arrow);

					}

				}
			});

			Button edit = (Button) convertView.findViewById(R.id.edit);
			edit.setVisibility(View.GONE);

			Button addDetails = (Button) convertView
					.findViewById(R.id.add_details);
			addDetails.setVisibility(View.GONE);

			Button confirm = (Button) convertView.findViewById(R.id.confirm);
			confirm.setVisibility(View.GONE);

			// Duration
			Date startDate = child.getStart();
			Date endDate = child.getEnd();

			long time_duration = endDate.getTime() - startDate.getTime();
			long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(time_duration);

			// Duration
			int hours = (int) (diffInMinutes / 60);
			int minutes = (int) (diffInMinutes % 60);
			String duration = "" + hours + " hr " + minutes + " mins";
			TextView tv_duration = (TextView) convertView
					.findViewById(R.id.tv_duration);
			tv_duration.setText(duration);

			// Distance
			double distance = summaryDataSource
					.getDistanceFromDB(child.getId());
			TextView tv_distance = (TextView) convertView
					.findViewById(R.id.distance);
			double distance_miles = 0.000621 * distance;
			tv_distance.setText(" " + round(distance, 2) + " m" + " ("
					+ round(distance_miles, 2) + " miles)");

			// Avg Speed
			long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(time_duration);
			double avgSpeed = distance / diffInSeconds;
			TextView tv_avgSpeed = (TextView) convertView
					.findViewById(R.id.avgSpeed);
			double avgSpeed_in_miles = avgSpeed * 2.23;
			tv_avgSpeed.setText(" " + round(avgSpeed, 2) + " m/s" + " ("
					+ round(avgSpeed_in_miles, 2) + " miles/hr)");

			if (child.isInProgress()) {
				endView.setText("");
			} else {
				endView.setText(SmartracDataFormat.getTimeFormat().format(
						child.getEnd()));
			}

			if (child.isCrossDay()) {
				startView.setText(SmartracDataFormat.getSimpleDateTimeFormat()
						.format(child.getStart()));
			} else {
				startView.setText(SmartracDataFormat.getTimeFormat().format(
						child.getStart()));
			}

			return convertView;
		}

		public double round(double value, int places) {
			if (places < 0)
				throw new IllegalArgumentException();

			long factor = (long) Math.pow(10, places);
			value = value * factor;
			long tmp = Math.round(value);
			return (double) tmp / factor;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// Log.i("Childs", groupPosition+"=  getChild =="+childPosition);
			return parents.get(groupPosition).getChildren().get(childPosition);
		}

		// Call when child row clicked
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int size = 0;
			if (parents.get(groupPosition).getChildren() != null) {
				size = parents.get(groupPosition).getChildren().size();
			}
			return size;
		}

		@Override
		public Object getGroup(int groupPosition) {
			Log.i("Parent", groupPosition + "=  getGroup ");

			return parents.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return parents.size();
		}

		// Call when parent row clicked
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public void notifyDataSetChanged() {
			// Refresh List rows
			super.notifyDataSetChanged();
		}

		@Override
		public boolean isEmpty() {
			return ((parents == null) || parents.isEmpty());
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}
	}

	@Override
	public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
			long arg3) {
		if (parents.get(arg2).getSingleItem() != null) {
			if (!(parents.get(arg2).getSingleItem() instanceof DummyCalendarItem || parents
					.get(arg2).getSingleItem() instanceof ServiceOffCalendarItem)) {
				selectItem(parents.get(arg2).getSingleItem());
			}

			return true;
		}

		if (parents.get(arg2).isExpanded) {
			// ((SmartracActivity)
			// getActivity()).editWholeTrip(parents.get(arg2));
			parents.get(arg2).expand(false);
			expandableList.collapseGroup(arg2);
			return true;
		}

		parents.get(arg2).expand(true);
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2,
			int arg3, long arg4) {
		CalendarItemParentView parent = parents.get(arg2);
		CalendarItem item = parent.getChildren().get(arg3);

		selectItem(item);

		return true;
	}

	/**
	 * Calendar item parent view.
	 * 
	 * @author kangx385
	 * 
	 */
	public class CalendarItemParentView {
		private List<CalendarItem> children;

		private CalendarItem singleItem;

		private String description;

		private Date start;

		private Date end;

		private List<TripCalendarItem> trip;

		private boolean isExpanded = false;

		/**
		 * Set expand.
		 * 
		 * @param value
		 */
		public void expand(boolean value) {
			isExpanded = value;
		}

		/**
		 * Get a value indicating whether or not item is expanded.
		 * 
		 * @return
		 */
		public boolean isExpanded() {
			return isExpanded;
		}

		/**
		 * Initializes a new instance of the CalendarItemParentView class.
		 * 
		 * @param calendarItems
		 */
		public CalendarItemParentView(List<CalendarItem> calendarItems) {
			StringBuilder sb = new StringBuilder();

			for (CalendarItem item : calendarItems) {
				sb.append(item.getDescription());
				sb.append(" - ");
			}

			this.description = sb.substring(0, sb.length() - 2);

			this.start = calendarItems.get(0).getStart();
			this.end = calendarItems.get(calendarItems.size() - 1).getEnd();

			setChildren(calendarItems);
		}

		/**
		 * Initializes a new instance of the CalendarItemParentView class.
		 * 
		 * @param calendarItem
		 */
		public CalendarItemParentView(CalendarItem calendarItem) {
			this.description = calendarItem.getDescription();
			this.start = calendarItem.getStart();
			this.end = calendarItem.getEnd();
			this.singleItem = calendarItem;
		}

		/**
		 * Get description.
		 * 
		 * @return
		 */
		public String getDescription() {
			return this.description;
		}

		/**
		 * Get start.
		 * 
		 * @return
		 */
		public Date getStart() {
			return this.start;
		}

		/**
		 * Get end.
		 * 
		 * @return
		 */
		public Date getEnd() {

			return this.end;
		}

		public boolean isInProgress() {
			if (this.children != null
					&& this.children.get(this.children.size() - 1)
							.isInProgress()) {
				return true;
			} else if (this.singleItem != null
					&& this.singleItem.isInProgress()) {
				return true;
			}

			return false;
		}

		/**
		 * Set children.
		 * 
		 * @param items
		 */
		public void setChildren(List<CalendarItem> items) {
			List<CalendarItem> copyItems = new ArrayList<CalendarItem>(items);
			this.children = copyItems;
		}

		/**
		 * Get children.
		 * 
		 * @return
		 */
		public List<CalendarItem> getChildren() {
			return this.children;
		}

		/**
		 * Get the single item.
		 * 
		 * @return
		 */
		public CalendarItem getSingleItem() {
			return this.singleItem;
		}

		/**
		 * Get a value indicating whether or not it is a cross day group.
		 * 
		 * @return
		 */
		public boolean isCrossDay() {
			Calendar start = Calendar.getInstance();
			start.setTime(this.start);
			Calendar end = Calendar.getInstance();
			end.setTime(this.end);

			return start.get(Calendar.DATE) != end.get(Calendar.DATE);
		}

		/**
		 * Get trip segments within parent view.
		 * 
		 * @return
		 */
		public List<TripCalendarItem> getTrip() {
			if (trip == null) {
				trip = new ArrayList<TripCalendarItem>();

				if (children != null) {
					for (CalendarItem item : children) {
						if (item instanceof TripCalendarItem) {
							trip.add((TripCalendarItem) item);
						}
					}
				}
			}

			Collections.sort(trip);

			return trip;
		}
	}

	/**
	 * Detects left and right swipes across a view.
	 */
	public abstract class OnSwipeTouchListener implements OnTouchListener {

		private final GestureDetector gestureDetector;

		public OnSwipeTouchListener(Context context) {
			gestureDetector = new GestureDetector(context,
					new GestureListener());
		}

		public abstract void onSwipeLeft();

		public abstract void onSwipeRight();

		public boolean onTouch(View v, MotionEvent event) {
			return gestureDetector.onTouchEvent(event);
		}

		private final class GestureListener extends SimpleOnGestureListener {

			private static final int SWIPE_DISTANCE_THRESHOLD = 100;
			private static final int SWIPE_VELOCITY_THRESHOLD = 100;

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				float distanceX = e2.getX() - e1.getX();
				float distanceY = e2.getY() - e1.getY();
				if (Math.abs(distanceX) > Math.abs(distanceY)
						&& Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD
						&& Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
					if (distanceX > 0)
						onSwipeRight();
					else
						onSwipeLeft();
					return true;
				}
				return false;
			}
		}
	}
}
