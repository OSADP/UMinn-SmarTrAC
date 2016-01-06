package com.smartracumn.smartrac.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class GetDayFeature {

	private Map<String, Double> days_map = new HashMap<String, Double>();

	private Context context;

	private JSONObject jsonobj;

	public GetDayFeature(Context c) {
		this.context = c;
	}

	public Map<String, Double> getDayofWeek(Map<String, Double> days_map) {

		this.days_map = days_map;
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK);

		switch (day) {
		case Calendar.MONDAY:
			days_map.put("Monday", (double) 1);
			break;
		case Calendar.TUESDAY:
			days_map.put("Tuesday", (double) 1);
			break;
		case Calendar.WEDNESDAY:
			days_map.put("Wednesday", (double) 1);
			break;
		case Calendar.THURSDAY:
			days_map.put("Thursday", (double) 1);
			break;
		case Calendar.FRIDAY:
			days_map.put("Friday", (double) 1);
			break;
		default:
			break;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String currentDateandTime = sdf.format(new Date());

		isHoliday(currentDateandTime);

		return days_map;

	}

	private void isHoliday(String currentDateandTime) {
		final String requesturl = "http://kayaposoft.com/enrico/json/v1.0/?action=isPublicHoliday&date="
				+ currentDateandTime + "&country=usa";

		try {
			String s;
			HttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet(requesturl);
			HttpResponse res;

			res = client.execute(req);
			HttpEntity jsonentity = res.getEntity();
			InputStream in = jsonentity.getContent();
			s = convertStreamToString(in);
			jsonobj = new JSONObject(s);

			boolean isHoliday = jsonobj.getBoolean("isPublicHoliday");
			if (isHoliday)
				days_map.put("holiday", (double) 1);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String convertStreamToString(InputStream in) {
		// TODO Auto-generated method stub
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

}
