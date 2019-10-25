package apptentive.com.android.feedback.engagement

import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.interactions.Interaction
import java.lang.IllegalStateException
import java.util.*

class MockInteractionEngagement : InteractionEngagement {
    private val results: Queue<EngagementResult> = LinkedList()

    override fun engage(context: EngagementContext, interaction: Interaction): EngagementResult {
        return results.poll() ?: throw IllegalStateException("No more results")
    }

    fun addResult(result: EngagementResult): MockInteractionEngagement {
        results.offer(result)
        return this
    }
}