package com.smartracumn.smartrac.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Handler;

import com.smartracumn.smartrac.model.MotionData;

/**
 * The class represents SmartracMotionListener which listen to accelerometer
 * changes and notify observers.
 * 
 * @author kangx385
 * 
 */
public class MotionListener implements SensorEventListener {
	private final String TAG = getClass().getName();

	private final int RATE = 200;

	private boolean sensorRegistered = false;

	private List<MotionData> cachedMotionData = new ArrayList<MotionData>();

	private final SensorManager sensorMngr;

	private Sensor gravitySensor;

	private Sensor magSensor;

	private Sensor linearAccSensor;

	private SensorEvent linearAccEvent;

	private float[] trueAcceleration = new float[4];
	private float[] linearAcceleration = new float[4];

	private float[] gravity = new float[4];
	private float[] last_gravity = new float[4];
	private float[] geomag = new float[4];

	private Handler accUpdateHandler = new Handler();

	private Runnable accRecorderRunnable = new Runnable() {

		@Override
		public void run() {
			if (linearAccEvent != null) {
				processSensor();
			}

			if (sensorRegistered) {
				accUpdateHandler.postDelayed(this, RATE);
			}
		}
	};

	/**
	 * Get sampled motion datas, write cached motion datas to database and then
	 * clear cache.
	 * 
	 * @return A copy of cached motion datas.
	 */
	public List<MotionData> getMotionDatas() {
		List<MotionData> temp = new ArrayList<MotionData>(cachedMotionData);
		cachedMotionData.clear();

		return temp;
	}

	/**
	 * Initializes a new instance of the SmartracMotionListener class.
	 * 
	 * @param context
	 * @param databaseWorkerHandler
	 */
	public MotionListener(Context context) {
		sensorMngr = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
	}

	/**
	 * Start motion data sampling at rate of 5Hz.
	 * 
	 * @return True is start success, false otherwise.
	 */
	public boolean start() {
		if (!sensorRegistered) {
			registerSensorListener();
			accUpdateHandler.removeCallbacks(accRecorderRunnable);
			accUpdateHandler.post(accRecorderRunnable);
			sensorRegistered = true;
			return true;
		}

		return false;
	}

	/**
	 * Stop smartrac motion listener.
	 */
	public boolean stop() {
		if (sensorRegistered) {
			accUpdateHandler.removeCallbacks(accRecorderRunnable);
			unregisterSensorListener();
			sensorRegistered = false;
			return true;
		}

		return false;
	}

	private void registerSensorListener() {
		// Get sensors
		linearAccSensor = sensorMngr
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		gravitySensor = sensorMngr.getDefaultSensor(Sensor.TYPE_GRAVITY);
		magSensor = sensorMngr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		sensorMngr.registerListener(this, linearAccSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorMngr.registerListener(this, gravitySensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorMngr.registerListener(this, magSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void unregisterSensorListener() {
		if (gravitySensor != null || magSensor != null
				|| linearAccSensor != null) {
			sensorMngr.unregisterListener(this);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// Log.i(TAG, "get sensor event: " + event.sensor.getType());

		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			linearAccEvent = event;
		} else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			gravity = event.values.clone();
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			geomag = event.values.clone();
		}
	}

	private synchronized void processSensor() {
		if (linearAccEvent != null) {

			linearAcceleration[0] = linearAccEvent.values[0];
			linearAcceleration[1] = linearAccEvent.values[1];
			linearAcceleration[2] = linearAccEvent.values[2];
			linearAcceleration[3] = (float) Math.sqrt(Math.pow(
					linearAcceleration[0], 2)
					+ Math.pow(linearAcceleration[1], 2)
					+ Math.pow(linearAcceleration[2], 2));
		}

		if (gravity != null && geomag != null && last_gravity != gravity) {
			// checks that the rotation matrix is found
			float[] inR = new float[16];
			float[] inR_inverse = new float[16];
			float[] I = new float[16];

			boolean success = SensorManager.getRotationMatrix(inR, I, gravity,
					geomag);

			if (success) {
				Matrix.invertM(inR_inverse, 0, inR, 0);
				Matrix.multiplyMV(trueAcceleration, 0, inR_inverse, 0,
						linearAcceleration, 0);

				last_gravity = gravity;
				trueAcceleration[3] = (float) Math.sqrt(Math.pow(
						trueAcceleration[0], 2)
						+ Math.pow(trueAcceleration[1], 2));
			}
		}

		cachedMotionData.add(new MotionData(new Date(), linearAcceleration,
				trueAcceleration));
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}
}
