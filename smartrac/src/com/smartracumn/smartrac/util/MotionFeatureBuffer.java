package com.smartracumn.smartrac.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.smartracumn.smartrac.model.MotionData;

/**
 * Motion feature buffer is used to hold features calculated from list of motion
 * data.
 * 
 * @author kangx385
 * 
 */
public class MotionFeatureBuffer {
	private int assignedBufferSize;
	private double meanacc;
	private double varacc;
	private double medianacc;
	private double qutweacc;
	private double queigacc;
	private double entacc;
	private double kurtacc;
	private double skewacc;
	private double iqracc;
	private double minacc;
	private double maxacc;
	private double auto_corr;

	private double meanseqacc;
	private double varseqacc;
	private double medianseqacc;
	private double qutweseqacc;
	private double queigseqacc;
	private double entseqacc;
	private double kurtseqacc;
	private double skewseqacc;
	private double iqrseqacc;
	private double minseqacc;
	private double maxseqacc;
	private double seqauto_corr;
	private LinkedList<Double> linearMags = new LinkedList<Double>();
	private Queue<Double> linearSeqMags = new LinkedList<Double>();
	private Queue<List<MotionData>> motionDataLists = new LinkedList<List<MotionData>>();

	private String featurePostfix;
	private String featurePrefix = "\"";

	public final String MEAN_ACC;
	public final String MAX_ACC;

	/**
	 * Initializes a new instance of the MotionFeatureBuffer class.
	 * 
	 * @param bufferSize
	 *            The buffer size.
	 */
	public MotionFeatureBuffer(int bufferSize) {
		this.assignedBufferSize = bufferSize;
		this.featurePostfix = "."
				+ String.valueOf(this.assignedBufferSize * 30) + "\"";
		MEAN_ACC = this.featurePrefix + "meanacc" + this.featurePostfix;
		MAX_ACC = this.featurePrefix + "maxacc" + this.featurePostfix;
	}

	/**
	 * Clear buffer.
	 */
	public void clear() {
		this.linearMags.clear();
		this.motionDataLists.clear();
		this.linearSeqMags.clear();
	}

	public Map<String, Double> getFeaturesMap() {
		Map<String, Double> featuresMap = new HashMap<String, Double>();

		featuresMap.put(this.featurePrefix + "meanacc" + this.featurePostfix,
				meanacc);
		featuresMap.put(this.featurePrefix + "varacc" + this.featurePostfix,
				varacc);
		featuresMap.put(this.featurePrefix + "medianacc" + this.featurePostfix,
				medianacc);
		featuresMap.put(this.featurePrefix + "qutweacc" + this.featurePostfix,
				qutweacc);
		featuresMap.put(this.featurePrefix + "queigacc" + this.featurePostfix,
				queigacc);
		featuresMap.put(this.featurePrefix + "entacc" + this.featurePostfix,
				entacc);
		featuresMap.put(this.featurePrefix + "kurtacc" + this.featurePostfix,
				kurtacc);
		featuresMap.put(this.featurePrefix + "skewacc" + this.featurePostfix,
				skewacc);
		featuresMap.put(this.featurePrefix + "iqracc" + this.featurePostfix,
				iqracc);
		featuresMap.put(this.featurePrefix + "minacc" + this.featurePostfix,
				minacc);
		featuresMap.put(this.featurePrefix + "maxacc" + this.featurePostfix,
				maxacc);
		featuresMap.put(this.featurePrefix + "auto.corr" + this.featurePostfix,
				auto_corr);

		featuresMap.put(
				this.featurePrefix + "meanseqacc" + this.featurePostfix,
				meanseqacc);
		featuresMap.put(this.featurePrefix + "varseqacc" + this.featurePostfix,
				varseqacc);
		featuresMap.put(this.featurePrefix + "medianseqacc"
				+ this.featurePostfix, medianseqacc);
		featuresMap.put(this.featurePrefix + "qutweseqacc"
				+ this.featurePostfix, qutweseqacc);
		featuresMap.put(this.featurePrefix + "queigseqacc"
				+ this.featurePostfix, queigseqacc);
		featuresMap.put(this.featurePrefix + "entseqacc" + this.featurePostfix,
				entseqacc);
		featuresMap.put(
				this.featurePrefix + "kurtseqacc" + this.featurePostfix,
				kurtseqacc);
		featuresMap.put(
				this.featurePrefix + "skewseqacc" + this.featurePostfix,
				skewseqacc);
		featuresMap.put(this.featurePrefix + "iqrseqacc" + this.featurePostfix,
				iqrseqacc);
		featuresMap.put(this.featurePrefix + "minseqacc" + this.featurePostfix,
				minseqacc);
		featuresMap.put(this.featurePrefix + "maxseqacc" + this.featurePostfix,
				maxseqacc);
		featuresMap.put(this.featurePrefix + "auto.corrseq"
				+ this.featurePostfix, seqauto_corr);

		// for (String key : featuresMap.keySet()) {
		// if (Double.isNaN(featuresMap.get(key))) {
		// featuresMap.put(key, 0.0);
		// }
		//
		// }

		return featuresMap;
	}

