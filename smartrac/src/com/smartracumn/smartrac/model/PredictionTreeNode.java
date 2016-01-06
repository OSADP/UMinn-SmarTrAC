package com.smartracumn.smartrac.model;

import java.util.Map;
import java.util.Random;

/**
 * The class represents prediction tree node.
 * 
 * @author kangx385
 * 
 */
public class PredictionTreeNode<T> {
	private int id;
	private int leftId;
	private int rightId;
	private String splitVar;
	private double splitPoint;
	private int status;
	private T prediction;

	private Random randomGenerator;

	/**
	 * Initializes a new instance of the PredictionTreeNode class.
	 * 
	 * @param id
	 * @param leftId
	 * @param rightId
	 * @param splitVar
	 * @param splitPoint
	 * @param status
	 * @param prediction
	 * @param random
	 *            The random generator used among all tree nodes.
	 */
	public PredictionTreeNode(int id, int leftId, int rightId, String splitVar,
			double splitPoint, int status, T prediction,
			Random random) {
		this.id = id;
		this.leftId = leftId;
		this.rightId = rightId;
		this.splitVar = splitVar;
		this.splitPoint = splitPoint;
		this.status = status;
		this.prediction = prediction;
		setRandomGenerator(random);
	}

	/**
	 * Set random generator.
	 * 
	 * @param random
	 */
	public void setRandomGenerator(Random random) {
		this.randomGenerator = random;
	}

	/**
	 * Get a value indicating whether or not this is a leaf which contains a
	 * prediction.
	 * 
	 * @return
	 */
	public boolean isLeaf() {
		return this.status < 0;
	}

	/**
	 * Get prediction.
	 * 
	 * @return
	 */
	public T getPrediction() {
		return this.prediction;
	}

	/**
	 * Get the id of next tree node to go to.
	 * 
	 * @param features
	 * @return
	 */
	public int next(Map<String, Double> features) {
		if (!features.containsKey(this.splitVar)
				|| features.get(this.splitVar).isNaN()) {
			return randomGenerator.nextDouble() >= 0.5 ? this.rightId
					: this.leftId;
		} else if (features.get(this.splitVar) <= this.splitPoint) {
			return this.leftId;
		} else {
			return this.rightId;
		}
	}

	/**
	 * Get tree node id.
	 * 
	 * @return
	 */
	public int getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return id + "," + leftId + "," + rightId + "," + splitVar + ","
				+ splitPoint + "," + isLeaf() + "," + prediction;
	}
}
