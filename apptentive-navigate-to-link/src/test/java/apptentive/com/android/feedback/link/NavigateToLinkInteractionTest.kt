package apptentive.com.android.feedback.link

import apptentive.com.android.feedback.link.NavigateToLinkInteraction.Target
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigateToLinkInteractionTest {
    @Test
    fun testParsingTarget() {
        assertThat(Target.parse("new")).isEqualTo(Target.new)
        assertThat(Target.parse("self")).isEqualTo(Target.self)
        assertThat(Target.parse("else")).isEqualTo(Target.new)
        assertThat(Target.parse("")).isEqualTo(Target.new)
        assertThat(Target.parse(null)).isEqualTo(Target.new)
    }
}