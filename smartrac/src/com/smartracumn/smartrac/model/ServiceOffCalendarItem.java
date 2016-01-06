package com.smartracumn.smartrac.model;

import java.util.Date;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.smartracumn.smartrac.util.SmartracDataFormat;

public class ServiceOffCalendarItem extends CalendarItem {
	/**
	 * Initializes a new instance of the NoDataCalendarItem class.
	 * 
	 * @param start
	 * @param end
	 * @param description
	 * 
	 */
	public ServiceOffCalendarItem(long id, Date start, Date end,
			CalendarItem.Type type) {
		this.id = id;
		this.type = type;
		this.start = start;
		this.end = end;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String getDescription() {
		String tail = isInProgress ? " in progress..." : "";

		return this.type.toString() + tail;
	}

	@Override
	public String getPolyCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LatLngBounds drawMap(GoogleMap map, boolean highlighted) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CalendarItem addMarker(GoogleMap map, MarkerOptions marker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(LatLng point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addLocations(List<LocationWrapper> locations,
			boolean insertToHead) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeLocations(List<LocationWrapper> locations,
			boolean removeFromHead) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "No service\n"
				+ SmartracDataFormat.getTimeFormat().format(this.getStart())
				+ " - "
				+ SmartracDataFormat.getTimeFormat().format(this.getEnd());
	}

	@Override
	public List<LocationWrapper> getLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends ClusterItem> void setClusterManager(
			ClusterManager<T> clusterManager) {
		// TODO Auto-generated method stub

	}
}