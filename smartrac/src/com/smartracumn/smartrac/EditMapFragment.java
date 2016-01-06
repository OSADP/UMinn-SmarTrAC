package com.smartracumn.smartrac;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.smartracumn.smartrac.CalendarListFragment.CalendarItemParentView;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.DwellingLocation;
import com.smartracumn.smartrac.model.LocationWrapper;
import com.smartracumn.smartrac.model.TripCalendarItem;
import com.smartracumn.smartrac.util.ActivityFactory;
import com.smartracumn.smartrac.util.CalendarItemUtil.ActivityMapDrawable;
import com.smartracumn.smartrac.util.CalendarItemUtil.TripMapDrawable;
import com.smartracumn.smartrac.util.SmartracDataFormat;

/**
 * The map fragment used to show single calendar item details and allow user to
 * interact with given calendar item.
 * 
 * @author kangx385
 * 
 */
public class EditMapFragment extends Fragment {
	private final String TAG = getClass().getName();

	/**
	 * On calendar item modified listener.
	 * 
	 * @author kangx385
	 * 
	 */
	public interface OnCalendarItemModifiedListener {
		void onCalendarItemModified(CalendarItem item,
				CalendarItemSplitter splitter, CalendarItemParentView parentView);
	}

	private Set<OnCalendarItemModifiedListener> listeners = new HashSet<OnCalendarItemModifiedListener>();

	private CalendarItem item;

	private CalendarItemParentView parent;

	private Button start;

	private Button end;

	private Spinner spinner;

	private TextView spinnerTitle;

	private Button finishEdit;

	private Button splitItem;

	private View splitView;

	private ArrayAdapter<String> activitySpinnerArrayAdapter;

	private ArrayAdapter<String> activitySpinnerArrayAdapterWithWait;

	private ArrayAdapter<String> predictedActivitySpinnerArrayAdapter;

	private ArrayAdapter<String> tripSpinnerArrayAdapter;

	private AlertDialog splitDialog;

	private boolean startModified;

	private boolean endModified;

	private Date modifiedStart;

	private Date modifiedEnd;

	private int originalSpinnerPosition;

	private CalendarItemSplitter splitter;

	private ActivityFactory activityFactory;

	private ProgressDialog loadingDialog;

	private boolean predictedActivity;

	private int predictedPosition;

	private boolean noModificationAllowed = false;

	private static final String WAIT_PART_OF_TRIP = "Wait (Part of Trip)";
	
	private static final String CHANGE_TO_ACTIVITY = "Change to Activity";

	private Predictor predict_task;

	/**
	 * Register on calendar item modified call back.
	 * 
	 * @param listener
	 * @return
	 */
	public boolean registerOnCalendarItemModifiedListener(
			OnCalendarItemModifiedListener listener) {
		return this.listeners.add(listener);
	}

	/**
	 * Unregister on calendar item modified call back.
	 * 
	 * @param listener
	 * @return
	 */
	public boolean unregisterOnCalendarItemModifiedListener(
			OnCalendarItemModifiedListener listener) {
		return this.listeners.remove(listener);
	}

	private void notifyCalendarItemModified() {
		for (OnCalendarItemModifiedListener listener : listeners) {
			Log.i(TAG, "modified.");

			if (item != null) {
				Log.i(TAG, "Item");
				listener.onCalendarItemModified(item, splitter, null);
			}

			if (parent != null) {
				Log.i(TAG, "Parent view");
				listener.onCalendarItemModified(null, splitter, parent);
			}
		}
	}

