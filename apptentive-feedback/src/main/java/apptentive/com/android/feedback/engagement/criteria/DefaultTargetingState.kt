package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.criteria.Field.*
import apptentive.com.android.feedback.model.AppRelease
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.SDK

data class DefaultTargetingState(
    private val person: Person,
    private val device: Device,
    private val sdk: SDK,
    private val appRelease: AppRelease
) : TargetingState {
    override fun getValue(field: Field): Value {
        return when (field) {
            is application.version_code -> Value.number(appRelease.versionCode)
            is application.version_name -> Value.version(appRelease.versionName)
            is sdk.version -> Value.version(sdk.version)
            is current_time -> TODO()
            is is_update.version_code -> TODO()
            is is_update.version_name -> TODO()
            is time_at_install.total -> TODO()
            is time_at_install.version_code -> TODO()
            is time_at_install.version_name -> TODO()
            is code_point.invokes.total -> TODO()
            is code_point.invokes.version_code -> TODO()
            is code_point.invokes.version_name -> TODO()
            is code_point.last_invoked_at.total -> TODO()
            is interactions.invokes.total -> TODO()
            is interactions.invokes.version_code -> TODO()
            is interactions.invokes.version_name -> TODO()
            is interactions.last_invoked_at.total -> TODO()
            is person.name -> Value.string(person.name)
            is person.email -> Value.string(person.email)
            is person.custom_data -> {
                val value = person.customData[field.key]
                return Value.any(value)
            }
            is device.os_name -> Value.string(device.osName)
            is device.os_version -> Value.string(device.osVersion)
            is device.os_build -> Value.string(device.osBuild)
            is device.manufacturer -> Value.string(device.manufacturer)
            is device.model -> Value.string(device.model)
            is device.board -> Value.string(device.board)
            is device.product -> Value.string(device.product)
            is device.brand -> Value.string(device.brand)
            is device.cpu -> Value.string(device.cpu)
            is device.hardware -> Value.Null // this key is unknown
            is device.device -> Value.string(device.device)
            is device.uuid -> Value.string(device.uuid)
            is device.carrier -> Value.string(device.carrier)
            is device.current_carrier -> Value.string(device.currentCarrier)
            is device.network_type -> Value.string(device.networkType)
            is device.build_type -> Value.string(device.buildType)
            is device.build_id -> Value.string(device.buildId)
            is device.bootloader_version -> Value.string(device.bootloaderVersion)
            is device.radio_version -> Value.string(device.radioVersion)
            is device.locale_country_code -> Value.string(device.localeCountryCode)
            is device.locale_language_code -> Value.string(device.localeLanguageCode)
            is device.locale_raw -> Value.string(device.localeRaw)
            is device.os_api_level -> Value.number(device.osApiLevel)
            is device.utc_offset -> Value.number(device.utcOffset)
            is device.custom_data -> {
                val value = device.customData[field.key]
                return Value.any(value)
            }

            else -> Value.Null // FIXME: add error description or return unknown value
        }
    }
}