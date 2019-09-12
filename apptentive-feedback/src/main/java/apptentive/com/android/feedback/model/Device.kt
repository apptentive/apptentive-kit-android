package apptentive.com.android.feedback.model

data class Device(
    val osName: String,
    val osVersion: String,
    val osBuild: String,
    val osApiLevel: Int,
    val manufacturer: String,
    val model: String,
    val board: String,
    val product: String,
    val brand: String,
    val cpu: String,
    val device: String,
    val uuid: String,
    val buildType: String,
    val buildId: String
) {
    private var carrier: String? = null
    private var currentCarrier: String? = null
    private var networkType: String? = null

    private var bootloaderVersion: String? = null
    private var radioVersion: String? = null

    private var localeCountryCode: String? = null
    private var localeLanguageCode: String? = null
    private var localeRaw: String? = null
    private var utcOffset: String? = null
    private var advertiserId: String? = null

    val customData = CustomData()
    val integrationConfig = IntegrationConfig()
}
