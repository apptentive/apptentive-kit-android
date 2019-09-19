package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.conversation.SingleFileConversationSerializer
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ConversationTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun binaryFileSerialization() {
        val expected = Conversation(
            localIdentifier = "localIdentifier",
            conversationToken = "conversationToken",
            conversationId = "conversationId",
            device = Device(
                osName = "osName",
                osVersion = "osVersion",
                osBuild = "osBuild",
                osApiLevel = 10,
                manufacturer = "manufacturer",
                model = "model",
                board = "board",
                product = "product",
                brand = "brand",
                cpu = "cpu",
                device = "device",
                uuid = "uuid",
                buildType = "buildType",
                buildId = "buildId",
                carrier = "carrier",
                currentCarrier = "currentCarrier",
                networkType = "networkType",
                bootloaderVersion = "bootloaderVersion",
                radioVersion = "radioVersion",
                localeCountryCode = "localeCountryCode",
                localeLanguageCode = "localeLanguageCode",
                localeRaw = "localeRaw",
                utcOffset = -8,
                advertiserId = "advertiserId",
                customData = CustomData(mapOf(Pair("key", "value"))),
                integrationConfig = IntegrationConfig(
                    apptentive = IntegrationConfigItem(mapOf(Pair("key1", "value1"))),
                    amazonAwsSns = IntegrationConfigItem(mapOf(Pair("key2", "value2"))),
                    urbanAirship = IntegrationConfigItem(mapOf(Pair("key3", "value3"))),
                    parse = IntegrationConfigItem(mapOf(Pair("key4", "value4")))
                )
            ),
            person = Person(
                id = "id",
                email = "email",
                name = "name",
                facebookId = "facebookId",
                phoneNumber = "phoneNumber",
                street = "street",
                city = "city",
                zip = "zip",
                country = "country",
                birthday = "birthday",
                mParticleId = "mParticleId",
                customData = CustomData(mapOf(Pair("key5", "value5")))
            )
        )

        val file = tempFolder.tempFile()

        val serializer = SingleFileConversationSerializer(file)

        // should return no conversation before anything was saved
        assertThat(serializer.loadConversation()).isNull()

        // save conversation
        serializer.saveConversation(expected)

        val actual = serializer.loadConversation()
        assertThat(expected).isEqualTo(actual)
    }

    @Test
    fun binarySerializationMissingData() {
        val expected = Conversation(
            localIdentifier = "localIdentifier",
            device = Device(
                osName = "osName",
                osVersion = "osVersion",
                osBuild = "osBuild",
                osApiLevel = 10,
                manufacturer = "manufacturer",
                model = "model",
                board = "board",
                product = "product",
                brand = "brand",
                cpu = "cpu",
                device = "device",
                uuid = "uuid",
                buildType = "buildType",
                buildId = "buildId",
                localeCountryCode = "EN",
                localeLanguageCode = "en_us",
                localeRaw = "localeRaw",
                utcOffset = -8
            ),
            person = Person()
        )

        val file = tempFolder.tempFile()

        val serializer = SingleFileConversationSerializer(file)

        // save conversation
        serializer.saveConversation(expected)

        val actual = serializer.loadConversation()
        assertThat(expected).isEqualTo(actual)
    }
}

fun TemporaryFolder.tempFile(): File {
    val file = createTempFile()
    file.delete()
    return file
}