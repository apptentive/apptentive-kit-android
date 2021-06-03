package com.apptentive.android.sdk.conversation;

class ConversationLoadException extends Exception {
	public ConversationLoadException(String message) {
		super(message);
	}
	public ConversationLoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
