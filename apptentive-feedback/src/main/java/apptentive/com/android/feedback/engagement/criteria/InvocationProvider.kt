package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.core.Converter
import apptentive.com.android.feedback.CRITERIA
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.util.Log

internal interface InvocationProvider {
    fun getInvocations(event: Event): List<Invocation>?
}

internal class CachedInvocationProvider(
    private val data: Map<String, List<InvocationData>>,
    private val converter: Converter<InvocationData, Invocation>
) : InvocationProvider {
    private val cache = mutableMapOf<Event, List<Invocation>>()

    override fun getInvocations(event: Event): List<Invocation>? {
        val cached = cache[event]
        if (cached != null) {
            Log.v(CRITERIA, "Using cached invocations for event: $event")
            return cached
        }

        val rawInvocations = data[event.fullName]
        if (rawInvocations == null) {
            Log.v(CRITERIA, "No invocations for event: $event")
            return null
        }

        try {
            val targets = rawInvocations.map { converter.convert(it) }
            cache[event] = targets
            Log.v(CRITERIA, "Cached invocations for event: $event")
            return targets
        } catch (e: Exception) {
            Log.e(CRITERIA, "Exception while converting invocations for: $event", e)
        }

        return null
    }
}
