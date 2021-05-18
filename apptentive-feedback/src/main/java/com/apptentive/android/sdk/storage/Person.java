/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import androidx.annotation.Nullable;

import com.apptentive.android.sdk.util.StringUtils;

import java.io.Serializable;

public class Person implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String email;
	private String name;
	private String facebookId;
	private String phoneNumber;
	private String street;
	private String city;
	private String zip;
	private String country;
	private String birthday;
	private String mParticleId;
	private CustomData customData;

	public Person() {
		customData = new CustomData();
	}

	//region Getters & Setters

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (!StringUtils.equal(this.id, id)) {
			this.id = id;

		}
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (!StringUtils.equal(this.email, email)) {
			this.email = email;

		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (!StringUtils.equal(this.name, name)) {
			this.name = name;

		}
	}

	public String getFacebookId() {
		return facebookId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getZip() {
		return zip;
	}

	public String getCountry() {
		return country;
	}

	public String getBirthday() {
		return birthday;
	}

	public @Nullable String getMParticleId() {
		return mParticleId;
	}

	public CustomData getCustomData() {
		return customData;
	}

	public void setCustomData(CustomData customData) {
		this.customData = customData;

	}

	//endregion
	
	//region Clone

	public Person clone() {
		Person person = new Person();
		person.id = id;
		person.email = email;
		person.name = name;
		person.facebookId = facebookId;
		person.phoneNumber = phoneNumber;
		person.street = street;
		person.city = city;
		person.zip = zip;
		person.country = country;
		person.birthday = birthday;
		if (customData != null) {
			person.customData.putAll(customData);
		}
		return person;
	}

	//endregion
}
