package com.smartracumn.smartrac.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.location.Location;
import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.smartracumn.smartrac.data.ActivityFeaturesDataSource;
import com.smartracumn.smartrac.data.CalendarItemDataSource;
import com.smartracumn.smartrac.model.ActivityCalendarItem;
import com.smartracumn.smartrac.model.CalendarItem;
import com.smartracumn.smartrac.model.CalendarItem.Activity;
import com.smartracumn.smartrac.model.CalendarItem.TravelMode;
import com.smartracumn.smartrac.model.PredictionTreeNode;
import com.smartracumn.smartrac.model.TripCalendarItem;

/**
 * Activity factory loads the forest and predicts the activity.
 * 
 * @author khand041
 * 
 */
public class ActivityFactory {

	private final String TAG = getClass().getName();

	private static ActivityFactory activityFactory;

	private List<Map<Integer, PredictionTreeNode<CalendarItem.Activity>>> nodeDicts;

	private boolean forestReady;

	private SummaryBuffer<CalendarItem.Activity> treePredictions;

	private static final String forestDirectory = "activity_random_forest";

	private CalendarItemDataSource calendarItemDataSource;

	private Context context;

	private ActivityCalendarItem item;

	private Map<String, Double> nearbyPlacesFeatures = new HashMap<String, Double>();

	private Map<String, Double> dayHolidayFeatures = new HashMap<String, Double>();
	
	private ActivityFeaturesDataSource activityFeaturesDataSource;

	private ActivityFactory(Context context) {
		this.context = context;
		initstringActivityMap();
		handler.post(loadForestRunnable);
		this.calendarItemDataSource = new CalendarItemDataSource(context);
		activityFeaturesDataSource = new ActivityFeaturesDataSource(context);
	}

	/**
	 * Get a instance of mode factory. Use lazy initialization and singleton
	 * since it is very costly to create a instance of mode factory.
	 * 
	 * @param context
	 *            The application context.
	 * @return Instance of mode factory.
	 */
	public static ActivityFactory getInstance(Context context) {
		if (activityFactory == null) {
			activityFactory = new ActivityFactory(context);
		}

		return activityFactory;
	}

	private Map<String, CalendarItem.Activity> stringActivityMap = new HashMap<String, CalendarItem.Activity>();

	private void initstringActivityMap() {
		stringActivityMap.put("\"Shopping\"", CalendarItem.Activity.SHOPPING);
		stringActivityMap.put("\"Home\"", CalendarItem.Activity.HOME);
		stringActivityMap.put("\"Other personal business\"",
				CalendarItem.Activity.OTHER_PERSONAL_BUSINESS);
		stringActivityMap.put("\"Work\"", CalendarItem.Activity.WORK);
		stringActivityMap.put("\"Soc/Rec/Comm\"",
				CalendarItem.Activity.SOCIAL_RECREATION_COMMUNITY);
		stringActivityMap.put("\"Eat out\"", CalendarItem.Activity.EAT_OUT);
		stringActivityMap.put("\"Education\"", CalendarItem.Activity.EDUCATION);
	}

