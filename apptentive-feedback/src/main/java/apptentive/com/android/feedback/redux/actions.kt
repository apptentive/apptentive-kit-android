package apptentive.com.android.feedback.redux

import org.rekotlin.Action

/*
 * User data
 * - ConversationSetUserData(key, value)
 * - ConversationRemoveUserData(key)
 * - ConversationSetUserCustomData(key, value)
 * - ConversationRemoveUserCustomData(key, value)
 *
 * Device data
 * - ConversationSetDeviceCustomData(key, value)
 * - ConversationRemoveDeviceCustomData(key, value)
 */

//region Conversation Fetch

internal abstract class ConversationAction(val localConversationIdentifier: String) : Action

internal abstract class ConversationMutatingAction(localConversationIdentifier: String) :
    ConversationAction(localConversationIdentifier)

internal class ConversationFetchAction(
    localConversationIdentifier: String
) : ConversationMutatingAction(localConversationIdentifier)

internal class ConversationFetchCompletedAction(
    localConversationIdentifier: String,
    val conversationIdentifier: String,
    val conversationToken: String
) : ConversationMutatingAction(localConversationIdentifier)

internal class ConversationFetchFailedAction(
    localConversationIdentifier: String,
    val error: Exception
) : ConversationMutatingAction(localConversationIdentifier)

internal class ConversationSaveFailedAction(
    localConversationIdentifier: String,
    val error: Exception
) : ConversationAction(localConversationIdentifier)

//endregion