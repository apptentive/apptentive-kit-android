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

	public EventData(Map<String, EventRecord> events, Map<String, EventRecord> interactions) {
		this.events = events;
		this.interactions = interactions;
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

	public Map<String, EventRecord> getEvents() {
		return events;
	}

	public Map<String, EventRecord> getInteractions() {
		return interactions;
	}

	//endregion
}
