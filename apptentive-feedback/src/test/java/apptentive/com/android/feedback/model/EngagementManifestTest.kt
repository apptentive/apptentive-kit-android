package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.test.TestCase
import org.junit.Test

import org.junit.Assert.*
import org.junit.Ignore

class EngagementManifestTest : TestCase() {
    @Test
    @Ignore
    fun getInteractions() {
        val json = readText("manifest.json")
        val manifest = EngagementManifest.fromJson(json)
        TODO()
    }
}