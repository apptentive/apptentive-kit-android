package apptentive.com.android.feedback.messagecenter.view

import android.os.Bundle
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents
import apptentive.com.android.feedback.messagecenter.view.custom.ProfileView
import apptentive.com.android.ui.ApptentiveGenericDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

internal class ProfileActivity : BaseProfileActivity() {

    private lateinit var topAppBar: MaterialToolbar
    private lateinit var topAppBarTitle: MaterialTextView
    private lateinit var profileView: ProfileView
    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_profile)

        topAppBar = findViewById(R.id.apptentive_profile_toolbar)
        topAppBarTitle = findViewById(R.id.apptentive_profile_title)
        profileView = findViewById(R.id.apptentive_edit_profile)
        saveButton = findViewById(R.id.apptentive_profile_save_button)

        topAppBar.title = ""
        topAppBarTitle.text = viewModel.profileTitle
        profileView.setEmailHint(viewModel.emailHint)
        profileView.setNameHint(viewModel.nameHint)
        saveButton.text = viewModel.profileSubmit

        // SupportActionBar should be set before setting NavigationOnClickListener
        setSupportActionBar(topAppBar)

        topAppBar.setNavigationOnClickListener {
            viewModel.exitProfileView(profileView.getName(), profileView.getEmail().trim())
        }

        saveButton.setOnClickListener {
            viewModel.submitProfile(profileView.getName(), profileView.getEmail().trim())
        }

        viewModel.errorMessagesStream.observe(this) { error ->
            profileView.setEmailError(error)
        }

        viewModel.profileStream.observe(this) { profile ->
            profileView.updateName(profile.name ?: "")
            profileView.updateEmail(profile.email ?: "")
        }

        viewModel.showConfirmationStream.observe(this) { showConfirmation ->
            if (showConfirmation) {
                val confirmationDialog = ApptentiveGenericDialog().getGenericDialog(
                    context = this@ProfileActivity,
                    title = getString(R.string.apptentive_profile_confirmation_dialog_title),
                    message = getString(R.string.apptentive_profile_confirmation_dialog_message),
                    positiveButton = ApptentiveGenericDialog.DialogButton(getString(R.string.apptentive_profile_back_to_profile)) {
                    },
                    negativeButton = ApptentiveGenericDialog.DialogButton(getString(R.string.apptentive_close)) {
                        viewModel.onMessageCenterEvent(
                            event = MessageCenterEvents.EVENT_NAME_PROFILE_CLOSE,
                            data = mapOf(
                                "required" to viewModel.isProfileRequired(),
                                "button_label" to getString(R.string.apptentive_close)
                            )
                        )
                        super.onBackPressed()
                    }
                )
                confirmationDialog.show()
            } else {
                viewModel.onMessageCenterEvent(
                    event = MessageCenterEvents.EVENT_NAME_PROFILE_SUBMIT,
                    data = mapOf(
                        "required" to viewModel.isProfileRequired(),
                        "button_label" to saveButton.text.toString()
                    )
                )
                super.onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        viewModel.exitProfileView(profileView.getName(), profileView.getEmail())
    }
}
