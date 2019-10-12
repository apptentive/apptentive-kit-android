package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.test.TestCase
import apptentive.com.android.serialization.json.JsonConverter
import org.junit.Ignore
import org.junit.Test

class TargetConverterTest : TestCase() {
    @Test
    @Ignore
    fun convert() {
        val json = readText("manifest.json")
        val manifest = JsonConverter.fromJson<EngagementManifest>(json)
        val converter = TargetConverter()
        val result = manifest.targets.mapValues { (_, targets) ->
            targets.map { converter.convert(it) }
        }
    }
}