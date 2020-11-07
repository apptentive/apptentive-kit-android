package apptentive.com.android.feedback.platform

import android.content.Context
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContext

/**
 * Android-specific implementation of the [EngagementContext].
 * @param androidContext - enclosing [android.content.Context] object.
 * @param engagement - target [Engagement] object.
 */
class AndroidEngagementContext(
    val androidContext: Context,
    engagement: Engagement,
    executors: Executors
) : EngagementContext(engagement, executors)