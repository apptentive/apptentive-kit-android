package apptentive.com.android.feedback.messagecenter.view

import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.ui.startViewModelActivity
import com.google.android.material.appbar.MaterialToolbar

class MessageCenterActivity : BaseMessageCenterActivity() {
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var messageText: EditText
    private lateinit var messageListAdapter: MessageListAdapter
    private lateinit var greetingGroup: Group
    private lateinit var messageList: RecyclerView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var profileView: ProfileView
    private lateinit var composerErrorView: TextView
    private var actionMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_message_center)

        constraintLayout = findViewById(R.id.apptentive_root)
        topAppBar = findViewById(R.id.apptentive_toolbar)
        messageText = findViewById(R.id.apptentive_composer_text)
        profileView = findViewById(R.id.apptentive_message_center_profile)
        messageList = findViewById(R.id.apptentive_message_list)
        greetingGroup = findViewById(R.id.apptentive_message_center_greeting_group)
        composerErrorView = findViewById(R.id.apptentive_composer_error)
        messageListAdapter = MessageListAdapter()

        topAppBar.title = viewModel.title
        messageText.hint = viewModel.composerHint
        findViewById<TextView>(R.id.apptentive_message_center_greeting).text = viewModel.greeting
        findViewById<TextView>(R.id.apptentive_message_center_greeting_body).text = viewModel.greetingBody
        profileView.setEmailHint(viewModel.getEmailHint() ?: "Email")
        profileView.setNameHint(viewModel.getNameHint() ?: "Name")

        messageList.adapter = messageListAdapter
        messageListAdapter.submitList(viewModel.messages) {
            val lastItem = messageListAdapter.currentList.size - 1
            if (lastItem >= 0) messageList.scrollToPosition(lastItem) // TODO Scroll to first unread
        }

        if (viewModel.showLauncherView)
            flipToLauncherView()
        else
            flipToMessageListView()

        // SupportActionBar should be set before setting NavigationOnClickListener
        setSupportActionBar(topAppBar)

        addObservers()
        setListeners()
    }

    private fun addObservers() {
        viewModel.exitStream.observe(this) { exit ->
            if (exit) finish()
        }

        viewModel.clearMessageStream.observe(this) { clearMessage ->
            if (clearMessage) messageText.text.clear()
        }

        viewModel.errorMessagesStream.observe(this) { errorMessages ->
            profileView.setEmailError(errorMessages.emailError)
            profileView.setNameError(errorMessages.nameError)
            if (errorMessages.messageError) {
                composerErrorView.visibility = View.VISIBLE
                composerErrorView.text = getString(R.string.apptentive_message_validation_error)
            } else {
                composerErrorView.visibility = View.GONE
                messageText.error = null
            }
        }

        viewModel.newMessages.observe(this) { newMessages ->
            greetingGroup.visibility = View.GONE
            messageList.visibility = View.VISIBLE
            messageListAdapter.submitList(newMessages) {
                val lastItem = messageListAdapter.currentList.size - 1
                if (lastItem >= 0) messageList.smoothScrollToPosition(lastItem) // TODO Scroll to first unread
            }
            flipToMessageListView()
        }
    }

    private fun setListeners() {
        topAppBar.setNavigationOnClickListener {
            viewModel.exitMessageCenter()
        }

        val sendButton = findViewById<ImageView>(R.id.apptentive_send_image)
        sendButton.setOnClickListener {
            if (viewModel.showLauncherView)
                viewModel.sendMessage(messageText.text.toString(), profileView.getName(), profileView.getEmail().trim())
            else
                viewModel.sendMessage(messageText.text.toString())
        }

        constraintLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            constraintLayout.getWindowVisibleDisplayFrame(r)
            val screenHeight: Int = constraintLayout.rootView.height
            val keypadHeight: Int = screenHeight - r.bottom
            when {
                keypadHeight > screenHeight * 0.15 || !viewModel.showLauncherView ->
                    greetingGroup.visibility = View.GONE
                else -> greetingGroup.visibility = View.VISIBLE
            }
        }

        messageText.addTextChangedListener {
            composerErrorView.visibility = View.INVISIBLE
        }
    }

    private fun flipToLauncherView() {
        profileView.visibility = View.VISIBLE
        greetingGroup.visibility = View.VISIBLE
        messageList.visibility = View.GONE
    }

    private fun flipToMessageListView() {
        profileView.visibility = View.GONE
        greetingGroup.visibility = View.GONE
        messageList.visibility = View.VISIBLE
        if (viewModel.showProfile())
            actionMenu?.findItem(R.id.action_profile)?.isVisible = true
    }

    private fun handleDraftMessage(shouldSave: Boolean) { // vs shouldRestore
        // Consts for shared prefs
        val MESSAGE_CENTER_DRAFT = "com.apptentive.sdk.messagecenter.draft"
        val MESSAGE_CENTER_DRAFT_TEXT = "message.text"
        val MESSAGE_CENTER_PROFILE_NAME = "profile.name"
        val MESSAGE_CENTER_PROFILE_EMAIL = "profile.email"
        val MESSAGE_CENTER_DRAFT_ATTACHMENT_1 = "message.attachment.1"
        val MESSAGE_CENTER_DRAFT_ATTACHMENT_2 = "message.attachment.2"
        val MESSAGE_CENTER_DRAFT_ATTACHMENT_3 = "message.attachment.3"
        val MESSAGE_CENTER_DRAFT_ATTACHMENT_4 = "message.attachment.4"

        val sharedPrefs = getSharedPreferences(MESSAGE_CENTER_DRAFT, MODE_PRIVATE)

        if (shouldSave) {
            sharedPrefs
                .edit()
                .putString(MESSAGE_CENTER_DRAFT_TEXT, messageText.text?.toString())
                .putString(MESSAGE_CENTER_PROFILE_NAME, profileView.getName())
                .putString(MESSAGE_CENTER_PROFILE_EMAIL, profileView.getEmail())
                // TODO Path to attachment files instead of storing the entire file.
                .putString(MESSAGE_CENTER_DRAFT_ATTACHMENT_1, "")
                .putString(MESSAGE_CENTER_DRAFT_ATTACHMENT_2, "")
                .putString(MESSAGE_CENTER_DRAFT_ATTACHMENT_3, "")
                .putString(MESSAGE_CENTER_DRAFT_ATTACHMENT_4, "")
                .apply()
        } else {
            val draftText = sharedPrefs.getString(MESSAGE_CENTER_DRAFT_TEXT, null)
            val name = sharedPrefs.getString(MESSAGE_CENTER_PROFILE_NAME, "")
            val email = sharedPrefs.getString(MESSAGE_CENTER_PROFILE_EMAIL, "")
            val draftAttachment1Path = sharedPrefs.getString(MESSAGE_CENTER_DRAFT_ATTACHMENT_1, null)
            val draftAttachment2Path = sharedPrefs.getString(MESSAGE_CENTER_DRAFT_ATTACHMENT_2, null)
            val draftAttachment3Path = sharedPrefs.getString(MESSAGE_CENTER_DRAFT_ATTACHMENT_3, null)
            val draftAttachment4Path = sharedPrefs.getString(MESSAGE_CENTER_DRAFT_ATTACHMENT_4, null)

            messageText.setText(draftText.orEmpty())
            if (profileView.isVisible) {
                profileView.updateEmail(email.orEmpty())
                profileView.updateName(name.orEmpty())
            }
            // TODO Set draft attachments
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onMessageViewStatusChanged(true)
        handleDraftMessage(false)
    }

    override fun onStop() {
        handleDraftMessage(true)
        viewModel.onMessageViewStatusChanged(false)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        actionMenu = menu
        menuInflater.inflate(R.menu.message_center_action, menu)
        if (viewModel.showLauncherView || !viewModel.showProfile())
            actionMenu?.findItem(R.id.action_profile)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            startViewModelActivity<ProfileActivity>()
        }
        return true
    }
}
