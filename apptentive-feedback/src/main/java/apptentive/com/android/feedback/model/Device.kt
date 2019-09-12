package apptentive.com.android.feedback.model

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

@Serializable
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
    var carrier: String? = null
        internal set
    var currentCarrier: String? = null
        internal set
    var networkType: String? = null
        internal set
    var bootloaderVersion: String? = null
        internal set
    var radioVersion: String? = null
        internal set
    var localeCountryCode: String? = null
        internal set
    var localeLanguageCode: String? = null
        internal set
    var localeRaw: String? = null
        internal set
    var utcOffset: String? = null
        internal set
    var advertiserId: String? = null
        internal set

    val customData = CustomData()
    val integrationConfig = IntegrationConfig()

    //region kotlinx custom serialization

    @Serializer(forClass = Device::class)
    companion object : KSerializer<Device> {
        override val descriptor: SerialDescriptor = StringDescriptor.withName("Device")

        override fun deserialize(input: Decoder): Device {
            val device = Device(
                osName = input.decodeString(),
                osVersion = input.decodeString(),
                osBuild = input.decodeString(),
                osApiLevel = input.decodeInt(),
                manufacturer = input.decodeString(),
                model = input.decodeString(),
                board = input.decodeString(),
                product = input.decodeString(),
                brand = input.decodeString(),
                cpu = input.decodeString(),
                device = input.decodeString(),
                uuid = input.decodeString(),
                buildType = input.decodeString(),
                buildId = input.decodeString()
            )

            device.carrier = input.decodeNullableString()
            device.currentCarrier = input.decodeNullableString()
            device.networkType = input.decodeNullableString()
            device.bootloaderVersion = input.decodeNullableString()
            device.radioVersion = input.decodeNullableString()
            device.localeCountryCode = input.decodeNullableString()
            device.localeLanguageCode = input.decodeNullableString()
            device.localeRaw = input.decodeNullableString()
            device.utcOffset = input.decodeNullableString()
            device.advertiserId = input.decodeNullableString()

            return device
        }

        override fun serialize(output: Encoder, obj: Device) {
            output.encodeString(obj.osName)
            output.encodeString(obj.osVersion)
            output.encodeString(obj.osBuild)
            output.encodeInt(obj.osApiLevel)
            output.encodeString(obj.manufacturer)
            output.encodeString(obj.model)
            output.encodeString(obj.board)
            output.encodeString(obj.product)
            output.encodeString(obj.brand)
            output.encodeString(obj.cpu)
            output.encodeString(obj.device)
            output.encodeString(obj.uuid)
            output.encodeString(obj.buildType)
            output.encodeString(obj.buildId)

            output.encodeNullableString(obj.carrier)
            output.encodeNullableString(obj.currentCarrier)
            output.encodeNullableString(obj.networkType)
            output.encodeNullableString(obj.bootloaderVersion)
            output.encodeNullableString(obj.radioVersion)
            output.encodeNullableString(obj.localeCountryCode)
            output.encodeNullableString(obj.localeLanguageCode)
            output.encodeNullableString(obj.localeRaw)
            output.encodeNullableString(obj.utcOffset)
            output.encodeNullableString(obj.advertiserId)
        }
    }

    //endregion
}
