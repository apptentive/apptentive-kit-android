package apptentive.com.android.feedback.model

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.readAssetFile
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EngagementManifestTest : TestCase() {
    @Test
    fun getInteractions() {
        val json = readAssetFile("manifest.json")
        val manifest = JsonConverter.fromJson<EngagementManifest>(json)
        assertThat(manifest.interactions).isEqualTo(
            listOf(
                InteractionData(
                    id = "1234567890",
                    displayType = "display_type",
                    type = "MyInteraction",
                    configuration = mapOf(
                        "string_key" to "value",
                        "int_key" to 10.0,
                        "boolean_key" to true,
                        "null_key" to null
                    )
                )
            )
        )
    }
}
