package apptentive.com.android.feedback.messagecenter.view

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import apptentive.com.android.feedback.Apptentive
import apptentive.com.android.feedback.messagecenter.R
import apptentive.com.android.feedback.messagecenter.utils.MessageCenterEvents
import apptentive.com.android.feedback.messagecenter.view.custom.AttachmentBottomSheet
import apptentive.com.android.feedback.messagecenter.view.custom.AttachmentBottomSheet.Companion.APPTENTIVE_ATTACHMENT_BOTTOMSHEET_TAG
import apptentive.com.android.feedback.messagecenter.view.custom.MessageCenterAttachmentThumbnailView
import apptentive.com.android.feedback.model.Message
import apptentive.com.android.feedback.utils.SystemUtils
import apptentive.com.android.platform.SharedPrefConstants
import apptentive.com.android.platform.SharedPrefConstants.MESSAGE_CENTER_DRAFT
import apptentive.com.android.platform.SharedPrefConstants.MESSAGE_CENTER_DRAFT_ATTACHMENTS
import apptentive.com.android.platform.SharedPrefConstants.MESSAGE_CENTER_DRAFT_TEXT
import apptentive.com.android.platform.SharedPrefConstants.MESSAGE_CENTER_PROFILE_EMAIL
import apptentive.com.android.platform.SharedPrefConstants.MESSAGE_CENTER_PROFILE_NAME
import apptentive.com.android.serialization.json.JsonConverter
import apptentive.com.android.ui.hideSoftKeyboard
import apptentive.com.android.ui.startViewModelActivity
import apptentive.com.android.util.Log
import apptentive.com.android.R.string.apptentive_message_validation_error
import apptentive.com.android.util.LogTags.PUSH_NOTIFICATION
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView
import kotlin.math.roundToInt

internal class MessageCenterActivity : BaseMessageCenterActivity() {
    private lateinit var rootLayout: ConstraintLayout
    private lateinit var messageText: EditText
    private lateinit var attachmentsLayout: LinearLayout
    private lateinit var attachmentButton: ImageView
    private lateinit var sendButton: ImageView
    private lateinit var messageListAdapter: MessageListAdapter
    private lateinit var messageList: RecyclerView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var topAppBarTitle: MaterialTextView
    private lateinit var composerErrorView: TextView
    private var actionMenu: Menu? = null
    private var hasScrolled = false

    private val draftSharedPrefs by lazy { // So this is only retrieved once
        getSharedPreferences(MESSAGE_CENTER_DRAFT, MODE_PRIVATE)
    }

    private val sharedPrefsPush by lazy {
        getSharedPreferences(SharedPrefConstants.APPTENTIVE, MODE_PRIVATE)
    }

