/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.conversation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.apptentive.android.sdk.Encryption;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EncryptedFileSerializer;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.FileSerializer;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.SerializerException;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.StringUtils;

import java.io.File;

import apptentive.com.android.feedback.LogTags;
import apptentive.com.android.util.Log;

import static apptentive.com.android.feedback.LogTags.*;
import static com.apptentive.android.sdk.conversation.ConversationState.ANONYMOUS;
import static com.apptentive.android.sdk.conversation.ConversationState.LOGGED_IN;

public class Conversation {

	/**
	 * Conversation data for this class to manage
	 */
	private ConversationData conversationData;

	/**
	 * Encryption for storing conversation data on disk.
	 */
	private @NonNull Encryption encryption;

	/**
	 * Payload encryption key (received from the backend). This would be missing for anonymous conversations.
	 * NOTE: we store the hex key separately in order to be able to update the corresponding conversation
	 * metadata item.
	 */
	private @Nullable String payloadEncryptionKey;

	/**
	 * Optional user id for logged-in conversations
	 */
	private String userId;

	/**
	 * File which represents serialized conversation data on the disk
	 */
	private final File conversationDataFile;

	/**
	 * File which represents serialized messages data on the disk
	 */
	private final File conversationMessagesFile;

	/**
	 * Current conversation state
	 */
	private ConversationState state = ConversationState.UNDEFINED;

	/**
	 * Cached conversation state for better transition tracking
	 */
	private ConversationState prevState = ConversationState.UNDEFINED;

	/**
	 * @param conversationDataFile     - file for storing serialized conversation data
	 * @param conversationMessagesFile - file for storing serialized conversation messages
	 * @param encryption               - encryption object for encrypting data, messages and logged-in payloads
	 * @param payloadEncryptionKey     - hex key used for creating the encryption object for the logged-in conversation. Would be <code>null</code> for anonymous conversations
	 */
	public Conversation(File conversationDataFile, File conversationMessagesFile, @NonNull Encryption encryption, @Nullable String payloadEncryptionKey) {
		if (conversationDataFile == null) {
			throw new IllegalArgumentException("Data file is null");
		}
		if (conversationMessagesFile == null) {
			throw new IllegalArgumentException("Messages file is null");
		}
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}

		this.conversationDataFile = conversationDataFile;
		this.conversationMessagesFile = conversationMessagesFile;
		this.encryption = encryption;
		this.payloadEncryptionKey = payloadEncryptionKey;

		conversationData = new ConversationData();
	}

	//region Saving

	void loadConversationData() throws SerializerException {
		long start = System.currentTimeMillis();

		FileSerializer serializer = new EncryptedFileSerializer(conversationDataFile, encryption);
		Log.d(CONVERSATION, "Loading conversation data...");
		conversationData = (ConversationData) serializer.deserialize();
		Log.d(CONVERSATION, "Conversation data loaded (took %d ms)", System.currentTimeMillis() - start);
	}

	//endregion

	//region Getters & Setters

	public String getLocalIdentifier() {
		return getConversationData().getLocalIdentifier();
	}

	public ConversationState getState() {
		return state;
	}

	public void setState(ConversationState state) {
		// TODO: check if state transition would make sense (for example you should not be able to move from 'logged' state to 'anonymous', etc.)
		this.prevState = this.state;
		this.state = state;
	}

	/**
	 * Returns <code>true</code> if conversation is in the given state
	 */
	public boolean hasState(ConversationState s) {
		return state.equals(s);
	}

	/**
	 * Returns <code>true</code> if conversation is in one of the given states
	 */
	public boolean hasState(ConversationState... states) {
		for (ConversationState s : states) {
			if (s.equals(state)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this conversation belongs to a logged-in user.
	 */
	public boolean isAuthenticated() {
		return hasState(LOGGED_IN);
	}

	/**
	 * Returns true if conversation is in "active" state (after receiving server response)
	 */
	public boolean hasActiveState() {
		return hasState(ConversationState.LOGGED_IN, ANONYMOUS);
	}

	public String getConversationToken() {
		return getConversationData().getConversationToken();
	}

	public void setConversationToken(String conversationToken) {
		getConversationData().setConversationToken(conversationToken);
	}

	public String getConversationId() {
		return getConversationData().getConversationId();
	}

	public void setConversationId(String conversationId) {
		getConversationData().setConversationId(conversationId);
	}

	public Device getDevice() {
		return getConversationData().getDevice();
	}

	public void setDevice(Device device) {
		getConversationData().setDevice(device);
	}

	public Device getLastSentDevice() {
		return getConversationData().getLastSentDevice();
	}

	public Person getPerson() {
		return getConversationData().getPerson();
	}

	public void setPerson(Person person) {
		getConversationData().setPerson(person);
	}

	public Person getLastSentPerson() {
		return getConversationData().getLastSentPerson();
	}

	public Sdk getSdk() {
		return getConversationData().getSdk();
	}

	public void setSdk(Sdk sdk) {
		getConversationData().setSdk(sdk);
	}

	public AppRelease getAppRelease() {
		return getConversationData().getAppRelease();
	}

	public void setAppRelease(AppRelease appRelease) {
		getConversationData().setAppRelease(appRelease);
	}

	public EventData getEventData() {
		return getConversationData().getEventData();
	}


	public String getLastSeenSdkVersion() {
		return getConversationData().getLastSeenSdkVersion();
	}

	public VersionHistory getVersionHistory() {
		return getConversationData().getVersionHistory();
	}

	public String getTargets() {
		return getConversationData().getTargets();
	}

	public String getInteractions() {
		return getConversationData().getInteractions();
	}

	public void setInteractions(String interactions) {
		getConversationData().setInteractions(interactions);
	}

	public double getInteractionExpiration() {
		return getConversationData().getInteractionExpiration();
	}

	public @Nullable String getMParticleId() {
		return getConversationData().getMParticleId();
	}

	// this is a synchronization hack: both save/load conversation data are synchronized so we can't
	// modify conversation data while it's being serialized/deserialized
	public synchronized @NonNull ConversationData getConversationData() {
		return conversationData;
	}

	public synchronized File getConversationDataFile() {
		return conversationDataFile;
	}

	public synchronized File getConversationMessagesFile() {
		return conversationMessagesFile;
	}

	public void setEncryption(@NonNull Encryption encryption) {
		if (encryption == null) {
			throw new IllegalArgumentException("Encryption is null");
		}
		this.encryption = encryption;
	}

	public void setPayloadEncryptionKey(@Nullable String payloadEncryptionKey) {
		this.payloadEncryptionKey = payloadEncryptionKey;
	}

	public @NonNull Encryption getEncryption() {
		return encryption;
	}

	public @Nullable String getPayloadEncryptionKey() {
		return payloadEncryptionKey;
	}

	public String getUserId() {
		return userId;
	}

	void setUserId(String userId) {
		this.userId = userId;
	}
	
	/**
	 * Checks the internal consistency of the conversation object (temporary solution)
	 */
	void checkInternalConsistency() throws IllegalStateException {
		if (encryption == null) {
			throw new IllegalStateException("Missing encryption");
		}

		switch (state) {
			case LOGGED_IN:
				if (StringUtils.isNullOrEmpty(userId)) {
					throw new IllegalStateException("Missing user id");
				}
				if (StringUtils.isNullOrEmpty(payloadEncryptionKey)) {
					throw new IllegalStateException("Missing payload encryption key");
				}
				break;
			case LOGGED_OUT:
				throw new IllegalStateException("Invalid conversation state: " + state);
			default:
				break;
		}
	}

}
