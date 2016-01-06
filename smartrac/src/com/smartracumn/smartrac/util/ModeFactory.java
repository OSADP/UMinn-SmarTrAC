package com.smartracumn.smartrac.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.util.Log;

import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.PredictionTreeNode;

/**
 * Mode factory used to generate travel mode based on given long and short
 * feature buffers.
 * 
 * @author kangx385
 * 
 */
public class ModeFactory {
	private final String TAG = getClass().getName();

	private static ModeFactory modeFactory;

	private List<Map<Integer, PredictionTreeNode<CalendarItem.TravelMode>>> nodeDicts;

	private boolean forestReady;

	private SummaryBuffer<CalendarItem.TravelMode> treePredictions;

	private static final String forestDirectory = "mode_random_forest";

	private Context context;

	private ModeFactory(Context context) {
		this.context = context;
		initStringModeMap();
		handler.post(loadForestRunnable);
	}

	/**
	 * Get a instance of mode factory. Use lazy initialization and singleton
	 * since it is very costly to create a instance of mode factory.
	 * 
	 * @param context
	 *            The application context.
	 * @return Instance of mode factory.
	 */
	public static ModeFactory getInstance(Context context) {
		if (modeFactory == null) {
			modeFactory = new ModeFactory(context);
		}

		return modeFactory;
	}

	private Map<String, CalendarItem.TravelMode> stringModeMap = new HashMap<String, CalendarItem.TravelMode>();

	private void initStringModeMap() {
		stringModeMap.put("\"Bicycle\"", CalendarItem.TravelMode.BIKE);
		stringModeMap.put("\"Bike\"", CalendarItem.TravelMode.BIKE);
		stringModeMap.put("\"Bus\"", CalendarItem.TravelMode.BUS);
		stringModeMap.put("\"Rail\"", CalendarItem.TravelMode.RAIL);
		stringModeMap.put("\"Car\"", CalendarItem.TravelMode.CAR);
		stringModeMap.put("\"Wait\"", CalendarItem.TravelMode.WAIT);
		stringModeMap.put("\"Walk\"", CalendarItem.TravelMode.WALKING);
	}

	private Map<Integer, PredictionTreeNode<CalendarItem.TravelMode>> genNodeDict(
			List<String> lines, Random random) {
		Map<Integer, PredictionTreeNode<CalendarItem.TravelMode>> nodeDict = new HashMap<Integer, PredictionTreeNode<CalendarItem.TravelMode>>();

		for (int i = 1; i < lines.size(); i++) {
			String[] values = lines.get(i).split(",");

			int id = i;
			int leftId = Integer.valueOf(values[0]);
			int rightId = Integer.valueOf(values[1]);
			String splitVar = values[2];
			double splitPoint = Double.valueOf(values[3]);
			int status = Integer.valueOf(values[4]);
			CalendarItem.TravelMode prediction = CalendarItem.TravelMode.UNKNOWN_TRAVEL_MODE;

			if (stringModeMap.containsKey(values[5])) {
				prediction = stringModeMap.get(values[5]);
			} else if (!values[5].equals("NA")) {
				Log.i(TAG, lines.get(i));
			}

			PredictionTreeNode<CalendarItem.TravelMode> node = new PredictionTreeNode<CalendarItem.TravelMode>(
					id, leftId, rightId, splitVar, splitPoint, status,
					prediction, random);

			nodeDict.put(id, node);
		}

		return nodeDict;
	}

	private Handler handler = new Handler();

	private Runnable loadForestRunnable = new Runnable() {

		@Override
		public void run() {
			// Load random forest from raw file.
			nodeDicts = new ArrayList<Map<Integer, PredictionTreeNode<CalendarItem.TravelMode>>>();
			Random randomGenerator = new Random();

			AssetManager am = context.getAssets();
			try {
				String[] getTrees = am.list(forestDirectory);
				treePredictions = new SummaryBuffer<CalendarItem.TravelMode>(
						getTrees.length);
				for (String tree : getTrees) {
					InputStream inputStream = am.open(forestDirectory + "/"
							+ tree);

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(inputStream));
					List<String> lines = new ArrayList<String>();
					String line;
					try {
						while ((line = reader.readLine()) != null) {
							lines.add(line);
							// Log.i(TAG, line);
						}

						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					nodeDicts.add(genNodeDict(lines, randomGenerator));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			forestReady = true;
			// for (Map<Integer, PredictionTreeNode<CalendarItem.TravelMode>>
			// nodeDict : nodeDicts) {
			// Log.i(getClass().getSimpleName(), nodeDict.toString());
			// }
		}
	};

	private Map<String, Double> getFeaturesMap(
			SpeedFeatureBuffer speedBuffer30,
			SpeedFeatureBuffer speedBuffer120,
			MotionFeatureBuffer motionBuffer30,
			MotionFeatureBuffer motionBuffer120) {
		Map<String, Double> featuresMap = new HashMap<String, Double>();

		featuresMap.putAll(speedBuffer120.getFeaturesMap());
		featuresMap.putAll(speedBuffer30.getFeaturesMap());
		featuresMap.putAll(motionBuffer30.getFeaturesMap());
		featuresMap.putAll(motionBuffer120.getFeaturesMap());

		// Log.i("ModeDetector", featuresMap.toString());

		return featuresMap;
	}

	private CalendarItem.TravelMode predictModeByTree(
			Map<String, Double> features,
			Map<Integer, PredictionTreeNode<CalendarItem.TravelMode>> nodeDict) {
		PredictionTreeNode<CalendarItem.TravelMode> currentNode = nodeDict
				.get(1);

		int nextId = 1;

		while (!currentNode.isLeaf()) {
			nextId = currentNode.next(features);
			currentNode = nodeDict.get(nextId);
		}

		return currentNode.getPrediction();
	}

	/**
	 * Predict mode based on given feature buffers.
	 * 
	 * @param speedBuffer30
	 * @param speedBuffer120
	 * @param motionBuffer30
	 * @param motionBuffer120
	 * @return Mode prediction.
	 */
	public CalendarItem.TravelMode predictMode(
			SpeedFeatureBuffer speedBuffer30,
			SpeedFeatureBuffer speedBuffer120,
			MotionFeatureBuffer motionBuffer30,
			MotionFeatureBuffer motionBuffer120) {
		if (!forestReady) {
			return CalendarItem.TravelMode.UNKNOWN_TRAVEL_MODE;
		}

		Map<String, Double> features = getFeaturesMap(speedBuffer30,
				speedBuffer120, motionBuffer30, motionBuffer120);

		// Log.i(TAG, features.toString());

		for (Map<Integer, PredictionTreeNode<CalendarItem.TravelMode>> nodeDict : nodeDicts) {
			this.treePredictions.enqueue(predictModeByTree(features, nodeDict));
		}

		return treePredictions.getMostFrequentEntity();
	}
}
