package com.smartracumn.smartrac.service;

import android.os.Handler;
import android.os.Looper;

/**
 * Provide looper to make different thread to make database operations
 * sequentially.
 * 
 * @author kangx385
 * 
 */
public class DatabaseWorkerThread extends Thread {
	private final static String TAG = DatabaseWorkerThread.class
			.getSimpleName();

	private Handler workerHandler;

	// what needs to be executed when thread starts
	@Override
	public void run() {
		Looper.prepare();
		workerHandler = new Handler();
		Looper.loop();
	}

	/**
	 * Get database worker handler.
	 * 
	 * @return
	 */
	public Handler getDatabaseWorker() {
		return workerHandler;
	}
}
