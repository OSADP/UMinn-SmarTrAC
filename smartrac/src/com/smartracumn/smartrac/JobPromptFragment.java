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
import android.widget.Button;
import android.widget.ToggleButton;

/**
 * The class represents job prompt fragment which check if user have primary
 * job.
 * 
 * @author kangx385
 * 
 */
public class JobPromptFragment extends Fragment {
	private final String TAG = getClass().getName();

	private ToggleButton jobToggleButton;

	private boolean hasWorkPlace = false;
	
	private ToggleButton studentToggleButton;

	private boolean isStudent = false;


	private Button next;

	/**
	 * Get a value indicating whether or not user has work place.
	 * 
	 * @return True if has work place is checked.
	 */
	public boolean getHasWorkPlace() {
		return hasWorkPlace;
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, getClass().getSimpleName() + ":entered onCreateView()");
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.job_prompt, container, false);
	}

	/**
	 * Attach call back methods for controls.
	 */
	private void attachCallBacks() {
		jobToggleButton = (ToggleButton) getView().findViewById(
				R.id.has_primary_job);

		jobToggleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hasWorkPlace = ((ToggleButton) v).isChecked();
			}
		});
		
		studentToggleButton = (ToggleButton)getView().findViewById(
				R.id.is_a_student);
		
		studentToggleButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isStudent = ((ToggleButton) v).isChecked();
			}
		});
		
		
		next = (Button) getView().findViewById(R.id.job_prompt_next);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences sharedPref = getActivity().getSharedPreferences(
				        "com.smartracumn.smartrac", Context.MODE_PRIVATE);	
				SharedPreferences.Editor editor = sharedPref.edit();

				if(isStudent)
					editor.putBoolean("Student", true);
				else
					editor.putBoolean("Student", false);
				
				if (hasWorkPlace) {
					editor.putBoolean("WorkPlace", true);
					editor.commit();
					((SearchMapActivity) getActivity())
							.setLocationTag(SearchMapActivity.WORKPLACE);
					((SearchMapActivity) getActivity()).gotoSearch();
				} else {
					editor.putBoolean("WorkPlace", true);
					editor.commit();
					((SearchMapActivity) getActivity()).gotoRestart();
				}
			}
		});
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
		attachCallBacks();
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
