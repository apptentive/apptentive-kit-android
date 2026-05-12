/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.storage.EventRecord;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Legacy event data representation.
 * See: <a href="https://github.com/apptentive/apptentive-android/blob/master/apptentive/src/main/java/com/apptentive/android/sdk/storage/EventData.java">...</a>
 * NOTE: THIS CLASS CAN'T BE RENAMED, MODIFIED, OR MOVED TO ANOTHER PACKAGE - OTHERWISE, JAVA SERIALIZABLE MECHANISM BREAKS!!!
 */
public class EventData implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, com.apptentive.android.sdk.storage.EventRecord> events; // we need a synchronized access to the map to avoid concurrent modification exceptions
	private Map<String, com.apptentive.android.sdk.storage.EventRecord> interactions; // we need a synchronized access to the map to avoid concurrent modification exceptions

	public EventData() {
		events = new HashMap<String, com.apptentive.android.sdk.storage.EventRecord>();
		interactions = new HashMap<String, com.apptentive.android.sdk.storage.EventRecord>();
	}

	public EventData(Map<String, com.apptentive.android.sdk.storage.EventRecord> events, Map<String, com.apptentive.android.sdk.storage.EventRecord> interactions) {
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

	public Map<String, com.apptentive.android.sdk.storage.EventRecord> getEvents() {
		return events;
	}

	public Map<String, EventRecord> getInteractions() {
		return interactions;
	}

	//endregion
}
