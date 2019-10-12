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
            is application.version_code -> Value.number(
                value = appRelease.versionCode,
                description = "app version code (${appRelease.versionCode})"
            )
            is application.version_name -> Value.version(
                value = appRelease.versionName,
                description = "app version name (${appRelease.versionName})"
            )
            is sdk.version -> Value.version(
                value = sdk.version,
                description = "SDK version (${sdk.version})"
            )
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
            is person.name -> Value.string(
                value = person.name,
                description = "person name (${person.name})"
            )
            is person.email -> Value.string(
                value = person.email,
                description = "person name (${person.email})"
            )
            is device.os_name -> TODO()
            is device.os_version -> TODO()
            is device.os_build -> TODO()
            is device.manufacturer -> TODO()
            is device.model -> TODO()
            is device.board -> TODO()
            is device.product -> TODO()
            is device.brand -> TODO()
            is device.cpu -> TODO()
            is device.hardware -> TODO()
            is device.device -> TODO()
            is device.uuid -> TODO()
            is device.carrier -> TODO()
            is device.current_carrier -> TODO()
            is device.network_type -> TODO()
            is device.build_type -> TODO()
            is device.build_id -> TODO()
            is device.bootloader_version -> TODO()
            is device.radio_version -> TODO()
            is device.locale_country_code -> TODO()
            is device.locale_language_code -> TODO()
            is device.locale_raw -> TODO()
            is device.os_api_level -> TODO()
            is device.utc_offset -> TODO()
            is device.custom_data -> TODO()
            is person.custom_data -> TODO()
            else -> TODO()
        }
    }
}