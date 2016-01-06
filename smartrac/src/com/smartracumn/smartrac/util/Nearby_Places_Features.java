package com.smartracumn.smartrac.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class Nearby_Places_Features {

	private double latitude;

	private double longitude;

	private String requestUrl;

	private Location curr_place;

	private Location google_place;

	private double radius = 100;

	public Nearby_Places_Features(double latitude, double longitude) {

		this.latitude = latitude;
		this.longitude = longitude;

		curr_place = new Location("Dwelling Location");
		curr_place.setLatitude(latitude);
		curr_place.setLongitude(longitude);
	}

	public Map<String, Double> calculateFeatures() {

		Map<String, Double> categoriesMap = new HashMap<String, Double>();

		Map<String, Double> nearestTagCategoryMap = getNearestTagMap();

		categoriesMap.put("accounting", (double) 0);
		categoriesMap.put("airport", (double) 0);
		categoriesMap.put("amusement_park", (double) 0);
		categoriesMap.put("aquarium", (double) 0);
		categoriesMap.put("art_gallery", (double) 0);
		categoriesMap.put("atm", (double) 0);
		categoriesMap.put("bakery", (double) 0);
		categoriesMap.put("bank", (double) 0);
		categoriesMap.put("bar", (double) 0);
		categoriesMap.put("beauty_salon", (double) 0);

		categoriesMap.put("bicycle_store", (double) 0);
		categoriesMap.put("book_store", (double) 0);
		categoriesMap.put("bowling_alley", (double) 0);
		categoriesMap.put("bus_station", (double) 0);
		categoriesMap.put("cafe", (double) 0);
		categoriesMap.put("campground", (double) 0);
		categoriesMap.put("car_dealer", (double) 0);
		categoriesMap.put("car_rental", (double) 0);
		categoriesMap.put("car_repair", (double) 0);
		categoriesMap.put("car_wash", (double) 0);

		categoriesMap.put("casino", (double) 0);
		categoriesMap.put("cemetery", (double) 0);
		categoriesMap.put("church", (double) 0);
		categoriesMap.put("city_hall", (double) 0);
		categoriesMap.put("clothing_store", (double) 0);
		categoriesMap.put("convenience_store", (double) 0);
		categoriesMap.put("courthouse", (double) 0);
		categoriesMap.put("dentist", (double) 0);
		categoriesMap.put("department_store", (double) 0);
		categoriesMap.put("doctor", (double) 0);

		categoriesMap.put("electrician", (double) 0);
		categoriesMap.put("electronics_store", (double) 0);

		categoriesMap.put("embassy", (double) 0);
		categoriesMap.put("establishment", (double) 0);
		categoriesMap.put("finance", (double) 0);
		categoriesMap.put("fire_station", (double) 0);
		categoriesMap.put("florist", (double) 0);
		categoriesMap.put("food", (double) 0);
		categoriesMap.put("funeral_home", (double) 0);
		categoriesMap.put("furniture_store", (double) 0);
		categoriesMap.put("gas_station", (double) 0);
		categoriesMap.put("general_contractor", (double) 0);

		categoriesMap.put("grocery_or_supermarket", (double) 0);
		categoriesMap.put("gym", (double) 0);
		categoriesMap.put("hair_care", (double) 0);
		categoriesMap.put("hardware_store", (double) 0);
		categoriesMap.put("health", (double) 0);
		categoriesMap.put("hindu_temple", (double) 0);
		categoriesMap.put("home_goods_store", (double) 0);
		categoriesMap.put("hospital", (double) 0);
		categoriesMap.put("insurance_agency", (double) 0);
		categoriesMap.put("jewelry_store", (double) 0);

		categoriesMap.put("laundry", (double) 0);
		categoriesMap.put("lawyer", (double) 0);
		categoriesMap.put("library", (double) 0);
		categoriesMap.put("liquor_store", (double) 0);
		categoriesMap.put("local_government_office", (double) 0);
		categoriesMap.put("locksmith", (double) 0);
		categoriesMap.put("lodging", (double) 0);
		categoriesMap.put("meal_delivery", (double) 0);
		categoriesMap.put("meal_takeaway", (double) 0);
		categoriesMap.put("mosque", (double) 0);

		categoriesMap.put("movie_rental", (double) 0);
		categoriesMap.put("movie_theater", (double) 0);

		categoriesMap.put("moving_company", (double) 0);
		categoriesMap.put("museum", (double) 0);
		categoriesMap.put("night_club", (double) 0);
		categoriesMap.put("painter", (double) 0);
		categoriesMap.put("park", (double) 0);
		categoriesMap.put("parking", (double) 0);
		categoriesMap.put("pet_store", (double) 0);
		categoriesMap.put("pharmacy", (double) 0);
		categoriesMap.put("physiotherapist", (double) 0);
		categoriesMap.put("place_of_worship", (double) 0);

		categoriesMap.put("plumber", (double) 0);
		categoriesMap.put("police", (double) 0);
		categoriesMap.put("post_office", (double) 0);
		categoriesMap.put("real_estate_agency", (double) 0);
		categoriesMap.put("restaurant", (double) 0);
		categoriesMap.put("roofing_contractor", (double) 0);
		categoriesMap.put("rv_park", (double) 0);
		categoriesMap.put("school", (double) 0);
		categoriesMap.put("shoe_store", (double) 0);
		categoriesMap.put("shopping_mall", (double) 0);

		categoriesMap.put("spa", (double) 0);
		categoriesMap.put("stadium", (double) 0);
		categoriesMap.put("storage", (double) 0);
		categoriesMap.put("store", (double) 0);
		categoriesMap.put("subway_station", (double) 0);
		categoriesMap.put("synagogue", (double) 0);
		categoriesMap.put("taxi_stand", (double) 0);
		categoriesMap.put("train_station", (double) 0);
		categoriesMap.put("travel_agency", (double) 0);
		categoriesMap.put("university", (double) 0);

		categoriesMap.put("veterinary_care", (double) 0);
		categoriesMap.put("zoo", (double) 0);

		requestUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
				+ latitude
				+ ","
				+ longitude
				+ "&radius="
				+ radius
				+ "&sensor=true&hasNextPage=true&nextPage()=true&key=AIzaSyDfWhJjY1TL94oHdSblQVP-GBQvMOCtM-Y";

		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet(requestUrl);
			HttpResponse res = client.execute(req);

			HttpEntity jsonentity = res.getEntity();
			InputStream in = jsonentity.getContent();
			String s = convertStreamToString(in);
			JSONObject jsonobj = new JSONObject(s);

			JSONArray resarray = jsonobj.getJSONArray("results");

			int total_cats = 0;

			double nearest_distance = radius;

			JSONArray nearest_cats = null;

			if (resarray.length() > 0) {
				int len = resarray.length();

				for (int j = 0; j < len; j++) {

					JSONArray arr = resarray.getJSONObject(j).getJSONArray(
							"types");

					double place_lat = Double.parseDouble(resarray
							.getJSONObject(j).getJSONObject("geometry")
							.getJSONObject("location").getString("lat"));
					double place_long = Double.parseDouble(resarray
							.getJSONObject(j).getJSONObject("geometry")
							.getJSONObject("location").getString("lng"));

					google_place = new Location("Location from Google API");
					google_place.setLatitude(place_lat);
					google_place.setLongitude(place_long);

					double distance = curr_place.distanceTo(google_place);

					if (arr != null && distance <= radius) {

						if (distance <= nearest_distance) {
							nearest_distance = distance;
							nearest_cats = arr;
						}

						total_cats = total_cats + arr.length();

						for (int i = 0; i < arr.length(); i++) {
							String cat = arr.get(i).toString();
							if (categoriesMap.get(cat) != null) {
								double count = categoriesMap.get(cat);
								count++;
								categoriesMap.put(cat, count);
							}
						}
					}
				}

				for (int i = 0; nearest_cats != null
						&& i < nearest_cats.length(); i++) {
					String cat = nearest_cats.get(i).toString();
					String nearest_cat = "nearesttag." + cat;
					if (!cat.equals("establishment"))
						nearestTagCategoryMap.put(nearest_cat, (double) 1);
				}

				double num_of_establishment = categoriesMap
						.get("establishment");

				Iterator it = categoriesMap.entrySet().iterator();
				while (it.hasNext()) {
					@SuppressWarnings("unchecked")
					Map.Entry<String, Double> pairs = (Map.Entry<String, Double>) it
							.next();
					double count = pairs.getValue();
					double value_cat = count / total_cats;
					categoriesMap.put(pairs.getKey(), value_cat);
				}
				categoriesMap.put("numestablishment", num_of_establishment);

				if (total_cats == 0)
					categoriesMap.put("notag", (double) 1);
				else
					categoriesMap.put("notag", (double) 0);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		categoriesMap.putAll(nearestTagCategoryMap);

		return categoriesMap;
	}

	public static String convertStreamToString(InputStream in) {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder jsonstr = new StringBuilder();
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String t = line + "\n";
				jsonstr.append(t);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonstr.toString();
	}

	private Map<String, Double> getNearestTagMap() {

		Map<String, Double> nearestTagCategoryMap = new HashMap<String, Double>();
		nearestTagCategoryMap.put("nearesttag.accounting", (double) 0);
		nearestTagCategoryMap.put("nearesttag.airport", (double) 0);
		nearestTagCategoryMap.put("nearesttag.amusement_park", (double) 0);
		nearestTagCategoryMap.put("nearesttag.aquarium", (double) 0);
		nearestTagCategoryMap.put("nearesttag.art_gallery", (double) 0);
		nearestTagCategoryMap.put("nearesttag.atm", (double) 0);
		nearestTagCategoryMap.put("nearesttag.bakery", (double) 0);
		nearestTagCategoryMap.put("nearesttag.bank", (double) 0);
		nearestTagCategoryMap.put("nearesttag.bar", (double) 0);
		nearestTagCategoryMap.put("nearesttag.beauty_salon", (double) 0);

		nearestTagCategoryMap.put("nearesttag.bicycle_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.book_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.bowling_alley", (double) 0);
		nearestTagCategoryMap.put("nearesttag.bus_station", (double) 0);
		nearestTagCategoryMap.put("nearesttag.cafe", (double) 0);
		nearestTagCategoryMap.put("nearesttag.campground", (double) 0);
		nearestTagCategoryMap.put("nearesttag.car_dealer", (double) 0);
		nearestTagCategoryMap.put("nearesttag.car_rental", (double) 0);
		nearestTagCategoryMap.put("nearesttag.car_repair", (double) 0);
		nearestTagCategoryMap.put("nearesttag.car_wash", (double) 0);

		nearestTagCategoryMap.put("nearesttag.casino", (double) 0);
		nearestTagCategoryMap.put("nearesttag.cemetery", (double) 0);
		nearestTagCategoryMap.put("nearesttag.church", (double) 0);
		nearestTagCategoryMap.put("nearesttag.city_hall", (double) 0);
		nearestTagCategoryMap.put("nearesttag.clothing_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.convenience_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.courthouse", (double) 0);
		nearestTagCategoryMap.put("nearesttag.dentist", (double) 0);
		nearestTagCategoryMap.put("nearesttag.department_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.doctor", (double) 0);

		nearestTagCategoryMap.put("nearesttag.electrician", (double) 0);
		nearestTagCategoryMap.put("nearesttag.electronics_store", (double) 0);

		nearestTagCategoryMap.put("nearesttag.embassy", (double) 0);
		nearestTagCategoryMap.put("nearesttag.establishment", (double) 0);
		nearestTagCategoryMap.put("nearesttag.finance", (double) 0);
		nearestTagCategoryMap.put("nearesttag.fire_station", (double) 0);
		nearestTagCategoryMap.put("nearesttag.florist", (double) 0);
		nearestTagCategoryMap.put("nearesttag.food", (double) 0);
		nearestTagCategoryMap.put("nearesttag.funeral_home", (double) 0);
		nearestTagCategoryMap.put("nearesttag.furniture_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.gas_station", (double) 0);
		nearestTagCategoryMap.put("nearesttag.general_contractor", (double) 0);

		nearestTagCategoryMap.put("nearesttag.grocery_or_supermarket",
				(double) 0);
		nearestTagCategoryMap.put("nearesttag.gym", (double) 0);
		nearestTagCategoryMap.put("nearesttag.hair_care", (double) 0);
		nearestTagCategoryMap.put("nearesttag.hardware_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.health", (double) 0);
		nearestTagCategoryMap.put("nearesttag.hindu_temple", (double) 0);
		nearestTagCategoryMap.put("nearesttag.home_goods_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.hospital", (double) 0);
		nearestTagCategoryMap.put("nearesttag.insurance_agency", (double) 0);
		nearestTagCategoryMap.put("nearesttag.jewelry_store", (double) 0);

		nearestTagCategoryMap.put("nearesttag.laundry", (double) 0);
		nearestTagCategoryMap.put("nearesttag.lawyer", (double) 0);
		nearestTagCategoryMap.put("nearesttag.library", (double) 0);
		nearestTagCategoryMap.put("nearesttag.liquor_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.local_government_office",
				(double) 0);
		nearestTagCategoryMap.put("nearesttag.locksmith", (double) 0);
		nearestTagCategoryMap.put("nearesttag.lodging", (double) 0);
		nearestTagCategoryMap.put("nearesttag.meal_delivery", (double) 0);
		nearestTagCategoryMap.put("nearesttag.meal_takeaway", (double) 0);
		nearestTagCategoryMap.put("nearesttag.mosque", (double) 0);

		nearestTagCategoryMap.put("nearesttag.movie_rental", (double) 0);
		nearestTagCategoryMap.put("nearesttag.movie_theater", (double) 0);

		nearestTagCategoryMap.put("nearesttag.moving_company", (double) 0);
		nearestTagCategoryMap.put("nearesttag.museum", (double) 0);
		nearestTagCategoryMap.put("nearesttag.night_club", (double) 0);
		nearestTagCategoryMap.put("nearesttag.painter", (double) 0);
		nearestTagCategoryMap.put("nearesttag.park", (double) 0);
		nearestTagCategoryMap.put("nearesttag.parking", (double) 0);
		nearestTagCategoryMap.put("nearesttag.pet_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.pharmacy", (double) 0);
		nearestTagCategoryMap.put("nearesttag.physiotherapist", (double) 0);
		nearestTagCategoryMap.put("nearesttag.place_of_worship", (double) 0);

		nearestTagCategoryMap.put("nearesttag.plumber", (double) 0);
		nearestTagCategoryMap.put("nearesttag.police", (double) 0);
		nearestTagCategoryMap.put("nearesttag.post_office", (double) 0);
		nearestTagCategoryMap.put("nearesttag.real_estate_agency", (double) 0);
		nearestTagCategoryMap.put("nearesttag.restaurant", (double) 0);
		nearestTagCategoryMap.put("nearesttag.roofing_contractor", (double) 0);
		nearestTagCategoryMap.put("nearesttag.rv_park", (double) 0);
		nearestTagCategoryMap.put("nearesttag.school", (double) 0);
		nearestTagCategoryMap.put("nearesttag.shoe_store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.shopping_mall", (double) 0);

		nearestTagCategoryMap.put("nearesttag.spa", (double) 0);
		nearestTagCategoryMap.put("nearesttag.stadium", (double) 0);
		nearestTagCategoryMap.put("nearesttag.storage", (double) 0);
		nearestTagCategoryMap.put("nearesttag.store", (double) 0);
		nearestTagCategoryMap.put("nearesttag.subway_station", (double) 0);
		nearestTagCategoryMap.put("nearesttag.synagogue", (double) 0);
		nearestTagCategoryMap.put("nearesttag.taxi_stand", (double) 0);
		nearestTagCategoryMap.put("nearesttag.train_station", (double) 0);
		nearestTagCategoryMap.put("nearesttag.travel_agency", (double) 0);
		nearestTagCategoryMap.put("nearesttag.university", (double) 0);

		nearestTagCategoryMap.put("nearesttag.veterinary_care", (double) 0);
		nearestTagCategoryMap.put("nearesttag.zoo", (double) 0);
		return nearestTagCategoryMap;

	}

}
