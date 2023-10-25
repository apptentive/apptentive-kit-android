package com.apptentive.android.sdk.conversation;

import androidx.annotation.Nullable;

/**
 * Abstract legacy [ConversationManager] implementation from the implementation.
 * See: https://github.com/apptentive/apptentive-android/blob/master/apptentive/src/main/java/com/apptentive/android/sdk/conversation/ConversationManager.java
 */
public interface LegacyConversationManager {
    /**
     * Attempts to load a legacy conversation data
     * @return null if not legacy data found (or can't be loaded)
     */
    @Nullable ConversationData loadLegacyConversationData(LegacyConversationMetadata conversationMetadata);

    /**
     * Attempts to load a legacy conversation meta data
     * @return null if not legacy meta data found (or can't be loaded)
     */
    @Nullable LegacyConversationMetadata loadLegacyConversationMetadata();

    /**
     * Attempts to load a legacy conversation data that has come active (i.e. has a encryption key)
     * @param conversationMetadataItem metadata item to load
     * @return null if not legacy data found (or can't be loaded)
     */
    @Nullable ConversationData loadEncryptedLegacyConversationData(LegacyConversationMetadataItem conversationMetadataItem);
}