	private Map<Integer, PredictionTreeNode<CalendarItem.Activity>> genNodeDict(
			List<String> lines, Random random) {
		Map<Integer, PredictionTreeNode<CalendarItem.Activity>> nodeDict = new HashMap<Integer, PredictionTreeNode<CalendarItem.Activity>>();

		for (int i = 1; i < lines.size(); i++) {
			String[] values = lines.get(i).split(",");

			int id = i;
			int leftId = Integer.valueOf(values[0]);
			int rightId = Integer.valueOf(values[1]);
			String splitVar = values[2];
			double splitPoint = Double.valueOf(values[3]);
			int status = Integer.valueOf(values[4]);
			CalendarItem.Activity prediction = CalendarItem.Activity.UNKNOWN_ACTIVITY;
			
			if (stringActivityMap.containsKey(values[5])) {
				prediction = stringActivityMap.get(values[5]);
			} else if (!values[5].equals("\"NA\"")) {
				// Log.i(TAG, lines.get(i));
			}

			PredictionTreeNode<CalendarItem.Activity> node = new PredictionTreeNode<CalendarItem.Activity>(
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
			nodeDicts = new ArrayList<Map<Integer, PredictionTreeNode<CalendarItem.Activity>>>();
			Random randomGenerator = new Random();

			AssetManager am = context.getAssets();
			try {
				String[] getTrees = am.list(forestDirectory);
				treePredictions = new SummaryBuffer<CalendarItem.Activity>(
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
		}
	};

	public TreeMap<String, Integer> predictActivity(ActivityCalendarItem item) {

		this.item = item;

		if (!forestReady) {
			return null;
		}

		// Getting Nearby features
		LatLng center = item.getWeightedCenter();
		double lat = center.latitude;
		double lng = center.longitude;
		Nearby_Places_Features obj = new Nearby_Places_Features(lat, lng);
		nearbyPlacesFeatures = obj.calculateFeatures();

		// Getting Holiday features
		GetDayFeature getDayFeature = new GetDayFeature(context);
		Map<String, Double> dayHolidayFeatures = new HashMap<String, Double>();

		dayHolidayFeatures.put("Monday", (double) 0);
		dayHolidayFeatures.put("Tuesday", (double) 0);
		dayHolidayFeatures.put("Wednesday", (double) 0);
		dayHolidayFeatures.put("Thursday", (double) 0);
		dayHolidayFeatures.put("Friday", (double) 0);
		dayHolidayFeatures.put("holiday", (double) 0);

		dayHolidayFeatures = getDayFeature.getDayofWeek(dayHolidayFeatures);

		// Predict the activity
		Map<String, Double> features = new HashMap<String, Double>();

		Map<String, Double> dwellingDBFeatures = new HashMap<String, Double>();
		dwellingDBFeatures = getdwellingDBFeatures(item);

		features.putAll(dayHolidayFeatures);
		features.putAll(nearbyPlacesFeatures);
		features.putAll(dwellingDBFeatures);

		// Tags for Student and WorkPlace
		SharedPreferences sharedPref = context.getSharedPreferences(
				"com.smartracumn.smartrac", Context.MODE_PRIVATE);

		boolean isStudent = sharedPref.getBoolean("Student", false);
		if (isStudent)
			features.put("currstudent", (double) 1);
		else
			features.put("currstudent", (double) 0);

		boolean isWorker = sharedPref.getBoolean("WorkPlace", false);

		if (isWorker)
			features.put("worker", (double) 1);
		else
			features.put("worker", (double) 0);

		
		activityFeaturesDataSource.writeActivityPredictionDB(features,item);

		String quote_string = "\"";

		Map<String, Double> features_in_quotes = new HashMap<String, Double>();

	    Iterator<?> it = features.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String,Double> pairs = (Map.Entry<String,Double>)it.next();
	        String key = quote_string+(String) pairs.getKey()+quote_string;
	        double value = (Double) pairs.getValue();
	        it.remove(); // avoids a ConcurrentModificationException
	        features_in_quotes.put(key, value);
	    }

		// Sorting predicted Activities
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		ValueComparator bvc = new ValueComparator(map);
		TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);

		map.put(CalendarItem.Activity.SHOPPING.toString(), 0);
		map.put(CalendarItem.Activity.HOME.toString(), 0);
		map.put(CalendarItem.Activity.OTHER_PERSONAL_BUSINESS.toString(), 0);
		map.put(CalendarItem.Activity.WORK.toString(), 0);
		map.put(CalendarItem.Activity.SOCIAL_RECREATION_COMMUNITY.toString(), 0);
		map.put(CalendarItem.Activity.EAT_OUT.toString(), 0);
		map.put(CalendarItem.Activity.EDUCATION.toString(), 0);

		for (Map<Integer, PredictionTreeNode<CalendarItem.Activity>> nodeDict : nodeDicts) {

			CalendarItem.Activity activity = predictActivityByTree(features_in_quotes,
					nodeDict);
			String str_Activity = activity.toString();
			if (map.get(str_Activity) != null) {
				map.put(str_Activity, (map.get(activity.toString()) + 1));
			}

		}

		sorted_map.putAll(map);
		// ArrayList<String> sorted_Activity = new ArrayList<String>(
		// sorted_map.keySet());

		return sorted_map;

	}

	class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;

		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		// this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}

	private CalendarItem.Activity predictActivityByTree(
			Map<String, Double> features,
			Map<Integer, PredictionTreeNode<CalendarItem.Activity>> nodeDict) {

		PredictionTreeNode<CalendarItem.Activity> currentNode = nodeDict.get(1);

		int nextId = 1;

		while (!currentNode.isLeaf()) {
			nextId = currentNode.next(features);
			currentNode = nodeDict.get(nextId);
		}

		return currentNode.getPrediction();
	}

	private Map<String, Double> getdwellingDBFeatures(ActivityCalendarItem item) {

		Map<String, Double> dbActivityFeatures = new HashMap<String, Double>();

		dbActivityFeatures.put("Car", (double) 0);
		dbActivityFeatures.put("Bus", (double) 0);
		dbActivityFeatures.put("Rail", (double) 0);
		dbActivityFeatures.put("Bike", (double) 0);
		dbActivityFeatures.put("Walk", (double) 0);

		TripCalendarItem prevTripitem = calendarItemDataSource.getLastTrip(item
				.getId());
		if (prevTripitem != null) {
			TravelMode prevTravelMode = prevTripitem.getMode();
			switch (prevTravelMode) {
			case WALKING:
				dbActivityFeatures.put("Walk", (double) 1);
				break;
			case BUS:
				dbActivityFeatures.put("Bus", (double) 1);
				break;
			case BIKE:
				dbActivityFeatures.put("Bike", (double) 1);
				break;
			case RAIL:
				dbActivityFeatures.put("Rail", (double) 1);
				break;
			case CAR:
				dbActivityFeatures.put("Car", (double) 1);
				break;
			default:
				dbActivityFeatures.put("Unknown", (double) 1);
				break;
			}
		}
		// Activity Duration
		long diffInMillies = item.getEnd().getTime()
				- item.getStart().getTime();
		TimeUnit timeUnit = TimeUnit.MINUTES;
		double duration = timeUnit.convert(diffInMillies, TimeUnit.MINUTES);
		dbActivityFeatures.put("actdur", duration);

		// Arrival Time
		item.getStart().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		String arrTime = sdf.format(item.getStart());
		dbActivityFeatures.put("arrival_time", Double.parseDouble(arrTime));

		// Airline Distance
		LatLng activityCenter = ((ActivityCalendarItem) item)
				.getWeightedCenter();
		ActivityCalendarItem lastActivityItem = calendarItemDataSource
				.getLastActivity(item.getId());
		if (lastActivityItem != null) {
			LatLng lastActivityCenter = lastActivityItem.getWeightedCenter();

			Location locA = new Location("Location A");
			locA.setLatitude(activityCenter.latitude);
			locA.setLongitude(activityCenter.longitude);

			Location locB = new Location("Location B");
			locB.setLatitude(lastActivityCenter.latitude);
			locB.setLongitude(lastActivityCenter.longitude);

			double air_distance = locA.distanceTo(locB);
			dbActivityFeatures.put("airline_dist", air_distance);

			dbActivityFeatures.put("prev_purpose_home", (double) 0);
			dbActivityFeatures.put("prev_purpose_work", (double) 0);

			// Previous Purpose
			Activity activity = lastActivityItem.getActivity();
			if (activity == Activity.WORK) {
				dbActivityFeatures.put("prev_purpose_work", (double) 1);
			} else if (activity == Activity.HOME) {
				dbActivityFeatures.put("prev_purpose_home", (double) 1);
			}

		}

		return dbActivityFeatures;
	}
}
