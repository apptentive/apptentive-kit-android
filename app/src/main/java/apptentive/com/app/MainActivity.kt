package apptentive.com.app

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.ApptentiveActivityInfo
import apptentive.com.android.feedback.EngagementResult
import apptentive.com.android.feedback.model.EventNotification
import apptentive.com.android.feedback.model.MessageCenterNotification
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags
import apptentive.com.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ApptentiveActivityInfo {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val shouldSanitize = prefs.getBoolean(SHOULD_SANITIZE, false)
        binding.sanitizeSwitch.isChecked = shouldSanitize
        binding.sanitizeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(SHOULD_SANITIZE, isChecked).apply()
            finishAndRemoveTask()
        }

        binding.infoIcon.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }

        binding.dataButton.setOnClickListener {
            val intent = Intent(this, DevFunctionsActivity::class.java)
            startActivity(intent)
        }

        binding.engageEventButton.setOnClickListener {
            val engageEvent = binding.eventTextEditText.text?.toString()?.trim()
            if (!engageEvent.isNullOrEmpty()) {
                Apptentive.engage(engageEvent) { handleResult(it) }
                binding.eventTextLayout.isErrorEnabled = false
                binding.eventTextLayout.error = ""
                binding.eventTextEditText.setText("")

                val color = ContextCompat.getColor(this, R.color.color_primary)
                binding.eventTextLayout.boxStrokeColor = color
            } else {
                binding.eventTextLayout.isErrorEnabled = true
                binding.eventTextLayout.error = "No event entered"
            }
        }

        binding.canShowInteractionButton.setOnClickListener {
            if (binding.eventTextLayout.requestFocus()) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
            binding.eventTextLayout.isErrorEnabled = false
            val canShowInteraction = Apptentive.canShowInteraction(binding.eventTextEditText.text.toString())
            val color = ContextCompat.getColor(this, if (canShowInteraction) R.color.color_green else R.color.color_error)
            binding.eventTextLayout.boxStrokeColor = color
        }

        binding.loveDialogButton.setOnClickListener {
            Apptentive.engage("love_dialog_event") { handleResult(it) }
        }

        binding.surveyButton.setOnClickListener {
            Apptentive.engage("survey_event") { handleResult(it) }
        }

        binding.noteButton.setOnClickListener {
            Apptentive.engage("note_event") { handleResult(it) }
        }

        binding.messageCenterButton.setOnClickListener {
            Apptentive.showMessageCenter {
                handleResult(it)
            }
        }

        binding.ratingDialogButton.setOnClickListener {
            Apptentive.engage("rating_dialog_event") { handleResult(it) }
        }

        binding.clearAppDataButton.setOnClickListener {
            (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
        }

        Apptentive.eventNotificationObservable.observe(::observeApptentiveEvents)

        Apptentive.messageCenterNotificationObservable.observe(::observeNewMessage)
    }

    private fun observeApptentiveEvents(notification: EventNotification?) {
        val name = notification?.name
        val vendor = notification?.vendor
        val interaction = notification?.interaction
        val interactionId = notification?.interactionId?.let { id -> "\"$id\"" } ?: "`null`"

        val notificationText = "Name: \"$name\". Vendor: \"$vendor\". " +
            "Interaction: \"$interaction\". Interaction ID: $interactionId"
        Log.d(LogTags.EVENT_NOTIFICATION, notificationText)

        // Survey interaction handling
//            if (interaction == "Survey") {
//                when (name) {
//                    "launch" -> { /* Survey shown */ }
//                    "submit" -> { /* Survey completed */ }
//                    "cancel", "cancel_partial" -> { /* Survey closed without completing */ }
//                }
//            }
    }

    private fun observeNewMessage(notification: MessageCenterNotification?) {
        val notificationText =
            "Can Show Message Center: ${notification?.canShowMessageCenter}. " +
                "Unread Message Count: ${notification?.unreadMessageCount}. " +
                "Person Name: ${notification?.personName}. " +
                "Person Email: ${notification?.personEmail}"

        Log.d(LogTags.MESSAGE_CENTER_NOTIFICATION, notificationText)

        runOnUiThread {
            //     binding.messageCenterButton.isEnabled = notification?.canShowMessageCenter == true

            binding.messageCenterButton.isEnabled = true
            notification?.unreadMessageCount?.let {
                binding.unreadMessagesText.text =
                    resources.getQuantityString(R.plurals.unread_messages, it, it)
            }
        }
    }

    private fun handleResult(it: EngagementResult) {
        if (it !is EngagementResult.InteractionShown) {
            Toast.makeText(this, "Interaction NOT shown: $it", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        Apptentive.registerApptentiveActivityInfoCallback(this)

        val initialUnread = Apptentive.getUnreadMessageCount()
        binding.unreadMessagesText.text = resources.getQuantityString(R.plurals.unread_messages, initialUnread, initialUnread)
    }

    override fun getApptentiveActivityInfo(): Activity {
        return this
    }

    override fun onDestroy() {
        Apptentive.eventNotificationObservable.removeObserver(::observeApptentiveEvents)
        Apptentive.messageCenterNotificationObservable.removeObserver(::observeNewMessage)
        super.onDestroy()
    }
}