    private val selectImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { returnUri ->
            returnUri?.let { uri ->
                viewModel.addAttachment(this, uri)
            } ?: viewModel.onMessageCenterEvent(
                event = MessageCenterEvents.EVENT_NAME_ATTACHMENT_CANCEL,
                data = null
            )
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(PUSH_NOTIFICATION, "Push notifications allowed")
            } else {
                Log.w(PUSH_NOTIFICATION, "Push notifications denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.apptentive_activity_message_center)

        rootLayout = findViewById(R.id.apptentive_root)
        topAppBar = findViewById(R.id.apptentive_toolbar)
        topAppBarTitle = findViewById(R.id.apptentive_message_center_title)
        messageText = findViewById(R.id.apptentive_composer_text)
        attachmentsLayout = findViewById(R.id.apptentive_composer_attachments_layout)
        attachmentButton = findViewById(R.id.apptentive_attachment_button)
        sendButton = findViewById(R.id.apptentive_send_message_button)
        messageList = findViewById(R.id.apptentive_message_list)
        composerErrorView = findViewById(R.id.apptentive_composer_error)
        title = viewModel.title
        topAppBar.title = ""
        topAppBarTitle.text = viewModel.title
        messageText.hint = viewModel.composerHint
        messageListAdapter = MessageListAdapter(viewModel)
        messageList.adapter = messageListAdapter
        messageListAdapter.submitList(viewModel.buildMessageViewDataModel()) {
            scrollRecyclerToFirstUnreadOrLastItem()
        }
        messageList.itemAnimator = null

        // SupportActionBar should be set before setting NavigationOnClickListener
        setSupportActionBar(topAppBar)

        addObservers()
        setListeners()
        getPushNotificationPermission()
    }

    private fun addObservers() {
        viewModel.exitStream.observe(this) { exit ->
            if (exit) if (isTaskRoot) {
                // Opens the Launch Activity of app
                packageManager.getLaunchIntentForPackage(packageName)?.let {
                    startActivity(Intent.makeMainActivity(it.component))
                } ?: finish()
            } else finish()
        }

        viewModel.clearMessageStream.observe(this) { clearMessage ->
            if (clearMessage) {
                messageText.text.clear()
                attachmentsLayout.removeAllViews()
                handleDraftMessage(true)
                val lastItem = messageListAdapter.itemCount - 1
                if (lastItem >= 0) messageList.smoothScrollToPosition(lastItem)
            }
        }

        viewModel.draftAttachmentsStream.observe(this) { attachments ->
            attachmentsLayout.removeAllViews()
            attachments.forEach { file ->
                attachmentsLayout.addView(getAttachmentView(file))
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
            messageListAdapter.setEmailError(errorMessages.emailError)
            messageListAdapter.setNameError(errorMessages.nameError)
            if (errorMessages.messageError) {
                composerErrorView.visibility = View.VISIBLE
                composerErrorView.text = getString(apptentive_message_validation_error)
            } else {
                composerErrorView.visibility = View.GONE
                messageText.error = null
            }
        }

        viewModel.newMessages.observe(this) {
            updateMessageListAdapter(it)
        }

        viewModel.avatarBitmapStream.observe(this) {
            updateMessageListAdapter()
        }
    }

    private fun updateMessageListAdapter(messageViewData: List<MessageViewData>? = null) {
        messageList.visibility = View.VISIBLE
        // Update adapter
        messageListAdapter.submitList(messageViewData ?: viewModel.buildMessageViewDataModel()) {
            scrollRecyclerToFirstUnreadOrLastItem()
            viewModel.handleUnreadMessages()
        }
        if (!viewModel.shouldHideProfileIcon()) {
            actionMenu?.findItem(R.id.action_profile)?.isVisible = true
        }
    }

    private fun setListeners() {
        topAppBar.setNavigationOnClickListener {
            viewModel.exitMessageCenter()
        }
        sendButton.setOnClickListener {
            currentFocus?.clearFocus()
            it.hideSoftKeyboard()
            if (viewModel.shouldCollectProfileData)
                viewModel.sendMessage(
                    messageText.text.toString(),
                    messageListAdapter.getProfileName(),
                    messageListAdapter.getProfileEmail()
                )
            else
                viewModel.sendMessage(messageText.text.toString())
        }

        messageText.addTextChangedListener {
            composerErrorView.visibility = View.GONE
        }

        attachmentButton.setOnClickListener {
            selectImage.launch("image/*")
        }

        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            if (isKeyboardOpen() && viewModel.isProfileViewVisible()) {
                messageList.smoothScrollToPosition(messageListAdapter.itemCount - 1)
            }
        }
    }

    private fun isKeyboardOpen(): Boolean {
        val visibleBounds = Rect()
        rootLayout.getWindowVisibleDisplayFrame(visibleBounds)
        val screenHeight: Int = rootLayout.rootView.height
        val keypadHeight: Int = screenHeight - visibleBounds.bottom
        val marginOfError: Int = convertDpToPx(50f).roundToInt()
        return keypadHeight > marginOfError
    }

    private fun convertDpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics)

    private fun getAttachmentView(file: Message.Attachment): MessageCenterAttachmentThumbnailView {
        return MessageCenterAttachmentThumbnailView(this, null).apply {
            setAttachmentView(file) {
                val bottomSheet = AttachmentBottomSheet(file.originalName, file.localFilePath) {
                    viewModel.removeAttachment(file)
                }
                if (supportFragmentManager.findFragmentByTag(APPTENTIVE_ATTACHMENT_BOTTOMSHEET_TAG) == null) {
                    bottomSheet.show(supportFragmentManager, APPTENTIVE_ATTACHMENT_BOTTOMSHEET_TAG)
                }
            }
        }
    }

