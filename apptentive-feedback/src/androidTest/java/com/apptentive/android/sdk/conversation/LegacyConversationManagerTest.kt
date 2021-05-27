package com.apptentive.android.sdk.conversation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.feedback.model.Conversation
import com.apptentive.android.sdk.encryption.EncryptionFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LegacyConversationManagerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun before() {
        // clear up device storage
        MigrationTestUtils.clearDeviceStorage(context);
    }

    @Test
    fun testMigrationFrom400() {
        pushFiles("4.0.0")

        val manager = LegacyConversationManager(context, EncryptionFactory.NULL) // we don't have any encryption support in 4.0.0
        val legacyData = manager.loadLegacyConversationData(context)
            ?: throw AssertionError("Unable to load legacy conversation")

        val expected = createExpectedConversation("4.0.0")
        val actual = legacyData.toConversation()

        Assert.assertEquals(expected, actual)
    }

    private fun createExpectedConversation(sdkVersion: String): Conversation {
        TODO()
    }

    private fun pushFiles(sdkVersion: String) {
        TODO()
    }
}