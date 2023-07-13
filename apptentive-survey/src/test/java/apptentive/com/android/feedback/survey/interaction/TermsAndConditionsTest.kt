package apptentive.com.android.feedback.survey.interaction

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TermsAndConditionsTest {
    private lateinit var termsAndConditions: SurveyInteraction.TermsAndConditions

    @Before
    fun setUp() {
        termsAndConditions = SurveyInteraction.TermsAndConditions(
            label = "Terms",
            link = "https://example.com/terms"
        )
    }

    @Test
    fun getLabel_returnsLabel() {
        val expected = "Terms"

        val result = termsAndConditions.label

        Assert.assertEquals(expected, result)
    }

    @Test
    fun getLink_returnsLink() {
        val expected = "https://example.com/terms"

        val result = termsAndConditions.link

        Assert.assertEquals(expected, result)
    }
}
