package com.smartracumn.smartrac.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The place details JSON parser.
 * 
 * @author kangx385
 * 
 */
public class PlaceDetailsJSONParser {

	/**
	 * Receives JSON Object and return a list containing parsed data.
	 * 
	 * @param jObject
	 *            The JSONObject.
	 * @return A list of HashMap containing property name and value pairs.
	 */
	public List<HashMap<String, String>> parse(JSONObject jObject) {

		Double lat = Double.valueOf(0);
		Double lng = Double.valueOf(0);
		String formattedAddress = "";

		HashMap<String, String> hm = new HashMap<String, String>();
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		try {
			lat = (Double) jObject.getJSONObject("result")
					.getJSONObject("geometry").getJSONObject("location")
					.get("lat");
			lng = (Double) jObject.getJSONObject("result")
					.getJSONObject("geometry").getJSONObject("location")
					.get("lng");
			formattedAddress = (String) jObject.getJSONObject("result").get(
					"formatted_address");

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		hm.put("lat", Double.toString(lat));
		hm.put("lng", Double.toString(lng));
		hm.put("formatted_address", formattedAddress);

		list.add(hm);

		return list;
	}
}