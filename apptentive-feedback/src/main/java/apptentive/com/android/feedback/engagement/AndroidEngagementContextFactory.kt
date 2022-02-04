package apptentive.com.android.feedback.engagement

import android.content.Context
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.feedback.platform.AndroidEngagementContext

interface AndroidEngagementContextFactory {
    fun engagementContext(): EngagementContext
}

class AndroidEngagementContextProvider(
    private val activity: Context,
    private val engagement: Engagement,
    private val payloadSender: PayloadSender,
    private val executor: Executors
) : Provider<AndroidEngagementContextFactory> {
    override fun get(): AndroidEngagementContextFactory = object : AndroidEngagementContextFactory {
        override fun engagementContext(): EngagementContext {
            return AndroidEngagementContext(activity, engagement, payloadSender, executor)
        }
    }
}
