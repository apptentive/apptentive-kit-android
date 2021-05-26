/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.Serializable;
import java.util.UUID;

public class ConversationData implements Serializable {

	private static final long serialVersionUID = 1L;
	private String localIdentifier;
	private String conversationToken;
	private String conversationId;
	private Device device;
	private Device lastSentDevice;
	private Person person;
	private Person lastSentPerson;
	private Sdk sdk;
	private AppRelease appRelease;
	private EventData eventData;
	private String lastSeenSdkVersion;
	private VersionHistory versionHistory;
	private boolean messageCenterFeatureUsed;
	private boolean messageCenterWhoCardPreviouslyDisplayed;
	private String messageCenterPendingMessage;
	private String messageCenterPendingAttachments;
	private String targets;
	private String interactions;
	private double interactionExpiration;

	public ConversationData() {
		this.localIdentifier = UUID.randomUUID().toString();
		this.device = new Device();
		this.person = new Person();
		this.sdk = new Sdk();
		this.appRelease = new AppRelease();
		this.eventData = new EventData();
		this.versionHistory = new VersionHistory();
	}
	
	@VisibleForTesting
	public ConversationData(
			String localIdentifier,
			String conversationToken,
			String conversationId,
			Device device,
			Device lastSentDevice,
			Person person,
			Person lastSentPerson,
			Sdk sdk,
			AppRelease appRelease,
			EventData eventData,
			String lastSeenSdkVersion,
			VersionHistory versionHistory,
			boolean messageCenterFeatureUsed,
			boolean messageCenterWhoCardPreviouslyDisplayed,
			String messageCenterPendingMessage,
			String messageCenterPendingAttachments,
			String targets,
			String interactions,
			double interactionExpiration
	) {
		this.localIdentifier = localIdentifier;
		this.conversationToken = conversationToken;
		this.conversationId = conversationId;
		this.device = device;
		this.lastSentDevice = lastSentDevice;
		this.person = person;
		this.lastSentPerson = lastSentPerson;
		this.sdk = sdk;
		this.appRelease = appRelease;
		this.eventData = eventData;
		this.lastSeenSdkVersion = lastSeenSdkVersion;
		this.versionHistory = versionHistory;
		this.messageCenterFeatureUsed = messageCenterFeatureUsed;
		this.messageCenterWhoCardPreviouslyDisplayed = messageCenterWhoCardPreviouslyDisplayed;
		this.messageCenterPendingMessage = messageCenterPendingMessage;
		this.messageCenterPendingAttachments = messageCenterPendingAttachments;
		this.targets = targets;
		this.interactions = interactions;
		this.interactionExpiration = interactionExpiration;
	}

	//region Getters & Setters

	public String getLocalIdentifier() {
		return localIdentifier;
	}

	public String getConversationToken() {
		return conversationToken;
	}

	public void setConversationToken(String conversationToken) {
		if (!StringUtils.equal(this.conversationToken, conversationToken)) {
			this.conversationToken = conversationToken;
		}
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		if (conversationId == null) {
			throw new IllegalArgumentException("Conversation id is null");
		}

		if (!StringUtils.equal(this.conversationId, conversationId)) {
			this.conversationId = conversationId;
		}
	}

	public @NonNull Device getDevice() {
		return device;
	}

	public void setDevice(@NonNull Device device) {
		this.device = device;
	}

	public Device getLastSentDevice() {
		return lastSentDevice;
	}

	public @NonNull Person getPerson() {
		return person;
	}

	public void setPerson(@NonNull Person person) {
		this.person = person;
	}

	public Person getLastSentPerson() {
		return lastSentPerson;
	}

	public Sdk getSdk() {
		return sdk;
	}

	public void setSdk(Sdk sdk) {
		this.sdk = sdk;
	}

	public AppRelease getAppRelease() {
		return appRelease;
	}

	public void setAppRelease(AppRelease appRelease) {
		this.appRelease = appRelease;
	}

	public EventData getEventData() {
		return eventData;
	}

	public String getLastSeenSdkVersion() {
		return lastSeenSdkVersion;
	}

	public VersionHistory getVersionHistory() {
		return versionHistory;
	}

	public boolean isMessageCenterFeatureUsed() {
		return messageCenterFeatureUsed;
	}

	public boolean isMessageCenterWhoCardPreviouslyDisplayed() {
		return messageCenterWhoCardPreviouslyDisplayed;
	}

	public String getMessageCenterPendingMessage() {
		return messageCenterPendingMessage;
	}

	public String getMessageCenterPendingAttachments() {
		return messageCenterPendingAttachments;
	}

	public String getTargets() {
		return targets;
	}

	public String getInteractions() {
		return interactions;
	}

	public void setInteractions(String interactions) {
		if (!StringUtils.equal(this.interactions, interactions)) {
			this.interactions = interactions;

		}
	}

	public double getInteractionExpiration() {
		return interactionExpiration;
	}

	public @Nullable String getMParticleId() {
		return getPerson().getMParticleId();
	}

	//endregion
}
