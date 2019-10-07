package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.test.TestCase
import org.junit.Test

import org.junit.Assert.*

class EngagementManifestTest : TestCase() {
    @Test
    fun getInteractions() {
        val json = readText("manifest.json")
        val manifest = EngagementManifest.fromJson(json)
        TODO()
    }
}