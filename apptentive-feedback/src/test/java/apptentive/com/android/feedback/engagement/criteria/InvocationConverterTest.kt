package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.mockAppRelease
import apptentive.com.android.feedback.mockDevice
import apptentive.com.android.feedback.mockPerson
import apptentive.com.android.feedback.mockRandomSampling
import apptentive.com.android.feedback.mockSdk
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.readAssetFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvocationConverterTest : TestCase() {
    private val state = DefaultTargetingState(mockPerson, mockDevice, mockSdk, mockAppRelease, mockRandomSampling, EngagementData())

    @Test
    fun convertToClass() {
        val json = readAssetFile("manifest_structure_test.json")
        val manifest = JsonConverter.fromJson<EngagementManifest>(json)
        val converter = InvocationConverter
        val result = manifest.targets.mapValues { (_, targets) ->
            targets.map { converter.convert(it) }
        }
        assertEquals(2, result.size)
    }

    @Test
    fun testMultipleCriteriaOneClause() {
        val stateCustomData = state.copy(
            device = mockDevice.copy(
                customData = CustomData(
                    mapOf("string_qwerty" to "qwerty")
                )
            )
        )

        val json = readAssetFile("manifest_structure_test.json")
        val manifest = JsonConverter.fromJson<EngagementManifest>(json)
        val converter = InvocationConverter
        val invocations = manifest.targets.mapValues { (_, targets) ->
            targets.map { converter.convert(it) }
        }
        invocations["local#app#criteria_together"]?.let { invocations ->
            assertEquals(1, invocations.size)

            val criteria = invocations[0].criteria
            assertTrue(criteria.isMet(stateCustomData))
        }
    }

    @Test
    fun testMultipleCriteriaSeparateClause() {
        val stateCustomData = state.copy(
            device = mockDevice.copy(
                customData = CustomData(
                    mapOf("string_qwerty" to "qwerty")
                )
            )
        )

        val json = readAssetFile("manifest_structure_test.json")
        val manifest = JsonConverter.fromJson<EngagementManifest>(json)
        val converter = InvocationConverter
        val invocations = manifest.targets.mapValues { (_, targets) ->
            targets.map { converter.convert(it) }
        }
        invocations["local#app#criteria_separate"]?.let { invocations ->
            assertEquals(1, invocations.size)

            val criteria = invocations[0].criteria
            assertTrue(criteria.isMet(stateCustomData))
        }
    }
}