	private TimePickerDialog.OnTimeSetListener startSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int min) {
			Calendar c = Calendar.getInstance();
			c.setTime(item.getStart());
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, min);

			if (c.getTime().getTime() < item.getEnd().getTime()) {
				startModified = c.get(Calendar.HOUR_OF_DAY) != item.getStart()
						.getHours()
						|| c.get(Calendar.MINUTE) != item.getStart()
								.getMinutes();
				modifiedStart = c.getTime();
				start.setText(SmartracDataFormat.getTimeFormat().format(
						modifiedStart));

				enableDisableModifyButton();
			} else {
				genAlert("Invalid Date", "Start time must less than end");
			}
		}
	};

	private TimePickerDialog.OnTimeSetListener endSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int min) {
			Calendar c = Calendar.getInstance();
			c.setTime(item.getEnd());
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, min);

			if (c.getTime().getTime() > item.getStart().getTime()) {
				endModified = c.get(Calendar.HOUR_OF_DAY) != item.getEnd()
						.getHours()
						|| c.get(Calendar.MINUTE) != item.getEnd().getMinutes();
				modifiedEnd = c.getTime();
				end.setText(SmartracDataFormat.getTimeFormat().format(
						modifiedEnd));

				enableDisableModifyButton();
			} else {
				genAlert("Invalid Date!", "End time must greater than start");
			}
		}
	};

	private void genAlert(String title, String msg) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());

		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder.setMessage(msg).setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});
		;

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	@Override
	public void onAttach(Activity activity) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onAttach()");
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Log.i(TAG, getClass().getSimpleName() + ":entered onCreate()");
		super.onCreate(savedInstanceState);
		initializeSpinnerEntries();
		this.activityFactory = ActivityFactory.getInstance(getActivity());
		setHasOptionsMenu(true);
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

		MenuItem calendar_item = menu.findItem(R.id.action_calendar);
		Drawable calIcon = getResources().getDrawable(
				R.drawable.action_calendar_list);
		calIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
		calendar_item.setIcon(calIcon);

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onCreateView()");
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.edit_map, container, false);

		Calendar cal = Calendar.getInstance();

		// Date two weeks ago
		cal.add(Calendar.DAY_OF_MONTH, -14);

		if (((SmartracActivity) getActivity()).getSelectedDate() != null
				&& ((SmartracActivity) getActivity()).getSelectedDate().before(
						cal.getTime())) {
			noModificationAllowed = true;
		}

		return v;
	}

	private void enableDisableModifyButton() {
		if (!startModified && !endModified
				&& originalSpinnerPosition == spinner.getSelectedItemPosition()) {
			finishEdit.setEnabled(false);
		} else {
			finishEdit.setEnabled(true);
		}

	}

	private CalendarItem getSelectedItem() {
		return ((SmartracActivity) getActivity()).getSelectedItem();
	}

	private CalendarItemParentView getSelectedParentView() {
		return ((SmartracActivity) getActivity()).getSelectedParentView();
	}

	protected void setCalendarItemParent(CalendarItemParentView parent) {
		if (parent != null) {
			this.parent = parent;
			endModified = false;
			startModified = false;

			start.setText(SmartracDataFormat.getTimeFormat().format(
					parent.getStart()));
			end.setText(SmartracDataFormat.getTimeFormat().format(
					parent.getEnd()));

			spinnerTitle
					.setText(getResources().getString(R.string.travel_mode));
			spinner.setAdapter(tripSpinnerArrayAdapter);
			originalSpinnerPosition = 0;
			spinner.setSelection(0);

			splitItem.setEnabled(false);
			start.setEnabled(false);
			end.setEnabled(false);
			enableDisableModifyButton();

			if (noModificationAllowed) {
				spinner.setEnabled(false);
			}
			item = null;
		}
	}

	protected void setCalendarItem(CalendarItem item) {
		if (item != null) {
			this.item = item;
			endModified = false;
			startModified = false;

			start.setText(SmartracDataFormat.getTimeFormat().format(
					item.getStart()));
			end.setText(SmartracDataFormat.getTimeFormat()
					.format(item.getEnd()));

			if (item.getType() == CalendarItem.Type.TRIP) {
				spinnerTitle.setText(getResources().getString(
						R.string.travel_mode));
				TripCalendarItem trip = (TripCalendarItem) item;
				spinner.setAdapter(tripSpinnerArrayAdapter);
				originalSpinnerPosition = trip.getMode().getValue();
				spinner.setSelection(trip.getMode().getValue());
			} else if (item.getType() == CalendarItem.Type.ACTIVITY) {
				spinnerTitle.setText(getResources().getString(
						R.string.activity_type));
				final ActivityCalendarItem activity = (ActivityCalendarItem) item;

				if (activity.getActivity() == com.smartracumn.smartrac.model.CalendarItem.Activity.UNKNOWN_ACTIVITY) {
					if (!item.isInProgress()) {
						predict_task = new Predictor(activity);

						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									predict_task.get(5000, TimeUnit.MILLISECONDS);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (TimeoutException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									Handler handler = new Handler(Looper.getMainLooper());
								    handler.post(
								        new Runnable()
								        {
								            @Override
								            public void run()
								            {
								                Toast.makeText(getActivity(), "Time out !", Toast.LENGTH_SHORT).show();
								                loadingDialog.dismiss();
								                predict_task.cancel(true);
								                
												spinner.setAdapter(activitySpinnerArrayAdapterWithWait);
												originalSpinnerPosition = activity.getActivity().getValue();
												spinner.setSelection(activity.getActivity().getValue());
								                
								            }
								        }
								    );								} 
							}
						}).start();

						 loadingDialog = ProgressDialog.show(
								getActivity(), "Predicting your activity", "",
								true);
						predict_task.execute(activity);
					}
				} else {
					spinner.setAdapter(activitySpinnerArrayAdapterWithWait);
					originalSpinnerPosition = activity.getActivity().getValue();
					// if (!predictedActivity) {
					// spinner.setSelection(activity.getActivity().getValue());
					// } else {
					// System.out.println("Predicted position");
					// spinner.setSelection(predictedPosition);
					// predictedActivity = false;
					// }
					spinner.setSelection(activity.getActivity().getValue());

				}
				enableDisableModifyButton();

			}

			if (item.isInProgress() || noModificationAllowed) {
				spinner.setEnabled(false);
				splitItem.setEnabled(false);
				start.setEnabled(false);
				end.setEnabled(false);
			} else {
				spinner.setEnabled(true);
				splitItem.setEnabled(true);
				start.setEnabled(true);
				end.setEnabled(true);
			}

			this.parent = null;
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onActivityCreated()");
		super.onActivityCreated(savedInstanceState);
	}

	private void buildDialogIfNeeded() {
		setupSplitView();

		if (splitDialog != null) {
			return;
		}

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				getActivity());

		// set title
		alertDialogBuilder.setTitle("Split Calendar Item");

		// set dialog message
		alertDialogBuilder
				.setMessage("How do you want to split it?")
				.setView(splitView)
				.setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (splitter != null && splitter.isValidSplit()) {
							notifyCalendarItemModified();
						}
						splitter = null;
						dialog.cancel();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								splitter = null;
								dialog.cancel();
							}
						});

		// create alert dialog
		splitDialog = alertDialogBuilder.create();
	}

	private void initializeSpinnerEntries() {
		activitySpinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.activity_types));
		activitySpinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		String[] arr = getResources().getStringArray(R.array.activity_types);
		List<String> list = new ArrayList<String>(Arrays.asList(arr));
		list.add(WAIT_PART_OF_TRIP);
		activitySpinnerArrayAdapterWithWait = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, list);
		activitySpinnerArrayAdapterWithWait
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		arr = getResources().getStringArray(R.array.travel_modes);
		list = new ArrayList<String>(Arrays.asList(arr));
		list.add(CHANGE_TO_ACTIVITY);
		tripSpinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, list);
		tripSpinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	private void setupSplitView() {
		if (splitView == null) {
			LayoutInflater inflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			splitView = inflater.inflate(R.layout.split_item, null);
		}

		if (splitView != null) {
			splitter = new CalendarItemSplitter(item);

			final CalendarItemSplitter currentSplitter = splitter;

			final TextView start = (TextView) splitView
					.findViewById(R.id.split_start);
			final TextView end = (TextView) splitView
					.findViewById(R.id.split_end);
			final TextView split = (TextView) splitView
					.findViewById(R.id.split_point);
			final SeekBar seekBar = (SeekBar) splitView
					.findViewById(R.id.seek_bar);
			final Spinner leftType = (Spinner) splitView
					.findViewById(R.id.left_type);
			final Spinner leftSub = (Spinner) splitView
					.findViewById(R.id.left_sub);
			final Spinner rightType = (Spinner) splitView
					.findViewById(R.id.right_type);
			final Spinner rightSub = (Spinner) splitView
					.findViewById(R.id.right_sub);

			start.setText(SmartracDataFormat.getTimeFormat().format(
					item.getStart()));
			end.setText(SmartracDataFormat.getTimeFormat()
					.format(item.getEnd()));

			leftType.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					currentSplitter.setTypes(CalendarItemSplitter.LEFT,
							CalendarItem.Type.get(position));

					if (position == CalendarItem.Type.TRIP.getValue()
							&& !tripSpinnerArrayAdapter.equals(leftSub
									.getAdapter())) {
						leftSub.setAdapter(tripSpinnerArrayAdapter);
					} else if (position == CalendarItem.Type.ACTIVITY
							.getValue()
							&& !activitySpinnerArrayAdapter.equals(leftSub
									.getAdapter())) {
						leftSub.setAdapter(activitySpinnerArrayAdapter);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});

			rightType.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					currentSplitter.setTypes(CalendarItemSplitter.RIGHT,
							CalendarItem.Type.get(position));

					if (position == CalendarItem.Type.TRIP.getValue()
							&& !tripSpinnerArrayAdapter.equals(rightSub
									.getAdapter())) {
						rightSub.setAdapter(tripSpinnerArrayAdapter);
					} else if (position == CalendarItem.Type.ACTIVITY
							.getValue()
							&& !activitySpinnerArrayAdapter.equals(rightSub
									.getAdapter())) {
						rightSub.setAdapter(activitySpinnerArrayAdapter);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});

			leftSub.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					currentSplitter.setSubIds(CalendarItemSplitter.LEFT,
							position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});

			rightSub.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					currentSplitter.setSubIds(CalendarItemSplitter.RIGHT,
							position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});

			leftType.setSelection(item.getType().getValue());
			rightType.setSelection(item.getType().getValue());

			if (item.getType() == CalendarItem.Type.ACTIVITY) {
				rightSub.setAdapter(activitySpinnerArrayAdapter);
				leftSub.setAdapter(activitySpinnerArrayAdapter);
				leftSub.setSelection(((ActivityCalendarItem) item)
						.getActivity().getValue());
				rightSub.setSelection(((ActivityCalendarItem) item)
						.getActivity().getValue());
			} else {
				rightSub.setAdapter(tripSpinnerArrayAdapter);
				leftSub.setAdapter(tripSpinnerArrayAdapter);
				leftSub.setSelection(((TripCalendarItem) item).getMode()
						.getValue());
				rightSub.setSelection(((TripCalendarItem) item).getMode()
						.getValue());
			}

			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					Calendar c = Calendar.getInstance();
					c.setTime(item.getStart());

					int milliSecs = (int) ((item.getEnd().getTime() - item
							.getStart().getTime()) * progress / 100);

					c.add(Calendar.MILLISECOND, milliSecs);

					Date splitPoint = c.getTime();

					currentSplitter.setSplitTime(splitPoint);

					split.setText(SmartracDataFormat.getTimeFormat().format(
							splitPoint));
				}
			});

			seekBar.setProgress(0);
			seekBar.setProgress(50);
		}
	}

	@Override
	public void onStart() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onStart()");
		super.onStart();

		start = (Button) getView().findViewById(R.id.edit_map_start);
		end = (Button) getView().findViewById(R.id.edit_map_end);
		spinner = (Spinner) getView().findViewById(R.id.spinner);
		spinnerTitle = (TextView) getView().findViewById(R.id.spinner_title);
		finishEdit = (Button) getView().findViewById(R.id.finish_edit);
		splitItem = (Button) getView().findViewById(R.id.split);

		predictedActivity = false;

		CalendarItem selectedItem = getSelectedItem();
		CalendarItemParentView selectedParentView = getSelectedParentView();

		if (selectedItem != null) {
			this.setCalendarItem(selectedItem);
		}

		if (selectedParentView != null) {
			this.setCalendarItemParent(selectedParentView);
		}

		start.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				TimePickerDialog picker = new TimePickerDialog(getActivity(),
						startSetListener, item.getStart().getHours(), item
								.getStart().getMinutes(), false);

				picker.show();
			}
		});

		end.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				TimePickerDialog picker = new TimePickerDialog(getActivity(),
						endSetListener, item.getEnd().getHours(), item.getEnd()
								.getMinutes(), false);
				picker.show();
			}
		});

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				enableDisableModifyButton();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		splitItem.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				buildDialogIfNeeded();

				// show it
				splitDialog.show();
			}
		});

		finishEdit.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!startModified
						&& !endModified
						&& originalSpinnerPosition == spinner
								.getSelectedItemPosition()) {
					genAlert("Modification fail!",
							"No change is made to current calendar item.");
					return;
				}

				updateItem();
			}
		});
	}

	private void updateItem() {

		int spinnerPositon = spinner.getSelectedItemPosition();

		if (startModified) {
			if (item != null) {
				item.setStart(modifiedStart);
			}
		}

		if (endModified) {
			if (item != null) {
				item.setEnd(modifiedEnd);
			}
		}

		if (spinner.getSelectedItem().toString().equals(WAIT_PART_OF_TRIP)) {
			SmartracActivity act = (SmartracActivity) getActivity();
			act.changeActivityToTrip(item);
			return;
		}

		if (spinner.getSelectedItem().toString().equals(CHANGE_TO_ACTIVITY)) {
			SmartracActivity act = (SmartracActivity) getActivity();
			act.changeTripToActivity(item);
			return;
		}

		if (item instanceof ActivityCalendarItem) {
			CalendarItem.Activity activity;
			if (predictedActivity) {
				String act_str = spinner.getSelectedItem().toString()
						.split(":")[0].toUpperCase(Locale.getDefault())
						.replace(",", "").replace(" ", "_");
				if (!act_str.equals("CHOOSE")) {
					activity = CalendarItem.Activity.valueOf(act_str);
				} else {
					activity = CalendarItem.Activity.UNKNOWN_ACTIVITY;
				}

				predictedPosition = spinnerPositon;
			} else {
				activity = CalendarItem.Activity.get(spinnerPositon);
			}
			((ActivityCalendarItem) item).setUserCorrectedActivity(activity);
		} else if (item instanceof TripCalendarItem) {
			if (item != null) {

				((TripCalendarItem) item)
						.setUserCorrectedMode(CalendarItem.TravelMode
								.get(spinnerPositon));

			}
		}

		if (parent != null) {
			for (CalendarItem item : parent.getChildren()) {
				if (item instanceof TripCalendarItem) {
					TripCalendarItem trip = (TripCalendarItem) item;
					trip.setUserCorrectedMode(CalendarItem.TravelMode
							.get(spinnerPositon));
				}

				if (item instanceof ActivityCalendarItem) {
					ActivityCalendarItem activity = (ActivityCalendarItem) item;
					activity.setUserCorrectedActivity(CalendarItem.Activity
							.get(spinnerPositon));
				}
			}
		}

		if (item != null) {
			item.setIsModified(startModified || endModified
					|| originalSpinnerPosition != spinnerPositon);
		}

		if (parent != null) {
			for (CalendarItem item : parent.getTrip()) {
				item.setIsModified(startModified || endModified
						|| originalSpinnerPosition != spinnerPositon);
			}
		}

		startModified = false;
		endModified = false;
		originalSpinnerPosition = spinnerPositon;

		notifyCalendarItemModified();
	}

	private boolean itemModified() {
		return startModified || endModified
				|| originalSpinnerPosition != spinner.getSelectedItemPosition();
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

		final SmartracActivity activity = (SmartracActivity) getActivity();

		if (activity != null && itemModified()) {
			updateItem();
		}

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

	public static class CalendarItemSplitter {
		public static final int LEFT = 0;
		public static final int RIGHT = 1;

		private CalendarItem item;
		private Date splitTime;
		private CalendarItem.Type leftType;
		private CalendarItem.Type rightType;
		private int leftSubId;
		private int rightSubId;

		public CalendarItemSplitter(CalendarItem item) {
			this.item = item;
			this.leftType = item.getType();
			this.rightType = item.getType();
			if (item instanceof TripCalendarItem) {
				this.leftSubId = ((TripCalendarItem) item).getMode().getValue();
				this.rightSubId = ((TripCalendarItem) item).getMode()
						.getValue();
			} else if (item instanceof ActivityCalendarItem) {
				this.leftSubId = ((ActivityCalendarItem) item).getActivity()
						.getValue();
				this.rightSubId = ((ActivityCalendarItem) item).getActivity()
						.getValue();
			}
		}

		public CalendarItem getSourceItem() {
			return this.item;
		}

		public void setSplitTime(Date splitTime) {
			this.splitTime = splitTime;
		}

		public void setTypes(int side, CalendarItem.Type type) {
			if (side == LEFT) {
				this.leftType = type;
			} else {
				this.rightType = type;
			}
		}

		public void setSubIds(int side, int id) {
			if (side == LEFT) {
				this.leftSubId = id;
			} else {
				this.rightSubId = id;
			}
		}

		public boolean isValidSplit() {
			boolean isValid = item != null && splitTime != null
					&& item.getStart().getTime() != splitTime.getTime()
					&& item.getEnd().getTime() != splitTime.getTime();

			return isValid;
		}

		public CalendarItem[] getCalendarItems() throws NullPointerException {
			if (this.item.getLocations() == null) {
				throw new NullPointerException();
			}

			return this.getCalendarItems(this.item.getLocations());
		}

		public CalendarItem[] getCalendarItems(List<LocationWrapper> locations) {
			CalendarItem[] items = new CalendarItem[2];

			List<LocationWrapper> leftLocs = new ArrayList<LocationWrapper>();
			List<LocationWrapper> rightLocs = new ArrayList<LocationWrapper>();

			for (LocationWrapper loc : locations) {
				if (loc.getTime().getTime() < splitTime.getTime()) {
					leftLocs.add(loc);
				} else {
					rightLocs.add(loc);
				}
			}

			if (leftType == CalendarItem.Type.ACTIVITY) {
				items[0] = new ActivityCalendarItem(item.getStart(), splitTime,
						CalendarItem.Activity.get(leftSubId),
						new ActivityMapDrawable(leftLocs));
				ActivityCalendarItem activity = (ActivityCalendarItem) items[0];

				DwellingLocation dl = new DwellingLocation(
						activity.getWeightedCenter(),
						CalendarItem.Activity.get(leftSubId));

				activity.setAssociateDwellingLocation(dl);
			} else {
				items[0] = new TripCalendarItem(item.getStart(), splitTime,
						CalendarItem.TravelMode.get(leftSubId),
						new TripMapDrawable(leftLocs));
			}

			if (rightType == CalendarItem.Type.ACTIVITY) {
				items[1] = new ActivityCalendarItem(splitTime, item.getEnd(),
						CalendarItem.Activity.get(rightSubId),
						new ActivityMapDrawable(rightLocs));

				ActivityCalendarItem activity = (ActivityCalendarItem) items[1];

				DwellingLocation dl = new DwellingLocation(
						activity.getWeightedCenter(),
						CalendarItem.Activity.get(rightSubId));

				activity.setAssociateDwellingLocation(dl);
			} else {
				items[1] = new TripCalendarItem(splitTime, item.getEnd(),
						CalendarItem.TravelMode.get(rightSubId),
						new TripMapDrawable(rightLocs));
			}

			return items;
		}
	}

	private class Predictor extends
			AsyncTask<ActivityCalendarItem, Void, TreeMap<String, Integer>> {

		private ActivityCalendarItem activity;

		public Predictor(ActivityCalendarItem activity) {
			this.activity = activity;
		}

		@Override
		protected TreeMap<String, Integer> doInBackground(
				ActivityCalendarItem... params) {

			TreeMap<String, Integer> predictions_map = activityFactory
					.predictActivity(params[0]);
			return predictions_map;
		}

		@Override
		protected void onProgressUpdate(Void... progress) {
		}

		@Override
		protected void onPostExecute(TreeMap<String, Integer> result_map) {

			ArrayList<String> result = new ArrayList<String>();

			double total = 0;
			for (Map.Entry<String, Integer> entry : result_map.entrySet()) {
				total = total + entry.getValue();
			}
			for (Map.Entry<String, Integer> entry : result_map.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();
				double prob = (double) (value / total);
				DecimalFormat df = new DecimalFormat("##");
				String percent = df.format(prob * 100);
				result.add(key + ": " + percent + "%");
			}

			result.add(0, "Choose: ");
			result.add(WAIT_PART_OF_TRIP);
			final ArrayList<String> predicted_results = result;
			if (result != null) {

				predictedActivitySpinnerArrayAdapter = new ArrayAdapter<String>(
						getActivity(), android.R.layout.simple_spinner_item,
						predicted_results) {
					public boolean isEnabled(int position) {
						if (position == 0) {
							return false;
						} else {
							return true;
						}
					}

					@Override
					public boolean areAllItemsEnabled() {
						return false;
					}

				};

				predictedActivitySpinnerArrayAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(predictedActivitySpinnerArrayAdapter);
				loadingDialog.dismiss();
				originalSpinnerPosition = 0;
				predictedActivity = true;

				// spinner.setSelection(activity.getActivity().getValue());
			}
		}
	}

}
