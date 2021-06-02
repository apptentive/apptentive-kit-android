/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import androidx.annotation.VisibleForTesting;

import java.io.Serializable;

/**
 * Legacy SDK data representation.
 * See: https://github.com/apptentive/apptentive-android/blob/master/apptentive/src/main/java/com/apptentive/android/sdk/storage/Sdk.java
 * NOTE: THIS CLASS CAN'T BE RENAMED, MODIFIED, OR MOVED TO ANOTHER PACKAGE - OTHERWISE, JAVA SERIALIZABLE MECHANISM BREAKS!!!
 */
public class Sdk implements Serializable {
	private static final long serialVersionUID = -6227767047869055574L;
	private String version;
	private String programmingLanguage;
	private String authorName;
	private String authorEmail;
	private String platform;
	private String distribution;
	private String distributionVersion;
	
	public Sdk() {} // for serialization

	@VisibleForTesting
	public Sdk(
			String version,
			String programmingLanguage,
			String authorName,
			String authorEmail,
			String platform,
			String distribution,
			String distributionVersion
	) {
		this.version = version;
		this.programmingLanguage = programmingLanguage;
		this.authorName = authorName;
		this.authorEmail = authorEmail;
		this.platform = platform;
		this.distribution = distribution;
		this.distributionVersion = distributionVersion;
		
	} // for serialization

	//region Getters & Setters

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getProgrammingLanguage() {
		return programmingLanguage;
	}

	public String getAuthorName() {
		return authorName;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getDistribution() {
		return distribution;
	}

	public String getDistributionVersion() {
		return distributionVersion;
	}

	//endregion
}
