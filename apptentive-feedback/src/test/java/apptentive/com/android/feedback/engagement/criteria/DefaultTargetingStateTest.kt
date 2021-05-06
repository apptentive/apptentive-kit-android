package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.Field.*
import apptentive.com.android.feedback.mockAppRelease
import apptentive.com.android.feedback.mockDevice
import apptentive.com.android.feedback.mockPerson
import apptentive.com.android.feedback.mockSdk
import apptentive.com.android.feedback.model.CustomData
import apptentive.com.android.feedback.model.Device
import apptentive.com.android.feedback.model.EngagementData
import apptentive.com.android.feedback.model.Person
import apptentive.com.android.feedback.model.VersionHistory
import apptentive.com.android.feedback.model.VersionHistoryItem
import apptentive.com.android.util.MockTimeSource
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DefaultTargetingStateTest : TestCase() {
    private val state = DefaultTargetingState(mockPerson, mockDevice, mockSdk, mockAppRelease, EngagementData())

    @Test
    fun application() {
        val state = state.copy(
            appRelease = mockAppRelease.copy(
                versionName = "1.0.0",
                versionCode = 100000
            )
        )

        assertThat(state.getValue(application.version_code)).isEqualTo(100000)
        assertThat(state.getValue(application.version_name)).isEqualTo(Version.parse("1.0.0"))
    }

    @Test
    fun sdk() {
        val state = state.copy(
            sdk = mockSdk.copy(
                version = "6.5.4"
            )
        )
        assertThat(state.getValue(sdk.version)).isEqualTo(Version.parse("6.5.4"))
    }

    @Test
    fun current_time() {
        val state = state.copy(
            timeSource = MockTimeSource(time = 1234.5)
        )
        assertThat(state.getValue(current_time)).isEqualTo(DateTime(1234.5))
    }

    @Test
    fun is_update() {
        // empty state
        var state = state.copy(
            engagementData = EngagementData()
        )
        assertThat(state.getValue(is_update.version_code)).isEqualTo(false)
        assertThat(state.getValue(is_update.version_name)).isEqualTo(false)

        // single item
        state = state.copy(
            engagementData = EngagementData(versionHistory = VersionHistory(
                items = listOf(
                    VersionHistoryItem(timestamp = 1.0, versionCode = 100, versionName = "1.0.0")
                )
            ))
        )
        assertThat(state.getValue(is_update.version_code)).isEqualTo(false)
        assertThat(state.getValue(is_update.version_name)).isEqualTo(false)

        // single version code
        state = state.copy(
            engagementData = EngagementData(versionHistory = VersionHistory(
                items = listOf(
                    VersionHistoryItem(timestamp = 1.0, versionCode = 100, versionName = "1.0.0"),
                    VersionHistoryItem(timestamp = 2.0, versionCode = 100, versionName = "1.0.1")
                )
            ))
        )
        assertThat(state.getValue(is_update.version_code)).isEqualTo(false)
        assertThat(state.getValue(is_update.version_name)).isEqualTo(true)

        // single version name
        state = state.copy(
            engagementData = EngagementData(versionHistory = VersionHistory(
                items = listOf(
                    VersionHistoryItem(timestamp = 1.0, versionCode = 100, versionName = "1.0.0"),
                    VersionHistoryItem(timestamp = 2.0, versionCode = 101, versionName = "1.0.0")
                )
            ))
        )
        assertThat(state.getValue(is_update.version_code)).isEqualTo(true)
        assertThat(state.getValue(is_update.version_name)).isEqualTo(false)

        // multiple version names and codes
        state = state.copy(
            engagementData = EngagementData(versionHistory = VersionHistory(
                items = listOf(
                    VersionHistoryItem(timestamp = 1.0, versionCode = 100, versionName = "1.0.0"),
                    VersionHistoryItem(timestamp = 2.0, versionCode = 101, versionName = "1.0.1")
                )
            ))
        )
        assertThat(state.getValue(is_update.version_code)).isEqualTo(true)
        assertThat(state.getValue(is_update.version_name)).isEqualTo(true)
    }

    @Test
    fun time_at_install_total() {
        // empty state
        var state = state.copy(
            engagementData = EngagementData(
                versionHistory = VersionHistory(timeSource = MockTimeSource(time = 0.5))
            )
        )
        assertThat(state.getValue(time_at_install.total)).isEqualTo(DateTime(0.5)) // current time

        // single item
        state = state.copy(
            engagementData = EngagementData(versionHistory = VersionHistory(
                items = listOf(
                    VersionHistoryItem(timestamp = 1.0, versionCode = 100, versionName = "1.0.0")
                )
            ))
        )
        assertThat(state.getValue(time_at_install.total)).isEqualTo(DateTime(1.0))

        // multiple items
        state = state.copy(
            engagementData = EngagementData(versionHistory = VersionHistory(
                items = listOf(
                    VersionHistoryItem(timestamp = 1.0, versionCode = 100, versionName = "1.0.0"),
                    VersionHistoryItem(timestamp = 2.0, versionCode = 200, versionName = "2.0.0")
                )
            ))
        )
        assertThat(state.getValue(time_at_install.total)).isEqualTo(DateTime(1.0))
    }

    @Test
    fun time_at_install_version() {
        // empty state
        var state = state.copy(
            appRelease = mockAppRelease.copy(
                versionCode = 100,
                versionName = "1.0.0"
            ),
            engagementData = EngagementData(
                versionHistory = VersionHistory(timeSource = MockTimeSource(time = 0.5))
            )
        )
        assertThat(state.getValue(time_at_install.version_code)).isEqualTo(DateTime(0.5)) // current time
        assertThat(state.getValue(time_at_install.version_name)).isEqualTo(DateTime(0.5)) // current time

        // single item
        state = state.copy(
            engagementData = EngagementData(versionHistory = VersionHistory(
                items = listOf(
                    VersionHistoryItem(timestamp = 1.0, versionCode = 100, versionName = "1.0.0")
                )
            ))
        )
        assertThat(state.getValue(time_at_install.version_code)).isEqualTo(DateTime(1.0))
        assertThat(state.getValue(time_at_install.version_name)).isEqualTo(DateTime(1.0))

        // multiple items
        state = state.copy(
            engagementData = EngagementData(versionHistory = VersionHistory(
                items = listOf(
                    VersionHistoryItem(timestamp = 1.0, versionCode = 100, versionName = "1.0.0"),
                    VersionHistoryItem(timestamp = 2.0, versionCode = 200, versionName = "2.0.0")
                )
            ))
        )
        assertThat(state.getValue(time_at_install.version_code)).isEqualTo(DateTime(1.0))
        assertThat(state.getValue(time_at_install.version_name)).isEqualTo(DateTime(1.0))

        // override app version
        state = state.copy(
            appRelease = mockAppRelease.copy(
                versionCode = 200,
                versionName = "2.0.0"
            )
        )

        assertThat(state.getValue(time_at_install.version_code)).isEqualTo(DateTime(2.0))
        assertThat(state.getValue(time_at_install.version_name)).isEqualTo(DateTime(2.0))
    }

    @Test
    fun code_point() {
        var state = state.copy(
            engagementData = EngagementData(),
            appRelease = mockAppRelease.copy(
                versionCode = 100,
                versionName = "1.0.0"
            )
        )
        val event = Event.local("event")
        assertThat(state.getValue(code_point.invokes.total(event))).isEqualTo(0)

        state = state.copy(
            engagementData = EngagementData()
                .addInvoke(event, "1.0.0", 100, DateTime(10.0))
                .addInvoke(event, "1.0.0", 101, DateTime(20.0))
                .addInvoke(event, "1.0.1", 101, DateTime(30.0))
        )

        assertThat(state.getValue(code_point.invokes.total(event))).isEqualTo(3)
        assertThat(state.getValue(code_point.invokes.version_code(event))).isEqualTo(1)
        assertThat(state.getValue(code_point.invokes.version_name(event))).isEqualTo(2)

        assertThat(state.getValue(code_point.last_invoked_at.total(event))).isEqualTo(DateTime(30.0))
    }

    @Test
    fun interactions() {
        var state = state.copy(
            engagementData = EngagementData(),
            appRelease = mockAppRelease.copy(
                versionCode = 100,
                versionName = "1.0.0"
            )
        )
        val interactionId = "12345"
        assertThat(state.getValue(interactions.invokes.total(interactionId))).isEqualTo(0)

        state = state.copy(
            engagementData = EngagementData()
                .addInvoke(interactionId, "1.0.0", 100, DateTime(10.0))
                .addInvoke(interactionId, "1.0.0", 101, DateTime(20.0))
                .addInvoke(interactionId, "1.0.1", 101, DateTime(30.0))
        )

        assertThat(state.getValue(interactions.invokes.total(interactionId))).isEqualTo(3)
        assertThat(state.getValue(interactions.invokes.version_code(interactionId))).isEqualTo(1)
        assertThat(state.getValue(interactions.invokes.version_name(interactionId))).isEqualTo(2)

        assertThat(state.getValue(interactions.last_invoked_at.total(interactionId))).isEqualTo(DateTime(30.0))
    }

    @Test
    fun person() {
        val state = state.copy(
            person = Person(
                email = "person@company.com",
                name = "First Last",
                customData = CustomData(mapOf("person_key" to "person_value"))
            )
        )

        assertThat(state.getValue(person.name)).isEqualTo("First Last")
        assertThat(state.getValue(person.email)).isEqualTo("person@company.com")
        assertThat(state.getValue(person.custom_data("person_key"))).isEqualTo("person_value")
    }

    @Test
    fun personMissingData() {
        val state = state.copy(
            person = Person()
        )

        assertThat(state.getValue(person.name)).isNull()
        assertThat(state.getValue(person.email)).isNull()
        assertThat(state.getValue(person.custom_data("person_key"))).isNull()
    }

    @Test
    fun device() {
        val state = state.copy(
            device = mockDevice.copy(
                osName = "device_os_name",
                osVersion = "device_os_version",
                osBuild = "device_os_build",
                osApiLevel = 30,
                manufacturer = "device_manufacturer",
                model = "device_model",
                board = "device_board",
                product = "device_product",
                brand = "device_brand",
                cpu = "device_cpu",
                device = "device_device",
                uuid = "device_uuid",
                buildType = "device_build_type",
                buildId = "device_build_id",
                carrier = "device_carrier",
                currentCarrier = "device_current_carrier",
                networkType = "device_network_type",
                bootloaderVersion = "device_bootloader_version",
                radioVersion = "device_radio_version",
                localeCountryCode = "device_locale_country_code",
                localeLanguageCode = "device_locale_language_code",
                localeRaw = "device_locale_raw",
                utcOffset = 18000,
                customData = CustomData(mapOf("device_key" to "device_value"))
            )
        )

        assertThat(state.getValue(device.os_name)).isEqualTo("device_os_name")
        assertThat(state.getValue(device.os_version)).isEqualTo("device_os_version")
        assertThat(state.getValue(device.os_build)).isEqualTo("device_os_build")
        assertThat(state.getValue(device.manufacturer)).isEqualTo("device_manufacturer")
        assertThat(state.getValue(device.model)).isEqualTo("device_model")
        assertThat(state.getValue(device.board)).isEqualTo("device_board")
        assertThat(state.getValue(device.product)).isEqualTo("device_product")
        assertThat(state.getValue(device.brand)).isEqualTo("device_brand")
        assertThat(state.getValue(device.cpu)).isEqualTo("device_cpu")
        assertThat(state.getValue(device.hardware)).isNull()
        assertThat(state.getValue(device.device)).isEqualTo("device_device")
        assertThat(state.getValue(device.uuid)).isEqualTo("device_uuid")
        assertThat(state.getValue(device.carrier)).isEqualTo("device_carrier")
        assertThat(state.getValue(device.current_carrier)).isEqualTo("device_current_carrier")
        assertThat(state.getValue(device.network_type)).isEqualTo("device_network_type")
        assertThat(state.getValue(device.build_type)).isEqualTo("device_build_type")
        assertThat(state.getValue(device.build_id)).isEqualTo("device_build_id")
        assertThat(state.getValue(device.bootloader_version)).isEqualTo("device_bootloader_version")
        assertThat(state.getValue(device.radio_version)).isEqualTo("device_radio_version")
        assertThat(state.getValue(device.locale_country_code)).isEqualTo("device_locale_country_code")
        assertThat(state.getValue(device.locale_language_code)).isEqualTo("device_locale_language_code")
        assertThat(state.getValue(device.locale_raw)).isEqualTo("device_locale_raw")
        assertThat(state.getValue(device.os_api_level)).isEqualTo(30)
        assertThat(state.getValue(device.utc_offset)).isEqualTo(18000)
        assertThat(state.getValue(device.custom_data("device_key"))).isEqualTo("device_value")
    }

    @Test
    fun deviceMissingData() {
        val state = state.copy(
            device = Device(
                osName = "device_os_name",
                osVersion = "device_os_version",
                osBuild = "device_os_build",
                osApiLevel = 30,
                manufacturer = "device_manufacturer",
                model = "device_model",
                board = "device_board",
                product = "device_product",
                brand = "device_brand",
                cpu = "device_cpu",
                device = "device_device",
                uuid = "device_uuid",
                buildType = "device_build_type",
                buildId = "device_build_id",
                localeCountryCode = "device_locale_country_code",
                localeLanguageCode = "device_locale_language_code",
                localeRaw = "device_locale_raw",
                utcOffset = 18000
            )
        )

        assertThat(state.getValue(device.os_name)).isEqualTo("device_os_name")
        assertThat(state.getValue(device.os_version)).isEqualTo("device_os_version")
        assertThat(state.getValue(device.os_build)).isEqualTo("device_os_build")
        assertThat(state.getValue(device.manufacturer)).isEqualTo("device_manufacturer")
        assertThat(state.getValue(device.model)).isEqualTo("device_model")
        assertThat(state.getValue(device.board)).isEqualTo("device_board")
        assertThat(state.getValue(device.product)).isEqualTo("device_product")
        assertThat(state.getValue(device.brand)).isEqualTo("device_brand")
        assertThat(state.getValue(device.cpu)).isEqualTo("device_cpu")
        assertThat(state.getValue(device.hardware)).isNull()
        assertThat(state.getValue(device.device)).isEqualTo("device_device")
        assertThat(state.getValue(device.uuid)).isEqualTo("device_uuid")
        assertThat(state.getValue(device.carrier)).isNull()
        assertThat(state.getValue(device.current_carrier)).isNull()
        assertThat(state.getValue(device.network_type)).isNull()
        assertThat(state.getValue(device.build_type)).isEqualTo("device_build_type")
        assertThat(state.getValue(device.build_id)).isEqualTo("device_build_id")
        assertThat(state.getValue(device.bootloader_version)).isNull()
        assertThat(state.getValue(device.radio_version)).isNull()
        assertThat(state.getValue(device.locale_country_code)).isEqualTo("device_locale_country_code")
        assertThat(state.getValue(device.locale_language_code)).isEqualTo("device_locale_language_code")
        assertThat(state.getValue(device.locale_raw)).isEqualTo("device_locale_raw")
        assertThat(state.getValue(device.os_api_level)).isEqualTo(30)
        assertThat(state.getValue(device.utc_offset)).isEqualTo(18000)
        assertThat(state.getValue(device.custom_data("device_key"))).isNull()
    }
}

