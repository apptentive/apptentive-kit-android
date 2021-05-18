package com.apptentive.android.sdk.util;

public class Constants {

    public static final String CONVERSATION_METADATA_FILE = "conversation-v2.meta";
    public static final String CONVERSATION_METADATA_FILE_LEGACY_V1 = "conversation-v1.meta";

    // Encryption
    /**
     * Transformation used for creating an encryption key for payloads using the hex value from the sever.
     */
    public static final String PAYLOAD_ENCRYPTION_KEY_TRANSFORMATION = "AES/CBC/PKCS5Padding";
}
