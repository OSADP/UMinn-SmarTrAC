package com.smartracumn.smartrac.model;

import java.util.Date;

/**
 * The class represent Motion Data.
 * 
 * @author kangx385
 * 
 */
public class MotionData {
	private long id;
	private Date time;
	private float linearX;
	private float linearY;
	private float linearZ;
	private float linearMag;
	private float trueX;
	private float trueY;
	private float trueZ;
	private float trueMag;

	/**
	 * Initializes a new instance of the MotionData class.
	 * 
	 * @param time
	 * @param linearAcc
	 * @param trueAcc
	 */
	public MotionData(Date time, float[] linearAcc, float[] trueAcc) {
		this(0, time, linearAcc, trueAcc);
	}

	/**
	 * Initializes a new instance of the MotionData class.
	 * 
	 * @param id
	 * @param time
	 * @param linearAcc
	 * @param trueAcc
	 */
	public MotionData(long id, Date time, float[] linearAcc, float[] trueAcc) {
		this.id = id;
		this.time = time;
		this.linearX = linearAcc[0];
		this.linearY = linearAcc[1];
		this.linearZ = linearAcc[2];
		this.linearMag = linearAcc[3];
		this.trueX = trueAcc[0];
		this.trueY = trueAcc[1];
		this.trueZ = trueAcc[2];
		this.trueMag = trueAcc[3];
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public long getId() {
		return this.id;
	}

	public Date getTime() {
		return this.time;
	}

	public float getLinearX() {
		return this.linearX;
	}

	public float getLinearY() {
		return this.linearY;
	}

	public float getLinearZ() {
		return this.linearZ;
	}

	public float getLinearMag() {
		return this.linearMag;
	}

	public float getTrueX() {
		return this.trueX;
	}

	public float getTrueY() {
		return this.trueY;
	}

	public float getTrueZ() {
		return this.trueZ;
	}

	public float getTrueMag() {
		return this.trueMag;
	}
}
