package com.smartracumn.smartrac.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.smartracumn.smartrac.model.LocationWrapper;

/**
 * Speed feature buffer is used to hold features calculated based on non-zero
 * speed from list of location data.
 * 
 * @author kangx385
 * 
 */
public class SpeedFeatureBuffer {
	private int assignedBufferSize;
	private double meannon;
	private double varnon;
	private double mediannon;
	private double qutwenon;
	private double queignon;
	private double entnon;
	private double kurtnon;
	private double skewnon;
	private double iqrnon;
	private double minnon;
	private double maxnon;
	private double auto_corrnon;

	private double meansp;
	private double mediansp;
	private double varsp;
	private double minsp;
	private double maxsp;
	private double skewsp;
	private double entsp;
	private double qutwesp;
	private double queigsp;
	private double iqrsp;

	private double meanseqsp;
	private double medianseqsp;
	private double varseqsp;
	private double minseqsp;
	private double maxseqsp;
	private double skewseqsp;
	private double entseqsp;
	private double qutweseqsp;
	private double queigseqsp;
	private double iqrseqsp;

	private Queue<Double> speednon = new LinkedList<Double>();
	private LinkedList<Double> speedQ = new LinkedList<Double>();
	private Queue<Double> seqSpeedQ = new LinkedList<Double>();
	private Queue<List<LocationWrapper>> locationLists = new LinkedList<List<LocationWrapper>>();

	private String featurePostfix;
	private String featurePrefix = "\"";

	/**
	 * Initializes a new instance of the SpeedFeatureBuffer class.
	 * 
	 * @param bufferSize
	 *            The buffer size.
	 */
	public SpeedFeatureBuffer(int bufferSize) {
		this.assignedBufferSize = bufferSize;
		this.featurePostfix = "."
				+ String.valueOf(this.assignedBufferSize * 30) + "\"";
	}

	/**
	 * Clear buffer.
	 */
	public void clear() {
		this.speednon.clear();
		this.speedQ.clear();
		this.seqSpeedQ.clear();
		this.locationLists.clear();
	}

	public Map<String, Double> getFeaturesMap() {
		Map<String, Double> featuresMap = new HashMap<String, Double>();

		featuresMap.put(this.featurePrefix + "meannon" + this.featurePostfix,
				meannon);
		featuresMap.put(this.featurePrefix + "varnon" + this.featurePostfix,
				varnon);
		featuresMap.put(this.featurePrefix + "mediannon" + this.featurePostfix,
				mediannon);
		featuresMap.put(this.featurePrefix + "qutwenon" + this.featurePostfix,
				qutwenon);
		featuresMap.put(this.featurePrefix + "queignon" + this.featurePostfix,
				queignon);
		featuresMap.put(this.featurePrefix + "entnon" + this.featurePostfix,
				entnon);
		featuresMap.put(this.featurePrefix + "kurtnon" + this.featurePostfix,
				kurtnon);
		featuresMap.put(this.featurePrefix + "skewnon" + this.featurePostfix,
				skewnon);
		featuresMap.put(this.featurePrefix + "iqrnon" + this.featurePostfix,
				iqrnon);
		featuresMap.put(this.featurePrefix + "minnon" + this.featurePostfix,
				minnon);
		featuresMap.put(this.featurePrefix + "maxnon" + this.featurePostfix,
				maxnon);
		featuresMap.put(this.featurePrefix + "auto.corrnon"
				+ this.featurePostfix, auto_corrnon);

		featuresMap.put(this.featurePrefix + "meansp" + this.featurePostfix,
				meansp);
		featuresMap.put(this.featurePrefix + "varsp" + this.featurePostfix,
				varsp);
		featuresMap.put(this.featurePrefix + "mediansp" + this.featurePostfix,
				mediansp);
		featuresMap.put(this.featurePrefix + "qutwesp" + this.featurePostfix,
				qutwesp);
		featuresMap.put(this.featurePrefix + "queigsp" + this.featurePostfix,
				queigsp);
		featuresMap.put(this.featurePrefix + "entsp" + this.featurePostfix,
				entsp);
		featuresMap.put(this.featurePrefix + "skewsp" + this.featurePostfix,
				skewsp);
		featuresMap.put(this.featurePrefix + "iqrsp" + this.featurePostfix,
				iqrsp);
		featuresMap.put(this.featurePrefix + "minsp" + this.featurePostfix,
				minsp);
		featuresMap.put(this.featurePrefix + "maxsp" + this.featurePostfix,
				maxsp);

		featuresMap.put(this.featurePrefix + "meanseqsp" + this.featurePostfix,
				meanseqsp);
		featuresMap.put(this.featurePrefix + "varseqsp" + this.featurePostfix,
				varseqsp);
		featuresMap.put(this.featurePrefix + "medianseqsp"
				+ this.featurePostfix, medianseqsp);
		featuresMap.put(
				this.featurePrefix + "qutweseqsp" + this.featurePostfix,
				qutweseqsp);
		featuresMap.put(
				this.featurePrefix + "queigseqsp" + this.featurePostfix,
				queigseqsp);
		featuresMap.put(this.featurePrefix + "entseqsp" + this.featurePostfix,
				entseqsp);
		featuresMap.put(this.featurePrefix + "skewseqsp" + this.featurePostfix,
				skewseqsp);
		featuresMap.put(this.featurePrefix + "iqrseqsp" + this.featurePostfix,
				iqrseqsp);
		featuresMap.put(this.featurePrefix + "minseqsp" + this.featurePostfix,
				minseqsp);
		featuresMap.put(this.featurePrefix + "maxseqsp" + this.featurePostfix,
				maxseqsp);

		// for (String key : featuresMap.keySet()) {
		// if (Double.isNaN(featuresMap.get(key))) {
		// featuresMap.put(key, 0.0);
		// }
		// }

		return featuresMap;
	}

