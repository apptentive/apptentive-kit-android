package apptentive.com.android.feedback.engagement.criteria

import apptentive.com.android.TestCase
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.Field.Companion.parse
import apptentive.com.android.feedback.engagement.criteria.Field.application
import apptentive.com.android.feedback.engagement.criteria.Field.code_point
import apptentive.com.android.feedback.engagement.criteria.Field.current_time
import apptentive.com.android.feedback.engagement.criteria.Field.device
import apptentive.com.android.feedback.engagement.criteria.Field.interactions
import apptentive.com.android.feedback.engagement.criteria.Field.is_update
import apptentive.com.android.feedback.engagement.criteria.Field.person
import apptentive.com.android.feedback.engagement.criteria.Field.sdk
import apptentive.com.android.feedback.engagement.criteria.Field.time_at_install
import apptentive.com.android.feedback.engagement.criteria.Field.unknown
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class FieldTest : TestCase() {
    @Test
    fun parseApplication() {
        assertThat(parse("application/version_code")).isEqualTo(application.version_code)
        assertThat(parse("application/version_name")).isEqualTo(application.version_name)
    }

    @Test
    fun parseSdk() {
        assertThat(parse("sdk/version")).isEqualTo(sdk.version)
    }

    @Test
    fun parseCurrentTime() {
        assertThat(parse("current_time")).isEqualTo(current_time)
    }

    @Test
    fun parseIsUpdate() {
        assertThat(parse("is_update/version_code")).isEqualTo(is_update.version_code)
        assertThat(parse("is_update/version_name")).isEqualTo(is_update.version_name)
    }

    @Test
    fun parseTimeAtInstall() {
        assertThat(parse("time_at_install/total")).isEqualTo(time_at_install.total)
        assertThat(parse("time_at_install/version_code")).isEqualTo(time_at_install.version_code)
        assertThat(parse("time_at_install/version_name")).isEqualTo(time_at_install.version_name)
    }

    @Test
    fun parseCodePoint() {
        val event = Event.local("event")
        assertThat(parse("code_point/local#app#event/invokes/total")).isEqualTo(
            code_point.invokes.total(
                event
            )
        )
        assertThat(parse("code_point/local#app#event/invokes/version_code")).isEqualTo(
            code_point.invokes.version_code(
                event
            )
        )
        assertThat(parse("code_point/local#app#event/invokes/version_name")).isEqualTo(
            code_point.invokes.version_name(
                event
            )
        )
        assertThat(parse("code_point/local#app#event/last_invoked_at/total")).isEqualTo(
            code_point.last_invoked_at.total(
                event
            )
        )
    }

    @Test
    fun parseInteractions() {
        assertThat(parse("interactions/12345/invokes/total")).isEqualTo(interactions.invokes.total("12345"))
        assertThat(parse("interactions/12345/invokes/version_code")).isEqualTo(
            interactions.invokes.version_code("12345")
        )
        assertThat(parse("interactions/12345/invokes/version_name")).isEqualTo(
            interactions.invokes.version_name("12345")
        )
        assertThat(parse("interactions/12345/last_invoked_at/total")).isEqualTo(
            interactions.last_invoked_at.total("12345")
        )
        assertThat(parse("interactions/12345/answers/id")).isEqualTo(
            interactions.answers.id("12345")
        )
        assertThat(parse("interactions/12345/answers/value")).isEqualTo(
            interactions.answers.value("12345")
        )
    }

    @Test
    fun parsePerson() {
        assertThat(parse("person/name")).isEqualTo(person.name)
        assertThat(parse("person/email")).isEqualTo(person.email)
        assertThat(parse("person/custom_data/my_key")).isEqualTo(person.custom_data("my_key"))
    }

    @Test
    fun parseDevice() {
        assertThat(parse("device/os_name")).isEqualTo(device.os_name)
        assertThat(parse("device/os_version")).isEqualTo(device.os_version)
        assertThat(parse("device/os_build")).isEqualTo(device.os_build)
        assertThat(parse("device/manufacturer")).isEqualTo(device.manufacturer)
        assertThat(parse("device/model")).isEqualTo(device.model)
        assertThat(parse("device/board")).isEqualTo(device.board)
        assertThat(parse("device/product")).isEqualTo(device.product)
        assertThat(parse("device/brand")).isEqualTo(device.brand)
        assertThat(parse("device/cpu")).isEqualTo(device.cpu)
        assertThat(parse("device/hardware")).isEqualTo(device.hardware)
        assertThat(parse("device/device")).isEqualTo(device.device)
        assertThat(parse("device/uuid")).isEqualTo(device.uuid)
        assertThat(parse("device/carrier")).isEqualTo(device.carrier)
        assertThat(parse("device/current_carrier")).isEqualTo(device.current_carrier)
        assertThat(parse("device/network_type")).isEqualTo(device.network_type)
        assertThat(parse("device/build_type")).isEqualTo(device.build_type)
        assertThat(parse("device/build_id")).isEqualTo(device.build_id)
        assertThat(parse("device/bootloader_version")).isEqualTo(device.bootloader_version)
        assertThat(parse("device/radio_version")).isEqualTo(device.radio_version)
        assertThat(parse("device/locale_country_code")).isEqualTo(device.locale_country_code)
        assertThat(parse("device/locale_language_code")).isEqualTo(device.locale_language_code)
        assertThat(parse("device/locale_raw")).isEqualTo(device.locale_raw)
        assertThat(parse("device/os_api_level")).isEqualTo(device.os_api_level)
        assertThat(parse("device/utc_offset")).isEqualTo(device.utc_offset)
        assertThat(parse("device/custom_data/my_key")).isEqualTo(device.custom_data("my_key"))
    }

    @Test
    fun parseRandomSamplingWithId() {
        assertEquals(Field.random.percent_with_id("12345"), parse("random/12345/percent"))
    }

    @Test
    fun parseRandomSampling() {
        assertEquals(Field.random.percent, parse("random/percent"))
    }

    @Test
    fun parseUnknown() {
        assertThat(parse("fake/code/point")).isEqualTo(unknown("fake/code/point"))
    }

    @Test
    fun convertValue() {
        // Custom data types are parsed as Any
        assertThat(parse("device/custom_data/my_key").convertValue("String") is Any)
        assertThat(parse("person/custom_data/my_key").convertValue(500) is Any)
        // Declared types
        assertThat(parse("device/radio_version").convertValue(Version(30, 19, 20, 0)) is Version)
        assertThat(parse("interactions/12345/invokes/total").convertValue(20.25) is Number)
        assertThat(parse("interactions/12345/invokes/total").convertValue(20.25) !is String)
    }
}
