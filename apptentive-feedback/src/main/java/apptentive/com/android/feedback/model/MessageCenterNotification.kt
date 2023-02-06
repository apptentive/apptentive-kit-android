package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.Apptentive

/**
 * Data class for sharing the Message Center information of the Apptentive SDK in an observable format
 *
 * @param canShowMessageCenter whether the Message Center interaction will show with `Apptentive.showMessageCenter()`
 * @param unreadMessageCount the count of unread messages in Message Center
 * @param personName the current set name of the customer. The customer can change it in Message Center.
 * @param personEmail the current set email of the customer. The customer can change it in Message Center if it is set to optional.
 */
data class MessageCenterNotification(
    val canShowMessageCenter: Boolean = Apptentive.canShowMessageCenter(),
    val unreadMessageCount: Int = Apptentive.getUnreadMessageCount(),
    val personName: String? = Apptentive.getPersonName(),
    val personEmail: String? = Apptentive.getPersonEmail()
)
