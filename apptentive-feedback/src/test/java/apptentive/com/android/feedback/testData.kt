package apptentive.com.android.feedback

import apptentive.com.android.feedback.model.*

const val SDK_VERSION = "6.0.0"
const val API_VERSION = 9

val mockDevice = Device(
    osName = "Android",
    osVersion = "10",
    osBuild = "osBuild",
    osApiLevel = 29,
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
    utcOffset = 18000,
    advertiserId = "advertiserId",
    customData = CustomData(content = mapOf(Pair("key", "value"))),
    integrationConfig = IntegrationConfig(
        apptentive = IntegrationConfigItem(
            contents = mapOf(
                Pair(
                    "apptentive_key",
                    "apptentive_value"
                )
            )
        ),
        amazonAwsSns = IntegrationConfigItem(
            contents = mapOf(
                Pair(
                    "amazon_key",
                    "amazon_value"
                )
            )
        ),
        urbanAirship = IntegrationConfigItem(
            contents = mapOf(
                Pair(
                    "urban_key",
                    "urban_value"
                )
            )
        )
    )
)

val mockSdk = SDK(
    version = SDK_VERSION,
    platform = "Android",
    distribution = "Default",
    distributionVersion = SDK_VERSION,
    programmingLanguage = "Kotlin",
    authorName = "Apptentive",
    authorEmail = "support@apptentive.com"
)

val mockAppRelease = AppRelease(
    type = "Android",
    identifier = "com.test.app",
    versionName = "1.0.0",
    versionCode = 1,
    targetSdkVersion = "29",
    debug = true,
    inheritStyle = true,
    overrideStyle = false,
    appStore = "google"
)

val mockPerson = Person()