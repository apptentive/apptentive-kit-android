package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.util.readAssetFile
import org.junit.Ignore
import org.junit.Test

class InvocationConverterTest : TestCase() {
    @Test
    @Ignore
    fun convert() {
        val json = readAssetFile("manifest.json")
        val manifest = JsonConverter.fromJson<EngagementManifest>(json)
        val converter = InvocationConverter
        val result = manifest.targets.mapValues { (_, targets) ->
            targets.map { converter.convert(it) }
        }
    }
}
