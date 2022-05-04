package apptentive.com.android.feedback.link

import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.util.readJson
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigateToLinkInteractionTypeConverterTest {
    @Test
    fun testDefaultTarget() {
        testConverter("navigate-to-link-default.json", NavigateToLinkInteraction.Target.new)
    }

    @Test
    fun testNewTarget() {
        testConverter("navigate-to-link-new.json", NavigateToLinkInteraction.Target.new)
    }

    @Test
    fun testSelfTarget() {
        testConverter("navigate-to-link-self.json", NavigateToLinkInteraction.Target.self)
    }

    @Test
    fun testUnknownTarget() {
        testConverter("navigate-to-link-unknown.json", NavigateToLinkInteraction.Target.new)
    }

    private fun testConverter(path: String, target: NavigateToLinkInteraction.Target) {
        val data = readJson<InteractionData>(path)
        val actual = NavigateToLinkInteractionTypeConverter().convert(data)
        val expected = NavigateToLinkInteraction(
            id = "id",
            url = "http://www.apptentive.com",
            target = target
        )
        assertThat(actual).isEqualTo(expected)
    }
}
