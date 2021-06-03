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
 * Legacy custom data representation.
 * See: https://github.com/apptentive/apptentive-android/blob/master/apptentive/src/main/java/com/apptentive/android/sdk/model/CustomData.java
 * NOTE: THIS CLASS CAN'T BE RENAMED, MODIFIED, OR MOVED TO ANOTHER PACKAGE - OTHERWISE, JAVA SERIALIZABLE MECHANISM BREAKS!!!
 */
public class CustomData extends HashMap<String, Serializable> implements Serializable {
	private static final long serialVersionUID = 1L;

	//region Saving when modified
	@Override
	public Serializable put(String key, Serializable value) {
		Serializable ret = super.put(key, value);
		return ret;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Serializable> m) {
		super.putAll(m);
	}

	@Override
	public Serializable remove(Object key) {
		Serializable ret = super.remove(key);
		return ret;
	}

	@Override
	public void clear() {
		super.clear();
	}

	//endregion
}
