package apptentive.com.android.feedback.survey.model

import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion.*
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MultiChoiceQuestionTest {
    @Test
    fun testRequiredValidAnswers() {
        val question = createQuestion(
            type = ChoiceType.select_option,
            choices = listOf(
                Answer.Choice(
                    id = "choice_id",
                    checked = true
                )
            )
        )
        val expected = listOf(
            Answer.Choice(
                id = "choice_id",
                checked = true,
                value = null
            )
        )

        assertThat(question.hasValidAnswer).isTrue()
        assertThat(question.choices).isEqualTo(expected)
    }

    @Test
    fun testRequiredNoAnswer() {
        val question = createQuestion(required = true)
        val expected = listOf(
            Answer.Choice(
                id = "choice_id",
                checked = false,
                value = null
            )
        )
        assertThat(question.hasValidAnswer).isFalse()
        assertThat(question.choices).isEqualTo(expected)
    }

    @Test
    fun testRequiredInvalidAnswer() {
        val question = createQuestion(
            type = ChoiceType.select_other,
            required = true,
            choices = listOf(
                Answer.Choice(id = "choice_id", checked = true)
            )
        )
        val expected = listOf(
            Answer.Choice(id = "choice_id", checked = true, value = null)
        )
        assertThat(question.choices).isEqualTo(expected)
        assertThat(question.hasValidAnswer).isFalse()
    }

    @Test
    fun testUpdateRequiredAnswers() {
        val question = createQuestion(
            type = ChoiceType.select_other,
            required = true,
            choices = listOf(
                Answer.Choice(id = "choice_id", checked = false, value = null)
            )
        )
        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_id", checked = false, value = null)
            )
        )
        assertThat(question.hasValidAnswer).isFalse()

        // update answer
        question.answer = Answer(listOf(Answer.Choice(id = "choice_id", checked = true, value = "value")))
        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_id", checked = true, value = "value")
            )
        )
        assertThat(question.hasValidAnswer).isTrue()

        // update answer
        question.answer =
            Answer(listOf(Answer.Choice(id = "choice_id", checked = false, value = "value")))
        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_id", checked = false, value = "value")
            )
        )
        assertThat(question.hasValidAnswer).isFalse()
    }

    @Test
    fun testMultiSelect() {
        val question = createMultiChoiceQuestion(
            required = true,
            allowMultipleAnswers = true,
            minSelections = 2,
            maxSelections = 4,
            answerChoiceConfigs = listOf(
                AnswerChoiceConfiguration(ChoiceType.select_option, "choice_1", "Choice 1"),
                AnswerChoiceConfiguration(ChoiceType.select_option, "choice_2", "Choice 2"),
                AnswerChoiceConfiguration(ChoiceType.select_option, "choice_3", "Choice 3"),
                AnswerChoiceConfiguration(ChoiceType.select_option, "choice_4", "Choice 4"),
                AnswerChoiceConfiguration(ChoiceType.select_option, "choice_5", "Choice 5")
            )
        )

        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_1"),
                Answer.Choice(id = "choice_2"),
                Answer.Choice(id = "choice_3"),
                Answer.Choice(id = "choice_4"),
                Answer.Choice(id = "choice_5")
            )
        )

        // no answers provide
        assertThat(question.hasValidAnswer).isFalse()

        // update answer
        question.updateAnswerChoice(choiceId = "choice_1", isChecked = true)

        // not enough selections
        assertThat(question.hasValidAnswer).isFalse()

        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_1", checked = true),
                Answer.Choice(id = "choice_2"),
                Answer.Choice(id = "choice_3"),
                Answer.Choice(id = "choice_4"),
                Answer.Choice(id = "choice_5")
            )
        )

        // update answer
        question.updateAnswerChoice(choiceId = "choice_2", isChecked = true)

        // min choices selected
        assertThat(question.hasValidAnswer).isTrue()

        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_1", checked = true),
                Answer.Choice(id = "choice_2", checked = true),
                Answer.Choice(id = "choice_3"),
                Answer.Choice(id = "choice_4"),
                Answer.Choice(id = "choice_5")
            )
        )

        // update answer
        question.updateAnswerChoice(choiceId = "choice_3", isChecked = true)

        // valid choices
        assertThat(question.hasValidAnswer).isTrue()

        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_1", checked = true),
                Answer.Choice(id = "choice_2", checked = true),
                Answer.Choice(id = "choice_3", checked = true),
                Answer.Choice(id = "choice_4"),
                Answer.Choice(id = "choice_5")
            )
        )

        // update answer
        question.updateAnswerChoice(choiceId = "choice_4", isChecked = true)

        // max choices
        assertThat(question.hasValidAnswer).isTrue()

        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_1", checked = true),
                Answer.Choice(id = "choice_2", checked = true),
                Answer.Choice(id = "choice_3", checked = true),
                Answer.Choice(id = "choice_4", checked = true),
                Answer.Choice(id = "choice_5")
            )
        )

        // update answer
        question.updateAnswerChoice(choiceId = "choice_5", isChecked = true)

        // too many choices
        assertThat(question.hasValidAnswer).isFalse()

        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_1", checked = true),
                Answer.Choice(id = "choice_2", checked = true),
                Answer.Choice(id = "choice_3", checked = true),
                Answer.Choice(id = "choice_4", checked = true),
                Answer.Choice(id = "choice_5", checked = true)
            )
        )

        // remove one choice
        question.updateAnswerChoice(choiceId = "choice_3", isChecked = false)

        // too many choices
        assertThat(question.hasValidAnswer).isTrue()

        assertThat(question.choices).isEqualTo(
            listOf(
                Answer.Choice(id = "choice_1", checked = true),
                Answer.Choice(id = "choice_2", checked = true),
                Answer.Choice(id = "choice_3"),
                Answer.Choice(id = "choice_4", checked = true),
                Answer.Choice(id = "choice_5", checked = true)
            )
        )
    }

    @Test
    fun testUpdateAnswerSingleChoice() {
        val allowMultipleAnswers = false
        var actual = Answer(
            choices = listOf(
                Answer.Choice("id_1"),
                Answer.Choice("id_2")
            )
        )

        /* select one */

        actual = actual.update(
            choiceId = "id_1",
            isChecked = true,
            allowMultipleAnswers = allowMultipleAnswers
        )

        // new selection appears
        var expected = Answer(
            choices = listOf(
                Answer.Choice("id_1", checked = true),
                Answer.Choice("id_2")
            )
        )

        assertThat(actual).isEqualTo(expected)

        /* select another */

        actual = actual.update(
            choiceId = "id_2",
            isChecked = true,
            allowMultipleAnswers = allowMultipleAnswers
        )

        // new selection appears
        expected = Answer(
            choices = listOf(
                Answer.Choice("id_1"),
                Answer.Choice("id_2", checked = true)
            )
        )

        assertThat(actual).isEqualTo(expected)

        /* select the same one more time */

        actual = actual.update(
            choiceId = "id_2",
            isChecked = true,
            allowMultipleAnswers = allowMultipleAnswers
        )

        // nothing changed
        expected = Answer(
            choices = listOf(
                Answer.Choice("id_1"),
                Answer.Choice("id_2", checked = true)
            )
        )

        assertThat(actual).isEqualTo(expected)

        /* update text */

        actual = actual.update(
            choiceId = "id_2",
            isChecked = true,
            allowMultipleAnswers = allowMultipleAnswers,
            text = "text"
        )

        // text gets updated
        expected = Answer(
            choices = listOf(
                Answer.Choice("id_1"),
                Answer.Choice("id_2", checked = true, value = "text")
            )
        )

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testUpdateAnswerMultipleChoice() {
        val allowMultipleAnswers = true
        var actual = Answer(
            choices = listOf(
                Answer.Choice("id_1"),
                Answer.Choice("id_2")
            )
        )

        /* select one */

        actual = actual.update(
            choiceId = "id_1",
            isChecked = true,
            allowMultipleAnswers = allowMultipleAnswers
        )

        // new selection appears
        var expected = Answer(
            choices = listOf(
                Answer.Choice("id_1", checked = true),
                Answer.Choice("id_2")
            )
        )

        assertThat(actual).isEqualTo(expected)

        /* select another */

        actual = actual.update(
            choiceId = "id_2",
            isChecked = true,
            allowMultipleAnswers = allowMultipleAnswers
        )

        // new selection appears
        expected = Answer(
            choices = listOf(
                Answer.Choice("id_1", checked = true),
                Answer.Choice("id_2", checked = true)
            )
        )

        assertThat(actual).isEqualTo(expected)

        /* select the same one more time */

        actual = actual.update(
            choiceId = "id_2",
            isChecked = true,
            allowMultipleAnswers = allowMultipleAnswers
        )

        // nothing changed
        expected = Answer(
            choices = listOf(
                Answer.Choice("id_1", checked = true),
                Answer.Choice("id_2", checked = true)
            )
        )

        assertThat(actual).isEqualTo(expected)

        /* deselect one */

        actual = actual.update(
            choiceId = "id_1",
            isChecked = false,
            allowMultipleAnswers = allowMultipleAnswers
        )

        // selection changed
        expected = Answer(
            choices = listOf(
                Answer.Choice("id_1"),
                Answer.Choice("id_2", checked = true)
            )
        )

        assertThat(actual).isEqualTo(expected)

        /* update text */

        actual = actual.update(
            choiceId = "id_2",
            isChecked = true,
            allowMultipleAnswers = allowMultipleAnswers,
            text = "text"
        )

        // text gets updated
        expected = Answer(
            choices = listOf(
                Answer.Choice("id_1"),
                Answer.Choice("id_2", checked = true, value = "text")
            )
        )

        assertThat(actual).isEqualTo(expected)
    }

    private fun createQuestion(
        type: ChoiceType = ChoiceType.select_option,
        required: Boolean = false,
        minSelections: Int = 1,
        maxSelections: Int = 1,
        answerChoiceConfigs: List<AnswerChoiceConfiguration>? = null,
        choices: List<Answer.Choice>? = null
    ) = createMultiChoiceQuestion(
        required = required,
        answerChoiceConfigs = answerChoiceConfigs ?: listOf(
            AnswerChoiceConfiguration(
                type = type,
                id = "choice_id",
                title = "choice value"
            )
        ),
        minSelections = minSelections,
        maxSelections = maxSelections,
        answer = choices
    )

    private fun MultiChoiceQuestion.updateAnswerChoice(
        choiceId: String,
        isChecked: Boolean,
        text: String? = null
    ) {
        answer = answer.update(choiceId, isChecked, allowMultipleAnswers, text)
    }
}