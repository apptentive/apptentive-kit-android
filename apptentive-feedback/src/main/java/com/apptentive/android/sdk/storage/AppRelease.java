/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import androidx.annotation.VisibleForTesting;

import java.io.Serializable;

/**
 * Legacy app release data representation.
 * See: https://github.com/apptentive/apptentive-android/blob/master/apptentive/src/main/java/com/apptentive/android/sdk/storage/AppRelease.java
 * NOTE: THIS CLASS CAN'T BE RENAMED, MODIFIED, OR MOVED TO ANOTHER PACKAGE - OTHERWISE, JAVA SERIALIZABLE MECHANISM BREAKS!!!
 */
public class AppRelease implements Serializable {
	private static final long serialVersionUID = 8789914596082013978L;
	private String appStore;
	private boolean debug;
	private String identifier;
	private boolean inheritStyle;
	private boolean overrideStyle;
	private String targetSdkVersion;
	private String type;
	private int versionCode;
	private String versionName;

	public AppRelease() {
	}

	@VisibleForTesting
	public AppRelease(String appStore,
					  boolean debug,
					  String identifier,
					  boolean inheritStyle,
					  boolean overrideStyle,
					  String targetSdkVersion,
					  String type,
					  int versionCode,
					  String versionName) {
		this.appStore = appStore;
		this.debug = debug;
		this.identifier = identifier;
		this.inheritStyle = inheritStyle;
		this.overrideStyle = overrideStyle;
		this.targetSdkVersion = targetSdkVersion;
		this.type = type;
		this.versionCode = versionCode;
		this.versionName = versionName;
	}

	//region Getters & Setters

	public String getAppStore() {
		return appStore;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public String getIdentifier() {
		return identifier;
	}

	public boolean isInheritStyle() {
		return inheritStyle;
	}

	public boolean isOverrideStyle() {
		return overrideStyle;
	}

	public String getTargetSdkVersion() {
		return targetSdkVersion;
	}

	public void setTargetSdkVersion(String targetSdkVersion) {
		this.targetSdkVersion = targetSdkVersion;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	//endregion
}
