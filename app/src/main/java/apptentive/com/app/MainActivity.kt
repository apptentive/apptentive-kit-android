package apptentive.com.app

import android.content.Intent
import android.os.Build
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
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteraction
import apptentive.com.android.feedback.ratingdialog.RatingDialogInteractionLauncher
import apptentive.com.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        val isNightMode = prefs.getBoolean(EXTRA_NIGHT_MODE, false)
        delegate.localNightMode = if (isNightMode) MODE_NIGHT_YES else MODE_NIGHT_NO

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nightSwitch.isChecked = isNightMode
        binding.nightSwitch.setOnCheckedChangeListener { _, isChecked ->
            delegate.localNightMode = if (isChecked) MODE_NIGHT_YES else MODE_NIGHT_NO
            prefs.edit().putBoolean(EXTRA_NIGHT_MODE, isChecked).apply()
        }

        val shouldSanitize = prefs.getBoolean(SHOULD_SANITIZE, false)
        binding.sanitizeSwitch.isChecked = shouldSanitize
        binding.sanitizeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(SHOULD_SANITIZE, isChecked).apply()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) finishAndRemoveTask()
        }

        binding.infoIcon.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        binding.dataButton.setOnClickListener {
            val intent = Intent(this, DataActivity::class.java)
            startActivity(intent)
        }

        binding.engageEventButton.setOnClickListener {
            val engageEvent = binding.eventTextEditText.text?.toString()?.trim()
            if (!engageEvent.isNullOrEmpty()) {
                Apptentive.engage(this, engageEvent) { handleResult(it) }
                binding.eventTextLayout.isErrorEnabled = false
                binding.eventTextLayout.error = ""
                binding.eventTextEditText.setText("")
            } else {
                binding.eventTextLayout.isErrorEnabled = true
                binding.eventTextLayout.error = "No event entered"
            }
        }

        binding.loveDialogButton.setOnClickListener {
            Apptentive.engage(this, "love_dialog_event") { handleResult(it) }
        }

        binding.surveyButton.setOnClickListener {
            Apptentive.engage(this, "survey_event") { handleResult(it) }
        }

        binding.noteButton.setOnClickListener {
            Apptentive.engage(this, "note_event") { handleResult(it) }
        }

        /**
         *  In-App Review needs to be tested in Internal Test Track
         *
         *  Apptentive Rating Dialog needs to be tested on Android OS < 5
         *  TODO: Alternate app stores integration
         **/
        binding.ratingDialogButton.setOnClickListener {
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
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Payload send: ${payload::class.java.simpleName}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                executors = Executors(
                    state = executorFactory.createSerialQueue("state"),
                    main = executorFactory.createMainQueue()
                )
            )

            val launcher = RatingDialogInteractionLauncher()
            val interaction = RatingDialogInteraction(
                id = "id",
                title = "Share the love!",
                body = "Thanks for being a loyal customer. Would you take 30 seconds and share your love in the app store?",
                rateText = "Rate Android App",
                remindText = "Remind me later",
                declineText = "No thanks"
            )

            launcher.launchInteraction(context, interaction)
        }

        /**
         * Alternatively, to get the real version from backend, follow the instruction below.
         *
         * 1. Uncomment the code below. (Starting at `binding.ratingDialogButton`) (CMD + /)
         *
         * 2. Reduce API_VERSION to 9 (10 and above enables In-App Review)
         * @see apptentive.com.android.feedback.Constants.API_VERSION
         *
         * 3. Replace `result` in `ConditionalClause` with `true`.
         * Changing `result` to `true` will bypass all criteria checks. Can still see in logs.
         * @see apptentive.com.android.feedback.engagement.criteria.ConditionalClause.evaluate
         */
//        binding.ratingDialogButton.setOnClickListener {
//            Apptentive.engage(this, "rating_dialog_event") { handleResult(it) }
//        }

        binding.resetSDKStateButton.setOnClickListener {
            Apptentive.reset(this)
        }
    }

    private fun handleResult(it: EngagementResult) {
        if (it !is EngagementResult.Success) {
            Toast.makeText(this, "Not engaged: $it", Toast.LENGTH_LONG).show()
        }
    }
}
