package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.core.Converter
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.FailureInteractionCriteria
import apptentive.com.android.feedback.model.InvocationData
import apptentive.com.android.feedback.test.TestCase
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.lang.RuntimeException

class CachedTargetRepositoryTest : TestCase() {
    @Test
    fun getTargets() {
        val event = Event.local("event")

        val interactionId = "123456789"
        val data = mapOf(
            event.fullName to listOf(InvocationData(interactionId = interactionId))
        )

        val target = Invocation(
            interactionId = interactionId,
            criteria = FailureInteractionCriteria
        )
        val result = mutableListOf<Invocation>()
        val converter = object : Converter<InvocationData, Invocation> {
            override fun convert(source: InvocationData): Invocation {
                result.add(target)
                return target
            }
        }
        val repository = CachedTargetRepository(data, converter)
        repository.getTargets(event)
        assertThat(result[0]).isEqualTo(target)

        result.clear()

        repository.getTargets(event)
        assertThat(result).isEmpty()
    }

    @Test
    fun testMissingData() {
        TODO("Try to get targets for missing event (there should be no raw data)")
    }

    @Test
    fun testExceptionWhileConvertingRawData() {
        val converter = object : Converter<InvocationData, Invocation> {
            override fun convert(source: InvocationData): Invocation {
                throw RuntimeException("Error")
            }
        }

        TODO("Converter should throw an exception")
    }
}