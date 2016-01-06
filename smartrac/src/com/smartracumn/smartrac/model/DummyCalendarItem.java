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

/**
 * Dummy calendar item which is used to show on calendar view to indicates item
 * that is not able to interact with user like lost of data.
 * 
 * @author kangx385
 * 
 */
public class DummyCalendarItem extends CalendarItem {

	/**
	 * Initializes a new instance of the DummyCalendarItem class.
	 * 
	 * @param start
	 * @param end
	 * @param description
	 * 
	 */
	public DummyCalendarItem(Date start, Date end, String description) {
		this.id = -1;
		this.description = description;
		this.start = start;
		this.end = end;
	}

	@Override
	public String getDescription() {
		String inProgress = isInProgress ? " in progress..." : "";

		return this.description + inProgress;
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
		return "Lost of Data\n"
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
