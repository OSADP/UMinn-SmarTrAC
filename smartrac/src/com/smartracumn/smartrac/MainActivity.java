package com.smartracumn.smartrac;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.smartracumn.smartrac.data.DwellingLocationDataSource;
import com.smartracumn.smartrac.model.DwellingLocation;

public class MainActivity extends Activity {

	private final String TAG = getClass().getName();

	private final int SPLASH_TIME = 2000;

	private boolean userDataCreated = false;

	ImageView imgView;

	private boolean timeReached = false;

	private boolean userDataChecked = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		imgView = (ImageView) findViewById(R.id.splash_view);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	// private boolean skipLocation;

	@Override
	public void onStart() {
		super.onStart();
		imgView.postDelayed(new Runnable() {

			@Override
			public void run() {
				timeReached = true;
				startApplication();
			}
		}, SPLASH_TIME);

		new CheckDwellingLocationTask().execute();
	}

	private void startApplication() {
		if (timeReached && userDataChecked) {
			if (!userDataCreated) {
				goToSearchMapActivity();
			} else {
				goToSmartracActivity();
			}

		}
	}

	protected void goToSmartracActivity() {
		Intent intent = new Intent(this, SmartracActivity.class);

		startActivity(intent);
	}

	protected void goToSearchMapActivity() {
		Intent intent = new Intent(this, SearchMapActivity.class);

		startActivity(intent);
	}

	private class CheckDwellingLocationTask extends
			AsyncTask<Void, Void, Boolean> {

		private List<DwellingLocation> getDwellingLocations() {
			DwellingLocationDataSource dataSource = new DwellingLocationDataSource(
					MainActivity.this);

			return dataSource.getAll();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// Check if user data is generated.
			Log.i(TAG, getClass().getSimpleName() + ": Check user data.");
			// return false;
			return getDwellingLocations().size() > 0;
		}

		@Override
		protected void onPostExecute(Boolean params) {
			userDataChecked = true;
			userDataCreated = params;
			startApplication();
		}
	}
}
