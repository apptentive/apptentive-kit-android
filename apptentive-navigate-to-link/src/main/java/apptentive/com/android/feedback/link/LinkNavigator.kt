package apptentive.com.android.feedback.link

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.link.interaction.NavigateToLinkInteraction
import apptentive.com.android.feedback.platform.tryStartActivity

internal object LinkNavigator {
    @MainThread
    fun navigate(
        context: EngagementContext,
        activityContext: Context,
        interaction: NavigateToLinkInteraction
    ) = navigate(
        context = context,
        interaction = interaction
    ) {
        activityContext.tryStartActivity(interaction.createIntent()) // this way we can use unit-tests
    }

    @MainThread
    @VisibleForTesting
    fun navigate(
        context: EngagementContext,
        interaction: NavigateToLinkInteraction,
        activityLauncher: () -> Boolean
    ) {
        val success = activityLauncher.invoke()
        context.executors.state.execute {
            val data = mapOf(
                KEY_URL to interaction.url,
                KEY_TARGET to interaction.target,
                KEY_SUCCESS to success
            )
            context.engage(
                event = Event.internal(CODE_POINT_NAVIGATE, interaction.type),
                interactionId = interaction.id,
                data = data
            )
        }
    }

    private const val KEY_URL = "url"
    private const val KEY_TARGET = "target"
    private const val KEY_SUCCESS = "success"
    private const val CODE_POINT_NAVIGATE = "navigate"
}

private fun NavigateToLinkInteraction.createIntent(): Intent {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    if (target == NavigateToLinkInteraction.Target.new) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    } else {
        intent.putExtra("SELF_TARGET", true)
    }
    return intent
}