	/**
	 * Enqueue motion data to feature buffer.
	 * 
	 * @param mDatas
	 *            The list of motion data to be inserted to feature buffer.
	 * @return Motion data removed from head of feature buffer if the size of
	 *         feature buffer after insertion exceeds assigned buffer size.
	 */
	public List<MotionData> enqueue(List<MotionData> mDatas) {
		this.motionDataLists.offer(mDatas);

		for (MotionData mData : mDatas) {
			float linearMag = mData.getLinearMag();
			if (linearMags.size() > 0) {
				linearSeqMags.add(linearMag - linearMags.peekLast());
			}
			linearMags.add((double) linearMag);
		}

		if (this.motionDataLists.size() > assignedBufferSize) {
			return dequeue();
		} else {
			computeFeatures();
			return null;
		}

	}

	/**
	 * Remove motion data from head of feature buffer.
	 * 
	 * @return Motion data at head of feature buffer.
	 */
	public List<MotionData> dequeue() {
		List<MotionData> removed = motionDataLists.poll();

		int removedSize = removed.size();

		for (int i = 0; i < removedSize; i++) {
			linearMags.poll();
			if (linearSeqMags.size() > 0) {
				linearSeqMags.poll();
			}
		}

		computeFeatures();

		return removed;
	}

	private void computeFeatures() {
		List<Double> linearMagsList = new ArrayList<Double>(linearMags);
		Collections.sort(linearMagsList);
		maxacc = MathUtil.max(linearMagsList, true);
		if (maxacc == Double.NaN) {
			maxacc = 0;
		}

		minacc = MathUtil.min(linearMagsList, true);
		medianacc = MathUtil.median(linearMagsList, true);

		meanacc = MathUtil.mean(linearMagsList);
		if (meanacc == Double.NaN) {
			meanacc = 0;
		}

		varacc = MathUtil.var(linearMagsList);
		entacc = MathUtil.shannonEntropy(linearMagsList);
		qutweacc = MathUtil.twentiethQuantile(linearMagsList);
		queigacc = MathUtil.eightiesQuantile(linearMagsList);
		kurtacc = MathUtil.kurtosis(linearMagsList);
		skewacc = MathUtil.skewness(linearMagsList);
		iqracc = MathUtil.iqr(linearMagsList);
		auto_corr = MathUtil.autoCorrelation(linearMagsList, meanacc, varacc);

		List<Double> linearSeqMagsList = new ArrayList<Double>(linearSeqMags);
		maxseqacc = MathUtil.max(linearSeqMagsList, false);
		if (maxseqacc == Double.NaN) {
			maxseqacc = 0;
		}

		minseqacc = MathUtil.min(linearSeqMagsList, false);
		medianseqacc = MathUtil.median(linearSeqMagsList, false);

		meanseqacc = MathUtil.mean(linearSeqMagsList);
		if (meanseqacc == Double.NaN) {
			meanseqacc = 0;
		}

		varseqacc = MathUtil.var(linearSeqMagsList);
		entseqacc = MathUtil.shannonEntropy(linearSeqMagsList);
		qutweseqacc = MathUtil.twentiethQuantile(linearSeqMagsList);
		queigseqacc = MathUtil.eightiesQuantile(linearSeqMagsList);
		kurtseqacc = MathUtil.kurtosis(linearSeqMagsList);
		skewseqacc = MathUtil.skewness(linearSeqMagsList);
		iqrseqacc = MathUtil.iqr(linearSeqMagsList);
		seqauto_corr = MathUtil.autoCorrelation(linearSeqMagsList, meanseqacc,
				varseqacc);
	}

	/**
	 * Get the number of motion data list based on which current feature is
	 * calculated.
	 * 
	 * @return
	 */
	public int size() {
		return motionDataLists.size();
	}
}
