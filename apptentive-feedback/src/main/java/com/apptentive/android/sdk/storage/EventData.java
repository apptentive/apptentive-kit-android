/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores a record of when events and interactions were triggered, as well as the number of times per versionName or versionCode.
 */
public class EventData implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, EventRecord> events; // we need a synchronized access to the map to avoid concurrent modification exceptions
	private Map<String, EventRecord> interactions; // we need a synchronized access to the map to avoid concurrent modification exceptions

	public EventData() {
		events = new HashMap<String, EventRecord>();
		interactions = new HashMap<String, EventRecord>();
	}


	public synchronized Long getEventCountTotal(String eventLabel) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord == null) {
			return 0L;
		}
		return eventRecord.getTotal();
	}

	public synchronized Long getInteractionCountTotal(String interactionId) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord != null) {
			return eventRecord.getTotal();
		}
		return 0L;
	}

	public synchronized Double getTimeOfLastEventInvocation(String eventLabel) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord != null) {
			return eventRecord.getLast();
		}
		return null;
	}

	public synchronized Double getTimeOfLastInteractionInvocation(String interactionId) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord != null) {
			return eventRecord.getLast();
		}
		return null;
	}

	public synchronized Long getEventCountForVersionCode(String eventLabel, Integer versionCode) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord != null) {
			return eventRecord.getCountForVersionCode(versionCode);
		}
		return 0L;
	}

	public synchronized Long getInteractionCountForVersionCode(String interactionId, Integer versionCode) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord != null) {
			return eventRecord.getCountForVersionCode(versionCode);
		}
		return 0L;
	}

	public synchronized Long getEventCountForVersionName(String eventLabel, String versionName) {
		EventRecord eventRecord = events.get(eventLabel);
		if (eventRecord != null) {
			return eventRecord.getCountForVersionName(versionName);
		}
		return 0L;
	}

	public synchronized Long getInteractionCountForVersionName(String interactionId, String versionName) {
		EventRecord eventRecord = interactions.get(interactionId);
		if (eventRecord != null) {
			return eventRecord.getCountForVersionName(versionName);
		}
		return 0L;
	}


	public synchronized String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Events: ");
		for (String key : events.keySet()) {
			builder.append("\n\t").append(key).append(": ").append(events.get(key).toString());
		}
		builder.append("\nInteractions: ");
		for (String key : interactions.keySet()) {
			builder.append("\n\t").append(key).append(": ").append(interactions.get(key).toString());
		}
		return builder.toString();
	}

	//region Getters & Setters

	/**
	 * Used for migration only.
	 */
	public synchronized void setEvents(Map<String, EventRecord> events) {
		this.events = events;

	}

	/**
	 * Used for migration only.
	 */
	public synchronized void setInteractions(Map<String, EventRecord> interactions) {
		this.interactions = interactions;

	}

	public synchronized void clear() {
		events.clear();
		interactions.clear();

	}

	//endregion
}
