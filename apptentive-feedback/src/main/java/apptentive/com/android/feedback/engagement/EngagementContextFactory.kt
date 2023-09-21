package apptentive.com.android.feedback.engagement

import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.Provider
import apptentive.com.android.feedback.payload.PayloadContext
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
interface EngagementContextFactory {
    fun engagementContext(): EngagementContext
}

internal class EngagementContextProvider(
    private val engagement: Engagement,
    private val payloadSender: PayloadSender,
    private val executor: Executors,
) : Provider<EngagementContextFactory> {
    override fun get(): EngagementContextFactory = object : EngagementContextFactory {
        override fun engagementContext(): EngagementContext {
            return EngagementContext(engagement, payloadSender, executor)
        }
    }
}
