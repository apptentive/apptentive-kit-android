package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.feedback.engagement.criteria.Field.*
import apptentive.com.android.feedback.model.*

data class DefaultTargetingState(
    private val person: Person,
    private val device: Device,
    private val sdk: SDK,
    private val appRelease: AppRelease,
    private val engagementData: EngagementData
) : TargetingState {
    override fun getValue(field: Field): Any? {
        return when (field) {
            is application.version_code -> appRelease.versionCode
            is application.version_name -> Version.tryParse(appRelease.versionName)

            is sdk.version -> Version.tryParse(sdk.version)

            is current_time -> DateTime.now()

            is is_update.version_code -> TODO()
            is is_update.version_name -> TODO()

            is time_at_install.total -> TODO()
            is time_at_install.version_code -> TODO()
            is time_at_install.version_name -> TODO()

            is code_point.invokes.total -> engagementData.events.totalInvokes(field.event)
            is code_point.invokes.version_code -> engagementData.events.invokesForVersionCode(
                field.event,
                appRelease.versionCode
            )
            is code_point.invokes.version_name -> engagementData.events.invokesForVersionName(
                field.event,
                appRelease.versionName
            )
            is code_point.last_invoked_at.total -> engagementData.events.lastInvoke(field.event)

            is interactions.invokes.total -> engagementData.interactions.totalInvokes(field.interactionId)
            is interactions.invokes.version_code -> engagementData.interactions.invokesForVersionCode(
                field.interactionId,
                appRelease.versionCode
            )
            is interactions.invokes.version_name -> engagementData.interactions.invokesForVersionName(
                field.interactionId,
                appRelease.versionName
            )
            is interactions.last_invoked_at.total -> engagementData.interactions.lastInvoke(field.interactionId)

            is person.name -> person.name
            is person.email -> person.email
            is person.custom_data -> person.customData[field.key]

            is device.os_name -> device.osName
            is device.os_version -> device.osVersion
            is device.os_build -> device.osBuild
            is device.manufacturer -> device.manufacturer
            is device.model -> device.model
            is device.board -> device.board
            is device.product -> device.product
            is device.brand -> device.brand
            is device.cpu -> device.cpu
            is device.hardware -> null // this key is unknown
            is device.device -> device.device
            is device.uuid -> device.uuid
            is device.carrier -> device.carrier
            is device.current_carrier -> device.currentCarrier
            is device.network_type -> device.networkType
            is device.build_type -> device.buildType
            is device.build_id -> device.buildId
            is device.bootloader_version -> device.bootloaderVersion
            is device.radio_version -> device.radioVersion
            is device.locale_country_code -> device.localeCountryCode
            is device.locale_language_code -> device.localeLanguageCode
            is device.locale_raw -> device.localeRaw
            is device.os_api_level -> device.osApiLevel
            is device.utc_offset -> device.utcOffset
            is device.custom_data -> device.customData[field.key]

            else -> null // TODO: throw an exception?
        }
    }
}