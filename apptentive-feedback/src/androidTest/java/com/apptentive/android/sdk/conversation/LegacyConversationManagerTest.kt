package com.apptentive.android.sdk.conversation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.feedback.model.Conversation
import com.apptentive.android.sdk.encryption.EncryptionFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.File

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
        copyAsset(sdkVersion, context.dataDir)
    }

    private fun copyAsset(path: String, dstDir: File) {
        val list = context.assets.list(path)
        if (list != null && list.isNotEmpty()) {
            list.forEach { copyAsset("$path/$it", dstDir) }
        } else {
            val dst = File(dstDir, path.removePrefixPathComponent())
            val parentDir = dst.parentFile
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }
            print("Copying $path -> $dst")

            context.assets.open(path).use { input ->
                dst.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

private fun String.removePrefixPathComponent(): String {
    val tokens = split("/")
    return tokens.subList(1, tokens.size).joinToString("/")
}
