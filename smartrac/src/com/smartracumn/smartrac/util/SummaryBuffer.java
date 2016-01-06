package com.smartracumn.smartrac.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Summary buffer used to maintain fixed size of entities and query for most
 * frequent entity.
 * 
 * @author kangx385
 * 
 * @param <T>
 */
public class SummaryBuffer<T> {
	private Map<T, Integer> summary = new HashMap<T, Integer>();

	private int assignedBufferSize;

	private Queue<T> entities = new LinkedList<T>();

	private T lastInQueue;

	/**
	 * Initializes a new instance of the SummaryBuffer class.
	 * 
	 * @param bufferSize
	 */
	public SummaryBuffer(int bufferSize) {
		this.assignedBufferSize = bufferSize;
	}

	/**
	 * Get current size of summary buffer.
	 * 
	 * @return
	 */
	public int size() {
		return entities.size();
	}

	/**
	 * Enqueue entity to summary buffer and remove oldest entity if exceeds
	 * assigned queue length;
	 * 
	 * @param entity
	 * @return entity removed from summary buffer.
	 */
	public T enqueue(T entity) {
		lastInQueue = entity;

		entities.offer(entity);

		if (!summary.containsKey(entity)) {
			summary.put(entity, 0);
		}

		summary.put(entity, summary.get(entity) + 1);

		if (entities.size() > assignedBufferSize) {
			return dequeue();
		}

		return null;
	}

	/**
	 * Dequeue entity from summary buffer.
	 * 
	 * @return The entity removed from summary buffer.
	 */
	public T dequeue() {
		T m = entities.poll();
		if (m != null) {
			summary.put(m, summary.get(m) - 1);
		}
		return m;
	}

	/**
	 * Get the most frequent occurred entity from summary buffer.
	 * 
	 * @return
	 */
	public T getMostFrequentEntity() {
		T freqEntity = null;
		int freqCount = 0;

		for (T mode : summary.keySet()) {
			if (summary.get(mode) > freqCount) {
				freqCount = summary.get(mode);
				freqEntity = mode;
			}
		}

		return freqEntity;
	}

	/**
	 * Get latest enqueued entity.
	 * 
	 * @return Latest enqueued entity.
	 */
	public T getLatestEntity() {
		return lastInQueue;
	}
}