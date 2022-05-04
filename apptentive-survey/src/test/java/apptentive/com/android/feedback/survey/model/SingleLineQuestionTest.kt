package apptentive.com.android.feedback.survey.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SingleLineQuestionTest {
    @Test
    fun testRequiredValidAnswers() {
        val question = createSingleLineQuestion(required = true, answer = "text")
        assertThat(question.answerString).isEqualTo("text")
        assertThat(question.hasValidAnswer).isTrue()
    }

    @Test
    fun testRequiredNoAnswer() {
        val question = createSingleLineQuestion(required = true)
        assertThat(question.answerString).isNull()
        assertThat(question.hasValidAnswer).isFalse()
    }

    @Test
    fun testRequiredInvalidAnswer() {
        val question = createSingleLineQuestion(required = true, answer = "")
        assertThat(question.answerString).isEmpty()
        assertThat(question.hasValidAnswer).isFalse()
    }

    @Test
    fun testUpdateRequiredAnswers() {
        val question = createSingleLineQuestion(required = true)

        // empty by default
        assertThat(question.answerString).isNull()
        assertThat(question.hasValidAnswer).isFalse()

        // set a valid answer
        question.answer = SingleLineQuestion.Answer("text")
        assertThat(question.answerString).isEqualTo("text")
        assertThat(question.hasValidAnswer).isTrue()

        // remove answer
        question.answer = SingleLineQuestion.Answer("")
        assertThat(question.answerString).isEqualTo("")
        assertThat(question.hasValidAnswer).isFalse()
    }
}