    private fun handleDraftMessage(shouldSave: Boolean) { // vs shouldRestore
        if (shouldSave) {
            draftSharedPrefs
                .edit()
                .putString(MESSAGE_CENTER_DRAFT_TEXT, messageText.text?.toString())
                .putString(MESSAGE_CENTER_PROFILE_NAME, messageListAdapter.getProfileName())
                .putString(MESSAGE_CENTER_PROFILE_EMAIL, messageListAdapter.getProfileEmail())
                .putStringSet(
                    MESSAGE_CENTER_DRAFT_ATTACHMENTS,
                    viewModel.draftAttachmentsStream.value?.mapNotNull { file ->
                        if (file.hasLocalFile()) JsonConverter.toJson(file) else null
                    }.orEmpty().toSet()
                )
                .apply()
        } else {
            // Restore draft message body
            val draftText = draftSharedPrefs.getString(MESSAGE_CENTER_DRAFT_TEXT, null)
            messageText.setText(draftText.orEmpty())

            // Restore draft attachments
            val stringSet =
                draftSharedPrefs.getStringSet(MESSAGE_CENTER_DRAFT_ATTACHMENTS, mutableSetOf())
                    .orEmpty()
            if (viewModel.draftAttachmentsStream.value.isNullOrEmpty() && stringSet.isNotEmpty()) {
                val draftAttachments: List<Message.Attachment> = stringSet.mapNotNull {
                    JsonConverter.fromJson(
                        it,
                        Message.Attachment::class.java
                    ) as? Message.Attachment
                }
                viewModel.addAttachments(draftAttachments)
            }

            // Restore profile view
            if (messageListAdapter.isProfileViewVisible()) {
                val name = draftSharedPrefs.getString(MESSAGE_CENTER_PROFILE_NAME, "")
                val email = draftSharedPrefs.getString(MESSAGE_CENTER_PROFILE_EMAIL, "")
                messageListAdapter.updateEmail(email)
                messageListAdapter.updateName(name)
            }
        }
    }

    private fun scrollRecyclerToFirstUnreadOrLastItem() {
        val firstUnreadItem = viewModel.getFirstUnreadMessagePosition(messageListAdapter.currentList)
        val lastItem = messageListAdapter.itemCount - 1
        if ((lastItem >= 0 && !hasScrolled) || firstUnreadItem >= 0) {
            hasScrolled = true
            messageList.scrollToPosition(if (firstUnreadItem >= 0) firstUnreadItem else lastItem)
        }
    }

    //region push notifications
    private fun getPushNotificationPermission() {
        val hasPushSetUp =
            sharedPrefsPush.getInt(SharedPrefConstants.PREF_KEY_PUSH_PROVIDER, -1) != -1 &&
                sharedPrefsPush.getString(SharedPrefConstants.PREF_KEY_PUSH_TOKEN, null) != null
        if (hasPushSetUp &&
            Build.VERSION.SDK_INT >= 33 &&
            applicationInfo.targetSdkVersion >= 33 &&
            !SystemUtils.hasPermission(this, SharedPrefConstants.POST_NOTIFICATIONS)
        ) {
            Log.d(PUSH_NOTIFICATION, "Requesting push notification")
            requestPermissionLauncher.launch(SharedPrefConstants.POST_NOTIFICATIONS)
        }
    }

    private fun clearNotifications() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Apptentive.APPTENTIVE_NOTIFICATION_ID)
    }
    //endregion

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // We need to remove focus from any TextView when user touches outside
        // of it. This would close the selectable clipboard menu if it is opened
        if (event.action == MotionEvent.ACTION_DOWN) {
            val focusedView = currentFocus
            if (focusedView is TextView) {
                focusedView.clearFocus()
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onMessageViewStatusChanged(true)
        handleDraftMessage(false)
        clearNotifications()
    }

    override fun onStop() {
        handleDraftMessage(true)
        viewModel.onMessageViewStatusChanged(false)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        actionMenu = menu
        menuInflater.inflate(R.menu.message_center_action, menu)
        // Do not show profile icon if there are no messages OR
        // there is only one automated message which is in progress OR
        // Profile is not configured
        if (viewModel.shouldHideProfileIcon())
            actionMenu?.findItem(R.id.action_profile)?.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_profile) {
            viewModel.onMessageCenterEvent(
                event = MessageCenterEvents.EVENT_NAME_PROFILE_OPEN,
                data = mapOf(
                    "required" to viewModel.isProfileRequired(),
                    "trigger" to "button"
                )
            )
            startViewModelActivity<ProfileActivity>()
        }
        return true
    }

    override fun onBackPressed() {
        viewModel.onMessageCenterEvent(
            event = MessageCenterEvents.EVENT_NAME_CANCEL,
            data = mapOf("cause" to "back_button")
        )

        if (isTaskRoot) packageManager.getLaunchIntentForPackage(packageName)?.let {
            // Opens the Launch Activity of app
            startActivity(Intent.makeMainActivity(it.component))
        } ?: finish() else finish()

        super.onBackPressed()
    }
}
