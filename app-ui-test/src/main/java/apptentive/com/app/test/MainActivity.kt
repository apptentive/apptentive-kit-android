package apptentive.com.app.test

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.InteractionModuleComponent
import apptentive.com.android.feedback.engagement.Engagement
import apptentive.com.android.feedback.engagement.EngagementContext
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.criteria.Invocation
import apptentive.com.android.feedback.engagement.interactions.Interaction
import apptentive.com.android.feedback.engagement.interactions.InteractionData
import apptentive.com.android.feedback.engagement.interactions.InteractionModule
import apptentive.com.android.feedback.engagement.interactions.InteractionResponse
import apptentive.com.android.feedback.model.payloads.ExtendedData
import apptentive.com.android.feedback.model.payloads.Payload
import apptentive.com.android.feedback.payload.PayloadSender
import apptentive.com.android.serialization.json.JsonConverter

class MainActivity : AppCompatActivity(), ApptentiveActivityInfo {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val path = intent.getStringExtra(EXTRA_INTERACTIONS_PATH) ?: "interactions/notes"
        if (path != null) {
            val containerView = findViewById<ViewGroup>(R.id.interaction_buttons_container)
            assets.list(path)?.forEach { filename ->
                val button = createInteractionButton("$path/${filename}", filename)
                containerView.addView(button)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Apptentive.registerApptentiveActivityInfoCallback(this)
    }

    private fun createInteractionButton(filename: String, title: String) = Button(this).apply {
        text = title
        setOnClickListener {
            launchInteraction(filename)
        }
    }

    private fun launchInteraction(filename: String) {
        val jsonString = assets.open(filename).bufferedReader().use { it.readText() }
        val interactionData = JsonConverter.fromJson<InteractionData>(jsonString)
        launchInteraction(interactionData)
    }

    private fun launchInteraction(data: InteractionData) {
        val module = interactionModules[data.type]
            ?: throw IllegalArgumentException("Invalid interaction type: ${data.type}")
        val converted = module.provideInteractionTypeConverter()
        val interaction = converted.convert(data)

        val launcher = module.provideInteractionLauncher()
        launcher.launchInteraction(engagementContext, interaction)
    }

    private val interactionModules: Map<String, InteractionModule<Interaction>> by lazy {
        InteractionModuleComponent.default().getModules()
    }

    companion object {
        const val EXTRA_INTERACTIONS_PATH = "interactions_path"
    }

    private val engagementContext: EngagementContext by lazy {
        EngagementContext(
            engagement = object : Engagement {
                override fun engage(
                    context: EngagementContext,
                    event: Event,
                    interactionId: String?,
                    data: Map<String, Any?>?,
                    customData: Map<String, Any?>?,
                    extendedData: List<ExtendedData>?,
                    interactionResponses: Map<String, Set<InteractionResponse>>?
                ): EngagementResult {
                    return EngagementResult.InteractionNotShown("No runnable interactions")
                }

                override fun engage(
                    context: EngagementContext,
                    invocations: List<Invocation>
                ): EngagementResult {
                    return EngagementResult.InteractionNotShown("No runnable interactions")
                }
            },
            payloadSender = object : PayloadSender {
                override fun sendPayload(payload: Payload) {
                }
            },
            executors = Executors(ImmediateExecutor, ImmediateExecutor),
        )
    }

    private object ImmediateExecutor : Executor {
        override fun execute(task: () -> Unit) {
            task()
        }
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    override fun onPause() {
        Apptentive.unregisterApptentiveActivityInfoCallback()
        super.onPause()
    }
}