package com.smartracumn.smartrac.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Math utility class take advantage of Apache's commons math library to provide
 * data computation needed by smartrac.
 * 
 * @author kangx385
 * 
 */
public class MathUtil {
	private static final int PRECISION = 10000;

	/**
	 * Get median.
	 * 
	 * @param vals
	 *            Source.
	 * @param isSorted
	 *            A value indicating whether or not source is sorted.
	 * @return Median value.
	 */
	public static double median(List<Double> vals, boolean isSorted) {
		double median = 0;
		if (vals.size() == 0)
			return Double.NaN;

		if (!isSorted) {
			List<Double> temp = new ArrayList<Double>(vals);
			Collections.sort(temp);
			vals = temp;
		}

		if (vals.size() % 2 == 0) {
			int left = (vals.size() - 1) / 2;
			median = (vals.get(left) + vals.get(left + 1)) / 2;
		} else {
			median = vals.get((vals.size() - 1) / 2);
		}

		return median;
	}

	/**
	 * Get minimum value.
	 * 
	 * @param vals
	 *            Source.
	 * @param isSorted
	 *            A value indicating whether or not source is sorted.
	 * @return Minimum value.
	 */
	public static double min(List<Double> vals, boolean isSorted) {
		if (vals.size() == 0) {
			return Double.NaN;
		}

		if (!isSorted) {
			DescriptiveStatistics ds = new DescriptiveStatistics(
					getDataArray(vals));

			return ds.getMin();
		}

		return vals.get(0);
	}

	/**
	 * Get maximum value.
	 * 
	 * @param vals
	 *            Source.
	 * @param isSorted
	 *            A value indicating whether or not source is sorted.
	 * @return Maximum value.
	 */
	public static double max(List<Double> vals, boolean isSorted) {
		if (vals.size() == 0) {
			return Double.NaN;
		}

		if (!isSorted) {
			DescriptiveStatistics ds = new DescriptiveStatistics(
					getDataArray(vals));

			return ds.getMax();
		}

		return vals.get(vals.size() - 1);
	}

	/**
	 * Get shannon's entropy value based on 4 decimal number precision.
	 * 
	 * @param vals
	 *            Source.
	 * @return Shannon entropy.
	 */
	public static double shannonEntropy(List<Double> vals) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		// count the occurrences of each value
		for (double val : vals) {
			if (!map.containsKey((int) (val * PRECISION))) {
				map.put((int) (val * PRECISION), 0);
			}

			map.put((int) (val * PRECISION),
					map.get((int) (val * PRECISION)) + 1);
		}

		// calculate the entropy
		double result = 0;
		for (int key : map.keySet()) {
			Double frequency = (double) map.get(key) / vals.size();
			result -= frequency * (Math.log(frequency) / Math.log(2));
		}

		return result;
	}

	/**
	 * Get mean value.
	 * 
	 * @param vals
	 *            Source.
	 * @return Mean value.
	 */
	public static double mean(List<Double> vals) {
		DescriptiveStatistics ds = new DescriptiveStatistics(getDataArray(vals));
		return ds.getMean();
	}

	/**
	 * Get variance.
	 * 
	 * @param vals
	 *            Source.
	 * @return Variance value.
	 */
	public static double var(List<Double> vals) {
		DescriptiveStatistics ds = new DescriptiveStatistics(getDataArray(vals));
		return ds.getVariance();
	}

	/**
	 * Get 20th quantile.
	 * 
	 * @param vals
	 *            Source.
	 * @return 20th quantile.
	 */
	public static double twentiethQuantile(List<Double> vals) {
		return quantile(vals, 20);
	}

	/**
	 * Get 80th quantile.
	 * 
	 * @param vals
	 *            Source.
	 * @return 80th quantile.
	 */
	public static double eightiesQuantile(List<Double> vals) {
		return quantile(vals, 80);
	}

	/**
	 * Get percentile value.
	 * 
	 * @param vals
	 *            Source.
	 * @param quantile
	 *            Quantile value.
	 * @return Percentile value based on given quantile.
	 */
	public static double quantile(List<Double> vals, double quantile) {
		DescriptiveStatistics ds = new DescriptiveStatistics(getDataArray(vals));
		return ds.getPercentile(quantile);
	}

	/**
	 * Get kurtosis value.
	 * 
	 * @param vals
	 *            Source.
	 * @return Kurtosis value.
	 */
	public static double kurtosis(List<Double> vals) {
		DescriptiveStatistics ds = new DescriptiveStatistics(getDataArray(vals));

		return ds.getKurtosis();
	}

	/**
	 * Get skewness value.
	 * 
	 * @param vals
	 *            Source.
	 * @return Skewness value.
	 */
	public static double skewness(List<Double> vals) {
		DescriptiveStatistics ds = new DescriptiveStatistics(getDataArray(vals));

		return ds.getSkewness();
	}

	/**
	 * Get Interquartile range between 75th quantile and 25the quantile.
	 * 
	 * @param vals
	 *            Source.
	 * @return Interquartile range.
	 */
	public static double iqr(List<Double> vals) {
		DescriptiveStatistics ds = new DescriptiveStatistics(getDataArray(vals));

		return ds.getPercentile(75) - ds.getPercentile(25);
	}

	/**
	 * Get the 1-lag autocorrelation.
	 * 
	 * @param vals
	 *            values.
	 * @param mean
	 *            mean.
	 * @param var
	 *            variation.
	 * @return 1-lag autocorrelation.
	 */
	public static double autoCorrelation(List<Double> vals, double mean,
			double var) {
		int n = vals.size();

		if (n <= 1 || var == 0) {
			return 1;
		}

		double sum = 0;

		for (int i = 0; i < n - 1; i++) {
			sum += (vals.get(i) - mean) * (vals.get(i + 1) - mean);
		}

		return 1 / (n - 1) * sum / var;
	}

	private static double[] getDataArray(List<Double> vals) {
		double[] data = new double[vals.size()];
		for (int i = 0; i < vals.size(); i++) {
			data[i] = vals.get(i);
		}

		return data;
	}
}
