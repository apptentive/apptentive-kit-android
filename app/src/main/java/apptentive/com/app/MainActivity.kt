package apptentive.com.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.core.DependencyProvider
import apptentive.com.android.core.ExecutorFactory
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.feedback.platform.AndroidEngagementContext
import apptentive.com.android.feedback.survey.interaction.SurveyInteraction
import apptentive.com.android.feedback.survey.interaction.SurveyInteractionLauncher
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        val night = prefs.getBoolean("night", false)
        nightSwitch.isChecked = night
        delegate.localNightMode = if (night) MODE_NIGHT_YES else MODE_NIGHT_NO

        nightSwitch.setOnCheckedChangeListener { _, isChecked ->
            delegate.localNightMode = if (isChecked) MODE_NIGHT_YES else MODE_NIGHT_NO
            prefs.edit().putBoolean("night", isChecked).apply()
        }


        engage_button.setOnClickListener {
            val eventName = event_name_edit_text.text.toString().trim()
            if (eventName.isEmpty()) {
                Toast.makeText(this, "Empty event name", Toast.LENGTH_LONG).show()
            } else {
                Apptentive.engage(this, eventName)
            }
        }

        love_dialog_button.setOnClickListener {
            Apptentive.reset()
            Apptentive.engage(this, "love_dialog_test") {
                if (it != EngagementResult.Success) {
                    Toast.makeText(this, "Not engaged: $it", Toast.LENGTH_LONG).show()
                }
            }
        }

        val ctx = this

        survey_button.setOnClickListener {
            val executorFactory = DependencyProvider.of<ExecutorFactory>()
            val context = AndroidEngagementContext(
                androidContext = this,
                engagement = object : Engagement {
                    override fun engage(
                        context: EngagementContext,
                        event: Event,
                        interactionId: String?,
                        data: Map<String, Any>?,
                        customData: Map<String, Any>?,
                        extendedData: List<ExtendedData>?
                    ): EngagementResult {
                        Log.i(LogTags.core, "Engaged event: $event")
                        return EngagementResult.Success
                    }

                    override fun engage(
                        context: EngagementContext,
                        invocations: List<Invocation>
                    ): EngagementResult {
                        return EngagementResult.Success
                    }
                },
                payloadSender = object : PayloadSender {
                    override fun sendPayload(payload: Payload) {
                        runOnUiThread {
                            Toast.makeText(ctx, "Payload send: ${payload::class.java.simpleName}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                executors = Executors(
                    state = executorFactory.createSerialQueue("state"),
                    main = executorFactory.createMainQueue()
                )
            )
            val launcher = SurveyInteractionLauncher()
            val interaction = SurveyInteraction(
                id = "id",
                description = "description",
                name = "name",
                submitText = "Submit",
                requiredText = "Required",
                validationError = "Validation error",
                showSuccessMessage = true,
                successMessage = "Success",
                isRequired = false,
                questions = listOf(
                    mapOf(
                        "id" to "question_1",
                        "type" to "singleline",
                        "value" to "Single line question 1",
                        "error_message" to "Error message 1",
                        "required" to true,
                        "freeform_hint" to "Freeform hint 1",
                        "multiline" to false
                    ),
                    mapOf(
                        "id" to "question_2",
                        "type" to "singleline",
                        "value" to "Single line question 2",
                        "error_message" to "Error message 2",
                        "required" to false,
                        "freeform_hint" to "Freeform hint 2",
                        "multiline" to true
                    ),
                    mapOf(
                        "id" to "question_3",
                        "type" to "multichoice",
                        "value" to "Mulitchoice question 3",
                        "error_message" to "Error message 3",
                        "required" to true,
                        "instructions" to "select one",
                        "answer_choices" to listOf(
                            mapOf<String, Any?>(
                                "id" to "choice_1",
                                "value" to "Title 1",
                                "type" to "select_option"
                            ),
                            mapOf<String, Any?>(
                                "id" to "choice_2",
                                "value" to "Other",
                                "type" to "select_other",
                                "hint" to "Hint"
                            )
                        )
                    ),
                    mapOf(
                        "id" to "question_4",
                        "type" to "multiselect",
                        "value" to "Multiselect question 4",
                        "error_message" to "Error message 4",
                        "required" to true,
                        "min_selections" to 1,
                        "max_selections" to 2,
                        "instructions" to "select one",
                        "answer_choices" to listOf(
                            mapOf<String, Any?>(
                                "id" to "choice_1",
                                "value" to "Title 1",
                                "type" to "select_option"
                            ),
                            mapOf<String, Any?>(
                                "id" to "choice_2",
                                "value" to "Other",
                                "type" to "select_option",
                                "hint" to "Hint"
                            )
                        )
                    ),
                    mapOf(
                        "id" to "question_5",
                        "type" to "range",
                        "value" to "How are you feeling about using this app?",
                        "error_message" to "Error - There was a problem with your NPS answer.",
                        "required" to true,
                        "min" to 1,
                        "max" to 5,
                        "min_label" to "Sad",
                        "max_label" to "Happy"
                    )
                )
            )
            launcher.launchInteraction(context, interaction)
        }

        notes_button.setOnClickListener {
            Apptentive.reset()
            Apptentive.engage(this, "note_event") {
                if (it != EngagementResult.Success) {
                    Toast.makeText(this, "Not engaged: $it", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
