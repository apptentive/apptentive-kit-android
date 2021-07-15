package apptentive.com.app

import android.content.Intent
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
import apptentive.com.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        val night = prefs.getBoolean("night", false)
        binding.nightSwitch.isChecked = night
        delegate.localNightMode = if (night) MODE_NIGHT_YES else MODE_NIGHT_NO

        binding.nightSwitch.setOnCheckedChangeListener { _, isChecked ->
            delegate.localNightMode = if (isChecked) MODE_NIGHT_YES else MODE_NIGHT_NO
            prefs.edit().putBoolean("night", isChecked).apply()
        }

        binding.engageButton.setOnClickListener {
            val eventName = binding.eventNameEditText.text.toString().trim()
            if (eventName.isEmpty()) {
                Toast.makeText(this, "Empty event name", Toast.LENGTH_LONG).show()
            } else {
                Apptentive.engage(this, eventName)
            }
        }

        binding.loveDialogButton.setOnClickListener {
            Apptentive.reset()
            Apptentive.engage(this, "love_dialog_test") {
                if (it !is EngagementResult.Success) {
                    Toast.makeText(this, "Not engaged: $it", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.surveyButton.setOnClickListener {
            val executorFactory = DependencyProvider.of<ExecutorFactory>()
            val context = AndroidEngagementContext(
                androidContext = this,
                engagement = object : Engagement {
                    override fun engage(
                        context: EngagementContext,
                        event: Event,
                        interactionId: String?,
                        data: Map<String, Any?>?,
                        customData: Map<String, Any?>?,
                        extendedData: List<ExtendedData>?
                    ): EngagementResult {
                        return if (interactionId != null)
                            EngagementResult.Success(interactionId = interactionId) else
                            EngagementResult.Failure("No runnable interactions")
                    }

                    override fun engage(
                        context: EngagementContext,
                        invocations: List<Invocation>
                    ): EngagementResult {
                        return EngagementResult.Failure("No runnable interactions")
                    }
                },
                payloadSender = object : PayloadSender {
                    override fun sendPayload(payload: Payload) {
//                        runOnUiThread {
//                            Toast.makeText(
//                                ctx,
//                                "Payload send: ${payload::class.java.simpleName}",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
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
                description = "Tell us about your experience!",
                name = "All question types report",
                submitText = "Submit",
                requiredText = "Required",
                validationError = "There are issues with your response.",
                showSuccessMessage = true,
                successMessage = "Thank you for taking this survey!",
                closeConfirmTitle = "Are you sure you want to close the survey?",
                closeConfirmMessage = null,
                closeConfirmCloseText = null,
                closeConfirmBackText = null,
                isRequired = false,
                questions = listOf(
                    mapOf(
                        "id" to "singleline_1",
                        "type" to "singleline",
                        "value" to "Single line optional",
                        "error_message" to "Error - There was a problem with your text answer.",
                        "required" to false,
                        "freeform_hint" to "Single line optional hint",
                        "multiline" to false
                    ),
                    mapOf(
                        "id" to "singleline_2",
                        "type" to "singleline",
                        "value" to "Single line not optional",
                        "error_message" to "Error - There was a problem with your text answer.",
                        "required" to true,
                        "freeform_hint" to "Single line required hint",
                        "multiline" to false
                    ),
                    mapOf(
                        "id" to "singleline_3",
                        "type" to "singleline",
                        "value" to "Multi line optional",
                        "error_message" to "Error - There was a problem with your text answer.",
                        "required" to false,
                        "freeform_hint" to "Single line multiline hint",
                        "multiline" to true
                    ),
                    mapOf(
                        "id" to "multichoice_1",
                        "type" to "multichoice",
                        "value" to "Single Select optional",
                        "error_message" to "Error - There was a problem with your single-select answer.",
                        "required" to false,
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
                                "hint" to "Other Hint"
                            )
                        )
                    ),
                    mapOf(
                        "id" to "multichoice_2",
                        "type" to "multichoice",
                        "value" to "Single Select not optional",
                        "error_message" to "Error - There was a problem with your single-select answer.",
                        "required" to true,
                        "instructions" to "select one",
                        "answer_choices" to listOf(
                            mapOf<String, Any?>(
                                "id" to "choice_1",
                                "value" to "A",
                                "type" to "select_option"
                            ),
                            mapOf<String, Any?>(
                                "id" to "choice_2",
                                "value" to "B",
                                "type" to "select_other",
                                "hint" to "B Hint"
                            )
                        )
                    ),
                    mapOf(
                        "id" to "multiselect_1",
                        "type" to "multiselect",
                        "value" to "Multi Select optional",
                        "error_message" to "Error - There was a problem with your multi-select answer.",
                        "required" to false,
                        "min_selections" to 0,
                        "max_selections" to 2,
                        "instructions" to "select all that apply",
                        "answer_choices" to listOf(
                            mapOf<String, Any?>(
                                "id" to "choice_1",
                                "value" to "Option 1",
                                "type" to "select_option"
                            ),
                            mapOf<String, Any?>(
                                "id" to "choice_2",
                                "value" to "Option 2",
                                "type" to "select_option"
                            ),
                            mapOf<String, Any?>(
                                "id" to "choice_3",
                                "value" to "Other Option",
                                "type" to "select_other",
                                "hint" to "Hint"
                            )
                        )
                    ),
                    mapOf(
                        "id" to "multiselect_2",
                        "type" to "multiselect",
                        "value" to "Multi Select not optional",
                        "error_message" to "Error - There was a problem with your multi-select answer.",
                        "required" to true,
                        "min_selections" to 1,
                        "max_selections" to 2,
                        "instructions" to "select all that apply",
                        "answer_choices" to listOf(
                            mapOf<String, Any?>(
                                "id" to "choice_1",
                                "value" to "Option 1",
                                "type" to "select_option"
                            ),
                            mapOf<String, Any?>(
                                "id" to "choice_2",
                                "value" to "Option 2",
                                "type" to "select_option",
                            ),
                            mapOf<String, Any?>(
                                "id" to "choice_3",
                                "value" to "Other Option",
                                "type" to "select_other",
                                "hint" to "Hint"
                            )
                        )
                    ),
                    mapOf(
                        "id" to "range_1",
                        "type" to "range",
                        "value" to "Range optional",
                        "error_message" to "Error - There was a problem with your range answer.",
                        "required" to false,
                        "min" to -5,
                        "max" to 5,
                        "min_label" to "Sad",
                        "max_label" to "Happy"
                    ),
                    mapOf(
                        "id" to "range_2",
                        "type" to "range",
                        "value" to "NPS Range not optional",
                        "error_message" to "Error - There was a problem with your NPS answer.",
                        "required" to true,
                        "min" to 0,
                        "max" to 10,
                        "min_label" to "Not Likely",
                        "max_label" to "Extremely Likely"
                    )
                )
            )
            launcher.launchInteraction(context, interaction)
        }

        binding.notesButton.setOnClickListener {
            Apptentive.reset()
            Apptentive.engage(this, "note_event") {
                if (it !is EngagementResult.Success) {
                    Toast.makeText(this, "Not engaged: $it", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.dataButton.setOnClickListener {
            val intent = Intent(this, DataActivity::class.java)
            startActivity(intent)
        }
    }
}