	/**
	 * Enqueue locations to feature buffer.
	 * 
	 * @param locs
	 *            List of location wrapper to be inserted.
	 * @return Locations dequeued from feature buffer if size of current buffer
	 *         exceeds assigned buffer size after insertion.
	 */
	public List<LocationWrapper> enqueue(List<LocationWrapper> locs) {
		this.locationLists.offer(locs);

		for (LocationWrapper loc : locs) {
			float speed = loc.getLocation().getSpeed();
			if (speedQ.size() > 0) {
				seqSpeedQ.add(speed - speedQ.peekLast());
			}
			speedQ.add((double) speed);

			if (speed != 0) {
				speednon.add((double) speed);
			}
		}

		if (this.locationLists.size() > assignedBufferSize) {
			return dequeue();
		} else {
			computeFeatures();
			return null;
		}
	}

	/**
	 * Dequeue locations from feature buffer.
	 * 
	 * @return Locations at the head of feature buffer.
	 */
	public List<LocationWrapper> dequeue() {
		List<LocationWrapper> removed = locationLists.poll();

		int removedSize = removed.size();

		for (int i = 0; i < removedSize; i++) {
			speedQ.poll();

			if (seqSpeedQ.size() > 0) {
				seqSpeedQ.poll();
			}

			if (removed.get(i).getLocation().getSpeed() != 0) {
				speednon.poll();
			}
		}

		computeFeatures();

		return removed;
	}

	private void computeFeatures() {
		List<Double> speednonList = new ArrayList<Double>(speednon);
		List<Double> speedList = new ArrayList<Double>(speedQ);
		List<Double> seqSpeedList = new ArrayList<Double>(seqSpeedQ);
		Collections.sort(speedList);
		Collections.sort(speednonList);
		maxnon = MathUtil.max(speednonList, true);
		minnon = MathUtil.min(speednonList, true);
		mediannon = MathUtil.median(speednonList, true);

		meannon = MathUtil.mean(speednonList);
		varnon = MathUtil.var(speednonList);
		entnon = MathUtil.shannonEntropy(speednonList);
		qutwenon = MathUtil.twentiethQuantile(speednonList);
		queignon = MathUtil.eightiesQuantile(speednonList);
		kurtnon = MathUtil.kurtosis(speednonList);
		skewnon = MathUtil.skewness(speednonList);
		iqrnon = MathUtil.iqr(speednonList);
		auto_corrnon = MathUtil.autoCorrelation(speednonList, meannon, varnon);

		maxsp = MathUtil.max(speedList, true);
		minsp = MathUtil.min(speedList, true);
		mediansp = MathUtil.median(speedList, true);

		meansp = MathUtil.mean(speedList);
		varsp = MathUtil.var(speedList);
		entsp = MathUtil.shannonEntropy(speedList);
		qutwesp = MathUtil.twentiethQuantile(speedList);
		queigsp = MathUtil.eightiesQuantile(speedList);
		skewsp = MathUtil.skewness(speedList);
		iqrsp = MathUtil.iqr(speedList);

		maxseqsp = MathUtil.max(seqSpeedList, false);
		minseqsp = MathUtil.min(seqSpeedList, false);
		medianseqsp = MathUtil.median(seqSpeedList, false);

		meanseqsp = MathUtil.mean(seqSpeedList);
		varseqsp = MathUtil.var(seqSpeedList);
		entseqsp = MathUtil.shannonEntropy(seqSpeedList);
		qutweseqsp = MathUtil.twentiethQuantile(seqSpeedList);
		queigseqsp = MathUtil.eightiesQuantile(seqSpeedList);
		skewseqsp = MathUtil.skewness(seqSpeedList);
		iqrseqsp = MathUtil.iqr(seqSpeedList);
	}

	/**
	 * Get the number of 30-second speed values based on which current speed
	 * feature is calculated.
	 * 
	 * @return
	 */
	public int size() {
		return locationLists.size();
	}

	/**
	 * Get the number of non-zero speed.
	 * 
	 * @return
	 */
	public int getNumnz() {
		return speednon.size();
	}
}
