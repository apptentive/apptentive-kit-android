package apptentive.com.android.feedback.engagement

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EventTest {
    @Test
    fun naming() {
        val event = Event("vendor", "interaction", "name")
        assertThat(event.vendor).isEqualTo("vendor")
        assertThat(event.interaction).isEqualTo("interaction")
        assertThat(event.name).isEqualTo("name")
        assertThat(event.fullName).isEqualTo("vendor#interaction#name")
    }

    @Test
    fun vendors() {
        assertThat(Event.local("event").fullName).isEqualTo("local#app#event")
        assertThat(Event.internal("event").fullName).isEqualTo("com.apptentive#app#event")
    }

    @Test
    fun whiteSpaces() {
        assertThat(Event.local("event name").fullName).isEqualTo("local#app#event name")
    }

    @Test
    fun whiteEscapingCharacters() {
        assertThat(Event.local("%event").fullName).isEqualTo("local#app#%25event")
        assertThat(Event.local("#event").fullName).isEqualTo("local#app#%23event")
        assertThat(Event.local("/event").fullName).isEqualTo("local#app#%2Fevent")
    }

    @Test
    fun parse() {
        assertThat(Event.parse("local#app#event")).isEqualTo(Event("local", "app", "event"))
    }

    @Test
    fun parseInvalid1() {
        try {
            Event.parse("local#app")
        } catch (e: IllegalArgumentException) {
            assertThat(e).hasMessageThat().isEqualTo("Invalid event name: 'local#app'")
        }
    }

    @Test
    fun parseInvalid2() {
        try {
            Event.parse("my#local#app#event")
        } catch (e: IllegalArgumentException) {
            assertThat(e).hasMessageThat().isEqualTo("Invalid event name: 'my#local#app#event'")
        }
    }
}
