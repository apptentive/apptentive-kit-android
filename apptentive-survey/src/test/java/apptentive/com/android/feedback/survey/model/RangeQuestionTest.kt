package apptentive.com.android.feedback.survey.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RangeQuestionTest {
    @Test
    fun testRequiredValidAnswers() {
        val question = createRangeQuestion(required = true, selectedIndex = 5)
        assertThat(question.selectedValue).isEqualTo(5)
        assertThat(question.hasValidAnswer).isTrue()
    }

    @Test
    fun testRequiredNoAnswer() {
        val question = createRangeQuestion(required = true)
        assertThat(question.selectedValue).isNull()
        assertThat(question.hasValidAnswer).isFalse()
    }

    @Test
    fun testRequiredInvalidAnswer() {
        val question = createRangeQuestion(required = true, selectedIndex = 15)
        assertThat(question.selectedValue).isEqualTo(15)
        assertThat(question.hasValidAnswer).isFalse()
    }

    @Test
    fun testUpdateRequiredAnswers() {
        val question = createRangeQuestion(required = true)

        // empty by default
        assertThat(question.selectedValue).isNull()
        assertThat(question.hasValidAnswer).isFalse()

        // set a valid answer
        question.answer = RangeQuestion.Answer(5)
        assertThat(question.selectedValue).isEqualTo(5)
        assertThat(question.hasValidAnswer).isTrue()
    }
}