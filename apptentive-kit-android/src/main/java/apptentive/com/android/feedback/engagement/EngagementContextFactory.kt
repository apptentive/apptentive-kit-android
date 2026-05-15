package apptentive.com.android.feedback.engagement

import apptentive.com.android.core.Provider
import apptentive.com.android.core.concurrent.Executors
import apptentive.com.android.feedback.payload.PayloadSender

internal interface EngagementContextFactory {
    fun engagementContext(): EngagementContext
}

internal class EngagementContextProvider(
    private val engagement: Engagement,
    private val payloadSender: PayloadSender,
    private val executor: Executors
) : Provider<EngagementContextFactory> {
    override fun get(): EngagementContextFactory = object : EngagementContextFactory {
        override fun engagementContext(): EngagementContext {
            return EngagementContext(engagement, payloadSender, executor)
        }
    }
}
