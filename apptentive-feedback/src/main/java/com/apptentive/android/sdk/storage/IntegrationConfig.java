/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import androidx.annotation.VisibleForTesting;

import java.io.Serializable;


public class IntegrationConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	private IntegrationConfigItem apptentive;
	private IntegrationConfigItem amazonAwsSns;
	private IntegrationConfigItem urbanAirship;
	private IntegrationConfigItem parse;

	public IntegrationConfig() {
	}

	@VisibleForTesting
	public IntegrationConfig(IntegrationConfigItem apptentive,
							 IntegrationConfigItem amazonAwsSns,
							 IntegrationConfigItem urbanAirship,
							 IntegrationConfigItem parse
	) {
		this.apptentive = apptentive;
		this.amazonAwsSns = amazonAwsSns;
		this.urbanAirship = urbanAirship;
		this.parse = parse;
	}


	//region Getters & Setters
	public IntegrationConfigItem getApptentive() {
		return apptentive;
	}

	public void setApptentive(IntegrationConfigItem apptentive) {
		this.apptentive = apptentive;
	}

	public IntegrationConfigItem getAmazonAwsSns() {
		return amazonAwsSns;
	}

	public IntegrationConfigItem getUrbanAirship() {
		return urbanAirship;
	}

	public IntegrationConfigItem getParse() {
		return parse;
	}

	public void setParse(IntegrationConfigItem parse) {
		this.parse = parse;
	}
	//endregion


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IntegrationConfig that = (IntegrationConfig) o;

		if (apptentive != null ? !apptentive.equals(that.apptentive) : that.apptentive != null)
			return false;
		if (amazonAwsSns != null ? !amazonAwsSns.equals(that.amazonAwsSns) : that.amazonAwsSns != null)
			return false;
		if (urbanAirship != null ? !urbanAirship.equals(that.urbanAirship) : that.urbanAirship != null)
			return false;
		return parse != null ? parse.equals(that.parse) : that.parse == null;

	}

	@Override
	public int hashCode() {
		int result = apptentive != null ? apptentive.hashCode() : 0;
		result = 31 * result + (amazonAwsSns != null ? amazonAwsSns.hashCode() : 0);
		result = 31 * result + (urbanAirship != null ? urbanAirship.hashCode() : 0);
		result = 31 * result + (parse != null ? parse.hashCode() : 0);
		return result;
	}

	// TODO: unit tests
	public IntegrationConfig clone() {
		IntegrationConfig clone = new IntegrationConfig();
		clone.apptentive = apptentive != null ? apptentive.clone() : null;
		clone.amazonAwsSns = amazonAwsSns != null ? amazonAwsSns.clone() : null;
		clone.urbanAirship = urbanAirship != null ? urbanAirship.clone() : null;
		clone.parse = parse != null ? parse.clone() : null;
		return clone;
	}

}
