package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.TestCase
import apptentive.com.android.core.Converter
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.FailureInteractionCriteria
import apptentive.com.android.feedback.model.InvocationData
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test
import java.lang.RuntimeException

class CachedInvocationRepositoryTest : TestCase() {
    @Test
    fun getInvocations() {
        val event = Event.local("event")

        val interactionId = "123456789"
        val data = mapOf(
            event.fullName to listOf(InvocationData(interactionId = interactionId))
        )

        val invocation = Invocation(
            interactionId = interactionId,
            criteria = FailureInteractionCriteria
        )
        val result = mutableListOf<Invocation>()
        val converter = object : Converter<InvocationData, Invocation> {
            override fun convert(source: InvocationData): Invocation {
                result.add(invocation)
                return invocation
            }
        }
        val provider = CachedInvocationProvider(data, converter)
        provider.getInvocations(event)
        assertThat(result[0]).isEqualTo(invocation)

        result.clear()

        provider.getInvocations(event)
        assertThat(result).isEmpty()
    }

    @Test
    @Ignore
    fun testMissingData() {
        TODO("Try to get invocations for missing event (there should be no raw data)")
    }

    @Test
    @Ignore
    fun testExceptionWhileConvertingRawData() {
        val converter = object : Converter<InvocationData, Invocation> {
            override fun convert(source: InvocationData): Invocation {
                throw RuntimeException("Error")
            }
        }

        TODO("Converter should throw an exception")
    }
}