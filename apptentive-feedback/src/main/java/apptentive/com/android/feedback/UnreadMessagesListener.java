package apptentive.com.android.feedback;

/**
 * This interface allows you to receive a notification when the number of unread messages changes.
 */
public interface UnreadMessagesListener {
	/**
	 * This method is called if the number of unread messages waiting to be viewed by your customer
	 * increases or decreases. Therefore, it will be called when new messages are available to be
	 * viewed, as well as when a message is viewed for the first time. You can use the count returned
	 * to decorate a view with a badge indicating how many unread messages are waiting to be seen.
	 *
	 * @param unreadMessages The total number of unread messages waiting to be viewed by the user.
	 */
	void onUnreadMessageCountChanged(int unreadMessages);
}
