package apptentive.com.android.feedback.engagement

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EventTest {
    @Test
    fun testNaming() {
        val event = Event("vendor", "interaction", "name")
        assertThat(event.vendor).isEqualTo("vendor")
        assertThat(event.interaction).isEqualTo("interaction")
        assertThat(event.name).isEqualTo("name")
        assertThat(event.fullName).isEqualTo("vendor#interaction#name")
    }

    @Test
    fun testVendors() {
        assertThat(Event.local("event").fullName).isEqualTo("local#app#event")
        assertThat(Event.internal("event").fullName).isEqualTo("com.apptentive#app#event")
    }

    @Test
    fun testWhiteSpaces() {
        assertThat(Event.local("event name").fullName).isEqualTo("local#app#event name")
    }

    @Test
    fun testWhiteEscapingCharacters() {
        assertThat(Event.local("%event").fullName).isEqualTo("local#app#%25event")
        assertThat(Event.local("#event").fullName).isEqualTo("local#app#%23event")
        assertThat(Event.local("/event").fullName).isEqualTo("local#app#%2Fevent")
    }
}