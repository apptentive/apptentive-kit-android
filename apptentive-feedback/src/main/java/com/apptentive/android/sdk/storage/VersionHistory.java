/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy version history data representation.
 * See: https://github.com/apptentive/apptentive-android/blob/master/apptentive/src/main/java/com/apptentive/android/sdk/storage/VersionHistory.java
 * NOTE: THIS CLASS CAN'T BE RENAMED, MODIFIED, OR MOVED TO ANOTHER PACKAGE - OTHERWISE, JAVA SERIALIZABLE MECHANISM BREAKS!!!
 */
public class VersionHistory implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * An ordered list of version history. Older versions are first, new versions are added to the end.
	 */
	private List<VersionHistoryItem> versionHistoryItems;

	public VersionHistory() {
		versionHistoryItems = new ArrayList<>();
	}

	public void setVersionHistoryItems(List<VersionHistoryItem> versionHistoryItems) {
		this.versionHistoryItems = versionHistoryItems;
	}

	public List<VersionHistoryItem> getVersionHistoryItems() {
		return versionHistoryItems;
	}
}
