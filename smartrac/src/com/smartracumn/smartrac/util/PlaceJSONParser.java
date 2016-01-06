package com.smartracumn.smartrac.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The place JSON Parser.
 * 
 * @author kangx385
 * 
 */
public class PlaceJSONParser {

	/**
	 * Receives JSON Object and return a list containing parsed data.
	 * 
	 * @param jObject
	 *            The JSONObject.
	 * @return A list of HashMap containing property name and value pairs.
	 */
	public List<HashMap<String, String>> parse(JSONObject jObject) {

		JSONArray jPlaces = null;
		try {
			/** Retrieves all the elements in the 'places' array */
			jPlaces = jObject.getJSONArray("predictions");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		/**
		 * Invoking getPlaces with the array of json object where each json
		 * object represent a place
		 */
		return getPlaces(jPlaces);
	}

	/**
	 * Get list of places from JSON place object.
	 * 
	 * @param jPlaces
	 *            The JSON Places object.
	 * @return Return a list of property name value pairs.
	 */
	private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
		int placesCount = jPlaces.length();
		List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> place = null;

		/** Taking each place, parses and adds to list object */
		for (int i = 0; i < placesCount; i++) {
			try {
				/** Call getPlace with place JSON object to parse the place */
				place = getPlace((JSONObject) jPlaces.get(i));
				placesList.add(place);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return placesList;
	}

	/**
	 * Get place from JSON Object.
	 * 
	 * @param jPlace
	 *            The JSON Place Object.
	 * @return Return property name and value pairs for suggestion.
	 */
	private HashMap<String, String> getPlace(JSONObject jPlace) {

		HashMap<String, String> place = new HashMap<String, String>();

		String id = "";
		String reference = "";
		String description = "";

		try {

			description = jPlace.getString("description");
			id = jPlace.getString("id");
			reference = jPlace.getString("reference");

			place.put("description", description);
			place.put("_id", id);
			place.put("reference", reference);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return place;
	}
}
