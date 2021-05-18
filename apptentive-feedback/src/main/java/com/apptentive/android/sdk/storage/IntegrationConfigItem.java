/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import java.io.Serializable;
import java.util.HashMap;

public class IntegrationConfigItem implements Serializable {
	private static final long serialVersionUID = 3509802144209212980L;
	private static final String KEY_TOKEN = "token";

	private HashMap<String, String> contents = new HashMap<>();

	public IntegrationConfigItem() {
	}

	public void setToken(String token) {
		contents.put(KEY_TOKEN, token);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IntegrationConfigItem that = (IntegrationConfigItem) o;

		return contents != null ? contents.equals(that.contents) : that.contents == null;

	}

	@Override
	public int hashCode() {
		return contents != null ? contents.hashCode() : 0;
	}

	// TODO: unit testing
	public IntegrationConfigItem clone() {
		IntegrationConfigItem clone = new IntegrationConfigItem();
		clone.contents.putAll(contents);
		return clone;
	}
}
