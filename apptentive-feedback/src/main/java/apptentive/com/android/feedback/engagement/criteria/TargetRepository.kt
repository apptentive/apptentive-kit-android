package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.core.Converter
import apptentive.com.android.feedback.CRITERIA
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.model.TargetData
import apptentive.com.android.util.Log

interface TargetRepository {
    fun getTargets(event: Event): List<Target>?
}

class CachedTargetRepository(
    private val data: Map<String, List<TargetData>>,
    private val converter: Converter<TargetData, Target>
) : TargetRepository {
    private val cache = mutableMapOf<Event, List<Target>>()

    override fun getTargets(event: Event): List<Target>? {
        val cached = cache[event]
        if (cached != null) {
            Log.v(CRITERIA, "Using cached targets for event: $event")
            return cached
        }

        val rawTargets = data[event.fullName]
        if (rawTargets == null) {
            Log.v(CRITERIA, "No targets for event: $event")
            return null
        }

        try {
            val targets = rawTargets.map { converter.convert(it) }
            cache[event] = targets
            Log.v(CRITERIA, "Cached targets for event: $event")
            return targets
        } catch (e: Exception) {
            Log.e(CRITERIA, "Exception while converting targets for: $event", e)
        }

        return null
    }
}