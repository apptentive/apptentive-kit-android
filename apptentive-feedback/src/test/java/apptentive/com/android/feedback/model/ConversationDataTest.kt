package apptentive.com.android.feedback.model

import apptentive.com.android.debug.Assert
import apptentive.com.android.util.decodeFromByteArray
import apptentive.com.android.util.encodeToByteArray
import org.junit.Test

class ConversationDataTest {
    @Test
    fun binarySerialization() {
        val expected = ConversationData(
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
                utcOffset = "utcOffset",
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

        val bytes = encodeToByteArray { out ->
            out.encodeIntegrationConfigItem(expected)
        }
        val actual =
            decodeFromByteArray(bytes) { input -> input.decodeConversationData() }

        Assert.assertEqual(expected, actual)
    }

    @Test
    fun binarySerializationMissingData() {
        val expected = ConversationData(
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
                buildId = "buildId"
            ),
            person = Person()
        )

        val bytes = encodeToByteArray { out ->
            out.encodeIntegrationConfigItem(expected)
        }
        val actual =
            decodeFromByteArray(bytes) { input -> input.decodeConversationData() }

        Assert.assertEqual(expected, actual)
    }
}