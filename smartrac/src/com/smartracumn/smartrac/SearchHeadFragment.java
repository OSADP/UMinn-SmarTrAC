package com.smartracumn.smartrac;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import com.smartracumn.smartrac.util.PlaceProvider;

/**
 * The fragment used in search map header.
 * 
 * @author kangx385
 * 
 */
public class SearchHeadFragment extends Fragment implements
		LoaderCallbacks<Cursor> {
	private final String TAG = getClass().getName();

	private AutoCompleteTextView searchView;

	private SimpleCursorAdapter suggestsAdapter;

	private Button searchButton;

	private TextView header;

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
		return inflater.inflate(R.layout.search_head, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onActivityCreated()");
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		Log.i(TAG, getClass().getSimpleName() + ":entered onStart()");
		initializeViews();
		setHeader(String.format("Please pin your %s location on the map.",
				((SearchMapActivity) getActivity()).getLocationTag()));
		super.onStart();
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

	/**
	 * Initializes views and controls.
	 */
	private void initializeViews() {
		// TODO Auto-generated method stub
		searchView = (AutoCompleteTextView) getView().findViewById(R.id.search);

		searchView.setText("");

		int[] to = new int[] { android.R.id.text1 };
		String[] from = new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 };

		suggestsAdapter = new SimpleCursorAdapter(getActivity(),
				android.R.layout.simple_dropdown_item_1line, null, from, to);

		searchView.setAdapter(suggestsAdapter);
		searchView.setThreshold(1);
		// Set an OnItemClickListener, to update dependent fields when
		// a choice is made in the AutoCompleteTextView.
		searchView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
				// Get the cursor, positioned to the corresponding row in the
				// result set
				Cursor cursor = (Cursor) listView.getItemAtPosition(position);

				// Get the state's capital from this row in the database.
				String ref = cursor.getString(cursor
						.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA));
			}
		});

		// searchView.set

		// Set the CursorToStringConverter, to provide the labels for the
		// choices to be displayed in the AutoCompleteTextView.
		suggestsAdapter
				.setCursorToStringConverter(new CursorToStringConverter() {
					public String convertToString(android.database.Cursor cursor) {
						// Get the label for this row out of the "state" column
						final int columnIndex = cursor
								.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
						final String str = cursor.getString(columnIndex);
						return str;
					}
				});

		// Set the FilterQueryProvider, to run queries for choices
		// that match the specified input.
		suggestsAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				// Search for states whose names begin with the specified
				// letters.

				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						getSuggestion(searchView.getText().toString());
					}
				});

				return null;
			}
		});

		searchButton = (Button) getView().findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						doSearch(searchView.getText().toString());
						hideSoftKeyboard();
					}
				});
			}
		});

		header = (TextView) getView().findViewById(R.id.search_map_header);
	}

	/**
	 * Set search box header.
	 * 
	 * @param msg
	 *            The message showing on search box header.
	 */
	private void setHeader(String msg) {
		if (header != null) {
			header.setText(msg);
		}
	}

	/**
	 * Set content of search box.
	 * 
	 * @param msg
	 *            The message.
	 */
	public void setSearch(String msg) {
		if (searchView != null) {
			searchView.setText(msg);
		}
	}

	/**
	 * Hide soft keyboard.
	 */
	private void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
	}

	/**
	 * Search places.
	 * 
	 * @param query
	 *            The query string.
	 */
	private void doSearch(String query) {
		Bundle data = new Bundle();
		data.putString("query", query);
		getActivity().getSupportLoaderManager().restartLoader(0, data, this);
	}

	/**
	 * Get details for given place.
	 * 
	 * @param query
	 *            The query string.
	 */
	private void getPlace(String query) {
		Bundle data = new Bundle();
		data.putString("query", query);
		getActivity().getSupportLoaderManager().restartLoader(1, data, this);
	}

	/**
	 * Get suggestion for places with given keyword.
	 * 
	 * @param query
	 *            The query string.
	 */
	private void getSuggestion(String query) {
		Bundle data = new Bundle();
		data.putString("query", query);
		getActivity().getSupportLoaderManager().restartLoader(2, data, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		CursorLoader cLoader = null;
		Log.i(TAG, getClass().getSimpleName() + ": onCreateLoader(" + id + ")");
		if (id == 0)
			cLoader = new CursorLoader(getActivity().getBaseContext(),
					PlaceProvider.SEARCH_URI, null, null,
					new String[] { args.getString("query") }, null);
		else if (id == 1)
			cLoader = new CursorLoader(getActivity().getBaseContext(),
					PlaceProvider.DETAILS_URI, null, null,
					new String[] { args.getString("query") }, null);
		else if (id == 2)
			cLoader = new CursorLoader(getActivity().getBaseContext(),
					PlaceProvider.SUGGESTS_URI, null, null,
					new String[] { args.getString("query") }, null);
		return cLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// TODO Auto-generated method stub
		Log.i(TAG,
				getClass().getSimpleName() + ": onLoadFinished(" + arg0.getId()
						+ ")");
		if (arg0.getId() == 2) {
			suggestsAdapter.swapCursor(arg1);
		}
		if (arg0.getId() == 1 || arg0.getId() == 0) {
			final Cursor cursor = arg1;
			// TODO Auto-generated method stub
			((SearchMapActivity) getActivity()).showLocations(cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
	}
}
