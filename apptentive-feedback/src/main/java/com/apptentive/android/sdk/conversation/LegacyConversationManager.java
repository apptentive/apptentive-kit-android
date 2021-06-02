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
    @Nullable ConversationData loadLegacyConversationData();
}
