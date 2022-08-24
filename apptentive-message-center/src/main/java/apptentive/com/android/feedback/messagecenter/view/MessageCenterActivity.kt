package apptentive.com.android.feedback.messagecenter.view

import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.model.StoredFile
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.ui.startViewModelActivity
import com.google.android.material.appbar.MaterialToolbar

class MessageCenterActivity : BaseMessageCenterActivity() {
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var messageText: EditText
    private lateinit var attachmentsLayout: LinearLayout
    private lateinit var attachmentButton: ImageView
    private lateinit var messageListAdapter: MessageListAdapter
    private lateinit var greetingGroup: Group
    private lateinit var messageList: RecyclerView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var profileView: ProfileView
    private lateinit var composerErrorView: TextView
    private var actionMenu: Menu? = null

    private val selectImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { returnUri ->
            returnUri?.let { uri ->
                viewModel.addAttachment(this, uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_message_center)

        constraintLayout = findViewById(R.id.apptentive_root)
        topAppBar = findViewById(R.id.apptentive_toolbar)
        messageText = findViewById(R.id.apptentive_composer_text)
        profileView = findViewById(R.id.apptentive_message_center_profile)
        attachmentsLayout = findViewById(R.id.apptentive_composer_attachments_layout)
        messageList = findViewById(R.id.apptentive_message_list)
        greetingGroup = findViewById(R.id.apptentive_message_center_greeting_group)
        composerErrorView = findViewById(R.id.apptentive_composer_error)
        messageListAdapter = MessageListAdapter(viewModel.messages)

        topAppBar.title = viewModel.title
        messageText.hint = viewModel.composerHint
        findViewById<TextView>(R.id.apptentive_message_center_greeting).text = viewModel.greeting
        findViewById<TextView>(R.id.apptentive_message_center_greeting_body).text =
            viewModel.greetingBody
        profileView.setEmailHint(viewModel.getEmailHint() ?: "Email")
        profileView.setNameHint(viewModel.getNameHint() ?: "Name")

        messageList.apply {
            layoutManager = LinearLayoutManager(this@MessageCenterActivity)
            adapter = messageListAdapter
            val lastItem = messageListAdapter.itemCount - 1
            if (lastItem >= 0) smoothScrollToPosition(lastItem)
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
            if (clearMessage) {
                messageText.text.clear()
                handleDraftMessage(true)
                attachmentsLayout.removeAllViews()
            }
        }

        viewModel.draftAttachmentsStream.observe(this) { attachments ->
            attachmentsLayout.removeAllViews()
            attachments.forEach { file ->
                attachmentsLayout.addView(
                    MessageCenterAttachmentThumbnailView(this, null).apply {
                        setAttachmentView(file.localFilePath, file.mimeType) { }
                    }
                )
            }

            if (viewModel.draftAttachmentsStream.value?.size == 4) {
                attachmentButton.isEnabled = false
                attachmentButton.alpha = .5f
            } else {
                attachmentButton.isEnabled = true
                attachmentButton.alpha = 1.0f
            }
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
            // Update adapter
            messageListAdapter.listItems.clear()
            messageListAdapter.listItems.addAll(newMessages)
            messageListAdapter.notifyDataSetChanged()
            val lastItem = messageListAdapter.itemCount - 1
            if (lastItem >= 0) messageList.smoothScrollToPosition(messageListAdapter.itemCount - 1)
            flipToMessageListView()
        }
    }

    private fun setListeners() {
        topAppBar.setNavigationOnClickListener {
            viewModel.exitMessageCenter()
        }
        val sendButton = findViewById<ImageView>(R.id.apptentive_send_message_button)
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
            composerErrorView.visibility = View.GONE
        }

        attachmentButton = findViewById(R.id.apptentive_attachment_button)
        attachmentButton.setOnClickListener {
            selectImage.launch("image/*")
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
        val MESSAGE_CENTER_DRAFT_ATTACHMENTS = "message.attachments"

        val MESSAGE_CENTER_PROFILE_NAME = "profile.name"
        val MESSAGE_CENTER_PROFILE_EMAIL = "profile.email"

        val sharedPrefs = getSharedPreferences(MESSAGE_CENTER_DRAFT, MODE_PRIVATE)

        if (shouldSave) {
            sharedPrefs
                .edit()
                .putString(MESSAGE_CENTER_DRAFT_TEXT, messageText.text?.toString())
                .putStringSet(
                    MESSAGE_CENTER_DRAFT_ATTACHMENTS,
                    viewModel.draftAttachmentsStream.value?.map { file ->
                        JsonConverter.toJson(file)
                    }?.toSet()
                )
                .putString(MESSAGE_CENTER_PROFILE_NAME, profileView.getName())
                .putString(MESSAGE_CENTER_PROFILE_EMAIL, profileView.getEmail())
                .apply()
        } else {
            // Restore draft message body
            val draftText = sharedPrefs.getString(MESSAGE_CENTER_DRAFT_TEXT, null)
            messageText.setText(draftText.orEmpty())

            // Restore draft attachments
            val stringSet = sharedPrefs.getStringSet(MESSAGE_CENTER_DRAFT_ATTACHMENTS, mutableSetOf()).orEmpty()
            if (viewModel.draftAttachmentsStream.value.isNullOrEmpty() && stringSet.isNotEmpty()) {
                val draftAttachments: List<StoredFile> = stringSet.mapNotNull {
                    JsonConverter.fromJson(it, StoredFile::class.java) as? StoredFile
                }
                viewModel.addAttachments(draftAttachments)
            }

            // Restore profile view
            if (profileView.isVisible) {
                val name = sharedPrefs.getString(MESSAGE_CENTER_PROFILE_NAME, "")
                val email = sharedPrefs.getString(MESSAGE_CENTER_PROFILE_EMAIL, "")
                profileView.updateEmail(email.orEmpty())
                profileView.updateName(name.orEmpty())
            }
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
