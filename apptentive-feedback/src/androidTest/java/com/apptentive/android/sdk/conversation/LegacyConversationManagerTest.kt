package com.apptentive.android.sdk.conversation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.DateTime
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Conversation
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.EngagementManifest
import apptentive.com.android.feedback.model.EngagementRecord
import apptentive.com.android.feedback.model.EngagementRecords
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.model.VersionHistoryItem
import com.apptentive.android.sdk.Encryption
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
    fun testMigrationFrom400() = testMigration("4.0.0", EncryptionFactory.NULL)

    @Test
    fun testMigrationFrom562() = testMigration("5.6.2", EncryptionFactory.NULL)

    private fun testMigration(sdkVersion: String, encryption: Encryption) {
        pushFiles(sdkVersion)

        val manager = LegacyConversationManager(context, encryption)
        val legacyData = manager.loadLegacyConversationData(context)
            ?: throw AssertionError("Unable to load legacy conversation")

        val expected = createExpectedConversation(sdkVersion)
        val actual = legacyData.toConversation()

        Assert.assertEquals(expected, actual)
    }

    private fun createExpectedConversation(sdkVersion: String): Conversation {
        if (sdkVersion == "4.0.0") {
            return Conversation(
                localIdentifier = "1c1042b6-8522-4952-b336-50d3b1807877",
                conversationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYW5vbiIsImlzcyI6ImFwcHRlbnRpdmUiLCJzdWIiOiI2MGIyYzg0ODRlYjAyYTZiNjUwZDRmZGMiLCJhcHBfaWQiOiI1MjFmOWJiYTY4ZTI3NThiNmQwMDA5OGQiLCJpYXQiOjE2MjIzMjk0MTZ9.x9u2nHHGLEEFRQOlo5XOwiSVjJ89how-ThKuITv4r0cXC2Lu7U-11e0MfKdModaZ6uppv4VzXF-MnLeLa64Tug",
                conversationId = "60b2c8484eb02a6b650d4fdc",
                device = Device(
                    osName = "Android",
                    osVersion = "9",
                    osBuild = "5124027",
                    osApiLevel = 28,
                    manufacturer = "Google",
                    model = "Android SDK built for x86_64",
                    board = "goldfish_x86_64",
                    product = "sdk_gphone_x86_64",
                    brand = "google",
                    cpu = "x86_64",
                    device = "generic_x86_64",
                    uuid = "cc736039c35ef28b",
                    buildType = "user",
                    buildId = "PSR1.180720.075",
                    carrier = "Android",
                    currentCarrier = "Android",
                    networkType = "LTE",
                    bootloaderVersion = "unknown",
                    radioVersion = "1.0.0.0",
                    localeCountryCode = "US",
                    localeLanguageCode = "en",
                    localeRaw = "en_US",
                    utcOffset = -18000,
                    advertiserId = null,
                    customData = CustomData(
                        content = mapOf(
                            "device-int" to 20,
                            "device-bool" to false,
                            "device-str" to "device"
                        )
                    )
                ),
                person = Person(
                    id = "60b2c8484eb02a6b650d4fdb",
                    email = "person@company.com",
                    name = "First Second",
                    customData = CustomData(
                        content = mapOf(
                            "person-int" to 10,
                            "person-bool" to true,
                            "person-str" to "person"
                        )
                    )
                ),
                sdk = SDK(
                    version = "4.0.0",
                    platform = "Android"
                ),
                appRelease = AppRelease(
                    type = "android",
                    identifier = "apptentive.com.android.feedback.test",
                    versionCode = 100000,
                    versionName = "1.0.0",
                    targetSdkVersion = "30",
                    debug = false,
                    inheritStyle = false,
                    overrideStyle = false,
                    appStore = null
                ),
                engagementData = EngagementData(
                    events = EngagementRecords(
                        records = mutableMapOf(
                            Event.parse("com.apptentive#app#launch") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622329412578E9)
                            ),
                            Event.parse("local#app#love_dialog_test") to EngagementRecord(
                                versionCode = 100000,
                                versionName = "1.0.0",
                                lastInvoked = DateTime(seconds = 1.622329453678E9)
                            )
                        )
                    ),
                    interactions = EngagementRecords(),
                    versionHistory = VersionHistory(
                        items = listOf(
                            VersionHistoryItem(
                                timestamp = 1.622329411908E9,
                                versionCode = 100000,
                                versionName = "1.0.0"
                            )
                        )
                    )
                ),
                engagementManifest = EngagementManifest()
            )
        }
        else TODO("Implement me")
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
